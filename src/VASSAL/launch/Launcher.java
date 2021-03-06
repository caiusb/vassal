/*
 * $Id: Launcher.java 5962 2009-08-24 14:23:16Z uckelman $
 *
 * Copyright (c) 2000-2009 by Rodney Kinney, Joel Uckelman 
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License (LGPL) as published by the Free Software Foundation.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, copies are available
 * at http://www.opensource.org.
 */

package VASSAL.launch;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import javax.swing.SwingUtilities;

import VASSAL.Info;
import VASSAL.build.GameModule;
import VASSAL.build.module.ExtensionsLoader;
import VASSAL.i18n.Resources;
import VASSAL.tools.ErrorDialog;
import VASSAL.tools.ThrowableUtils;
import VASSAL.tools.WriteErrorDialog;
import VASSAL.tools.logging.CommandClientAdapter;
import VASSAL.tools.logging.LoggedOutputStream;
import VASSAL.tools.logging.Logger;
import VASSAL.tools.logging.LogOutputStreamAdapter;
import VASSAL.tools.menu.MenuManager;

/**
 * @author Joel Uckelman
 * @since 3.1.0
 */
public abstract class Launcher {
  protected CommandClient cmdC = null;
  protected CommandServer cmdS = null;

  protected final LaunchRequest lr;

  private static Launcher instance = null;

  public static Launcher getInstance() {
    return instance;
  }

  protected Launcher(String[] args) {
    if (instance != null) throw new IllegalStateException();
    instance = this;

    final boolean standalone = args.length > 0;

    // parse the command line args now if we're standalone, since they
    // could be messed up and so we'll bail before setup
    LaunchRequest lr = null; 
    if (standalone) {
      // Note: We could do more sanity checking of the launch request
      // in standalone mode, but we don't bother because this is meant
      // only for debugging, not for normal use. If you pass nonsense
      // arguments (e.g., '-e' to the Player), don't expect it to work.
      try {
        lr = LaunchRequest.parseArgs(args);
      }
      catch (LaunchRequestException e) {
        System.err.println("VASSAL: " + e.getMessage());
        System.exit(1);
      }
    }

    // start the error log and setup system properties
    final StartUp start = Info.isMacOSX() ? new MacOSXStartUp() : new StartUp();

    if (standalone) {
      // start logging to the errorLog
      final File errorLog = new File(Info.getHomeDir(), "errorLog");
      try {
        Logger.addLogListener(
          new LogOutputStreamAdapter(
            new FileOutputStream(errorLog)));
      }
      catch (IOException e) {
        WriteErrorDialog.error(e, errorLog);
      }
    }

    start.startErrorLog();

    // log everything which comes across our stderr
    System.setErr(
      new PrintStream(new LoggedOutputStream(Info.getInstanceID()), true));

    Logger.log("-- " + getClass().getSimpleName());
    Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler());
    start.initSystemProperties();

    // if we're not standalone, contact the module manager for instructions 
    if (!standalone) {
      try {
        // set up our command listener
        final ServerSocket serverSocket =
          new ServerSocket(0, 0, InetAddress.getByName(null));
        cmdS = new CommandServer(serverSocket);
        new Thread(cmdS, "command server").start();

        // write our socket port out to the module manager
        final DataOutputStream out = new DataOutputStream(System.out);
        out.writeInt(serverSocket.getLocalPort());
        out.flush();

        // read the module manager's socket port and launch request from stdin
        final ObjectInputStream in = new ObjectInputStream(System.in);
        final int port = in.readInt();
        
        lr = (LaunchRequest) in.readObject();

        // set up our command client
        cmdC = new CommandClient(new Socket((String) null, port));

        Logger.addLogListener(new CommandClientAdapter(cmdC));
      }
      catch (ClassNotFoundException e) {
        ErrorDialog.bug(e);
        System.exit(1);
      }
      catch (IOException e) {
        // What we've got here is failure to communicate.
        ErrorDialog.show(
          e,
          "Error.communication_error", 
          Resources.getString(getClass().getSimpleName() + ".app_name")
        );         
        System.exit(1);
      }
    }

    this.lr = lr;

    createMenuManager();

    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        try {
          launch();
        }
        catch (ExtensionsLoader.LoadExtensionException e2) {
          warn(e2);
        }
        catch (IOException e1) {
          warn(e1);
        }
      }
      
      private void warn (Exception e1) {
        if (cmdC == null) {
          // we are standalone, so warn the user directly
          ErrorDialog.showDetails(
            e1,
            ThrowableUtils.getStackTrace(e1),
            "Error.module_load_failed",
            e1.getMessage()
          );
        }
        else {
          // we have a manager, so pass the load failure back to it
          try {
            cmdC.request(new AbstractLaunchAction.NotifyOpenModuleFailed(e1));
          }
          catch (IOException e2) {
            // warn the user directly as a last resort 
            ErrorDialog.showDetails(
              e1,
              ThrowableUtils.getStackTrace(e1),
              "Error.module_load_failed",
              e1.getMessage()
            );

            ErrorDialog.show(
              e2,
              "Error.communication_error",
              Resources.getString(getClass().getSimpleName() + ".app_name")
            );
          }
        }

        System.exit(1);
      }
    });
  }

  protected abstract void launch() throws IOException;

  protected abstract MenuManager createMenuManager();

  /**
   * A request from the Module Manager to close.
   */
  public static class CloseRequest implements Command {
    private static final long serialVersionUID = 1L;

    private boolean shutdown = true;

    public Object execute() {
      final GameModule module = GameModule.getGameModule();
      if (module != null) {
        try {
          SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
              module.getFrame().toFront();
              shutdown = module.shutDown();
            }
          });
        }
        catch (InterruptedException e) {
          Logger.log(e);
          shutdown = false;
        }
        catch (InvocationTargetException e) {
          ErrorDialog.bug(e);
          shutdown = false;
        }
      }

      try {
        return shutdown ? "OK" : "NOK";
      }
      finally {
        if (shutdown) System.exit(0);
      }
    }
  }

  /**
   * Send a message to the ModuleManager that a file has been saved by the
   * Editor or the Player
   * @param f
   */
// FIXME: this isn't called from anywhere yet!
  public void sendSaveCmd(File f) {
    if (cmdC != null) {
      try {
        cmdC.request(new AbstractLaunchAction.NotifySaveFileOk(f));
      }
      // FIXME: review error message
      catch (IOException e) {
      }
    }
  }
}

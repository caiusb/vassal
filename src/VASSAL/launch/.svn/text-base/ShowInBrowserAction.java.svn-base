/*
 * $Id: ShowHelpAction.java 3365 2008-03-25 18:10:42Z uckelman $
 *
 * Copyright (c) 2009 by Joel Uckelman 
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

import java.awt.event.ActionEvent;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import VASSAL.tools.BrowserSupport;

/**
 * {@link Action} that opens a {@link URL} in a browser.
 */
public class ShowInBrowserAction extends AbstractAction {
  private static final long serialVersionUID = 1L;

  private final URL location;

  public ShowInBrowserAction(String name, File basedir,
                             String rpath, ImageIcon icon)
                                                 throws MalformedURLException {
    this(name, new File(basedir, rpath).toURI().toURL(), icon);
  }

  public ShowInBrowserAction(String name, URL location, ImageIcon icon) {
    super(name, icon);
    this.location = location;
  }
  
  public void actionPerformed(ActionEvent e) {
    if (location != null) {
      BrowserSupport.openURL(location.toString());
    }
  }
}

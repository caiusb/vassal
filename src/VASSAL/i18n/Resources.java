/*
 * $Id: Resources.java 5378 2009-03-23 14:43:53Z uckelman $
 *
 * Copyright (c) 2007 by Rodney Kinney, Brent Easton
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
package VASSAL.i18n;

import java.awt.Component;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.UIManager;

import VASSAL.Info;
import VASSAL.build.module.gamepieceimage.StringEnumConfigurer;
import VASSAL.preferences.Prefs;
import VASSAL.tools.ErrorDialog;

public class Resources {
  /*
   * Translation of VASSAL is handled by standard Java I18N tools.
   * 
   * vassalBundle - Resource Bundle for the VASSAL player interface editorBundle - Resource Bundle for the Module Editor
   * 
   * These are implemented as PropertyResourceBundles, normally to be found in the VASSAL jar file. VASSAL will search
   * first in the VASSAL install directory for bundles, then follow the standard Java Class Path
   */
  protected static BundleHelper vassalBundle;
  protected static BundleHelper editorBundle;
  private static VassalPropertyClassLoader bundleLoader = new VassalPropertyClassLoader();

  /** Preferences key for the user's Locale */
  public static final String LOCALE_PREF_KEY = "Locale";

  // Note: The Locale ctor takes the lower-case two-letter ISO language code. 
  protected static final List<Locale> supportedLocales =
    new ArrayList<Locale>(Arrays.asList(
      Locale.ENGLISH,
      Locale.GERMAN,
      Locale.FRENCH,
      Locale.ITALIAN,
      new Locale("es"), // Spanish
      Locale.JAPANESE,
      new Locale("nl")  // Dutch
    )
  );

  protected static Locale locale = Locale.getDefault();
  protected static String DATE_FORMAT = "{0,date}";

  static {
    // If the user has a resource bundle for their default language on their
    // local machine, add it to the list of supported locales
    if (ResourceBundle.getBundle("VASSAL.i18n.VASSAL", Locale.getDefault(), bundleLoader).getLocale().getLanguage().equals(Locale.getDefault().getLanguage())) {
      addSupportedLocale(Locale.getDefault());
    }

    final ArrayList<String> languages = new ArrayList<String>();
    for (Locale l : getSupportedLocales()) {
      languages.add(l.getLanguage());
    }

    Locale myLocale = Locale.getDefault();
    String savedLocale = Prefs.getGlobalPrefs().getStoredValue(LOCALE_PREF_KEY);

    if (savedLocale == null) {
      myLocale = supportedLocales.iterator().next();
    }
    else {
      myLocale = new Locale(savedLocale);
    }

    Resources.setLocale(myLocale);
    StringEnumConfigurer localeConfig = new StringEnumConfigurer(Resources.LOCALE_PREF_KEY, getString("Prefs.language"), languages.toArray(new String[languages
        .size()])) {
      public Component getControls() {
        if (box == null) {
          Component c = super.getControls();
          box.setRenderer(new DefaultListCellRenderer() {
            private static final long serialVersionUID = 1L;

            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
              JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
              l.setText(new Locale((String) value).getDisplayLanguage());
              return l;
            }
          });
          return c;
        }
        else {
          return super.getControls();
        }
      }
    };
    localeConfig.setValue(Resources.getLocale().getLanguage());
    Prefs.getGlobalPrefs().addOption(getString("Prefs.general_tab"), localeConfig);
  }

  public static Collection<Locale> getSupportedLocales() {
    return supportedLocales;
  }
  
  public static void addSupportedLocale(Locale l) {
    l = new Locale(l.getLanguage());
    if (!supportedLocales.contains(l)) {
      supportedLocales.add(0,l);
      StringEnumConfigurer config = (StringEnumConfigurer) Prefs.getGlobalPrefs().getOption(LOCALE_PREF_KEY);
      if (config != null) {
        ArrayList<String> valid = new ArrayList<String>(Arrays.asList(config.getValidValues()));
        valid.add(0,l.getLanguage());
        config.setValidValues(valid.toArray(new String[valid.size()]));
      }
    }
  }

  public static Collection<String> getVassalKeys() {
    return Collections.list(vassalBundle.getResourceBundle().getKeys());
  }

  public static Collection<String> getEditorKeys() {
    return Collections.list(editorBundle.getResourceBundle().getKeys());
  }

  /*
   * Translation of individual modules is handled differently.
   * There may be multiple Module.properties file active -
   * Potentially one in the module plus one in each Extension loaded.
   * These will be read into UberProperties structures
   * with each file loaded supplying defaults for subsequent files.
   */
  protected static String MODULE_BUNDLE = "Module"; //$NON-NLS-1$

  /*
   * Commonly used i18n keys used in multiple components. By defining them
   * centrally, they will only have to be translated once. Reference to these
   * string should be made as follows:
   * 
   * Resources.getString(Resources.VASSAL)
   */
  public static final String VASSAL = "General.VASSAL"; //$NON-NLS-1$
  public static final String ADD = "General.add"; //$NON-NLS-1$
  public static final String REMOVE = "General.remove"; //$NON-NLS-1$
  public static final String INSERT = "General.insert"; //$NON-NLS-1$
  public static final String YES = "General.yes"; //$NON-NLS-1$
  public static final String NO = "General.no"; //$NON-NLS-1$
  public static final String CANCEL = "General.cancel"; //$NON-NLS-1$
  public static final String SAVE = "General.save"; //$NON-NLS-1$
  public static final String OK = "General.ok"; //$NON-NLS-1$
  public static final String MENU = "General.menu"; //$NON-NLS-1$
  public static final String LOAD = "General.load"; //$NON-NLS-1$
  public static final String QUIT = "General.quit"; //$NON-NLS-1$
  public static final String EDIT = "General.edit"; //$NON-NLS-1$
  public static final String NEW = "General.new"; //$NON-NLS-1$
  public static final String FILE = "General.file"; //$NON-NLS-1$
  public static final String TOOLS = "General.tools"; //$NON-NLS-1$
  public static final String HELP = "General.help"; //$NON-NLS-1$
  public static final String CLOSE = "General.close"; //$NON-NLS-1$
  public static final String DATE_DISPLAY = "General.date_display"; //$NON-NLS-1$
  public static final String NEXT = "General.next"; //$NON-NLS-1$
  public static final String REFRESH = "General.refresh"; //$NON-NLS-1$
  public static final String SELECT = "General.select"; //$NON-NLS-1$

  /*
   * All i18n keys for the Module Editor must commence with "Editor."
   * This allows us to use a single Resources.getString() call for both
   * resource bundles.
   */
  public static final String EDITOR_PREFIX = "Editor."; //$NON-NLS-1$

  /*
   * Common Editor labels that appear in many components.
   */
  public static final String BUTTON_TEXT = "Editor.button_text_label"; //$NON-NLS-1$
  public static final String TOOLTIP_TEXT = "Editor.tooltip_text_label"; //$NON-NLS-1$
  public static final String BUTTON_ICON = "Editor.button_icon_label"; //$NON-NLS-1$
  public static final String HOTKEY_LABEL = "Editor.hotkey_label"; //$NON-NLS-1$
  public static final String COLOR_LABEL = "Editor.color_label"; //$NON-NLS-1$
  public static final String NAME_LABEL = "Editor.name_label"; //$NON-NLS-1$

  /**
   * Localize a user interface String.
   * 
   * @param id
   *          String Id
   * @return Localized result
   */
  public static String getString(String id) {
    return getBundleForKey(id).getString(id);
  }

  protected static BundleHelper getBundleForKey(String id) {
    return id.startsWith(EDITOR_PREFIX) ? getEditorBundle() : getVassalBundle();
  }

  protected static BundleHelper getEditorBundle() {
    if (editorBundle == null) {
      editorBundle = new BundleHelper(ResourceBundle.getBundle("VASSAL.i18n.Editor", locale, bundleLoader));
    }
    return editorBundle;
  }

  protected static BundleHelper getVassalBundle() {
    if (vassalBundle == null) {
      vassalBundle = new BundleHelper(ResourceBundle.getBundle("VASSAL.i18n.VASSAL", locale, bundleLoader));
    }
    return vassalBundle;
  }

  /**
   * Localize a VASSAL user interface string
   * 
   * @param id
   *          String id
   * @return Localized result
   */
  @Deprecated
  public static String getVassalString(String id) {
    return getVassalBundle().getString(id);
  }

  /**
   * Localize a VASSAL Module Editor String
   * 
   * @param id
   *          String Id
   * @return Localized Result
   */
  @Deprecated
  public static String getEditorString(String id) {
    return getEditorBundle().getString(id);
  }

  /**
   * Localize a string using the supplied resource bundle
   * 
   * @param bundle
   *          Resource bundle
   * @param id
   *          String Id
   * @return Localized result
   */
  @Deprecated
  public static String getString(ResourceBundle bundle, String id) {
    String s = null;
    try {
      s = bundle.getString(id);
    }
    catch (Exception ex) {
      System.err.println("No Translation: " + id);
    }
    // 2. Worst case, return the key
    if (s == null) {
      s = id;
    }
    return s;
  }

  public static String getString(String id, Object... params) {
    return getBundleForKey(id).getString(id, params);
  }

  protected static final String BASE_BUNDLE = "VASSAL.properties";
  protected static final String EN_BUNDLE = "VASSAL_en.properties";
  
  /**
   * Custom Class Loader for loading VASSAL property files.
   * Check first for files in the VASSAL home directory.
   * 
   * @author Brent Easton
   */
  public static class VassalPropertyClassLoader extends ClassLoader {
    public URL getResource(String name) {
      URL url = getAResource(name);
      
      // If no English translation of Vassal is available (as will usually be the case),
      // drop back to the Base bundle.
      if (url == null && name.endsWith(EN_BUNDLE)) {
        url = getAResource(name.substring(0, name.lastIndexOf('/')+1)+BASE_BUNDLE);
      }
      
      return url;
    }
    
    public URL getAResource(String name) {
      URL url = null;
      final String propFileName = name.substring(name.lastIndexOf('/') + 1);
      final File propFile = new File(Info.getHomeDir(), propFileName);
      if (propFile.exists()) {
        try {
          url = propFile.toURI().toURL();
        }
        catch (MalformedURLException e) {
          ErrorDialog.bug(e);
        }
      }

      // No openable file in home dir, so let Java find one for us in
      // the standard classpath.
      if (url == null) {
        url = this.getClass().getClassLoader().getResource(name);
      }

      return url;
    }
  }

  public static void setLocale(Locale l) {
    locale = l;
    editorBundle = null;
    vassalBundle = null;
    UIManager.put("OptionPane.yesButtonText", getString(YES)); //$NON-NLS-1$
    UIManager.put("OptionPane.cancelButtonText", getString(CANCEL)); //$NON-NLS-1$
    UIManager.put("OptionPane.noButtonText", getString(NO)); //$NON-NLS-1$
    UIManager.put("OptionPane.okButtonText", getString(OK)); //$NON-NLS-1$
  }

  public static Locale getLocale() {
    return locale;
  }
  
  /**
   * Return a standard formatted localised date
   * @param date date to format
   * @return formatted localized date
   */
  public static String formatDate(Date date) {
    return new MessageFormat(DATE_FORMAT).format(new Object[] {date});
  }
}

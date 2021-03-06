/*
 * $Id: SpecialDieFace.java 4561 2008-11-28 15:57:51Z uckelman $
 *
 * Copyright (c) 2004 by Michael Blumohr, Rodney Kinney
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
package VASSAL.build.module;

import VASSAL.build.AbstractConfigurable;
import VASSAL.build.AutoConfigurable;
import VASSAL.build.Buildable;
import VASSAL.build.IllegalBuildException;
import VASSAL.build.module.documentation.HelpFile;
import VASSAL.configure.Configurer;
import VASSAL.configure.ConfigurerFactory;
import VASSAL.configure.IconConfigurer;

public class SpecialDieFace extends AbstractConfigurable {

  public static final String TEXT = "text"; //$NON-NLS-1$
  public static final String NUMERICAL_VALUE = "value"; //$NON-NLS-1$
  public static final String ICON = "icon"; //$NON-NLS-1$
  public static final String IMAGE = "image"; //$NON-NLS-1$

  private int value;
  private String imageName;

  public static String getConfigureTypeName() {
    return "Symbolic Die Face";
  }

  public String[] getAttributeDescriptions() {
    return new String[]{
      "Text Value:  ",
      "Numerical value:  ",
      "Icon:  "
    };
  }

  public Class<?>[] getAttributeTypes() {
    return new Class<?>[]{
      String.class,
      Integer.class,
      IconConfig.class
    };
  }

  public static class IconConfig implements ConfigurerFactory {
    public Configurer getConfigurer(
        AutoConfigurable c,
        String key,
        String name) {
      return new IconConfigurer(key, name, null);
    }
  }


  public String[] getAttributeNames() {
    String s[] = {TEXT, NUMERICAL_VALUE, ICON};
    return s;
  }

  public String getTextValue() {
    return getConfigureName();
  }

  public int getIntValue() {
    return value;
  }

  public String getImageName() {
    return imageName;
  }

  public void setAttribute(String key, Object o) {
    if (TEXT.equals(key)) {
      setConfigureName((String) o);
    }
    else if (NUMERICAL_VALUE.equals(key)) {
      try {
        if (o instanceof String) {
          o = Integer.valueOf((String) o);
        }
        value = ((Integer) o).intValue();
      }
      catch (NumberFormatException e) {
        throw new IllegalBuildException(e);
      }
    }
    else if (ICON.equals(key)) {
      imageName = (String) o;
    }
  }

  public String getAttributeValueString(String key) {
    if (TEXT.equals(key)) {
      return getConfigureName();
    }
    else if (NUMERICAL_VALUE.equals(key)) {
      return String.valueOf(value);
    }
    else if (ICON.equals(key)) {
      return imageName;
    }
    else
      return null;
  }

  public HelpFile getHelpFile() {
    return HelpFile.getReferenceManualPage("GameModule.htm", "SpecialDiceButton"); //$NON-NLS-1$ //$NON-NLS-2$
  }

  public Class<?>[] getAllowableConfigureComponents() {
    return new Class<?>[0];
  }

  public void addTo(Buildable parent) {
    ((SpecialDie) parent).addFace(this);
  }

  public void removeFrom(Buildable parent) {
    ((SpecialDie) parent).removeFace(this);
  }
}

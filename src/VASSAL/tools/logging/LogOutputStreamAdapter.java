/*
 * $Id: LogOutputStreamAdapter.java 5488 2009-04-11 14:01:20Z uckelman $
 *
 * Copyright (c) 2008-2009 by Joel Uckelman
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License (LGPL) as published by the Free Software Foundation.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, copies are available
 * at http://www.opensource.org.
 */

package VASSAL.tools.logging;

import java.io.IOException;
import java.io.OutputStream;

public class LogOutputStreamAdapter implements LogListener {
  private final OutputStream out;

  public LogOutputStreamAdapter(OutputStream out) {
    if (out == null) throw new NullPointerException();
    this.out = out;
  }

  public void handle(LogEntry entry) {
    try {
      out.write(entry.toString().getBytes());
      out.write('\n');
      out.flush();
    }
    catch (IOException e) {
      // FIXME: What to do here????
    }
  }
}
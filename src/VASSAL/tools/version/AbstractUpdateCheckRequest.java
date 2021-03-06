/*
 * $Id: AbstractUpdateCheckRequest.java 4254 2008-10-15 13:41:17Z uckelman $
 *
 * Copyright (c) 2008 by Joel Uckelman
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
package VASSAL.tools.version;

import java.io.IOException;
import org.jdesktop.swingworker.SwingWorker;

import VASSAL.Info;

/**
 * @since 3.1.0
 * @author Joel Uckelman
 */
public abstract class AbstractUpdateCheckRequest
                                      extends SwingWorker<VassalVersion,Void> {
  @Override
  protected VassalVersion doInBackground() throws IOException {
    final VassalVersion running = new VassalVersion(Info.getVersion());
    final VassalVersion update = VersionUtils.update(running);
    return update.equals(running) ? null : update;
  }

  @Override
  protected abstract void done();
}

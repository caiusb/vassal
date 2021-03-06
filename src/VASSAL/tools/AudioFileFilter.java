/*
 * $Id: AudioFileFilter.java 3562 2008-05-06 12:46:57Z uckelman $
 *
 * Copyright (c) 2006 by Joel Uckelman
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
package VASSAL.tools;

/**
 * A FileFilter for AIFF, AU, and WAV files. Used by file choosers to
 * filter out files which aren't audio files.
 * 
 * @author Joel Uckelman
 * @deprecated Moved to {@link VASSAL.tools.filechooser.AudioFileFilter}.
 */
@Deprecated
public class AudioFileFilter extends VASSAL.tools.filechooser.AudioFileFilter {
}

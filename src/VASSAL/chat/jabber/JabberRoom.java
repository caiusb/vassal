/*
 *
 * Copyright (c) 2000-2007 by Rodney Kinney
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
package VASSAL.chat.jabber;

import java.util.HashMap;
import java.util.Map;

import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.Form;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.RoomInfo;

import VASSAL.chat.LockableRoom;
import VASSAL.chat.SimpleRoom;

public class JabberRoom extends SimpleRoom implements LockableRoom {
  private String jid;
  private RoomInfo info;
  private boolean ownedByMe;

  private JabberRoom(String name, String jid, RoomInfo info) {
    super(name);
    this.jid = jid;
    this.info = info;
  }

  public String getJID() {
    return jid;
  }

  public boolean isLocked() {
    return info != null && info.isMembersOnly();
  }

  public MultiUserChat join(JabberClient client, JabberPlayer me) throws XMPPException {
    MultiUserChat chat = new MultiUserChat(client.getConnection(), getJID());
    chat.join(StringUtils.parseName(me.getJid()));
    try {
      // This is necessary to create the room if it doesn't already exist
      chat.sendConfigurationForm(new Form(Form.TYPE_SUBMIT));
      ownedByMe = true;
    }
    catch (XMPPException e) {
      // 403 code means the room already exists and user is not an owner
      if (e.getXMPPError().getCode() != 403) {
        throw e;
      }
    }

    try {
      chat.changeSubject(getName());
    }
    catch (XMPPException e) {
      // Room already exists but we're not the owner
    }

    chat.addMessageListener(client);
    return chat;
  }

  public boolean equals(Object o) {
    if (o instanceof JabberRoom) {
      JabberRoom r = (JabberRoom) o;
      return r.jid.equals(jid);
    }
    else {
      return false;
    }
  }

  public int hashCode() {
    return jid.hashCode();
  }

  public boolean isOwnedByMe() {
    return ownedByMe;
  }
  public static class Manager {
    private Map<String, JabberRoom> jidToRoom = new HashMap<String, JabberRoom>();

    public synchronized JabberRoom getRoomByJID(JabberClient client, String jid) {
      if (jid == null) {
        return null;
      }
      JabberRoom newRoom = jidToRoom.get(jid);
      if (newRoom == null) {
        String subject = "<no name>";
        RoomInfo info = null;
        try {
          info = MultiUserChat.getRoomInfo(client.getConnection(), jid);
          subject = info.getSubject();
        }
        // FIXME: review error message
        catch (XMPPException e) {
          e.printStackTrace();
        }
        newRoom = new JabberRoom(subject, jid, info);
        jidToRoom.put(jid, newRoom);
      }
      return newRoom;
    }

    public synchronized JabberRoom getRoomByName(JabberClient client, String name) {
      String jid = StringUtils.escapeNode(client.getModule() + "/" + name).toLowerCase() + "@" + client.getConferenceService();
      JabberRoom room = jidToRoom.get(jid);
      if (room == null) {
        room = new JabberRoom(name, jid, null);
        jidToRoom.put(jid, room);
      }
      return room;
    }

    public void deleteRoom(String jid) {
      jidToRoom.remove(jid);
    }
    
    public synchronized void clear() {
      jidToRoom.clear();
    }
  }
}

// Copyright (C) 2002  Strangeberry Inc.
// @(#)ServiceInfo.java, 1.21, 11/29/2002
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

package com.strangeberry.rendezvous;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Rendezvous service information.
 *
 * @author	Arthur van Hoff
 * @version 	1.21, 11/29/2002
 */
public class ServiceInfo extends Rendezvous.Listener
{
    public final static byte[] NO_VALUE = new byte[0];
    
    String type;
    String name;
    String server;
    int port;
    int weight;
    int priority;
    byte text[];
    Hashtable props;
    InetAddress addr;

    /**
     * Construct a service description for registrating with Rendezvous.
     * @param type fully qualified service type name
     * @param name fully qualified service name
     * @param addr the address to which the service is bound
     * @param port the local port on which the service runs
     * @param weight weight of the service
     * @param priority priority of the service
     * @param text string describing the service
     */
    public ServiceInfo(String type, String name, InetAddress addr, int port, int weight, int priority, String text)
    {
	this(type, name, addr, port, weight, priority, (byte[])null);
	try {
	    ByteArrayOutputStream out = new ByteArrayOutputStream(text.length());
	    writeUTF(out, text);
	    this.text = out.toByteArray();
	} catch (IOException e) {
	    throw new RuntimeException("unexpected exception: " + e);
	}
    }

    /**
     * Construct a service description for registrating with Rendezvous. The properties hashtable must
     * map property names to either Strings or byte arrays describing the property values.
     * @param type fully qualified service type name
     * @param name fully qualified service name
     * @param addr the address to which the service is bound
     * @param port the local port on which the service runs
     * @param weight weight of the service
     * @param priority priority of the service
     * @param props properties describing the service
     */
    public ServiceInfo(String type, String name, InetAddress addr, int port, int weight, int priority, Hashtable props)
    {
	this(type, name, addr, port, weight, priority, (byte [])null);
	try {
	    ByteArrayOutputStream out = new ByteArrayOutputStream(256);
	    for (Enumeration e = props.keys() ; e.hasMoreElements() ;) {
		String key = (String)e.nextElement();
		Object val = props.get(key);
		ByteArrayOutputStream out2 = new ByteArrayOutputStream(100);
		writeUTF(out2, key);
		if (val instanceof String) {
		    out2.write('=');
		    writeUTF(out2, (String)val);
		} else if (val instanceof byte[]) {
		    out2.write('=');
		    byte[] bval = (byte[])val;
		    out2.write(bval, 0, bval.length);
		} else if (val != NO_VALUE) {
		    throw new IllegalArgumentException("invalid property value: " + val);
		}
		byte data[] = out2.toByteArray();
		out.write(data.length);
		out.write(data, 0, data.length);
	    }
	    this.text = out.toByteArray();
	} catch (IOException e) {
	    throw new RuntimeException("unexpected exception: " + e);
	}
    }

    /**
     * Construct a service description for registrating with Rendezvous.
     * @param type fully qualified service type name
     * @param name fully qualified service name
     * @param addr the address to which the service is bound
     * @param port the local port on which the service runs
     * @param weight weight of the service
     * @param priority priority of the service
     * @param text bytes describing the service
     */
    public ServiceInfo(String type, String name, InetAddress addr, int port, int weight, int priority, byte text[])
    {
	this.type = type;
	this.name = name;
	this.port = port;
	this.weight = weight;
	this.priority = priority;
	this.server = name;
	this.text = text;
	this.addr = addr;
    }

    /**
     * Construct a serive record during service discovery.
     */
    ServiceInfo(String type, String name)
    {
	this.type = type;
	this.name = name;
    }

    /**
     * Fully qualified service type name, such as <code>_http._tcp.local.</code>.
     */
    public String getType()
    {
	return type;
    }
    /**
     * Service name, such as <code>foobar</code>.
     */
    public String getName()
    {
	if ((type != null) && name.endsWith("." + type)) {
	    return name.substring(0, name.length() - (type.length() + 1));
	}
	return name;
    }
    /**
     * Get the host address of the service (ie X.X.X.X).
     */
    public String getAddress()
    {
	byte data[] = addr.getAddress();
	return (data[0] & 0xFF) + "." + (data[1] & 0xFF) + "." + (data[2] & 0xFF) + "." + (data[3] & 0xFF);
    }
    /**
     * Get the port for the service.
     */
    public int getPort()
    {
	return port;
    }
    /**
     * Get the priority of the service.
     */
    public int getPriority()
    {
	return priority;
    }
    /**
     * Get the weight of the service.
     */
    public int getWeight()
    {
	return weight;
    }

    /**
     * Get the text for the serivce as raw bytes.
     */
    public byte[] getTextBytes()
    {
	return text;
    }

    /**
     * Get the text for the service. This will interpret the text bytes
     * as a UTF8 encoded string. Will return null if the bytes are not
     * a valid UTF8 encoded string.
     */
    public String getTextString()
    {
	if ((text == null) || (text.length == 0) || ((text.length == 1) && (text[0] == 0))) {
	    return null;
	}
	return readUTF(text, 0, text.length);
    }

    /**
     * Get a property of the service. This involves decoding the
     * text bytes into a property list. Returns null if the property
     * is not found or the text data could not be decoded correctly.
     */
    public synchronized byte[] getPropertyBytes(String name)
    {
	return (byte [])getProperties().get(name);
    }

    /**
     * Get a property of the service. This involves decoding the
     * text bytes into a property list. Returns null if the property
     * is not found, the text data could not be decoded correctly, or
     * the resulting bytes are not a valid UTF8 string.
     */
    public synchronized String getPropertyString(String name)
    {
	byte data[] = (byte [])getProperties().get(name);
	if (data == null) {
	    return null;
	}
	if (data == NO_VALUE) {
	    return "true";
	}
	return readUTF(data, 0, data.length);
    }

    /**
     * Enumeration of the property names.
     */
    public Enumeration getPropertyNames()
    {
	Hashtable props = getProperties();
	return (props != null) ? props.keys() : new Vector().elements();
    }

    /**
     * Write a UTF string with a length to a stream.
     */
    void writeUTF(OutputStream out, String str) throws IOException
    {
	for (int i = 0, len = str.length() ; i < len ; i++) {
	    int c = str.charAt(i);
	    if ((c >= 0x0001) && (c <= 0x007F)) {
		out.write(c);
	    } else if (c > 0x07FF) {
		out.write(0xE0 | ((c >> 12) & 0x0F));
		out.write(0x80 | ((c >>  6) & 0x3F));
		out.write(0x80 | ((c >>  0) & 0x3F));
	    } else {
		out.write(0xC0 | ((c >>  6) & 0x1F));
		out.write(0x80 | ((c >>  0) & 0x3F));
	    }
	}
    }

    /**
     * Read data bytes as a UTF stream.
     */
    String readUTF(byte data[], int off, int len)
    {
	StringBuffer buf = new StringBuffer();
	for (int end = off + len ; off < end ; ) {
	    int ch = data[off++] & 0xFF;
	    switch (ch >> 4) {
	      case 0: case 1: case 2: case 3: case 4: case 5: case 6: case 7:
		// 0xxxxxxx
		break;
	      case 12: case 13:
		if (off >= len) {
		    return null;
		}
		// 110x xxxx   10xx xxxx
		ch = ((ch & 0x1F) << 6) | (data[off++] & 0x3F);
		break;
	      case 14:
		if (off + 2 >= len) {
		    return null;
		}
		// 1110 xxxx  10xx xxxx  10xx xxxx
		ch = ((ch & 0x0f) << 12) | ((data[off++] & 0x3F) << 6) | (data[off++] & 0x3F);
		break;
	      default:
		if (off + 1 >= len) {
		    return null;
		}
		// 10xx xxxx,  1111 xxxx
		ch = ((ch & 0x3F) << 4) | (data[off++] & 0x0f);
		break;
	    }
	    buf.append((char)ch);
	}
	return buf.toString();
    }

    synchronized Hashtable getProperties()
    {
	if ((props == null) && (text != null)) {
	    props = new Hashtable();
	    int off = 0;
	    while (off < text.length) {
		// length of the next key value pair
		int len = text[off++] & 0xFF;
		if ((len == 0) || (off + len > text.length)) {
		    props.clear();
		    break;
		}
		// look for the '='
		int i = 0;
		for (; (i < len) && (text[off + i] != '=') ; i++);

		// get the property name
		String name = readUTF(text, off, i);
		if (name == null) {
		    props.clear();
		    break;
		}
		if (i == len) {
		    props.put(name, NO_VALUE);
		} else {
		    byte value[] = new byte[len - i];
		    System.arraycopy(text, off + i + 1, value, 0, len - i -1);
		    props.put(name, value);
		    off += len;
		}
	    }
	}
	return props;
    }

    /**
     * Get the ip address of the service.
     */
    int getIPAddress()
    {
	byte data[] = addr.getAddress();
	return ((data[0] & 0xFF) << 24) | ((data[1] & 0xFF) << 16) | ((data[2] & 0xFF) << 8) | (data[3] & 0xFF);
    }

    /**
     * Rendezvous callback to update a DNS record.
     */
    void updateRecord(Rendezvous rendezvous, long now, DNSRecord rec)
    {
	if ((rec != null) && !rec.isExpired(now)) {
	    switch (rec.type) {
	      case TYPE_A:
		if (rec.name.equals(server)) {
		    addr = ((DNSRecord.Address)rec).getAddress();
		}
		break;
	      case TYPE_SRV:
		if (rec.name.equals(name)) {
		    DNSRecord.Service srv = (DNSRecord.Service)rec;
		    server = srv.server;
		    port = srv.port;
		    weight = srv.weight;
		    priority = srv.priority;
		    addr = null;
		    updateRecord(rendezvous, now, (DNSRecord)rendezvous.cache.get(server, TYPE_A, CLASS_IN));
		}
		break;
	      case TYPE_TXT:
		if (rec.name.equals(name)) {
		    DNSRecord.Text txt = (DNSRecord.Text)rec;
		    text = txt.text;
		}
		break;
	    }
	}
    }

    /**
     * Update the server information from the cache, send out
     * repeated DNS queries for updated information.
     */
    boolean request(Rendezvous rendezvous, long timeout)
    {
	long now = System.currentTimeMillis();
	int delay = 200;
	long next = now + delay;
	long last = now + timeout;
	try {
	    rendezvous.addListener(this, new DNSQuestion(name, TYPE_ANY, CLASS_IN));
	    while (server == null || addr == null || text == null) {
		// check if timeout was reached
		if (last <= now) {
		    return false;
		}
		// check if we need to send out another request
		if (next <= now) {
		    DNSOutgoing out = new DNSOutgoing(FLAGS_QR_QUERY);
		    out.addQuestion(new DNSQuestion(name, TYPE_SRV, CLASS_IN));
		    out.addQuestion(new DNSQuestion(name, TYPE_TXT, CLASS_IN));
		    if (server != null) {
			out.addQuestion(new DNSQuestion(server, TYPE_A, CLASS_IN));
		    }
		    out.addAnswer((DNSRecord)rendezvous.cache.get(name, TYPE_SRV, CLASS_IN), now);
		    out.addAnswer((DNSRecord)rendezvous.cache.get(name, TYPE_TXT, CLASS_IN), now);
		    if (server != null) {
			out.addAnswer((DNSRecord)rendezvous.cache.get(server, TYPE_A, CLASS_IN), now);
		    }
		    rendezvous.send(out);

		    next = now + delay;
		    delay = delay * 2;
		}
		// wait for an update or a timeout
		synchronized (rendezvous) {
		    rendezvous.wait(Math.min(next, last) - now);
		}
		now = System.currentTimeMillis();
	    }
	    return true;
	} catch (IOException e) {
	    return false;
	} catch (InterruptedException e) {
	    return false;
	} finally {
	    rendezvous.removeListener(this);
	}
    }

    public int hashCode()
    {
	return name.hashCode();
    }

    public boolean equals(Object obj)
    {
	return (obj instanceof ServiceInfo) && name.equals(((ServiceInfo)obj).name);
    }

    public String toString()
    {
	StringBuffer buf = new StringBuffer();
	buf.append("service[");
	buf.append(name);
	buf.append(',');
	buf.append(getAddress());
	buf.append(':');
	buf.append(port);
	buf.append(',');
	buf.append((text.length < 20) ? new String(text) : new String(text, 0, 17) + "...");
	buf.append(']');
	return buf.toString();
    }
}

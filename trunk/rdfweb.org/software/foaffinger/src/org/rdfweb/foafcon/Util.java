package org.rdfweb.foafcon;

import java.io.*;
import java.net.*;

public class Util
{
  public static String replace(String s, char oldChar, String newChar)
  {
    char [] myCharArray = s.toCharArray();
    StringBuffer sb = new StringBuffer();

    for (int counter=0; counter<myCharArray.length; counter++)
      {
	if (myCharArray[counter] == oldChar)
	  sb.append(newChar);
	else
	  sb.append(myCharArray[counter]);
      }

    return sb.toString();
  }

  public static String escape(String toEscape)
  {
    toEscape = replace(toEscape, '&', "&amp;");
    toEscape = replace(toEscape, '<', "&lt;");
    toEscape = replace(toEscape, '>', "&gt;");
    toEscape = replace(toEscape, '\"', "&quot;");

    return toEscape;
  }
  
  public static String get(String scheme,
			   String address,
			   int port,
			   String object)
  {
    try
      {
	URL url = new URL(scheme,
			  address,
			  port,
			  object);
	
	InputStream conn = url.openStream();
	
	Reader reader =
	  new InputStreamReader(conn);
	BufferedReader bufReader =
	  new BufferedReader(reader);

	StringBuffer sb = new StringBuffer();
	
	while (true)
	  {
	    String textLine = bufReader.readLine();
	    
	    if (textLine == null) break;
	    
	    sb.append(textLine);
	    sb.append("\n");
	  }

	bufReader.close();
	reader.close();
	conn.close();
	
	return sb.toString();
      }
    catch (Exception e)
      {
	return (e.getMessage());
      }
  }

  public static void toFile(String file, String content)
    throws Exception
  {
    Writer writer = new FileWriter(file);

    writer.write(content, 0, content.length());

    writer.flush();
    writer.close();
  }

  public static InetAddress getAddress()
    throws Exception
  {

    InetAddress yourIP=InetAddress.getLocalHost();
    
    InetAddress[] allHostInfo = InetAddress.getAllByName(yourIP.getHostName());

    for (int i=0; i<allHostInfo.length; i++)
      {
	System.out.println(allHostInfo[i]);
	
	if (!allHostInfo[i].getHostAddress().equals("127.0.0.1"))
	  return allHostInfo[i];
	
	
      }

    throw new Exception("Can't get a non-loopback address");
    
  }
  
  
}

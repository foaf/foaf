package org.rdfweb.foafcon;

import java.io.*;
import java.net.*;
import java.util.*;

import java.math.BigInteger;
import java.security.MessageDigest;

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

  public static String sha1Hash(String string)
  {
    try
      {
	MessageDigest md = MessageDigest.getInstance("SHA");
	
	md.update(string.getBytes());
	
	byte[] digest = md.digest();
	
	BigInteger integer = new BigInteger(1, digest);

	return integer.toString(16);
      }
    catch (Exception e)
      {
	return null;
      }
  }
  
  public static List tokenise(StreamTokenizer in)
  {
    ArrayList toReturn = new ArrayList();
    try
      {
	for (int i = in.nextToken();
	     (i != StreamTokenizer.TT_EOL) &&
	       (i != StreamTokenizer.TT_EOF);
	     i = in.nextToken())
	  {
	    if ((i == StreamTokenizer.TT_WORD) ||
		(i == '\"') ||
		(i == '\''))
	      toReturn.add(in.sval);
	    else if (i == StreamTokenizer.TT_NUMBER)
	      toReturn.add(new Double(in.nval));
	    else
	      System.err.println("Unknown token?");
	  }
      }
    catch (IOException e)
      {
	System.err.println("IO Exception while tokenising: " +
			   e.getMessage());
      }
    
    return toReturn;
  }

  public static boolean isURL(String pUrl)
  {
    try
      {
	URL url = new URL(pUrl);

	return true;
      }
    catch (MalformedURLException e)
      {
	return false;
      }
  }
  
}

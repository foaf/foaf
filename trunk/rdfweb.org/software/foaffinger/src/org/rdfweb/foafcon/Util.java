package org.rdfweb.foafcon;

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
			   String port,
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

	return sb.toString();
      }
    catch (Exception e)
      {
	return (e.getMessage());
      }
  }
  
}

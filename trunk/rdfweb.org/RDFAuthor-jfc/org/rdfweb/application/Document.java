/* Document */

/* $Id: Document.java,v 1.1.1.1 2002-04-09 12:49:40 pldms Exp $ */

/*
    Copyright 2002 Damian Steer <pldms@mac.com>

    This file is part of RDFAuthor.

    RDFAuthor is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    RDFAuthor is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
    
    You should have received a copy of the GNU General Public License
    along with RDFAuthor; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
    
*/

/**
 *
 * This class is inspired by the OpenStep/Cocoa multiple document
 * architecture. See Application for details.
 *
 **/

package org.rdfweb.application;

import java.net.URL;
import java.io.*;

public class Document
{
  static int untitledCount = 0;
  
  public Window window;

  Object source;
  String docType;
  String title;

  public Document()
  {
    setSource(null);
    setDocType(null);
  }
  
  public Document(File file, String docType) throws DocumentLoadFailedException
  {
    System.out.println("Open file: " + file + " Doc type: " + docType);

    if (!file.exists()) throw
			  (new DocumentLoadFailedException("File does not exist"));
    if (!file.canRead()) throw
			   (new DocumentLoadFailedException("File is unreadable"));
    if (file.isDirectory()) throw
			      (new DocumentLoadFailedException("Can't load directories"));
    
    setSource(file);
    setDocType(docType);

    boolean successful = false;
    
    try
      {
	FileInputStream fileInput = new FileInputStream(file);
	successful = loadStreamOfType(fileInput, docType);
	fileInput.close();
      }
    catch (Exception e)
      {
	System.out.println(e);
	successful = true;
      }

    if (!successful) throw (new DocumentLoadFailedException("Loading failed"));
  }
  
  public Document(URL url, String docType) throws DocumentLoadFailedException
  {
    System.out.println("Open URL: " + url + " Doc type: " + docType);
    
    setSource(url);
    setDocType(docType);

    boolean successful = false;

    try
      {
	InputStream input = url.openStream();
	successful = loadStreamOfType(input, docType);
	input.close();
      }
    catch (Exception e)
      {
	System.out.println(e);
	successful = false;
      }

    if (!successful) throw (new DocumentLoadFailedException("Loading failed"));
  }

  public boolean loadStreamOfType(InputStream stream, String docType)
  {
    return false;
  }
  
  public void setSource(Object source)
  {
    this.source = source;
    
    if (source instanceof File)
      {
		title = ((File) source).getName();
      }
    else if (source instanceof URL)
      {
	title = ((URL) source).toString();
      }
    else
      {
	if (untitledCount == 0)
	  {
	    title = "Untitled";
	  }
	else
	  {
	    title = "Untitled " + untitledCount;
	  }
	
	untitledCount++;
      }
    
	if (window != null) 
	  {
	    window.setTitle(title);
	    Application.sharedApplication().buildWindowMenus();
	  }
  }

  public Object source()
  {
    return source;
  }

  public String title()
  {
    return title;
  }
  
  public void setDocType(String docType)
  {
    this.docType = docType;
  }
  
  public String docType()
  {
    return docType;
  }
  
  public static String windowClass()
  {
    return "org.rdfweb.application.Window";
  }

  public static DocTypeFilter[] docTypes()
  {
    return new DocTypeFilter[] {
      new DocTypeFilter( "XML Document", "xml" ),
	new DocTypeFilter( "RDFAuthor Document", "rdfa"),
	new DocTypeFilter( "Image", new String[] 
	{
	  "gif", "jpeg", "jpg", "png" 
	    }
			   ) 
	};
    
    
  }
  
  public void createInterface()
  {
    Class windowClass = Application.sharedApplication.windowClass();
    
    try
      {
	window = (Window) windowClass.getConstructor(new Class[] {Document.class}).newInstance(new Object[] {this});
	interfaceLoaded();
      }
    catch (Exception e)
      {
	System.out.println(e);
	e.printStackTrace();
      }
    
    Application.sharedApplication().buildWindowMenus();
    
    window.setTitle(title);
    //window.pack();
    window.show();
    
  }
  
  public Window window()
  {
    return window;
  }

  public void interfaceLoaded()
  {
  }
  
}
	
class DocumentLoadFailedException extends Exception
{
  public DocumentLoadFailedException()
  {
    super();
  }

  public DocumentLoadFailedException(String s)
  {
    super(s);
  }
}

	

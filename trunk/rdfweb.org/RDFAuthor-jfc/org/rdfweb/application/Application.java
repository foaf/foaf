/* Application */

/* $Id: Application.java,v 1.2 2002-04-11 12:32:05 pldms Exp $ */

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
 * architecture: specifically NSApplication and
 * NSDocumentController. Together with org.rdfweb.application.Document
 * and org.rdfweb.application.Window hopefully we'll get a nice
 * separation of application logic and document logic. Well, here's
 * hoping.
 * RDFAuthor is a port to JFC of an OS X app of the same name.
 *
 **/
   
package org.rdfweb.application;

import java.util.*;
import java.net.URL;
import java.io.File;
import java.lang.reflect.*;
import javax.swing.*;

public class Application
{
  // One Appliction per application, please...
  protected static Application sharedApplication;
  
  protected ArrayList documents;
  
  protected Class documentClass;
  protected Class windowClass;

  protected DocTypeFilter[] docTypes;
    
  public Application(String documentClass)
  {
    if (sharedApplication != null)
      {
	System.err.println("One Application only!");
	System.exit(0);
      }
    
    try
      {
	this.documentClass = Class.forName(documentClass);
      }
    catch (Exception e)
      {
	System.err.println(e);
	e.printStackTrace();
	System.exit(1);
      }
    
    // Is this a subclass of Document?
    if (!Document.class.isAssignableFrom(this.documentClass))
      {
	System.err.println(documentClass +
			   " is not a subclass of Document");
	
	System.exit(1);
      }
    
    String interfaceClass = "";
    try
      {
	Method getInterface =
	  this.documentClass.getMethod("windowClass", null);
	interfaceClass =
	  (String) getInterface.invoke(null, null);
	      
	this.windowClass = Class.forName(interfaceClass);
	      
      }
    catch (Exception e)
      {
	System.out.println(e);
	e.printStackTrace();
	System.exit(1);
      }
    
    if (!Window.class.isAssignableFrom(windowClass))
      {
	System.err.println(interfaceClass +
			   " not a Window subclass");
	System.exit(1);
      }


    try
      {
	Method getDocTypes =
	  this.documentClass.getMethod("docTypes", null);
	docTypes =
	  (DocTypeFilter[]) getDocTypes.invoke(null, null);
	      
	this.windowClass = Class.forName(interfaceClass);
	      
      }
    catch (Exception e)
      {
	System.out.println(e);
	e.printStackTrace();
	System.exit(1);
      }
    
    documents = new ArrayList();
    sharedApplication = this;
  }

  public Class windowClass()
  {
    return windowClass;
  }
    
  public static Application sharedApplication()
  {
    return sharedApplication;
  }

  public static void run(String documentClass, String[] args)
  {
    Application app = new Application(documentClass);

    if (args.length != 0)
      {
	for (int i = 0; i < args.length; i++)
	  {
	    app.openDocument(args[i]);
	  }
      }
    else
      {
	app.openDocument();
      }
  }

  public void newDoc(EventObject e)
  {
    Document document = newUntitledDocument();
    addDocument(document);
  }
    
  public void open(EventObject e)
  {
    JFileChooser chooser = new JFileChooser();

    if (docTypes != null)
      {
	for (int i = 0; i < docTypes.length; i++)
	  {
	    chooser.addChoosableFileFilter(docTypes[i]);
	  }
      }
    
    int result = chooser.showOpenDialog(null);
    if (result == JFileChooser.APPROVE_OPTION)
      {
	openDocumentWithContentsOfFile(chooser.getSelectedFile());
      }
  }

  public void openURL(EventObject e)
  {
    String result = (String) JOptionPane.showInputDialog(null,
						"Enter a URL to open:",
						"Open URL",
						JOptionPane.QUESTION_MESSAGE,
						new ImageIcon("org/rdfweb/application/Images/earth.png"),
						null,
						null);
    
    if ((result != null) && (result.trim() != ""))
      {
	URL url = null;
	try
	  {
	    url = new URL(result.trim());
	  }
	catch (Exception except)
	  {
	    System.out.println("Not a URL." + except);
	    
	    JOptionPane.showMessageDialog(null, except.getMessage(),
					  "Error",
					  JOptionPane.ERROR_MESSAGE);
	    return;
	  }
	
	openDocumentWithContentsOfURL(url);
      }
    
  }

  public void quit(EventObject e)
  {
  }
  
  public Document newUntitledDocument()
  {
    Document document = null;

    try
      {
	document = (Document) documentClass.newInstance();
      }
    catch (Exception e)
      {
	System.err.println(e);
	e.printStackTrace();
      }
	
    return document;
  }
    
  public Document newDocumentFromFile(File file)
  {
    Document document = null;
    String docType = docTypeForObject(file);
    
    try
      {
	document = (Document) documentClass.getConstructor(new Class[] {File.class, String.class}).newInstance(new Object[] {file, docType});
      }
    catch (InvocationTargetException ie)
      {
	Throwable exception = ie.getTargetException();

	JOptionPane.showMessageDialog(null, exception.getMessage(),
				      "Error",
				      JOptionPane.ERROR_MESSAGE);
      }
    catch (Exception e)
      {
	System.err.println(e);
	e.printStackTrace();
      }
	
    return document;
  }
    
  public Document newDocumentFromURL(URL url)
  {
    Document document = null;
    String docType = docTypeForObject(url);
    
    try
      {
	document = (Document) documentClass.getConstructor(new Class[] {URL.class, String.class}).newInstance(new Object[] {url, docType});
      }
    catch (InvocationTargetException ie)
      {
	Throwable exception = ie.getTargetException();

	JOptionPane.showMessageDialog(null, exception.getMessage(),
				      "Error",
				      JOptionPane.ERROR_MESSAGE);
      }
    catch (Exception e)
      {
	System.err.println(e);
	e.printStackTrace();
      }
	
    return document;
  }


  public void openDocument()
  {
    Document document = newUntitledDocument();
    if (document != null)
      {
	addDocument(document);
      }
  }

  public void openDocument(String source)
  {
    URL sourceURL = null;
    Document document = null;
    try
      {
	sourceURL = new URL(source);
      }
    catch (Exception e)
      {
      }

    if (sourceURL == null)
      {
	openDocumentWithContentsOfFile(new File(source));
      }
    else
      {
	openDocumentWithContentsOfURL(sourceURL);
      }
  }

  public void openDocumentWithContentsOfFile(File file)
  {
    Document document = newDocumentFromFile(file);
    addDocument(document);
  }
    
  public void openDocumentWithContentsOfURL(URL url)
  {
    Document document = newDocumentFromURL(url);
    addDocument(document);
  }
    
  public void addDocument(Document document)
  {
    if (document == null) return;

    documents.add(document);
    document.createInterface();
  }

  public void quit()
  {
  }

  public void buildWindowMenus()
  {
    for (Iterator i = documents.iterator(); i.hasNext();)
      {
	Document doc = (Document) i.next();
		
	JMenu windowMenu = doc.window().windowMenu();
	windowMenu.removeAll();
		
	for (Iterator j = documents.iterator(); j.hasNext();)
	  {
	    Document doc2 = (Document) j.next();
			
	    JMenuItem item = new JMenuItem(doc2.title());
	    item.addActionListener(new
				   TargetSelectorAction(this, "changeWindow"));
	    item.putClientProperty("Document", doc2);
			
	    windowMenu.add(item);
	  }
      }
  }

  public void changeWindow(EventObject e)
  {
    JMenuItem sender = (JMenuItem) e.getSource();

    Document doc = (Document) sender.getClientProperty("Document");

    doc.window().toFront();
  }

  public String docTypeForObject(Object ob)
  {
    if (docTypes == null) return null;
    
    String name = ob.toString();

    for (int i = 0; i < docTypes.length; i++)
      {
	DocTypeFilter filter = docTypes[i];

	if (filter.accept(name)) return filter.getDescription();
      }
    
    return null;
  }
  
}
	

	

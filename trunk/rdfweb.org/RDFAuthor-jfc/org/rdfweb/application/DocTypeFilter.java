/* DocTypeFilter */

/* $Id: DocTypeFilter.java,v 1.1.1.1 2002-04-09 12:49:40 pldms Exp $ */

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
 * This is a simple file filter
 *
 **/

package org.rdfweb.application;

import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.net.URL;
import java.util.HashSet;

public class DocTypeFilter extends FileFilter
{
  String docType;
  HashSet extensions;

  public DocTypeFilter()
  {
    this("", (String[]) null);
  }

  public DocTypeFilter(String docType)
  {
    this(docType, (String[]) null);
  }

  public DocTypeFilter(String docType, String extension)
  {
    this(docType, new String[] { extension });
  }
	 
  public DocTypeFilter(String[] extensionArray)
  {
    this("", extensionArray);
  }
  
  public DocTypeFilter(String docType, String[] extensionArray)
  {
    extensions = new HashSet();
    this.docType = docType;
    
    if (extensionArray != null)
      {
	// Hmm - shouldn't this be in the Collection constructor?
	for (int i = 0; i < extensionArray.length; i++)
	  {
	    extensions.add(extensionArray[i].toLowerCase());
	  }
      }
  }

  public void add(String extension)
  {
    extensions.add(extension);
  }
  
  public String getDescription()
  {
    return docType;
  }  
  
  public boolean accept(File f)
  {
    if (f.isDirectory()) return true;
    
    String extension = getExtension(f.getName());

    return extensions.contains(extension);
  }

  public boolean accept(String object)
  {
    String extension = getExtension(object);

    return extensions.contains(extension);
  }
      
  public String getExtension(String string)
  {
    int i = string.lastIndexOf(".");
    
    if ((i < 0) || (i == string.length())) return null;

    return string.substring(i+1).toLowerCase();
  }

}


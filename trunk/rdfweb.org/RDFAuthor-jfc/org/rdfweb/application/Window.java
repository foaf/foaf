/* Window */

/* $Id: Window.java,v 1.1.1.1 2002-04-09 12:49:40 pldms Exp $ */

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

import javax.swing.*;
import java.awt.event.*;
import java.awt.Dimension;

public class Window extends JFrame
    implements WindowListener
{
    protected Document document;
    protected JMenu windowMenu;
    
    public Window(Document document)
    {
	super();
	
	this.document = document;

	addWindowListener(this);

	setJMenuBar(new JMenuBar());

	setBounds(100,100,300,200);

	createMenus();

	initInterface();

	createOtherMenus();
    }

  public void initInterface()
  {
  }
  
  public void createMenus()
  {
	JMenu menu;
	JMenuItem item;
	Application app = Application.sharedApplication();
	
	menu = new JMenu("File");
	item = new JMenuItem("New");
	item.addActionListener(new
			       TargetSelectorAction(app, "newDoc"));
	menu.add(item);

	item = new JMenuItem("Open");
	item.addActionListener(new
			       TargetSelectorAction(app, "open"));
	menu.add(item);

	item = new JMenuItem("Open URL");
	item.addActionListener(new
			       TargetSelectorAction(app, "openURL"));
	menu.add(item);

	item = new JMenuItem("Close");

	item.addActionListener(new
			       TargetSelectorAction(this, "close"));
	menu.add(item);

	menu.addSeparator();

	item = new JMenuItem("Quit");

	item.addActionListener(new
			       TargetSelectorAction(app, "quit"));
	menu.add(item);

	getJMenuBar().add(menu);
    }

  public void createOtherMenus()
  {
    windowMenu = new JMenu("Window");

    getJMenuBar().add(windowMenu);
  }
  
  public JMenu windowMenu()
  {
    return windowMenu;
  }

    public void close(ActionEvent e)
    {
    }
    
    public void windowActivated(WindowEvent e)
    {
    }

    public void windowClosed(WindowEvent e)
    {
    }

    public void windowClosing(WindowEvent e)
    {
    }

    public void windowDeactivated(WindowEvent e)
    {
    }

    public void windowDeiconified(WindowEvent e)
    {
    }

    public void windowIconified(WindowEvent e)
    {
    }

    public void windowOpened(WindowEvent e)
    {
    }

}
	



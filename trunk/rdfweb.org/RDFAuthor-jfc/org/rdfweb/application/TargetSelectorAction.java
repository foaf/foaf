/* TargetSelectorAction */

/* $Id: TargetSelectorAction.java,v 1.1.1.1 2002-04-09 12:49:40 pldms Exp $ */

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
 * This class is intended to augment JFCs 'ActionListener' model,
 * which I find amazingly annoying compared with Cocoa/NeXT's
 * Target/Selector model. Many things (like menu code) becomes
 * horribly verbose - but maybe I'm not doing it right.
 *
 * Anyway, this class implements ActionListener and takes a target (an
 * object to which the message is sent) and a selector (a method).
 *
 **/

package org.rdfweb.application;

import java.awt.event.*;
import java.lang.reflect.Method;

public class TargetSelectorAction implements ActionListener
{
    Object target;
    String selector;
    Method method;

    public TargetSelectorAction(Object target, String selector)
    {
	this.target = target;
	this.selector = selector;

	Class targetClass = target.getClass();

	try
	    {
		method = targetClass.getMethod(selector,
					       new Class[] {ActionEvent.class} );
	    }
	catch (Exception e)
	    {
		System.out.println("Method not found: " +
				   selector +
				   " for object: " + target);
		System.err.println(e);
		e.printStackTrace();
	    }
    }

    public void actionPerformed(ActionEvent event)
    {
	if (method == null)
	    {
		System.err.println("Can't send " +
				   selector +
				   " to object " +
				   target);
		return;
	    }
	try
	    {
		method.invoke(target, new Object[] {event});
	    }
	catch (Exception e)
	    {
		System.err.println(e);
		e.printStackTrace();
	    }
    }

}

	

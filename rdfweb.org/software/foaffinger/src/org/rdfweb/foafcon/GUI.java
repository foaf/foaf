/*
 * Created on Aug 15, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.rdfweb.foafcon;

import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

/**
 * @author pldms
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class GUI extends ApplicationWindow implements UI
{
    private Table table;
    private Image presentImage;
    private Image absentImage;

    public GUI()
	{
		super(null);
	}
	
	public void run()
	{
		this.setBlockOnOpen(true);
		this.open();
		Display.getCurrent().dispose();
	}
	
	protected Control createContents(Composite parent)
	{
		table = new Table(parent, SWT.CHECK);
		
		try
		{
		    presentImage = new Image(Display.getCurrent(), "images/present.gif");
		    absentImage = new Image(Display.getCurrent(), "images/absent.gif");
		}
		catch (SWTException e)
		{
		    System.err.println("Error: " + e);
		}
		System.out.println("Images: " + presentImage + " " + absentImage);
		
		/*TableItem pres = new TableItem(table, 0);
		pres.setImage(presentImage);
		pres.setText("Damian");
		
		TableItem abs = new TableItem(table, 0);
		abs.setImage(absentImage);
		abs.setText("Libby");*/
		
		return table;
	}

	/* (non-Javadoc)
	 * @see org.rdfweb.foafcon.UI#showMessage()
	 */
	public void addMessage(final Message message) 
	{

	    Display.getDefault().syncExec( new Runnable() { public void run() {
	    if (message instanceof Message.PersonOnline)
	        {
	        		TableItem item;
	        		
	        		if (table.getItemCount() <= message.getIndex())
	        		    	item = new TableItem(table, 0);
	        		else
	        		    item = table.getItem(message.getIndex());
	        		
	        		item.setImage(presentImage);
	        		item.setText(message.getPerson().getName());
	        }
	    else if (message instanceof Message.PersonOffline)
	        {
	        		TableItem item = table.getItem(message.getIndex());
	        		item.setImage(absentImage);
	        }
	    else
	        System.out.println("Message: " + message.getMessage());
	    }});
	}
	
	
}

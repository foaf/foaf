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
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

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
    private Text nameText;
    private Text mboxText;
    private Text homepageText;
    private Text seeAlsoText;
    private Text interestText;
    
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
	    getShell().setText("Foaf Finger");
	    
	    /*TabFolder tabFolder = new TabFolder(parent, SWT.NONE);
	    
	    TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
	    
	    tabItem.setText("My Details");
	    
	    tabItem = new TabItem(tabFolder, SWT.NONE);
	    
	    tabItem.setText("Local Network");
	    */
	    SashForm sash = new SashForm(parent, SWT.HORIZONTAL);
	    
		table = new Table(sash, SWT.CHECK);
		
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
		
		Composite container = new Composite(sash, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		
		container.setLayout(layout);
		
		Label label = new Label(container, SWT.NONE);
		label.setText("Name:");
		nameText = new Text(container, SWT.SINGLE | SWT.READ_ONLY);
		nameText.setText("Name");
		
		label = new Label(container, SWT.NONE);
		label.setText("Mail Hash:");
		mboxText = new Text(container, SWT.SINGLE | SWT.READ_ONLY);
		mboxText.setText("0x39DF908G");
		
		label = new Label(container, SWT.NONE);
		label.setText("Homepage:");
		homepageText = new Text(container, SWT.SINGLE | SWT.READ_ONLY);
		homepageText.setText("http://example.com/");
		
		label = new Label(container, SWT.NONE);
		label.setText("Interest:");
		interestText = new Text(container, SWT.SINGLE | SWT.READ_ONLY);
		interestText.setText("http://example.com/foaffinger/");
		
		label = new Label(container, SWT.NONE);
		label.setText("See Also:");
		seeAlsoText = new Text(container, SWT.SINGLE | SWT.READ_ONLY);
		seeAlsoText.setText("http://example.com/me.rdf");
		
		return sash;
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
	        		
	        		Person person = message.getPerson();
	        		
	        		setField(nameText, person.getName());
	        		setField(mboxText, person.getMboxHash());
	        		setField(homepageText, person.getHomepage());
	        		setField(interestText, person.getInterest());
	        		setField(seeAlsoText, person.getSeeAlso());
	        }
	    else if (message instanceof Message.PersonOffline)
	        {
	        		TableItem item = table.getItem(message.getIndex());
	        		item.setImage(absentImage);
	        }
	    else
	        System.out.println("Message: " + message.getMessage());
	    }

        private void setField(Text nameText, String name)
        {
            if (name == null)
                nameText.setText("");
            else
                nameText.setText(name);
        }});
	}
	
	
}

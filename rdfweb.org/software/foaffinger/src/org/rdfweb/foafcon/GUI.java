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
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;

/**
 * @author pldms
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class GUI extends ApplicationWindow 
	implements UI, ModifyListener, SelectionListener
{
	private FoafFingerController controller;
    private Table table;
    private Image presentImage;
    private Image absentImage;
    private Text nameText;
    private Text mboxText;
    private Text homepageText;
    private Text seeAlsoText;
    private Text interestText;
	private Text myNameText;
	private Text myMailText;
	private Text myHomepageText;
	private Text myInterestText;
	private Text mySeeAlsoText;
	private Text myPlanText;
	private Button revertButton;
	private Button changeButton;
	private Button showMboxButton;
    
    public GUI(FoafFingerController controller)
	{
		super(null);
		this.controller = controller;
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
	    
	    TabFolder tabFolder = new TabFolder(parent, SWT.NONE);
	    
	    TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
	    
	    tabItem.setText("My Details");
	    
	    Composite container = new Composite(tabFolder, SWT.NONE);
	    
	    tabItem.setControl(container);
	    
	    GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		
		container.setLayout(layout);
	    
		Label label = new Label(container, SWT.NONE);
		label.setText("Name:");
		myNameText = new Text(container, SWT.SINGLE);
		myNameText.setText(controller.getPerson().getName());
		myNameText.addModifyListener(this);
		
		label = new Label(container, SWT.NONE);
		label.setText("Mail Box:");
		myMailText = new Text(container, SWT.SINGLE | SWT.READ_ONLY);
		myMailText.setText(controller.getPerson().getMbox());
		myMailText.addModifyListener(this);
		
		label = new Label(container, SWT.NONE);
		label.setText("Homepage:");
		myHomepageText = new Text(container, SWT.SINGLE);
		myHomepageText.addModifyListener(this);
		
		label = new Label(container, SWT.NONE);
		label.setText("Interest:");
		myInterestText = new Text(container, SWT.SINGLE);
		myInterestText.addModifyListener(this);
		
		label = new Label(container, SWT.NONE);
		label.setText("See Also:");
		mySeeAlsoText = new Text(container, SWT.SINGLE);
		mySeeAlsoText.addModifyListener(this);
		
		label = new Label(container, SWT.NONE);
		label.setText("Plan:");
		myPlanText = new Text(container, SWT.MULTI);
		myPlanText.addModifyListener(this);
		
		label = new Label(container, SWT.NONE);
		label.setText("Visible Mail Address:");
		
		showMboxButton = new Button(container, SWT.CHECK);
		showMboxButton.addSelectionListener(this);
		
		revertButton = new Button(container, SWT.PUSH);
		revertButton.setText("Revert");
		revertButton.addSelectionListener(this);
		revertButton.setEnabled(false);
		
		changeButton = new Button(container, SWT.PUSH);
		changeButton.setText("Change");
		changeButton.addSelectionListener(this);
		changeButton.setEnabled(false);
		
	    tabItem = new TabItem(tabFolder, SWT.NONE);
	    
	    tabItem.setText("Local Network");
	    
	    SashForm sash = new SashForm(tabFolder, SWT.HORIZONTAL);
	    
	    tabItem.setControl(sash);
	    
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
		
		container = new Composite(sash, SWT.NONE);
		layout = new GridLayout();
		layout.numColumns = 2;
		
		container.setLayout(layout);
		
		label = new Label(container, SWT.NONE);
		label.setText("Name:");
		nameText = new Text(container, SWT.SINGLE | SWT.READ_ONLY);
		//nameText.setText("Name");
		
		label = new Label(container, SWT.NONE);
		label.setText("Mail Hash:");
		mboxText = new Text(container, SWT.SINGLE | SWT.READ_ONLY);
		//mboxText.setText("0x39DF908G");
		
		label = new Label(container, SWT.NONE);
		label.setText("Homepage:");
		homepageText = new Text(container, SWT.SINGLE | SWT.READ_ONLY);
		//homepageText.setText("http://example.com/");
		
		label = new Label(container, SWT.NONE);
		label.setText("Interest:");
		interestText = new Text(container, SWT.SINGLE | SWT.READ_ONLY);
		//interestText.setText("http://example.com/foaffinger/");
		
		label = new Label(container, SWT.NONE);
		label.setText("See Also:");
		seeAlsoText = new Text(container, SWT.SINGLE | SWT.READ_ONLY);
		//seeAlsoText.setText("http://example.com/me.rdf");
		
		return tabFolder;
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
	        		
	        		/*Person person = message.getPerson();
	        		
	        		setField(nameText, person.getName());
	        		setField(mboxText, person.getMboxHash());
	        		setField(homepageText, person.getHomepage());
	        		setField(interestText, person.getInterest());
	        		setField(seeAlsoText, person.getSeeAlso());*/
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

	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.ModifyListener#modifyText(org.eclipse.swt.events.ModifyEvent)
	 */
	public void modifyText(ModifyEvent event) 
	{
		changeButton.setEnabled(true);
		revertButton.setEnabled(true);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	public void widgetSelected(SelectionEvent event)
	{
		Widget widget = event.widget;
		
		if (widget == showMboxButton)
		{
			modifyText(null);
		}
		else if (widget == changeButton)
		{
			Person me = controller.getPerson();
			me.setName(value(myNameText, me.getName()));
			me.setHomepage(value(myHomepageText, null));
			me.setInterest(value(myInterestText, null));
			//me.setMbox(value(myMailText));
			me.setSeeAlso(value(mySeeAlsoText, null));
			me.setPlan(value(myPlanText, null));
			me.setShowMbox(showMboxButton.getSelection());
			
			try
			{
				System.out.println("Kicking Service");
				controller.kickService();
			}
			catch (Exception e)
			{
				System.err.println("Error kicking service: " + e.getMessage());
			}
		}
		else
		{
			System.out.println("Table selection...");
			Person person = controller.getPerson(table.getSelectionIndex());
			
			setField(nameText, person.getName());
    			setField(mboxText, person.getMboxHash());
    			setField(homepageText, person.getHomepage());
    			setField(interestText, person.getInterest());
    			setField(seeAlsoText, person.getSeeAlso());
		}
	}
	
	private void setField(Text nameText, String name)
    {
        if (name == null)
            nameText.setText("");
        else
            nameText.setText(name);
    }
	
	public String value(Text text, String revert)
	{
		String val = text.getText().trim();
		if ("".equals(val))
		{
			if (revert == null)
				val = null;
			else
			{
				text.setText(revert);
				val = revert;
			}
		}
		
		return val;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	public void widgetDefaultSelected(SelectionEvent event)
	{
		// TODO Auto-generated method stub
		
	}
	
	
}

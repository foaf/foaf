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
import org.eclipse.swt.layout.GridData;
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
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.MessageBox;

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
    private Image logoImage;
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
    private Button moreInfoButton;

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
	    getShell().setText("FoafFinger");
	    
	    try
		{
		    presentImage = 
			new Image(Display.getCurrent(), "images/present.gif");
		    absentImage = 
			new Image(Display.getCurrent(), "images/absent.gif");
		    logoImage = 
			new Image(Display.getCurrent(), "images/ffinger.gif");
		    
		    getShell().setImage(logoImage);
		}
	    catch (SWTException e)
		{
		    System.err.println("Error: " + e);
		}

	    Composite composite = new Composite(parent, SWT.NONE);
	    
	    GridLayout layout = new GridLayout();
	    layout.numColumns = 2;
	    
	    composite.setLayout(layout);
	    
	    Font bigFont = new Font(Display.getCurrent(),
				    "helvetica",
				    20,
				    SWT.BOLD);

	    Label label = new Label(composite, SWT.NONE);
	    
	    label.setFont(bigFont);
	    label.setText("FoafFinger");
	    //bigFont.dispose();
	    
	    label = new Label(composite, SWT.NONE);
	    
	    label.setImage(logoImage);
	    
	    GridData layoutData =
		new GridData(GridData.HORIZONTAL_ALIGN_END);
	    
	    label.setLayoutData(layoutData);
	    
	    layoutData =
		new GridData(GridData.FILL_HORIZONTAL |
			     GridData.FILL_VERTICAL);
	    layoutData.horizontalSpan = 2;

	    TabFolder tabFolder = new TabFolder(composite, SWT.NONE);
	    
	    tabFolder.setLayoutData(layoutData);

	    TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
	    
	    tabItem.setText("My Details");
	    
	    Composite container = new Composite(tabFolder, SWT.NONE);
	    
	    tabItem.setControl(container);
	    
	    layout = new GridLayout();
		layout.numColumns = 2;
		
		layoutData =
		    new GridData(GridData.FILL_HORIZONTAL);
		layoutData.widthHint = 200;

		container.setLayout(layout);
	    
		label = new Label(container, SWT.NONE);
		label.setText("Name:");
		myNameText = new Text(container, SWT.SINGLE);
		myNameText.setText(controller.getPerson().getName());
		myNameText.addModifyListener(this);
		myNameText.setLayoutData(layoutData);

		label = new Label(container, SWT.NONE);
		label.setText("Mail Box:");
		myMailText = new Text(container, SWT.SINGLE | SWT.READ_ONLY);
		myMailText.setEnabled(false);
		myMailText.setText(controller.getPerson().getMbox());
		myMailText.addModifyListener(this);
		layoutData =
		    new GridData(GridData.FILL_HORIZONTAL);
		layoutData.widthHint = 200;
		myMailText.setLayoutData(layoutData);

		label = new Label(container, SWT.NONE);
		label.setText("Homepage:");
		myHomepageText = new Text(container, SWT.SINGLE);
		myHomepageText.addModifyListener(this);
		layoutData =
		    new GridData(GridData.FILL_HORIZONTAL);
		layoutData.widthHint = 200;
		myHomepageText.setLayoutData(layoutData);

		label = new Label(container, SWT.NONE);
		label.setText("Interest:");
		myInterestText = new Text(container, SWT.SINGLE);
		myInterestText.addModifyListener(this);
		layoutData =
		    new GridData(GridData.FILL_HORIZONTAL);
		layoutData.widthHint = 200;
		myInterestText.setLayoutData(layoutData);

		label = new Label(container, SWT.NONE);
		label.setText("See Also:");
		mySeeAlsoText = new Text(container, SWT.SINGLE);
		mySeeAlsoText.addModifyListener(this);
		layoutData =
		    new GridData(GridData.FILL_HORIZONTAL);
		layoutData.widthHint = 200;
		mySeeAlsoText.setLayoutData(layoutData);

		label = new Label(container, SWT.NONE);
		label.setText("Plan:");
		layoutData =
		    new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
		label.setLayoutData(layoutData);
		myPlanText = new Text(container, SWT.MULTI | SWT.WRAP |
				      SWT.H_SCROLL | SWT.V_SCROLL);
		myPlanText.addModifyListener(this);
		layoutData =
		    new GridData(GridData.FILL_HORIZONTAL | 
				 GridData.FILL_VERTICAL);
		layoutData.widthHint = 200;
		layoutData.heightHint = 150;
		myPlanText.setLayoutData(layoutData);

		//label = new Label(container, SWT.NONE);
		//label.setText("Visible Mail Address:");
		
		showMboxButton = new Button(container, SWT.CHECK);
		showMboxButton.addSelectionListener(this);
		showMboxButton.setText("Mail Address Visible");
		layoutData =
		    new GridData(GridData.FILL_HORIZONTAL);
		layoutData.horizontalSpan = 2;
		showMboxButton.setLayoutData(layoutData);

		/*revertButton = new Button(container, SWT.PUSH);
		revertButton.setText("Revert");
		revertButton.addSelectionListener(this);
		revertButton.setEnabled(false);*/
		
		changeButton = new Button(container, SWT.PUSH);
		changeButton.setText("Change");
		changeButton.addSelectionListener(this);
		changeButton.setEnabled(false);
		layoutData =
		    new GridData(GridData.HORIZONTAL_ALIGN_END);
		layoutData.horizontalSpan = 2;
		changeButton.setLayoutData(layoutData);
		changeButton.setFocus();
		
		tabItem = new TabItem(tabFolder, SWT.NONE);
		
		tabItem.setText("Local Network");
		
		container = new Composite(tabFolder, SWT.NONE);
		
		layout = new GridLayout();
		layout.numColumns = 2;

		container.setLayout(layout);
		
		tabItem.setControl(container);
	    
		table = new Table(container, SWT.CHECK | SWT.H_SCROLL | 
				  SWT.V_SCROLL);
		table.addSelectionListener(this);
		layoutData = new GridData(GridData.FILL_VERTICAL);
		layoutData.verticalSpan = 14;
		layoutData.widthHint = 200;
		table.setLayoutData(layoutData);
		
		label = new Label(container, SWT.NONE);
		label.setText("Name:");
		nameText = new Text(container, SWT.SINGLE | SWT.READ_ONLY);
		layoutData =
		    new GridData(GridData.FILL_HORIZONTAL);
		layoutData.widthHint = 200;
		nameText.setLayoutData(layoutData);
		
		label = new Label(container, SWT.NONE);
		label.setText("Mail Hash:");
		mboxText = new Text(container, SWT.SINGLE | SWT.READ_ONLY);
		layoutData =
		    new GridData(GridData.FILL_HORIZONTAL);
		layoutData.widthHint = 200;
		mboxText.setLayoutData(layoutData);
		
		label = new Label(container, SWT.NONE);
		label.setText("Homepage:");
		homepageText = new Text(container, SWT.SINGLE | SWT.READ_ONLY);
		layoutData =
		    new GridData(GridData.FILL_HORIZONTAL);
		layoutData.widthHint = 200;
		homepageText.setLayoutData(layoutData);
		
		label = new Label(container, SWT.NONE);
		label.setText("Interest:");
		interestText = new Text(container, SWT.SINGLE | SWT.READ_ONLY);
		layoutData =
		    new GridData(GridData.FILL_HORIZONTAL);
		layoutData.widthHint = 200;
		interestText.setLayoutData(layoutData);
		
		label = new Label(container, SWT.NONE);
		label.setText("See Also:");
		seeAlsoText = new Text(container, SWT.SINGLE | SWT.READ_ONLY);
		layoutData =
		    new GridData(GridData.FILL_HORIZONTAL);
		layoutData.widthHint = 200;
		seeAlsoText.setLayoutData(layoutData);

		moreInfoButton = new Button(container, SWT.PUSH);
		moreInfoButton.setText("More Information...");
		moreInfoButton.addSelectionListener(this);
		
		return composite;
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
		//revertButton.setEnabled(true);
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
				changeButton.setEnabled(false);
			}
			catch (Exception e)
			{
				System.err.println("Error kicking service: " + e.getMessage());
			}
		}
		else if (widget == moreInfoButton)
		    {
			InfoDialog dialog = new InfoDialog(getShell());
			
			dialog.setInfoText("This is some\ninformation");
			dialog.open();
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

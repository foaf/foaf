/* RDFToolbar */

import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.*;

public class RDFToolbar extends NSToolbar {

    RDFAuthorDocument rdfAuthorDocument;
    
    NSView editView;
    NSView showView;
    
    static String identifier = "rdf Toolbar";
    static String editToolsIdentifier = "edit tools identifier";
    static String showToolsIdentifier = "show tools identifier";
    static String checkIdentifier = "check model identifier";
    
    public RDFToolbar()
    {
        super(identifier);
        setAllowsUserCustomization(true);
	setAutosavesConfiguration(true);
	setDisplayMode(NSToolbar.NSToolbarDisplayModeIconOnly);
        //setDelegate(this);
    }
    
    public void awakeFromNib()
    {
        setDelegate(this);
    }
    
    public NSToolbarItem toolbarItemForItemIdentifier(NSToolbar toolbar, String itemIdent, boolean willBeInserted) 
    {
	// Required delegate method.  Given an item identifier, this method returns an item.
	// The toolbar will use this method to obtain toolbar items that can be displayed in the customization sheet, or in the toolbar itself.
	NSToolbarItem toolbarItem = new NSToolbarItem(itemIdent);
	
	if (itemIdent.equals(editToolsIdentifier))
        {
	    toolbarItem.setLabel("Editing Tools");
	    toolbarItem.setPaletteLabel("Editing Tooled");
            
            toolbarItem.setView(editView);
            toolbarItem.setMinSize(editView.frame().size());
            toolbarItem.setMaxSize(editView.frame().size());
	}
        else if(itemIdent.equals(showToolsIdentifier))
        {
	    toolbarItem.setLabel("Display Options");
	    toolbarItem.setPaletteLabel("Display Options");
	    
            System.out.println("Currently showView is "+showView);
            
	    toolbarItem.setView(showView);
            toolbarItem.setMinSize(showView.frame().size());
            toolbarItem.setMaxSize(showView.frame().size());
	} 
        else if (itemIdent.equals(checkIdentifier))
        {
	    toolbarItem.setLabel("Check Model");
	    toolbarItem.setPaletteLabel("Check Model");
	    
	    toolbarItem.setToolTip("Check this model for errors");
	    toolbarItem.setImage(NSImage.imageNamed("check"));
	    
	    toolbarItem.setTarget(this);
	    toolbarItem.setAction(new NSSelector("doCheck", new Class[] { NSToolbarItem.class }) );
        }
        else {
	    // itemIdent refered to a toolbar item that is not provide or supported by us or cocoa.
	    // Returning null will inform the toolbar this kind of item is not supported.
	    toolbarItem = null;
	}
	return toolbarItem;
    }
    
    public NSArray toolbarDefaultItemIdentifiers(NSToolbar toolbar) {
	// Required delegate method.  Returns the ordered list of items to be shown in the toolbar by default.   
	// If during the toolbar's initialization, no overriding values are found in the user defaults, or if the
	// user chooses to revert to the default items this set will be used.
	return new NSArray(new String[] 
            { 
                editToolsIdentifier,
                NSToolbarItem.NSToolbarSeparatorItemIdentifier, 		
                showToolsIdentifier,
                NSToolbarItem.NSToolbarSeparatorItemIdentifier,
                checkIdentifier } );
    }
    
    public NSArray toolbarAllowedItemIdentifiers(NSToolbar toolbar) {
	// Required delegate method.  Returns the list of all allowed items by identifier.  By default, the toolbar 
	// does not assume any items are allowed, even the separator.  So, every allowed item must be explicitly listed.  
	// The set of allowed items is used to construct the customization palette.
	return new NSArray(new String[] 
            {   
                NSToolbarItem.NSToolbarPrintItemIdentifier, 
                NSToolbarItem.NSToolbarCustomizeToolbarItemIdentifier,
                NSToolbarItem.NSToolbarFlexibleItemIdentifier, 
                NSToolbarItem.NSToolbarSpaceItemIdentifier, 
                NSToolbarItem.NSToolbarSeparatorItemIdentifier, 
                editToolsIdentifier, showToolsIdentifier, checkIdentifier} );
    }
  
    public void toolbarWillAddItem(NSNotification notif) {
        // Optional delegate method.  Before an new item is added to the toolbar, this notification is posted.  
	// This is the best place to notice a new item is going into the toolbar.  For instance, if you need to 
	// cache a reference to the toolbar item or need to set up some initial state, this is the best place 
	// to do it.   The notification object is the toolbar to which the item is being added.  The item being 
	// added is found by referencing the @"item" key in the userInfo.
	NSToolbarItem addedItem = (NSToolbarItem) notif.userInfo().objectForKey("item");
	
    }  
    
    public void toolbarDidRemoveItem(NSNotification notif) {
	// Optional delegate method.  After an item is removed from a toolbar the notification is sent.  This allows 
	// the chance to tear down information related to the item that may have been cached.  The notification object
	// is the toolbar to which the item is being added.  The item being added is found by referencing the @"item"
	// key in the userInfo.
	NSToolbarItem removedItem = (NSToolbarItem) notif.userInfo().objectForKey("item");

    }

    public boolean validateToolbarItem (NSToolbarItem toolbarItem) {
    	// Optional method.  This message is sent to us since we are the target of some toolbar item actions 
	// (for example:  of the save items action).
	boolean enable = true;
		
	return enable;
    }
    
    public void selectMoveMode(NSButton sender)
    {
        rdfAuthorDocument.addNodes(false);
        rdfAuthorDocument.addArcs(false);
    }
    
    public void selectAddNodeMode(NSButton sender)
    {
        rdfAuthorDocument.addNodes(true);
    }
    
    public void selectAddArcMode(NSButton sender)
    {
        rdfAuthorDocument.addArcs(true);
    }
    
    public void selectDeleteMode(NSButton sender)
    {
        System.out.println("Delete mode selected");
        rdfAuthorDocument.addNodes(false);
        rdfAuthorDocument.addArcs(false);
    }
    
    public void showTypes(NSButton sender)
    {
        rdfAuthorDocument.showTypes(sender.state() == NSCell.OnState);
    }
    
    public void showProperties(NSButton sender)
    {
        rdfAuthorDocument.showProperties(sender.state() == NSCell.OnState);
    }
    
    public void showIds(NSButton sender)
    {
        rdfAuthorDocument.showIds(sender.state() == NSCell.OnState);
    }
    
    public void doCheck(NSToolbarItem sender)
    {
        rdfAuthorDocument.doCheckModel();
    }
}

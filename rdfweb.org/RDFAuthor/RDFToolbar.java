/* RDFToolbar */

import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.*;

public class RDFToolbar extends NSObject {

    RDFAuthorDocument rdfAuthorDocument;
    
    static String identifier = "rdf Toolbar";
    static String pointerIdentifier = "pointer item";
    static String addNodeIdentifier = "add node item";
    static String addArcIdentifier = "add arc item";
    static String deleteIdentifier = "delete identifier";
    static String showTypesIdentifier = "show types identifier";
    static String showIdsIdentifier = "show ids identifier";
    static String showPropertiesIdentifier = "show properties identifier";
    static String checkIdentifier = "check model identifier";
    
    public RDFToolbar(RDFAuthorDocument rdfAuthorDocument)
    {
        this.rdfAuthorDocument = rdfAuthorDocument;
    }
    
    public NSToolbarItem toolbarItemForItemIdentifier(NSToolbar toolbar, String itemIdent, boolean willBeInserted)  {
	// Required delegate method.  Given an item identifier, this method returns an item.
	// The toolbar will use this method to obtain toolbar items that can be displayed in the customization sheet, or in the toolbar itself.
	NSToolbarItem toolbarItem = new NSToolbarItem(itemIdent);
	
	if (itemIdent.equals(pointerIdentifier)) {
	    toolbarItem.setLabel("Select and Move");
	    toolbarItem.setPaletteLabel("Select and Move");
	    
	    toolbarItem.setToolTip("Select and move items");
	    toolbarItem.setImage(NSImage.imageNamed("Arrow"));
	    
	    toolbarItem.setTarget(this);
	    toolbarItem.setAction(new NSSelector("pointerSelect", new Class[] { NSToolbarItem.class }) );
	} else if(itemIdent.equals(addNodeIdentifier)) {
	    toolbarItem.setLabel("Add Node");
	    toolbarItem.setPaletteLabel("Add Node");
	    
	    toolbarItem.setToolTip("Add nodes to the model");
	    toolbarItem.setImage(NSImage.imageNamed("addNode"));
	    
	    toolbarItem.setTarget(this);
	    toolbarItem.setAction(new NSSelector("addNodeSelect", new Class[] { NSToolbarItem.class }) );
	} 
        else if (itemIdent.equals(addArcIdentifier))
        {
	    toolbarItem.setLabel("Add Connection");
	    toolbarItem.setPaletteLabel("Add Connection");
	    
	    toolbarItem.setToolTip("Connect nodes together");
	    toolbarItem.setImage(NSImage.imageNamed("addArc"));
	    
	    toolbarItem.setTarget(this);
	    toolbarItem.setAction(new NSSelector("addArcSelect", new Class[] { NSToolbarItem.class }) );
        }
        else if (itemIdent.equals(showTypesIdentifier))
        {
	    toolbarItem.setLabel("Show Types");
	    toolbarItem.setPaletteLabel("Show Types");
	    
	    toolbarItem.setToolTip("Show node types");
	    toolbarItem.setImage(NSImage.imageNamed("showTypes"));
	    
	    toolbarItem.setTarget(this);
	    toolbarItem.setAction(new NSSelector("showTypes", new Class[] { NSToolbarItem.class }) );
        }
        else if (itemIdent.equals(showIdsIdentifier))
        {
	    toolbarItem.setLabel("Show Names");
	    toolbarItem.setPaletteLabel("Show Names");
	    
	    toolbarItem.setToolTip("Show node names");
	    toolbarItem.setImage(NSImage.imageNamed("showIds"));
	    
	    toolbarItem.setTarget(this);
	    toolbarItem.setAction(new NSSelector("showIds", new Class[] { NSToolbarItem.class }) );
        }
        else if (itemIdent.equals(showPropertiesIdentifier))
        {
	    toolbarItem.setLabel("Show Properties");
	    toolbarItem.setPaletteLabel("Show Properties");
	    
	    toolbarItem.setToolTip("Show properties");
	    toolbarItem.setImage(NSImage.imageNamed("showProperties"));
	    
	    toolbarItem.setTarget(this);
	    toolbarItem.setAction(new NSSelector("showProperties", new Class[] { NSToolbarItem.class }) );
        }
        else if (itemIdent.equals(deleteIdentifier))
        {
	    toolbarItem.setLabel("Delete Object");
	    toolbarItem.setPaletteLabel("Delete Object");
	    
	    toolbarItem.setToolTip("Delete the currently selected object");
	    toolbarItem.setImage(NSImage.imageNamed("delete"));
	    
	    toolbarItem.setTarget(this);
	    toolbarItem.setAction(new NSSelector("deleteCurrent", new Class[] { NSToolbarItem.class }) );
        }
        else if (itemIdent.equals(checkIdentifier))
        {
	    toolbarItem.setLabel("Check Model");
	    toolbarItem.setPaletteLabel("Check Model");
	    
	    toolbarItem.setToolTip("Check this model for errors");
	    toolbarItem.setImage(NSImage.imageNamed("delete"));
	    
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
                pointerIdentifier, addNodeIdentifier, addArcIdentifier, deleteIdentifier,
                NSToolbarItem.NSToolbarSeparatorItemIdentifier, 		
                showTypesIdentifier, showIdsIdentifier, showPropertiesIdentifier,
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
                pointerIdentifier, addNodeIdentifier, addArcIdentifier, showTypesIdentifier, 
                showIdsIdentifier, showPropertiesIdentifier, deleteIdentifier, checkIdentifier} );
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
    
    public void pointerSelect(NSToolbarItem sender)
    {
        rdfAuthorDocument.addNodes(false);
        rdfAuthorDocument.addArcs(false);
    }
    
    public void addNodeSelect(NSToolbarItem sender)
    {
        rdfAuthorDocument.addNodes(true);
    }
    
    public void addArcSelect(NSToolbarItem sender)
    {
        rdfAuthorDocument.addArcs(true);
    }
    
    public void showTypes(NSToolbarItem sender)
    {
        rdfAuthorDocument.showTypes();
    }
    
    public void showProperties(NSToolbarItem sender)
    {
        rdfAuthorDocument.showProperties();
    }
    
    public void showIds(NSToolbarItem sender)
    {
        rdfAuthorDocument.showIds();
    }
    
    public void deleteCurrent(NSToolbarItem sender)
    {
        rdfAuthorDocument.deleteCurrentItem();
    }
    
    public void doCheck(NSToolbarItem sender)
    {
        rdfAuthorDocument.doCheckModel();
    }
}

/* RDFToolbar */

import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.*;

public class RDFToolbar extends NSObject {

    RDFAuthorDocument rdfAuthorDocument;
    
    NSMatrix editView;
    NSView showView;
    
    NSButton showTypesButton;
    NSButton showIdsButton;
    NSButton showPropertiesButton;
    NSPopUpButton previewModePopup;
    
    QueryController queryController;
    BookmarkController bookmarkController;
    RDFModelView rdfModelView;
    
    static final String identifier = "rdf Toolbar";
    static final String editToolsIdentifier = "edit tools identifier";
    static final String showToolsIdentifier = "show tools identifier";
    static final String checkIdentifier = "check model identifier";
    static final String toggleViewsIdentifier = "toggle view identifier";
    static final String previewPopupIdentifier = "preview popup identifier";
    static final String queryPanelIdentifier = "query panel identifier";
    static final String bookmarkPanelIdentifier = "bookmark panel identifier";
    
    boolean textPreview = false;
    String[] popupMappings;
    
    public RDFToolbar()
    {
        popupMappings = new String[] {"RDF/XML-ABBREV", "N-TRIPLE", "N3" };
    }
    
    public NSToolbarItem toolbarItemForItemIdentifier(NSToolbar toolbar, String itemIdent, boolean willBeInserted) 
    {
	// Required delegate method.  Given an item identifier, this method returns an item.
	// The toolbar will use this method to obtain toolbar items that can be displayed in the customization sheet, or in the toolbar itself.
	NSToolbarItem toolbarItem = new NSToolbarItem(itemIdent);
	
	if (itemIdent.equals(editToolsIdentifier))
        {
	    toolbarItem.setLabel("Editing Tools");
	    toolbarItem.setPaletteLabel("Editing Tools");
            toolbarItem.setView(editView);
            toolbarItem.setMinSize(editView.frame().size());
            toolbarItem.setMaxSize(editView.frame().size());
	}
        else if(itemIdent.equals(showToolsIdentifier))
        {
	    toolbarItem.setLabel("Display Options");
	    toolbarItem.setPaletteLabel("Display Options");
            
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
        else if (itemIdent.equals(toggleViewsIdentifier))
        {
            toolbarItem.setLabel("Toggle Model/Text View");
	    toolbarItem.setPaletteLabel("Toggle Model/Text View");
	    
	    toolbarItem.setToolTip("Toggles between the model view and a preview of the text export");
	    toolbarItem.setImage(NSImage.imageNamed("modelView"));
	    
	    toolbarItem.setTarget(this);
	    toolbarItem.setAction(new NSSelector("toggleView", new Class[] { NSToolbarItem.class }) );
        }
        else if (itemIdent.equals(previewPopupIdentifier))
        {
            toolbarItem.setLabel("Preview Mode");
	    toolbarItem.setPaletteLabel("Preview Mode");
            
	    toolbarItem.setView(previewModePopup);
            toolbarItem.setMinSize(previewModePopup.frame().size());
            toolbarItem.setMaxSize(previewModePopup.frame().size());
        }
        else if (itemIdent.equals(queryPanelIdentifier))
        {
            toolbarItem.setLabel("Query Panel");
            toolbarItem.setPaletteLabel("Query Panel");
            
            toolbarItem.setToolTip("Show or hide the query window");
            toolbarItem.setImage(NSImage.imageNamed("queryPanel.tiff"));
            toolbarItem.setTarget(this);
            toolbarItem.setAction(new NSSelector("showQueryPanel", new Class[] { NSToolbarItem.class }));
        }
        else if (itemIdent.equals(bookmarkPanelIdentifier))
        {
            toolbarItem.setLabel("Bookmarks");
            toolbarItem.setPaletteLabel("Bookmarks");
            
            toolbarItem.setToolTip("Show or hide the bookmark window");
            toolbarItem.setImage(NSImage.imageNamed("bookmarkWindow.tiff"));
            toolbarItem.setTarget(this);
            toolbarItem.setAction(new NSSelector("showBookmarkWindow", new Class[] { NSToolbarItem.class }));
        }
        else
        {
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
                checkIdentifier, toggleViewsIdentifier, previewPopupIdentifier, queryPanelIdentifier,
                bookmarkPanelIdentifier } );
    }
    
    public NSArray toolbarAllowedItemIdentifiers(NSToolbar toolbar) {
	// Required delegate method.  Returns the list of all allowed items by identifier.  By default, the toolbar 
	// does not assume any items are allowed, even the separator.  So, every allowed item must be explicitly listed.  
	// The set of allowed items is used to construct the customization palette.
	return new NSArray(new String[] 
            {   
                editToolsIdentifier, showToolsIdentifier, checkIdentifier, toggleViewsIdentifier,
                previewPopupIdentifier, queryPanelIdentifier, bookmarkPanelIdentifier,
                NSToolbarItem.NSToolbarPrintItemIdentifier, 
                NSToolbarItem.NSToolbarCustomizeToolbarItemIdentifier,
                NSToolbarItem.NSToolbarFlexibleItemIdentifier, 
                NSToolbarItem.NSToolbarSpaceItemIdentifier, 
                NSToolbarItem.NSToolbarSeparatorItemIdentifier, 
            } );
    }
  
    public void toolbarWillAddItem(NSNotification notif) {
        // Optional delegate method.  Before an new item is added to the toolbar, this notification is posted.  
	// This is the best place to notice a new item is going into the toolbar.  For instance, if you need to 
	// cache a reference to the toolbar item or need to set up some initial state, this is the best place 
	// to do it.   The notification object is the toolbar to which the item is being added.  The item being 
	// added is found by referencing the @"item" key in the userInfo.
	NSToolbarItem addedItem = (NSToolbarItem) notif.userInfo().objectForKey("item");
	
        if (addedItem.itemIdentifier().equals(showToolsIdentifier))
        {
            // Sync buttons with state of the document (edit state isn't saved btw)
            
            int typesState = (rdfAuthorDocument.showTypes) ? NSCell.OnState : NSCell.OffState;
            int idsState = (rdfAuthorDocument.showIds) ? NSCell.OnState : NSCell.OffState;
            int propertiesState = (rdfAuthorDocument.showProperties) ? NSCell.OnState : NSCell.OffState;
            
            showTypesButton.setState(typesState);
            showIdsButton.setState(idsState);
            showPropertiesButton.setState(propertiesState);
        }
    } 
    
    /*public void toolbarDidRemoveItem(NSNotification notif) {
	// Optional delegate method.  After an item is removed from a toolbar the notification is sent.  This allows 
	// the chance to tear down information related to the item that may have been cached.  The notification object
	// is the toolbar to which the item is being added.  The item being added is found by referencing the @"item"
	// key in the userInfo.
	NSToolbarItem removedItem = (NSToolbarItem) notif.userInfo().objectForKey("item");

    }*/

    /*public boolean validateToolbarItem (NSToolbarItem toolbarItem) {
    	// Optional method.  This message is sent to us since we are the target of some toolbar item actions 
	// (for example:  of the save items action).
	boolean enable = true;
		
	return enable;
    }*/
    
    public void selectMoveMode(Object sender)
    {
        rdfModelView.setEditingMode( RDFModelView.MoveSelectMode );
    }
    
    public void selectAddNodeMode(Object sender)
    {
        rdfModelView.setEditingMode( RDFModelView.AddNodeMode );
    }
    
    public void selectAddArcMode(Object sender)
    {
        rdfModelView.setEditingMode( RDFModelView.AddConnectionMode );
    }
    
    public void selectDeleteMode(Object sender)
    {
        rdfModelView.setEditingMode( RDFModelView.DeleteItemsMode );
    }
    
    public void selectMarkQueryMode(Object sender)
    {
        rdfModelView.setEditingMode( RDFModelView.AddQueryItemMode );
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
    
    public void toggleView(NSToolbarItem sender)
    {
        if (textPreview)
        {
            boolean successful = rdfAuthorDocument.showTextPreview(false, null);  // this will never fail
            textPreview = false;
            sender.setImage(NSImage.imageNamed("modelView"));
        }
        else
        {
            String jenaType = popupMappings[previewModePopup.indexOfSelectedItem()];
            System.out.println("jena Type: " + jenaType);
            boolean successful = rdfAuthorDocument.showTextPreview(true, jenaType);
            if (!successful)
            {
                return;
            }
            textPreview = true;
            sender.setImage(NSImage.imageNamed("textPreview"));
        }
    }
    
    public void showQueryPanel(NSToolbarItem sender)
    {
        queryController.toggleShow();
    }
    
    public void showBookmarkWindow(NSToolbarItem sender)
    {
        bookmarkController.toggleShow();
    }
    
    public void previewModeChanged(Object sender)
    {
        if (textPreview)
        {
            String jenaType = popupMappings[previewModePopup.indexOfSelectedItem()];
            rdfAuthorDocument.createPreviewText(jenaType);
        }
    }
}

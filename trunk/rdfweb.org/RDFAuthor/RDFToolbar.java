/* RDFToolbar */

/* $Id: RDFToolbar.java,v 1.18 2002-02-07 16:09:56 pldms Exp $ */

/*
    Copyright 2001 Damian Steer <dm_steer@hotmail.com>

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

import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.*;

public class RDFToolbar extends NSObject {

    RDFAuthorDocument rdfAuthorDocument;
    
    RDFModelView rdfModelView;
    
    NSMatrix editView;
    NSView showView;
    
    NSButton showTypesButton;
    NSButton showIdsButton;
    NSButton showPropertiesButton;
    NSPopUpButton previewModePopup;
    
    NSToolbarItem toggleViewItem;
    
    static final String identifier = "rdf Toolbar";
    static final String editToolsIdentifier = "edit tools identifier";
    static final String showToolsIdentifier = "show tools identifier";
    static final String checkIdentifier = "check model identifier";
    static final String toggleViewsIdentifier = "toggle view identifier";
    static final String previewPopupIdentifier = "preview popup identifier";
    static final String queryPanelIdentifier = "query panel identifier";
    static final String bookmarkPanelIdentifier = "bookmark panel identifier";
    static final String autoLayoutIdentifier = "autolayout identifier";
    
    boolean textPreview = false;
    String[] popupMappings;
    
    public RDFToolbar()
    {
        popupMappings = new String[] {"RDF/XML", "N-TRIPLE"}; //, "N3" };
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
	    
	    toolbarItem.setTarget(rdfAuthorDocument);
	    toolbarItem.setAction(new NSSelector(("doCheckModel"), new Class[] { Object.class }));
        }
        else if (itemIdent.equals(toggleViewsIdentifier))
        {
            toolbarItem.setLabel("Toggle Model/Text View");
	    toolbarItem.setPaletteLabel("Toggle Model/Text View");
	    
	    toolbarItem.setToolTip("Toggles between the model view and a preview of the text export");
	    toolbarItem.setImage(NSImage.imageNamed("modelView"));
	    
	    toolbarItem.setTarget(rdfAuthorDocument);
	    toolbarItem.setAction(new NSSelector("showTextPreview", new Class[] { Object.class }) );
            
            toggleViewItem = toolbarItem; // Remember this so we can alter it
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
            toolbarItem.setTarget(rdfAuthorDocument);
            toolbarItem.setAction(new NSSelector("showQueryPanel", new Class[] { Object.class }));
        }
        else if (itemIdent.equals(bookmarkPanelIdentifier))
        {
            toolbarItem.setLabel("Bookmarks");
            toolbarItem.setPaletteLabel("Bookmarks");
            
            toolbarItem.setToolTip("Show or hide the bookmark window");
            toolbarItem.setImage(NSImage.imageNamed("bookmarkWindow.tiff"));
            toolbarItem.setTarget(rdfAuthorDocument);
            toolbarItem.setAction(new NSSelector("showBookmarkWindow", new Class[] { Object.class }));
        }
        else if (itemIdent.equals(autoLayoutIdentifier))
        {
            toolbarItem.setLabel("Layout");
            toolbarItem.setPaletteLabel("Layout");
            
            toolbarItem.setToolTip("Automatically layout the graph");
            toolbarItem.setImage(NSImage.imageNamed("layout.tiff"));
            toolbarItem.setTarget(rdfAuthorDocument);
            toolbarItem.setAction(new NSSelector("autoLayout", new Class[] { Object.class }));
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
                checkIdentifier, autoLayoutIdentifier, toggleViewsIdentifier, previewPopupIdentifier, 
                queryPanelIdentifier,
                bookmarkPanelIdentifier } );
    }
    
    public NSArray toolbarAllowedItemIdentifiers(NSToolbar toolbar) {
	// Required delegate method.  Returns the list of all allowed items by identifier.  By default, the toolbar 
	// does not assume any items are allowed, even the separator.  So, every allowed item must be explicitly listed.  
	// The set of allowed items is used to construct the customization palette.
	return new NSArray(new String[] 
            {   
                editToolsIdentifier, showToolsIdentifier, checkIdentifier, autoLayoutIdentifier,
                toggleViewsIdentifier,
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
            
            syncButtonState();
        }
    } 
    
    // This is used to sync the (stateful) buttons in the toolbar with
    // the relevent states (which might have been set by using other means)
    
    public void syncButtonState()
    {
        int typesState = (rdfAuthorDocument.showTypes) ? NSCell.OnState : 
                                                            NSCell.OffState;
        int idsState = (rdfAuthorDocument.showIds) ? NSCell.OnState : 
                                                            NSCell.OffState;
        int propertiesState = (rdfAuthorDocument.showProperties) ? NSCell.OnState : 
                                                            NSCell.OffState;
        showTypesButton.setState(typesState);
        showIdsButton.setState(idsState);
        showPropertiesButton.setState(propertiesState);
        
        switch (rdfModelView.editingMode())
        {
            case RDFModelView.MoveSelectMode:		editView.selectCellAtLocation(0,0); break;
            case RDFModelView.AddNodeMode:		editView.selectCellAtLocation(0,1); break;
            case RDFModelView.AddConnectionMode:	editView.selectCellAtLocation(0,2); break;
            case RDFModelView.DeleteItemsMode:		editView.selectCellAtLocation(0,3); break;
            case RDFModelView.AddQueryItemMode:		editView.selectCellAtLocation(0,4); break;
        }
    }
    
    public void setPreviewType(int item)
    {
        previewModePopup.selectItemAtIndex(item);
    }
    
    public String previewType()
    {
        return popupMappings[previewModePopup.indexOfSelectedItem()];
    }
    
    public void syncPreview(boolean showingPreview)
    {
        if (showingPreview)
        {
            toggleViewItem.setImage(NSImage.imageNamed("textPreview"));
        }
        else
        {
            toggleViewItem.setImage(NSImage.imageNamed("modelView"));
        }
    }
}

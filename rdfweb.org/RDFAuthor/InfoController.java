/* InfoController */

/* $Id: InfoController.java,v 1.20 2002-02-05 16:02:57 pldms Exp $ */

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

import java.net.URL;
import java.net.MalformedURLException;

import com.hp.hpl.mesa.rdf.jena.common.*;

public class InfoController extends NSObject {
    
    NSTabView infoTabs;
    
    NSTabViewItem objectTabItem;
    
    NSPanel infoWindow;
    
    NSView nothingView;
    
    NSView multipleView;
    
    NSView literalView;
    
    NSView resourceView;
    
    NSView propertyView;

    NSButton literalChangeButton;
    
    NSButton resourceChangeButton;

    NSTextView literalTextField;

    NSTextField propertyTextField;

    NSTextField resourceIdField;

    NSTextField resourceTypeField;
    
    NSTextField documentWidthField;
    
    NSTextField documentHeightField;
    
    static final String itemChangedNotification = "org.rdfweb.RDFAuthor.itemChanged";
    
    static final String showInfoNotification = "org.rdfweb.RDFAuthor.showInfo";
    
    ModelItem currentItem = null;
    
    NSWindow currentWindow = null;

    public InfoController()
    {
        // Initialise notification stuff
        
        NSSelector itemSelector = new NSSelector("currentItemChanged", 
                new Class[] {NSNotification.class} );
        
        NSSelector windowSelector = new NSSelector("currentWindowChanged", 
                new Class[] {NSNotification.class} );
        
        NSSelector infoSelector = new NSSelector("showInfo", 
                new Class[] {NSNotification.class} );
        
        NSSelector windowClosedSelector = new NSSelector("windowClosed",
                new Class[] {NSNotification.class} );
        
        NSNotificationCenter.defaultCenter().addObserver(
            this, itemSelector, itemChangedNotification, null);
        
        NSNotificationCenter.defaultCenter().addObserver(
            this, infoSelector, showInfoNotification, null);
        
        NSNotificationCenter.defaultCenter().addObserver(
            this, windowSelector, NSWindow.WindowDidBecomeMainNotification, null);
        
        NSNotificationCenter.defaultCenter().addObserver(
            this, windowClosedSelector, NSWindow.WindowWillCloseNotification, null);
    }
    
    public void awakeFromNib()
    {
        // This is useful, but I couldn't get it in IB
        objectTabItem = infoTabs.tabViewItemAtIndex(0);
        
        // Initialise sizes
        NSRect rect = infoTabs.contentRect();
        nothingView.setFrame(rect);
        literalView.setFrame(rect);
        resourceView.setFrame(rect);
        propertyView.setFrame(rect);
        multipleView.setFrame(rect);
        
        // Set object tab to 'nothingView' initially
        
        objectTabItem.setView(nothingView);
        
    }
    
    private void currentWindowChanged(NSNotification notification)
    {
        NSWindow window = (NSWindow) notification.object();
        if (window != currentWindow)
        {
            currentWindow = window;
            RDFAuthorDocument currentDocument = (RDFAuthorDocument) currentWindow.delegate(); // gives the document
            ArcNodeSelection selection = currentDocument.selection();
            setInfoFromSelection(selection);
        }
    }
    
    private void windowClosed(NSNotification notification)
    {
        NSWindow window = (NSWindow) notification.object();
        if (currentWindow == window)
        {
            currentWindow = null;
            setInfoFromSelection(null);
        }
    }
    
    private void currentItemChanged(NSNotification notification)
    {
        RDFAuthorDocument document = (RDFAuthorDocument) notification.object();
        
        if (currentWindow == null)
        {
            return;
        }
        
        if (document == currentWindow.delegate())
        {
            ArcNodeSelection selection = document.selection();
            setInfoFromSelection(selection); // cheap way to update
        }
    }
    
    private void setInfoFromSelection(ArcNodeSelection selection)
    {
        if (selection == null) // no document is current
        {
            showNothing();
            documentWidthField.setStringValue("");
            documentHeightField.setStringValue("");
            documentWidthField.setEnabled(false);
            documentHeightField.setEnabled(false);
            return;
        }
        
        documentWidthField.setEnabled(true);
        documentHeightField.setEnabled(true);
        
        NSSize size = ((RDFAuthorDocument) currentWindow.delegate()).rdfModelView.frame().size();
        documentWidthField.setStringValue("");
        documentHeightField.setStringValue("");
        documentWidthField.setFloatValue(size.width());
        documentHeightField.setFloatValue(size.height());
        
        if (selection.kind() == ArcNodeSelection.Empty)
        {
            showNothing();
        }
        else if (selection.kind() == ArcNodeSelection.Multiple)
        {
            showMultiple();
        }
        else // must be single
        {
            setCurrentItem( selection.selectedObject() );
        }
    }
    
    private void setCurrentItem(ModelItem item)
    {
        currentItem = item;
        
        if (!currentItem.isNode())
        {
            showArc((Arc) currentItem);
        }
        else if (((Node) currentItem).isLiteral())
        {
            showLiteral((Node) currentItem);
        }
        else
        {
            showNode((Node) currentItem);
        }
    }
    
    private void revertObject(Object sender)
    {
        if (currentWindow == null) return;

        setInfoFromSelection(((RDFAuthorDocument) currentWindow.delegate()).selection()); // reinitialises the fields
    }
    
    private void changeObject(Object sender)
    {
        if (currentWindow == null) // nothing to change
        {
            return;
        }

        changeSize(); // see if size needs changing

        if (currentItem == null) // no object, so finish
        {
            return;
        }
        else if (!currentItem.isNode())
        {
            changeProperty();
        }
        else if (((Node) currentItem).isLiteral())
        {
            changeLiteral();
        }
        else
        {
            changeResource();
        }
        setCurrentItem(currentItem); // reinitialises the fields
    }
    
    private void changeLiteral() 
    {
        String value = literalTextField.string().trim();
        
        if (literalChangeButton.state() == NSCell.OnState)
        {
            ((Node) currentItem).setIsLiteral(false);
            return;
        }
        
        value = (value.equals(""))?null:value;
        ((Node) currentItem).setId(value);
    }

    private void changeProperty() 
    {
        String property = propertyTextField.stringValue().trim();
        
        if (property.equals("")) // I allow this, although it isn't right
        {
            ((Arc) currentItem).setProperty(null);
        }
        else if (RDFAuthorUtilities.isValidURI(property))
        {
            ((Arc) currentItem).setProperty(property);
        }
        else // error in input
        {
            uriError(property);
        }
    }

    private void changeResource()
    {
        String id = resourceIdField.stringValue().trim();
        String type = resourceTypeField.stringValue().trim();
        
        if (resourceChangeButton.state() == NSCell.OnState)
        {
            ((Node) currentItem).setIsLiteral(true);
            return; // Don't need subsequent checks in this case
        }
        
        if (id.equals(""))
        {
            ((Node) currentItem).setId(null);
        }
        else if (RDFAuthorUtilities.isValidURI(id))
        {
                ((Node) currentItem).setId(id);
        }
        else
        {
            uriError(id);
        }
        
        if (type.equals(""))
        {
            ((Node) currentItem).setType(null);
        }
        else if (RDFAuthorUtilities.isValidURI(type))
        {
            ((Node) currentItem).setType(type);
        }
        else
        {
            uriError(type);
        }
    }
    
    private void changeSize()
    {
        float width = documentWidthField.floatValue();
        float height = documentHeightField.floatValue();
        
        NSSize size = ((RDFAuthorDocument) currentWindow.delegate()).rdfModelView.frame().size();
        
        if ((size.width() == width) && (size.height() == height)) return; // no change
        
        if ((width > 40) && (height > 40))
        {
            ((RDFAuthorDocument) currentWindow.delegate()).setDocumentSize(new NSSize(width, height));
        }
        else
        {
            RDFAuthorUtilities.ShowError("Document Too Small", 
                "Sorry, that's too small. Try again.",
                RDFAuthorUtilities.Normal, (NSWindow) infoWindow);
            // Revert to previous value
            setCurrentItem(null);
        }
    }
    
    private void uriError(String uri)
    {
        RDFAuthorUtilities.ShowError(
                "Not a valid URI: ",
                "\"" + uri + "\" is not a valid URI. " +
                "This field requires either a valid URI or an empty value.",
                RDFAuthorUtilities.Normal, (NSWindow) infoWindow);
        setCurrentItem(currentItem); // sneaky way to revert to previous
    }
    
    private void showInfoWindow(Object sender) 
    {
        if (infoWindow.isVisible())
        {
            infoWindow.orderOut(this);
        }
        else
        {
            infoWindow.orderFront(this);
        }
    }
    
    private void showInfo(NSNotification notification)
    {
        infoWindow.orderFront(this);
    }
    
    private void showNothing()
    {
        objectTabItem.setView(nothingView);
        setFields("", "", "", "");
    }
    
    private void showMultiple()
    {
        objectTabItem.setView(multipleView);
        setFields("", "", "", "");
    }
    
    private void showArc(Arc arc)
    {
        
        String propertyNS = (arc.propertyNamespace() == null)?"":arc.propertyNamespace();
        String propertyN = (arc.propertyName() == null)?"":arc.propertyName();
        
        objectTabItem.setView(propertyView);
        setFields("", "", "", propertyNS + propertyN);
    }
    
    private void showLiteral(Node literal)
    {
        objectTabItem.setView(literalView);
        
        String literalVal = (literal.id() == null)?"":literal.id();
        
        setFields(literalVal, "", "", "");
    }
    
    private void showNode(Node node)
    {
        objectTabItem.setView(resourceView);
        
        String typeNS = (node.typeNamespace() == null)?"":node.typeNamespace();
        String typeN = (node.typeName() == null)?"":node.typeName();
        
        String id = (node.id() == null)?"":node.id();
        
        setFields("", typeNS + typeN, id, "");
    }
  
    private void setFields( String literal, String type, String id, String property)
    {
        literalChangeButton.setState(NSCell.OffState);
        resourceChangeButton.setState(NSCell.OffState);
        
        literalTextField.setString(literal);
        propertyTextField.setStringValue(property);
        resourceIdField.setStringValue(id);
        resourceTypeField.setStringValue(type);
    }
}

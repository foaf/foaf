/* InfoController */

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

    NSTextField literalTextField;

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
    
    public void currentWindowChanged(NSNotification notification)
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
    
    public void windowClosed(NSNotification notification)
    {
        NSWindow window = (NSWindow) notification.object();
        if (currentWindow == window)
        {
            currentWindow = null;
            setInfoFromSelection(null);
        }
    }
    
    public void currentItemChanged(NSNotification notification)
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
    
    public void setInfoFromSelection(ArcNodeSelection selection)
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
    
    public void setCurrentItem(ModelItem item)
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
    
    public void revertObject(Object sender)
    {
        if (currentWindow == null) return;

        setInfoFromSelection(((RDFAuthorDocument) currentWindow.delegate()).selection()); // reinitialises the fields
    }
    
    public void changeObject(Object sender)
    {
        if (currentWindow == null)
        {
            return;
        }

        changeSize();

        if (!currentItem.isNode())
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
    
    public void changeLiteral() 
    {
        String value = literalTextField.stringValue().trim();
        
        if (literalChangeButton.state() == NSCell.OnState)
        {
            ((Node) currentItem).setIsLiteral(false);
            return; // Don't need subsequent checks in this case
        }
        
        value = (value.equals(""))?null:value;
        ((Node) currentItem).setId(value);
    }

    public void changeProperty() 
    {
        String property = propertyTextField.stringValue().trim();
        
        if (property.equals(""))
        {
            ((Arc) currentItem).setProperty(null, null);
        }
        else
        {
            if (checkUrl(property))
            {
                int sep = Util.splitNamespace(property);
                
                String namespace = property.substring(0, sep);
                String name = property.substring(sep);
                ((Arc) currentItem).setProperty(namespace, name);
            }
        }
    }

    public void changeResource()
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
        else
        {
            //if (checkUrl(id)) // Removed check on this
            //{
                ((Node) currentItem).setId(id);
            //}
        }
        
        if (type.equals(""))
        {
            ((Node) currentItem).setType(null, null);
        }
        else
        {
            if (checkUrl(type))
            {
                int sep = Util.splitNamespace(type);
                String namespace = type.substring(0, sep);
                String name = type.substring(sep);
                ((Node) currentItem).setType(namespace, name);
            }
        }
    }
    
    public void changeSize()
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
            RDFAuthorUtilities.ShowError("Document Too Small", "Sorry, that's too small. Try again.",
                RDFAuthorUtilities.Normal, (NSWindow) infoWindow);
            // Revert to previous value
            setCurrentItem(null);
        }
    }
    
    public boolean checkUrl(String url)
    {
        try
        {
            URL temp = new URL(url);
            return true;
        }
        catch (MalformedURLException error)
        {
            RDFAuthorUtilities.ShowError(
                "Not a URL: ",
                error + "\nThis field requires either a URL or an empty value.",
                RDFAuthorUtilities.Normal, (NSWindow) infoWindow);
            setCurrentItem(currentItem); // sneaky way to revert to previous
            return false;
        }
    }
    
    public void showInfoWindow(Object sender) 
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
    
    public void showInfo(NSNotification notification)
    {
        infoWindow.orderFront(this);
    }
    
    
    
    public void showNothing()
    {
        objectTabItem.setView(nothingView);
        setFields("", "", "", "");
    }
    
    public void showMultiple()
    {
        objectTabItem.setView(multipleView);
        setFields("", "", "", "");
    }
    
    public void showArc(Arc arc)
    {
        
        String propertyNS = (arc.propertyNamespace() == null)?"":arc.propertyNamespace();
        String propertyN = (arc.propertyName() == null)?"":arc.propertyName();
        
        objectTabItem.setView(propertyView);
        setFields("", "", "", propertyNS + propertyN);
    }
    
    public void showLiteral(Node literal)
    {
        objectTabItem.setView(literalView);
        
        String literalVal = (literal.id() == null)?"":literal.id();
        
        setFields(literalVal, "", "", "");
    }
    
    public void showNode(Node node)
    {
        objectTabItem.setView(resourceView);
        
        String typeNS = (node.typeNamespace() == null)?"":node.typeNamespace();
        String typeN = (node.typeName() == null)?"":node.typeName();
        
        String id = (node.id() == null)?"":node.id();
        
        setFields("", typeNS + typeN, id, "");
    }
  
    public void setFields( String literal, String type, String id, String property)
    {
        literalChangeButton.setState(NSCell.OffState);
        resourceChangeButton.setState(NSCell.OffState);
        
        literalTextField.setStringValue(literal);
        propertyTextField.setStringValue(property);
        resourceIdField.setStringValue(id);
        resourceTypeField.setStringValue(type);
    }
}

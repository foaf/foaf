/* InfoController */

import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.*;

import java.net.URL;
import java.net.MalformedURLException;

public class InfoController extends NSObject {

    NSPanel infoWindow;
    
    NSView currentView;
    
    NSView contentView;
    
    NSView nothingView;
    
    NSView literalView;
    
    NSView resourceView;
    
    NSView propertyView;

    NSButton literalChangeButton;
    
    NSButton resourceChangeButton;

    NSTextField literalTextField;

    NSTextField propertyTextField;

    NSTextField resourceIdField;

    NSTextField resourceTypeField;
    
    static String itemChangedNotification = "org.rdfweb.RDFAuthor.itemChanged";
    
    static String showInfoNotification = "org.rdfweb.RDFAuthor.showInfo";
    
    ModelItem currentItem = null;
    
    RDFAuthorDocument currentDocument = null;

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
        // Initialise frames from the original view (which is then removed)
        NSRect rect = contentView.frame();
        nothingView.setFrame(rect);
        literalView.setFrame(rect);
        resourceView.setFrame(rect);
        propertyView.setFrame(rect);
        
        // Initialise the panel with the 'nothing' view
        infoWindow.contentView().replaceSubview(contentView, nothingView);
        currentView = nothingView;
    }
    
    public void setInfoView(NSView view)
    {
        infoWindow.contentView().replaceSubview(currentView, view);
        currentView = view;
    }
    
    public void currentWindowChanged(NSNotification notification)
    {
        NSWindow currentWindow = (NSWindow) notification.object();
        currentDocument = (RDFAuthorDocument) currentWindow.delegate(); // gives the document
        ModelItem item = currentDocument.currentObject();
        setCurrentItem(item);
    }
    
    public void windowClosed(NSNotification notification)
    {
        NSWindow window = (NSWindow) notification.object();
        RDFAuthorDocument document = (RDFAuthorDocument) window.delegate();
        if (document == currentDocument)
        {
            setCurrentItem(null);
        }
    }
    
    public void currentItemChanged(NSNotification notification)
    {
        RDFAuthorDocument document = (RDFAuthorDocument) notification.object();
        
        if (document == currentDocument)
        {
            ModelItem item = document.currentObject();
            setCurrentItem(item); // cheap way to update
        }
    }
    
    public void setCurrentItem(ModelItem item)
    {
        currentItem = item;
        
        if (currentItem == null)
        {
            showNothing();
        }
        else if (!currentItem.isNode())
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
        setCurrentItem(currentItem); // reinitialises the fields
    }
    
    public void changeObject(Object sender)
    {
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
            currentItem.setIsLiteral(false);
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
                int sep = property.lastIndexOf("/");
                int hash = property.lastIndexOf("#");
                
                sep = (hash > sep)?hash:sep;
                
                String namespace = property.substring(0, sep+1);
                String name = property.substring(sep+1);
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
            currentItem.setIsLiteral(true);
            return; // Don't need subsequent checks in this case
        }
        
        if (id.equals(""))
        {
            ((Node) currentItem).setId(null);
        }
        else
        {
            if (checkUrl(id))
            {
                ((Node) currentItem).setId(id);
            }
        }
        
        if (type.equals(""))
        {
            ((Node) currentItem).setType(null, null);
        }
        else
        {
            if (checkUrl(type))
            {
                int sep = type.lastIndexOf("/");
                int hash = type.lastIndexOf("#");
                
                sep = (hash > sep)?hash:sep;
                
                String namespace = type.substring(0, sep+1);
                String name = type.substring(sep+1);
                ((Node) currentItem).setType(namespace, name);
            }
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
            NSAlertPanel alert = new NSAlertPanel();
            alert.runAlert("Not a URL: ",
                error + "\nThis field requires either a URL or an empty value.",
                null, null, null);
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
        setInfoView(nothingView);
        setFields("", "", "", "");
    }
    
    public void showArc(Arc arc)
    {
        
        String propertyNS = (arc.propertyNamespace() == null)?"":arc.propertyNamespace();
        String propertyN = (arc.propertyName() == null)?"":arc.propertyName();
        
        setInfoView(propertyView);
        setFields("", "", "", propertyNS + propertyN);
    }
    
    public void showLiteral(Node literal)
    {
        setInfoView(literalView);
        
        String literalVal = (literal.id() == null)?"":literal.id();
        
        setFields(literalVal, "", "", "");
    }
    
    public void showNode(Node node)
    {
        setInfoView(resourceView);
        
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

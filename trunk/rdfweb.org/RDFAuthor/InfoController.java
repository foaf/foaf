/* InfoController */

import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.*;

import java.net.URL;
import java.net.MalformedURLException;

public class InfoController extends NSObject {

    NSPanel infoWindow;

    NSButton literalButton;

    NSTextField literalTextField;

    NSTextField propertyTextField;

    NSTextField resourceIdField;

    NSTextField resourceTypeField;
    
    static String itemChangedNotification = "org.rdfweb.RDFAuthor.itemChanged";
    
    ModelItem currentItem = null;
    
    MyDocument currentDocument = null;

    public InfoController()
    {
        // Initialise notification stuff
        
        NSSelector itemSelector = new NSSelector("currentItemChanged", 
                new Class[] {NSNotification.class} );
        
        NSSelector windowSelector = new NSSelector("currentWindowChanged", 
                new Class[] {NSNotification.class} );
        
        NSNotificationCenter.defaultCenter().addObserver(
            this, itemSelector, itemChangedNotification, null);
        
        NSNotificationCenter.defaultCenter().addObserver(
            this, windowSelector, NSWindow.WindowDidBecomeMainNotification, null);
    }
    
    public void currentWindowChanged(NSNotification notification)
    {
        NSWindow currentWindow = (NSWindow) notification.object();
        currentDocument = (MyDocument) currentWindow.delegate(); // gives the document
        ModelItem item = currentDocument.currentObject();
        setCurrentItem(item);
    }
    
    public void currentItemChanged(NSNotification notification)
    {
        MyDocument document = (MyDocument) notification.object();
        
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
        
    public void literalButtonChanged(Object sender) {
        ((Node) currentItem).setIsLiteral(literalButton.state() == NSCell.OnState);
    }

    public void literalTextChanged(Object sender) {
        String value = literalTextField.stringValue().trim();
        value = (value.equals(""))?null:value;
        ((Node) currentItem).setId(value);
    }

    public void propertyTextChanged(Object sender) {
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

    public void resourceIdTextChanged(Object sender) {
        String id = resourceIdField.stringValue().trim();
        
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
    }

    public void resourceTypeTextChanged(Object sender) {
        String type = resourceTypeField.stringValue().trim();
        
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
    
    public void showInfoWindow(Object sender) {
        if (infoWindow.isVisible())
        {
            infoWindow.orderOut(this);
        }
        else
        {
            infoWindow.makeKeyAndOrderFront(this);
        }
    }
    
    public void showNothing()
    {
        literalButton.setState(NSCell.OffState);
        literalButton.setEnabled(false);
        literalTextField.setEnabled(false);
        propertyTextField.setEnabled(false);
        resourceIdField.setEnabled(false);
        resourceTypeField.setEnabled(false);
        
        setFields("", "", "", "");
    }
    
    public void showArc(Arc arc)
    {
        literalButton.setState(NSCell.OffState);
        literalButton.setEnabled(false);
        literalTextField.setEnabled(false);
        propertyTextField.setEnabled(true);
        resourceIdField.setEnabled(false);
        resourceTypeField.setEnabled(false);
        
        String propertyNS = (arc.propertyNamespace() == null)?"":arc.propertyNamespace();
        String propertyN = (arc.propertyName() == null)?"":arc.propertyName();
        
        setFields("", "", "", propertyNS + propertyN);
    }
    
    public void showLiteral(Node literal)
    {
        literalButton.setState(NSCell.OnState);
        literalButton.setEnabled(true);
        literalTextField.setEnabled(true);
        propertyTextField.setEnabled(true);
        resourceIdField.setEnabled(false);
        resourceTypeField.setEnabled(false);
        
        String literalVal = (literal.id() == null)?"":literal.id();
        
        setFields(literalVal, "", "", "");
    }
    
    public void showNode(Node node)
    {
        literalButton.setState(NSCell.OffState);
        literalButton.setEnabled(true);
        literalTextField.setEnabled(false);
        propertyTextField.setEnabled(false);
        resourceIdField.setEnabled(true);
        resourceTypeField.setEnabled(true);
        
        String typeNS = (node.typeNamespace() == null)?"":node.typeNamespace();
        String typeN = (node.typeName() == null)?"":node.typeName();
        
        String id = (node.id() == null)?"":node.id();
        
        setFields("", typeNS + typeN, id, "");
    }
    
    public void setFields( String literal, String type, String id, String property)
    {
        literalTextField.setStringValue(literal);
        propertyTextField.setStringValue(property);
        resourceIdField.setStringValue(id);
        resourceTypeField.setStringValue(type);
    }
}

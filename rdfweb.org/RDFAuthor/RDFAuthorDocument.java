/* RDFAuthorDocument */

/* $Id: RDFAuthorDocument.java,v 1.30 2002-01-06 22:15:29 pldms Exp $ */

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

import java.io.*;
import java.util.HashMap;
import java.util.ArrayList;

public class RDFAuthorDocument extends NSDocument {
    
    static final String FileFormatPrefix = "RDFAuthor File Format Version ";
    static final String FileFormatNumber = "0.3";
    
    NSTextField textDescriptionField;

    RDFModelView rdfModelView;
    
    NSScrollView rdfModelScrollView;
    
    RDFToolbar rdfToolbar;
    
    NSWindow window;
    
    NSTextView previewTextView;
    NSScrollView previewView;
    
    QueryController queryController;

    BookmarkController bookmarkController;
    
    ArcNodeList rdfModel;
    GraphicalModel rdfGraphicModel;
    
    HashMap exportMappings;
    boolean needsAutoLayout; // indicate whether we've loaded something which needs laying out
    float modelWidth; // for remembering sizes when loading
    float modelHeight;
    ArrayList bookmarkedItems; // Temporary storage for loading bookmarks
    
    boolean showTypes;
    boolean showIds;
    boolean showProperties;
    
    String defaultPropertyNamespace = null;
    String defaultPropertyName = null;
    String defaultClassNamespace = null;
    String defaultClassName = null;
    
    public RDFAuthorDocument() {
        super();
        rdfModel = new ArcNodeList(this);
        showTypes = false;
        showIds = false;
        showProperties = false;
    }
    
    public RDFAuthorDocument(String fileName, String fileType) {
        super(fileName, fileType);
    }
    
    public RDFAuthorDocument( java.net.URL anURL, String docType) {
        super(anURL, docType);
    }
    
    public void printDocumentUsingPrintPanel(boolean flag)
    {
        NSPrintOperation printOperation = 
            NSPrintOperation.printOperationWithView((NSView) rdfModelView);
        printOperation.runModalOperation(window, null, null, null);
    }
    
    public void printDocument(Object sender) {
        printDocumentUsingPrintPanel(true);
    }
    
    public NSData dataRepresentationOfType(String aType) {
        // Insert code here to create and return the data for your document.
        System.out.println("Wants to save as " + aType);
        
        if (aType.equals("RDFAuthor Document"))
        {
            try
            {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                ObjectOutputStream s = new ObjectOutputStream(out);
                s.writeObject(FileFormatPrefix + FileFormatNumber);
                s.writeBoolean(showTypes);
                s.writeBoolean(showIds);
                s.writeBoolean(showProperties);
                s.writeFloat(rdfModelView.frame().width());
                System.out.println("Wrote width: " + rdfModelView.frame().width());
                s.writeFloat(rdfModelView.frame().height());
                System.out.println("Wrote height: " + rdfModelView.frame().height());
                s.writeObject(rdfModel);
                s.writeObject(bookmarkController.items());

                s.flush();
                out.flush();
                
                NSData savedData = new NSData(out.toByteArray());

                s.close();
                out.close();

                return savedData;
            }
            catch (Exception e)
            {
                System.out.println("Got error: "+e);
                return null;
            }
        }
        else if (aType.equals("PDF Document")) // PDF Export
        {
            return rdfModelView.dataWithPDFInsideRect(rdfModelView.frame());
        }
        else if (aType.equals("EPS Document")) // EPS Export
        {
            return rdfModelView.dataWithEPSInsideRect(rdfModelView.frame());
        }
        else if (aType.equals("TIFF Image")) // TIFF export
        {
            return rdfModelView.TIFFRepresentation();
        }
        else if (aType.equals("SVG Document")) // SVG Export
        {
            try
            {
                StringWriter stringOutput = new StringWriter();
                rdfGraphicModel.svgRepresentation(stringOutput, this, rdfModel, rdfModelView);
                stringOutput.flush();
                NSMutableStringReference svgString = new NSMutableStringReference();
                svgString.setString(stringOutput.toString());
                stringOutput.close();
                return svgString.dataUsingEncoding(NSStringReference.UTF8StringEncoding, false);
            }
            catch (Exception e)
            {
                RDFAuthorUtilities.ShowError(
                    "SVG Export Failed",
                    "Export failed, I'm afraid. Can't imagine why.",
                    RDFAuthorUtilities.Critical, window);
                return null;
            }
        }
        else if (exportMappings.get(aType) != null)
        {
            String outputType = (String) exportMappings.get(aType);

            try
            {
                StringWriter stringOutput = new StringWriter();
                rdfModel.exportAsRDF(stringOutput, outputType);
                stringOutput.flush();
                NSMutableStringReference rdfString = new NSMutableStringReference();
                rdfString.setString(stringOutput.toString());
                stringOutput.close();
                return rdfString.dataUsingEncoding(NSStringReference.UTF8StringEncoding, false);
            }
            catch (Exception e)
            {
                RDFAuthorUtilities.ShowError(
                    "RDF Export Failed",
                    "Export failed, I'm afraid. Try using 'Check Model' for possible problems.",
                    RDFAuthorUtilities.Critical, window);
                return null;
            }
        }
        else
        {
            System.out.println("Unknown save type: " + aType);
            return null;
        }
    }

    public boolean loadDataRepresentation(NSData data, String aType) {
        // Insert code here to read your document from the given data.
        System.out.println("Wants to load something of type " + aType);
        
        exportMappings = new HashMap();
        
        exportMappings.put("RDF/XML Document", "RDF/XML-ABBREV");
        exportMappings.put("N-Triple Document", "N-TRIPLE");
        //exportMappings.put("N3 Document", "N3");
        
        boolean success;
        
        if (aType.equals("RDFAuthor Document"))
        {
            try
            {
                ByteArrayInputStream in = 
                    new ByteArrayInputStream( data.bytes(0, data.length()) );
                ObjectInputStream s = new ObjectInputStream(in);
                String formatVersion = (String) s.readObject();
                if (!formatVersion.startsWith(FileFormatPrefix))
                {
                    RDFAuthorUtilities.ShowError(
                        "Incompatible File Format",
                        "This version requires " + FileFormatPrefix + "0.1 (or above), but this file is in "
                        + formatVersion
                        + ".\nBlame the author." ,
                        RDFAuthorUtilities.Critical, null);
                    success = false;
                }
                else
                {
                    String versionNumber = formatVersion.substring(FileFormatPrefix.length());
                    System.out.println("Version: " + versionNumber);
                    showTypes = s.readBoolean();
                    showIds = s.readBoolean();
                    showProperties = s.readBoolean();
                    if (!versionNumber.equals("0.1")) // I've added doc size info since 0.1
                    {
                        modelWidth = s.readFloat();
                        System.out.println("Width: " + modelWidth);
                        modelHeight = s.readFloat();
                        System.out.println("Height: " + modelHeight);
                    }

                    rdfModel = (ArcNodeList) s.readObject();
                    rdfModel.setController(this);

                    if (!versionNumber.equals("0.1") && !versionNumber.equals("0.2")) // bookmarks added later
                    {
                        bookmarkedItems = (ArrayList) s.readObject();
                    }
                    
                    success = true;
                    needsAutoLayout = false;

                    s.close();
                    in.close();
                }
            }
            catch (Exception e)
            {
                RDFAuthorUtilities.ShowError(
                    "File Loading Failed", 
                    "Loading failed. Is this really an RDFAuthor file?\nError:\n"+e,
                    RDFAuthorUtilities.Critical, null);
                e.printStackTrace();
                success = false;
            }
        }
        // Import from serialised form
        else if (exportMappings.get(aType) != null)
        {
            String inputType = (String) exportMappings.get(aType);
            
            try
            {
                String rdf = new String(data.bytes(0, data.length()), "UTF-8");
            
                System.out.print("Data is: " + rdf);
            
                StringReader reader = new StringReader(rdf);
                rdfModel = new ArcNodeList(this, reader, inputType );
                success = true;
                needsAutoLayout = true;
            }
            catch (Exception e)
            {
                System.out.println("Deserialisation: " + e);
                e.printStackTrace();
                RDFAuthorUtilities.ShowError(
                    "File Import Failed", 
                    "Loading failed. There may be errors in the serialisation\nError:\n"+e,
                    RDFAuthorUtilities.Critical, null);
                success = false;
            }
        }
        else
        {
            System.out.println("Don't know this type");
            success = false;
        }
        
        if (rdfModelView != null) // We seem to be reverting - nib won't be loaded
        {
            rdfModelView.setNeedsDisplay(true);
            if (modelWidth > 0) // revert size (if needed)
            {
                rdfModelView.setFrameSize(new NSSize(modelWidth, modelHeight));
            }
            if (needsAutoLayout) // Revert autolayout
            {
                RDFAuthorUtilities.layoutModel(rdfModel, 
                    rdfModelView.frame().x(), 
                    rdfModelView.frame().y(), 
                    rdfModelView.frame().maxX(), 
                    rdfModelView.frame().maxY());
            }
            // This caused a nasty bug - if it reverted it loaded the doc, but lost the Graphics
            // Initialise rdfGraphicModel
            rdfGraphicModel = new GraphicalModel(rdfModel, rdfModelView);
            if (bookmarkedItems != null) // we loaded bookmarked items
            {
                bookmarkController.setItems(bookmarkedItems);
            }
        }
        
        if (!success)
        {
            System.out.println("Loading failed :-(");
        }
        
        return success;
    }
    
    public String windowNibName() {
        return "RDFAuthorDocument";
    }
    
    // Most of the initialisation happens here
    
    public void windowControllerDidLoadNib(NSWindowController  aController) {
        super.windowControllerDidLoadNib(aController);
        // Add any code here that need to be executed once the windowController has loaded the document's window.
	
        window = aController.window();
        
	// Attach the toolbar to the document window.
	NSToolbar theToolbar = new NSToolbar(RDFToolbar.identifier);
        theToolbar.setAllowsUserCustomization(true);
	theToolbar.setAutosavesConfiguration(true);
	theToolbar.setDisplayMode(NSToolbar.NSToolbarDisplayModeIconOnly);
        
        theToolbar.setDelegate((NSObject) rdfToolbar);
        
        window.setToolbar(theToolbar);
        
        // Set printing to auto paginate
        
        NSPrintInfo.sharedPrintInfo().setHorizontalPagination(NSPrintInfo.AutoPagination);
        NSPrintInfo.sharedPrintInfo().setVerticalPagination(NSPrintInfo.AutoPagination);
        
        // Initialise rdfGraphicModel
        
        rdfGraphicModel = new GraphicalModel(rdfModel, rdfModelView);
        
        if (modelWidth > 0) // file had size info
        {
            rdfModelView.setFrameSize(new NSSize(modelWidth, modelHeight));
        }
        else
        {
            // Set rdfModelView's size to current paper size
            
            rdfModelView.setSizeFromPrintInfo( NSPrintInfo.sharedPrintInfo() );
        }
        if (needsAutoLayout)
        {
            RDFAuthorUtilities.layoutModel(rdfModel, 
            rdfModelView.frame().x(), rdfModelView.frame().y(), rdfModelView.frame().maxX(), rdfModelView.frame().maxY());
        }

        if (bookmarkedItems != null) // we loaded bookmarked items
        {
            bookmarkController.setItems(bookmarkedItems);
        }
        
        // There must be a way to do this in interface builder...
        
        rdfModelScrollView.setHasHorizontalScroller(true);
        rdfModelScrollView.setHasVerticalScroller(true);
        rdfModelScrollView.setDocumentView(rdfModelView);
        rdfModelScrollView.setDrawsBackground(true);
        rdfModelScrollView.setBackgroundColor(NSColor.lightGrayColor());
        
        // This is for exporting
        exportMappings = new HashMap();
        
        exportMappings.put("RDF/XML Document", "RDF/XML-ABBREV");
        exportMappings.put("N-Triple Document", "N-TRIPLE");
        //exportMappings.put("N3 Document", "N3");
    }
    
    public void setPrintInfo(NSPrintInfo printInfo)
    {
        // Set printing to auto paginate
        
        printInfo.setHorizontalPagination(NSPrintInfo.AutoPagination);
        printInfo.setVerticalPagination(NSPrintInfo.AutoPagination);
        
        NSPrintInfo.setSharedPrintInfo(printInfo);
        super.setPrintInfo(printInfo);
    }

    public void windowDidBecomeKey(NSNotification notification)
    {
        if (notification.object() == window)
        {
            window.makeFirstResponder(rdfModelView);
        }
    }
    
    public void setDocumentSize(NSSize size)
    {
        rdfModelView.setFrameSize(size);
        rdfModelView.setBoundsSize(size);
        rdfModelScrollView.setNeedsDisplay(true);
    }
    
    public boolean showTextPreview(boolean showPreview, String type)
    {
        if (showPreview)
        {
            boolean success = createPreviewText(type);
            if (!success)
            {
                return false;
            }
            NSRect rect = rdfModelScrollView.frame();
            previewView.setFrame(rect);
            window.contentView().replaceSubview(rdfModelScrollView, previewView);
            return true;
        }
        else
        {
            NSRect rect = previewView.frame();
            rdfModelScrollView.setFrame(rect);
            window.contentView().replaceSubview(previewView, rdfModelScrollView);
            return true;
        }
    }
    
    public boolean createPreviewText(String type)
    {
        try
        {
            StringWriter stringOutput = new StringWriter();
            rdfModel.exportAsRDF(stringOutput, type);
            stringOutput.flush();
            previewTextView.setString(stringOutput.toString());
            stringOutput.close();
            return true;
        }
        catch (Exception e)
        {
            RDFAuthorUtilities.ShowError(
                "Serialisation Failed",
                "I couldn't convert this to '" + type + 
                "'. Try using 'Check Model' for possible problems.", //\n(Note: N3 Doesn't work currently)",
                RDFAuthorUtilities.Critical, window);
            previewTextView.setString("");
            return false;
        }
    }
    
    public void modelChanged()
    {
        updateChangeCount(1);
        //if (rdfModelView != null) // I need to check for this since the model changed message can occur when loading
        //{
            // Tell info window that something changed
            NSNotificationCenter.defaultCenter().postNotification(
                new NSNotification(InfoController.itemChangedNotification, this) );
        //}
    }
    
    public void showTypes(boolean value)
    {
        showTypes = value;
        rdfModel.showTypes(value);
    }

    public void showIds(boolean value) 
    {
        showIds = value;
        rdfModel.showIds(value);
    }

    public void showProperties(boolean value) 
    {
        showProperties = value;
        rdfModel.showProperties(value);
    }

    public void showInfoForObjectAtPoint(NSPoint point)
    {
        ModelItem item = rdfGraphicModel.objectAtPoint(rdfModel, point);
        if (item != null)
        {
            rdfModel.selection.set(item);
            NSNotificationCenter.defaultCenter().postNotification(
                new NSNotification(InfoController.showInfoNotification, null) );
        }
    }
    
    public void openUrlForObjectAtPoint(NSPoint point)
    {
        ModelItem item = rdfGraphicModel.objectAtPoint(rdfModel, point);
        if (item != null)
        {
            if (item.isNode() && !((Node) item).isLiteral())
            {
                String urlString = ((Node) item).id();
                if (urlString != null)
                {
                    try
                    {
                        java.net.URL url = new java.net.URL( urlString );
                        NSWorkspace.sharedWorkspace().openURL( url );
                    }
                    catch (Exception e)
                    {
                        RDFAuthorUtilities.ShowError("Cannot Open URL",
                            "I cannot open <" + urlString +
                            ">. Perhaps it isn't a URL?",
                            RDFAuthorUtilities.Normal, window);
                    }
                }
            }
        }
    }
    
    public void deleteObjectAtPoint(NSPoint point)
    {
        ModelItem item = rdfGraphicModel.objectAtPoint(rdfModel, point);
        if (item != null)
        {
            rdfModel.deleteObject(item);
            queryController.checkForDeletedItems(rdfModel);
        }
    }

    // This is used to add a pre-existing object to the model
    // (used by paste)
    
    public void addObject(ModelItem object)
    {
        rdfModel.add(object);
        
        if (object.isNode())
        {
            ((Node) object).setGraphicRep(new GraphicalNode((Node) object, rdfModelView));
            ((Node) object).setShowId(showIds);
            ((Node) object).setShowType(showTypes);
        }
        else
        {
            ((Arc) object).setGraphicRep(new GraphicalArc((Arc) object, rdfModelView));
            ((Arc) object).setShowProperty(showProperties);
        }

        rdfModel.selection().add(object);
    }
        
    
    public void addNodeAtPoint(String id, String typeNamespace, String typeName, NSPoint point, boolean isLiteral)
    {
        // Do the 'defaults' thing
        
        typeName = (typeName == null)? defaultClassName : typeName ;
        typeNamespace = (typeNamespace == null)? defaultClassNamespace : typeNamespace ;
        
        Node newNode = new Node(id, typeNamespace, typeName, point.x(), point.y());
        rdfModel.add(newNode);
        
        // Create the corresponding graphical object
        
        newNode.setGraphicRep(new GraphicalNode(newNode, rdfModelView));

        // These have to come after the above otherwise the node tries to message a null

        rdfModel.selection().set(newNode);
        newNode.setShowId(showIds);
        newNode.setShowType(showTypes);
        newNode.setIsLiteral(isLiteral);
        NSNotificationCenter.defaultCenter().postNotification(
            new NSNotification(InfoController.itemChangedNotification, this) );
    }
    
    public void addConnectionFromPoint(NSPoint fromPoint, NSPoint toPoint)
    {
        ModelItem startNode = rdfGraphicModel.objectAtPoint(rdfModel, fromPoint);
        ModelItem endNode = rdfGraphicModel.objectAtPoint(rdfModel, toPoint);
        if (startNode != null && endNode != null
            && startNode.isNode() && endNode.isNode()
            && (startNode != endNode) )
        {
            Arc newArc = new Arc((Node)startNode, (Node)endNode, defaultPropertyNamespace, defaultPropertyName);
            rdfModel.add(newArc);
            
            // Create the corresponding graphical object
        
            newArc.setGraphicRep(new GraphicalArc(newArc, rdfModelView));
            
            // These come afterwards to stop something messaging a non-existent GraphicalArc
            
            rdfModel.selection().set(newArc);
            newArc.setShowProperty(showProperties);
            NSNotificationCenter.defaultCenter().postNotification(
                new NSNotification(InfoController.itemChangedNotification, this) );
        }
    }
    
    public ArcNodeSelection selection()
    {
        return rdfModel.selection();
    }
    
    public boolean addObjectAtPointToSelection(NSPoint point)
    {
        ModelItem item = rdfGraphicModel.objectAtPoint(rdfModel, point);
        if (item != null)
        {
            NSNotificationCenter.defaultCenter().postNotification(
                new NSNotification(InfoController.itemChangedNotification, this) );
                
            if (rdfModel.selection().contains(item))
            {
                rdfModel.selection().remove(item);
                return false;
            }
            else
            {
                rdfModel.selection().add(item);
                return true;
            }
        }
        
        return false;
    }
    
    public boolean setSelectionToObjectAtPoint(NSPoint point)
    {
        ModelItem item = rdfGraphicModel.objectAtPoint(rdfModel, point);
        if (item != null)
        {
            if (!rdfModel.selection().contains(item))
            {
                rdfModel.selection().set(item);
                NSNotificationCenter.defaultCenter().postNotification(
                    new NSNotification(InfoController.itemChangedNotification, this) );
            }
            return true;
        }
        else
        {
            rdfModel.selection().set(null);
            NSNotificationCenter.defaultCenter().postNotification(
                new NSNotification(InfoController.itemChangedNotification, this) );
        }
        return false;
    }
    
    public void setSelectionToObject(ModelItem object)
    {
        rdfModel.selection().set(object);
        NSNotificationCenter.defaultCenter().postNotification(
                new NSNotification(InfoController.itemChangedNotification, this) );
    }
    
    public void deleteSelection()
    {
        rdfModel.deleteSelection();
        NSNotificationCenter.defaultCenter().postNotification(
                new NSNotification(InfoController.itemChangedNotification, this) );
    }

    public void selectAll()
    {
        rdfModel.selectAll();
        NSNotificationCenter.defaultCenter().postNotification(
                new NSNotification(InfoController.itemChangedNotification, this) );
    }
    
    public void setSelectionFromRect(NSRect rect, boolean adding)
    {
        if (!adding)
        {
            rdfModel.selection().clear();
        }
        
        rdfModel.selection().add( rdfGraphicModel.objectsInRect(rdfModel, rect) );
        NSNotificationCenter.defaultCenter().postNotification(
                new NSNotification(InfoController.itemChangedNotification, this) );
    }
        
    public void moveSelectionBy(float dx, float dy)
    {
        rdfModel.selection().moveBy(dx, dy);
    }
    
    public void addQueryItemAtPoint(NSPoint point)
    {
        ModelItem item = rdfGraphicModel.objectAtPoint(rdfModel, point);
        queryController.addQueryItem(item);
        rdfModelView.setNeedsDisplay(true);
    }
    
    public void selectNextObject()
    {
        rdfModel.selectNextObject();
        NSNotificationCenter.defaultCenter().postNotification(
                new NSNotification(InfoController.itemChangedNotification, this) );
    }
    
    public void selectPreviousObject()
    {
        rdfModel.selectPreviousObject();
        NSNotificationCenter.defaultCenter().postNotification(
                new NSNotification(InfoController.itemChangedNotification, this) );
    }

    public void setIdForNodeAtPoint(String id, NSPoint point, boolean isLiteral)
    {
        ModelItem item = rdfGraphicModel.objectAtPoint(rdfModel, point);
        if ((item !=null) && item.isNode())
        {
            ((Node) item).setId(id);
        }
        else
        {
            addNodeAtPoint(id, null, null, point, isLiteral);
        }
    }
    
    public void setTypeForNodeAtPoint(String namespace, String name, NSPoint point)
    {
        ModelItem item = rdfGraphicModel.objectAtPoint(rdfModel, point);
        if ((item !=null) && item.isNode())
        {
            ((Node) item).setType(namespace, name);
        }
        else
        {
            addNodeAtPoint(null, namespace, name, point, false);  // false - not a literal
        }
    }
    
    public void setTypeForArcAtPoint(String namespace, String name, NSPoint point)
    {
        ModelItem item = rdfGraphicModel.objectAtPoint(rdfModel, point);
        if ((item !=null) && !item.isNode())
        {
            ((Arc) item).setProperty(namespace, name);
        }
    }
    
    public void drawModel(NSRect rect)
    {
        rdfGraphicModel.drawModel(rdfModel, rect);
        queryController.drawQueryItems();
    }
    
    public void autoLayout()
    {
        RDFAuthorUtilities.layoutModel(rdfModel,
            rdfModelView.frame().x(),
            rdfModelView.frame().y(),
            rdfModelView.frame().maxX(),
            rdfModelView.frame().maxY());
    }
    
    public void doCheckModel()
    {
        NSNotificationCenter.defaultCenter().postNotification(
            new NSNotification(ErrorWindowController.checkModelNotification, window) );
    }
    
    public void checkModel(ModelErrorData errorData)
    {
        rdfModel.checkModel(errorData);
    }
    
    public void setClassPropertyDefaults(String classNamespace, String className, 
            String propertyNamespace, String propertyName)
    {
        defaultPropertyNamespace = propertyNamespace;
        defaultPropertyName = propertyName;
        defaultClassNamespace = classNamespace;
        defaultClassName = className;
    }

}
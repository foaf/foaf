/* RDFAuthorDocument */

/* $Id: RDFAuthorDocument.java,v 1.35 2002-03-27 10:22:36 pldms Exp $ */

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
    //GraphicalModel rdfGraphicModel;
    
    HashMap exportMappings;
    boolean needsAutoLayout; // indicate whether we've loaded something which needs laying out
    float modelWidth; // for remembering sizes when loading
    float modelHeight;
    ArrayList bookmarkedItems; // Temporary storage for loading bookmarks
    
    boolean showTypes;
    boolean showIds;
    boolean showProperties;
    
    boolean showingPreview;
    
    String defaultPropertyNamespace = null;
    String defaultPropertyName = null;
    String defaultClassNamespace = null;
    String defaultClassName = null;
    
    public RDFAuthorDocument() 
    {
        super();
        rdfModel = new ArcNodeList(this);
    }
    
    public RDFAuthorDocument(String fileName, String fileType) 
    {
        super(fileName, fileType);
        System.out.println("Loading file: " + fileName + " (" + fileType + ")");
        // Big secret - Document and Stationery identical
        // However we have to change the Stationery so it isn't
        // linked to the loaded file, and change the type to Document
        if (fileType.equals("RDFAuthor Stationery"))
        {
            this.setFileName(null);
            this.setFileType("RDFAuthor Document");
        }
    }
    
    public RDFAuthorDocument( java.net.URL anURL, String docType) 
    {
        super(anURL, docType);
        System.out.println("Loading url: " + anURL + " (" + docType + ")");
    }
    
    public boolean readFromURL( java.net.URL anURL, String docType)
    {
        System.out.println("Hehe, we are in! " + anURL + " " + docType);
        try{
            java.net.URLConnection conn = anURL.openConnection();
            int length = conn.getContentLength();
            byte[] content = new byte[length];
            InputStream stream = conn.getInputStream();
            
            /**
             * Warning
             * This doesn't do timing out etc
             */
             
            int read = 0;
            while (read != length)
            {
                read += stream.read(content, read, stream.available());
            }
            
            System.out.println("Read: " + read + " bytes of " + length);
            
            stream.close();
            
            NSData data = new NSData(content);
            
            boolean success = loadDataRepresentation(data, docType);
            
            setFileName(anURL.toString());
            
            return success;            
        }
        catch (Exception e) {System.err.println("Error: e"); return false;}
    }
    
    public NSData dataRepresentationOfType(String aType) {
        // Insert code here to create and return the data for your document.
        System.out.println("Wants to save as " + aType);
        
        // These are identical - only loading has any differences
        
        if (aType.equals("RDFAuthor Document") || aType.equals("RDFAuthor Stationery"))
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
                rdfModelView.svgRepresentation(stringOutput);
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
        
        exportMappings.put("RDF/XML Document", "RDF/XML");
        exportMappings.put("N-Triple Document", "N-TRIPLE");
        //exportMappings.put("N3 Document", "N3");
        
        boolean success;
        
        if (aType.equals("RDFAuthor Document") || aType.equals("RDFAuthor Stationery"))
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
                    
                    // bookmarks added later
                    if (!versionNumber.equals("0.1") && !versionNumber.equals("0.2"))	
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
            rdfModelView.addObject(rdfModel);
            if (bookmarkedItems != null) // we loaded bookmarked items
            {
                bookmarkController.setItems(bookmarkedItems);
            }
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
        
        // Initialise graphic reps
        
        rdfModelView.addObject(rdfModel);
        
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
        
        exportMappings.put("RDF/XML Document", "RDF/XML");
        exportMappings.put("N-Triple Document", "N-TRIPLE");
        //exportMappings.put("N3 Document", "N3");
    }

    public void printDocumentUsingPrintPanel(boolean flag)
    {
        NSPrintOperation printOperation = 
            NSPrintOperation.printOperationWithView((NSView) rdfModelView);
        printOperation.runModalOperation(window, null, null, null);
    }
    
    public void printDocument(Object sender) 
    {
        printDocumentUsingPrintPanel(true);
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
                "'. Try using 'Check Model' for possible problems.",
                RDFAuthorUtilities.Critical, window);
            previewTextView.setString("");
            return false;
        }
    }
    
    public void modelChanged()
    {
        updateChangeCount(1);
        NSNotificationCenter.defaultCenter().postNotification(
            new NSNotification(InfoController.itemChangedNotification, this) );
    }
    
    public void showInfoForObject(ModelItem item)
    {
        if (item != null)
        {
            rdfModel.selection.set(item);
            NSNotificationCenter.defaultCenter().postNotification(
                new NSNotification(InfoController.showInfoNotification, null) );
        }
    }
    
    public void openUrlForObject(ModelItem item)
    {
        if ((item != null) && item.isNode() && !((Node) item).isLiteral())
        {
            String urlString = ((Node) item).id();
            if (urlString != null)
            {
                try
                {
                    java.net.URL url = new java.net.URL( urlString );
                    
                    if (((Node) item).isObjectOfSeeAlso())
                    {
                        NSDocumentController.sharedDocumentController().openDocumentWithContentsOfURL(url, true);
                    }
                    else
                    {
                        NSWorkspace.sharedWorkspace().openURL( url );
                    }
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
    
    public void deleteObject(ModelItem item)
    {
        rdfModel.deleteObject(item);
        queryController.checkForDeletedItems(rdfModel);
    }

    // This is used to add a pre-existing object to the model
    // (used by paste)
    
    public void addObject(ModelItem object)
    {
        rdfModel.add(object);
        
        if (object.isNode())
        {
            ((Node) object).setShowId(showIds);
            ((Node) object).setShowType(showTypes);
            ((Node) object).setGraphicRep(new GraphicalNode((Node) object, rdfModelView));
        }
        else
        {
            ((Arc) object).setShowProperty(showProperties);
            ((Arc) object).setGraphicRep(new GraphicalArc((Arc) object, rdfModelView));
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

        // These have to come after the above otherwise the node tries to message a null

        rdfModel.selection().set(newNode);
        newNode.setShowId(showIds);
        newNode.setShowType(showTypes);
        newNode.setIsLiteral(isLiteral);
        newNode.setGraphicRep(new GraphicalNode(newNode, rdfModelView));
        NSNotificationCenter.defaultCenter().postNotification(
            new NSNotification(InfoController.itemChangedNotification, this) );
    }
    
    public void addConnectionFrom(ModelItem fromItem, ModelItem toItem)
    {
        if ((fromItem != null) && (toItem != null) &&
            fromItem.isNode() && toItem.isNode()
            && (fromItem != toItem) )
        {
            Arc newArc = new Arc((Node)fromItem, (Node)toItem, defaultPropertyNamespace, defaultPropertyName);
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
    
    public void addObjectToSelection(ModelItem item)
    {
        NSNotificationCenter.defaultCenter().postNotification(
            new NSNotification(InfoController.itemChangedNotification, this) );

        if (rdfModel.selection().contains(item))
        {
            rdfModel.selection().remove(item);
        }
        else
        {
            rdfModel.selection().add(item);
        }
    }
    
    public void setSelectionToObject(ModelItem item)
    {
        if (item != null)
        {
            if (!rdfModel.selection().contains(item))
            {
                rdfModel.selection().set(item);
                NSNotificationCenter.defaultCenter().postNotification(
                    new NSNotification(InfoController.itemChangedNotification, this) );
            }
            
        }
        else
        {
            rdfModel.selection().set(null);
            NSNotificationCenter.defaultCenter().postNotification(
                new NSNotification(InfoController.itemChangedNotification, this) );
        }
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
    
    public void setSelection(ArrayList items, boolean adding)
    {
        if (!adding)
        {
            rdfModel.selection().clear();
        }
        
        rdfModel.selection().add( items );
        NSNotificationCenter.defaultCenter().postNotification(
                new NSNotification(InfoController.itemChangedNotification, this) );
    }
        
    public void moveSelectionBy(float dx, float dy)
    {
        rdfModel.selection().moveBy(dx, dy);
    }
    
    public void addQueryItem(ModelItem item)
    {
        queryController.addQueryItem(item);
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

    public void setIdForNode(String id, ModelItem item, boolean isLiteral)
    {
        if (item.isNode())
        {
            ((Node) item).setId(id);
        }
    }
    
    public void setTypeForNode(String namespace, String name, ModelItem item)
    {
        if ((item != null) && item.isNode())
        {
            ((Node) item).setType(namespace, name);
        }
    }
    
    public void setTypeForArc(String namespace, String name, ModelItem item)
    {
        if ((item != null) && !item.isNode())
        {
            ((Arc) item).setProperty(namespace, name);
        }
    }
    
    public void drawModel(NSRect rect)
    {
        queryController.drawQueryItems(rect);
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
    
    /*
        The following are driven by user input, either via the toolbar
        or menu. They set editing modes, display settings, start error
        checking and auto-layout, and show the query and bookmark panels.
        
        Since the toolbar items are sometimes stateful (eg buttons)
        if an action is made from the menu we need to check for it and
        sync the button states.
    */
    
    private void selectMoveMode(Object sender)
    {
        rdfModelView.setEditingMode( RDFModelView.MoveSelectMode );
        if (sender instanceof NSMenuItem) rdfToolbar.syncButtonState();
    }
    
    private void selectAddNodeMode(Object sender)
    {
        rdfModelView.setEditingMode( RDFModelView.AddNodeMode );
        if (sender instanceof NSMenuItem) rdfToolbar.syncButtonState();
    }
    
    private void selectAddArcMode(Object sender)
    {
        rdfModelView.setEditingMode( RDFModelView.AddConnectionMode );
        if (sender instanceof NSMenuItem) rdfToolbar.syncButtonState();
    }
    
    private void selectDeleteMode(Object sender)
    {
        rdfModelView.setEditingMode( RDFModelView.DeleteItemsMode );
        if (sender instanceof NSMenuItem) rdfToolbar.syncButtonState();
    }
    
    private void selectMarkQueryMode(Object sender)
    {
        rdfModelView.setEditingMode( RDFModelView.AddQueryItemMode );
        if (sender instanceof NSMenuItem) rdfToolbar.syncButtonState();
    }
    
    private void showTypes(Object sender)
    {
        showTypes = !showTypes;
        rdfModel.showTypes(showTypes);
        if (sender instanceof NSMenuItem) rdfToolbar.syncButtonState();
    }
    
    private void showProperties(Object sender)
    {
        showProperties = !showProperties;
        rdfModel.showProperties(showProperties);
        if (sender instanceof NSMenuItem) rdfToolbar.syncButtonState();
    }
    
    private void showIds(Object sender)
    {
        showIds = !showIds;
        rdfModel.showIds(showIds);
        if (sender instanceof NSMenuItem) rdfToolbar.syncButtonState();
    }

    private void showQueryPanel(Object sender)
    {
        queryController.toggleShow();
    }
    
    private void showBookmarkWindow(NSToolbarItem sender)
    {
        bookmarkController.toggleShow();
    }
    
    public void autoLayout(Object sender)
    {
        RDFAuthorUtilities.layoutModel(rdfModel,
            rdfModelView.frame().x(),
            rdfModelView.frame().y(),
            rdfModelView.frame().maxX(),
            rdfModelView.frame().maxY());
    }
    
    public void doCheckModel(Object sender)
    {
        NSNotificationCenter.defaultCenter().postNotification(
            new NSNotification(ErrorWindowController.checkModelNotification, window) );
    }
    
    public void showTextPreview(Object sender)
    {
        String jenaType = rdfToolbar.previewType();
    
        if (!showingPreview)
        {
            boolean success = createPreviewText(jenaType);
            if (!success) return;
            
            NSRect rect = rdfModelScrollView.frame();
            previewView.setFrame(rect);
            window.contentView().replaceSubview(rdfModelScrollView, previewView);
            showingPreview = true;
        }
        else
        {
            NSRect rect = previewView.frame();
            rdfModelScrollView.setFrame(rect);
            window.contentView().replaceSubview(previewView, rdfModelScrollView);
            showingPreview = false;
        }
        
        // Set the toolbar image correctly
        rdfToolbar.syncPreview(showingPreview);
    }
    
    private void previewModeChanged(Object sender)
    {
        if (sender instanceof NSMenuItem) // Set popupmenu to selected type
        {
            int item = ((NSMenuItem) sender).menu().indexOfItem((NSMenuItem) sender);
            rdfToolbar.setPreviewType(item);
        }
        
        String jenaType = rdfToolbar.previewType();
        
        if (showingPreview)
        {
            createPreviewText(jenaType);
        }
    }

}
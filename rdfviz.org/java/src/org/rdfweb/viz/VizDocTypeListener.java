//refactoring

//controller class with not much in it
//each listener a separate class and register controller class with it
//maybe get and set method sfor the terms we need?
//or can we register the relevant opbjects more tightly
//keep lists of node arcs and schemas in the controller class
//have things draw themselves

//put saving and loading in a diferent class

package org.rdfweb.viz;

import java.io.PrintWriter;
import javax.swing.text.*;
import com.hp.hpl.mesa.rdf.jena.model.*;
import com.hp.hpl.mesa.rdf.jena.mem.*;
import com.hp.hpl.mesa.rdf.jena.common.prettywriter.*;
import com.hp.hpl.mesa.rdf.jena.vocabulary.RDF;
import com.hp.hpl.mesa.rdf.jena.vocabulary.RDFS;
import java.io.*;
import java.net.*;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import javax.swing.event.*;
import java.util.*;
import javax.swing.border.*;

public class VizDocTypeListener implements DocumentListener {

Viz viz;

public VizDocTypeListener(Viz viz){
this.viz=viz;
}

    /**

    doclistener stuff - updates the text in the focussed item (node or arc)
    when it changes in the text area


    */

    public void changedUpdate(DocumentEvent e) {

System.out.println("got c update "+e.getType());

        try {
            viz.getFocussedItem().updateType( e.getDocument().getText(0,
                    e.getDocument().getLength()));
            System.out.println("changed "+ e.getDocument().getText(0,
                    e.getDocument().getLength()));
        } catch (BadLocationException bl) {
            System.err.println("text problem "+bl);
        }
    }

    public void insertUpdate(DocumentEvent e) {

    System.out.println("got i update "+e.getType());

        try {
            viz.getFocussedItem().updateType( e.getDocument().getText(0,
                    e.getDocument().getLength()));
            System.out.println("insert"+ e.getDocument().getText(0,
                    e.getDocument().getLength()));
        } catch (BadLocationException bl) {
            System.err.println("text problem "+bl);
        }
    }

    public void removeUpdate(DocumentEvent e) {

System.out.println("got r update "+e.getType());

        try {
            viz.getFocussedItem().updateType( e.getDocument().getText(0,
                    e.getDocument().getLength()));
            System.out.println("remove"+ e.getDocument().getText(0,
                    e.getDocument().getLength()));
        } catch (BadLocationException bl) {
            System.err.println("text problem "+bl);
        }
    }


}



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

public class ExportActionListener implements  ActionListener {

Viz viz;

public ExportActionListener(Viz viz){
this.viz=viz;
}

    /**

     actionPerformed captures events from the menu.
     Node creates a new node, initialises it, puts it in the nodes vector, and repaints
     Arc creates a new arc, initialises it, puts it in the arcs vector and repaints
     arc also puts the state in state2
     */

    public void actionPerformed(ActionEvent e) {

        System.out.println("got action: "+ e.getActionCommand());
        String errorMsg = "couldn't load url ";
	Model mem=new ModelMem();

        if (e.getActionCommand().equals("Export")) {

            try {
                Enumeration en = viz.getArcs().elements();
                while (en.hasMoreElements()) {
                    DrawableProperty a =
                            (DrawableProperty) en.nextElement();
                    DrawableNode startN = (DrawableNode) a.startNode;
                    DrawableNode endN = (DrawableNode) a.endNode;

                    String sText = startN.getTmpText();
                    String eText = endN.getTmpText();

                    URL eUri = null;

                    try {
                        eUri = new URL(eText);
                    } catch (Exception ex2) {
                        System.err.println("not a uri "+ex2);
                    }

                    RDFNode endNode;
                    Resource startNode;
                    Property prop;

                    if (eUri != null) {
                        endNode = mem.createResource(eText);
                        System.out.println("GOT resource!");
                    } else {
                        System.out.println("GOT literal! [1] "+eText);
                        endNode = mem.createLiteral(eText);
                        System.out.println("GOT literal! [2] "+eText);
                    }

                    System.out.println("stext "+sText);
                    if (sText.trim().equals("")) {
                        startNode = mem.createResource();
                    } else {
                        startNode = mem.createResource(sText);
                    }
                    System.out.println("[1]");

                    prop = mem.createProperty(a.getTmpText());

                    System.out.println("[2]");

                    //add to the model
                    mem.add(startNode, prop, endNode);

                    System.out.println("[3] startnode "+
                            startNode.toString() + " "+
                            prop.toString() + " "+endNode.toString());

                }

            } catch (Exception ex) {
                System.err.println("something went wrong in conversion "+ex);
            }

            //	System.out.println(mem.toString());

            try {
                mem.write(new PrintWriter(System.out));
            } catch (Exception er) {
                System.err.println("could not write "+er);
            }

        }//end if export

    }

}

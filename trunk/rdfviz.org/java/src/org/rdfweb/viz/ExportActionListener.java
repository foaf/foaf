
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

System.out.println("got arc "+a);
                    DrawableNode startN = (DrawableNode) a.startNode;

System.out.println("[7]");
                    DrawableNode endN = (DrawableNode) a.endNode;

System.out.println("[8a]"+startN);
System.out.println("[8b]"+endN);
//                    String sText = startN.getTmpText();
  //                  String eText = endN.getTmpText();

if(startN!=null && endN!=null){


                    String sType = startN.getProp("type");
                    String eType = endN.getProp("type");

                    String sText = startN.getProp("label");
                    String eText = endN.getProp("label");
                    String sURI = startN.getProp("uri");
                    String eURI = endN.getProp("uri");

System.out.println("[9]");
                    URL eUri = null;
                    URL sUri = null;


		if(sURI!=null && (!startN.isLiteral())){
                    try {
                        sUri = new URL(sURI);
                    } catch (Exception ex22) {
                        System.err.println("not a uri "+ex22);
                    }

		}


System.out.println("[10]");

                    RDFNode endNode=null;
                    Resource startNode=null;
                    Resource startType=null;
                    Resource endType=null;
                    Property prop=null;

		if(eType!=null){
                        endType = mem.createResource(eType);
		}

		if(sType!=null){
                        startType = mem.createResource(sType);
		}


		if(endN.isLiteral()){

			if(eText!=null&& (!eText.equals(""))){
                        endNode = mem.createLiteral(eText);
			}else{
			endNode = mem.createLiteral("");
			}

		}else{

		if(eURI!=null){
                    try {
                        eUri = new URL(eURI);
                    } catch (Exception ex2) {
                        System.err.println("not a uri "+ex2);
                    }

		}


                    if (eUri != null) {
			if(eType!=null){
                        endNode = mem.createResource(eURI, endType);
			}else{
                        endNode = mem.createResource(eURI);
			}
                        System.out.println("GOT resource!");
                    } else {
			if(eType!=null){
                        endNode = mem.createResource(endType);
			}else{
                        endNode = mem.createResource();
			}
                        System.out.println("GOT anon! [1] ");
                    }
		}

//                    if (sText.trim().equals("")) {
                    if (sURI!=null || sURI.trim().equals("")) {
			if(sType!=null){
                        startNode = mem.createResource(startType);
			}else{
                        startNode = mem.createResource();
			}
                    } else {
			if(sType!=null){
//                        startNode = mem.createResource(sText);
                        startNode = mem.createResource(sURI);
			}else{
//                        startNode = mem.createResource(sText,startType);
                        startNode = mem.createResource(sURI,startType);
			}	
                    }
                    System.out.println("[1]");

                    prop = mem.createProperty(a.getProp("uri"));

                    System.out.println("[2]");

                    //add to the model
                    mem.add(startNode, prop, endNode);

System.out.println("[3] startn "+startNode.toString() );
System.out.println("[3] prop "+prop.toString() );
System.out.println("[3] endn "+endNode.toString() );

}//???


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

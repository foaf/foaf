//refactoring

//controller class with not much in it
//each listener a separate class and register controller class with it
//maybe get and set method sfor the terms we need?
//or can we register the relevant opbjects more tightly
//keep lists of node arcs and schemas in the controller class
//have things draw themselves

//put saving and loading in a diferent class

package org.rdfweb.viz;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import javax.swing.event.*;
import java.util.*;
import javax.swing.border.*;

public class NodeActionListener implements  ActionListener {

Viz viz;

public NodeActionListener(Viz viz){
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

	viz.setEditState(viz.NODE_FOCUS);

                DrawableNode n = new DrawableNode(100, 100);
                viz.setFocussedItem( n );
                viz.getNodes().addElement(n);
            viz.repaint();

        } 

}

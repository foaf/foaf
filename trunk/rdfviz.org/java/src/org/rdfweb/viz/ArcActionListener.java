
package org.rdfweb.viz;

import com.hp.hpl.mesa.rdf.jena.model.*;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import javax.swing.event.*;
import java.util.*;
import javax.swing.border.*;


public class ArcActionListener implements  ActionListener {

Viz viz;

public ArcActionListener(Viz viz){
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

            try {


//shoudl change url to getactioncommand
                DrawableProperty p = new DrawableProperty(viz.defaultURL);

                viz.setFocussedItem(p);
                viz.getArcs().addElement(p);

            } catch (RDFException ex) {
                System.err.println("can't create property "+ex);
            }

            viz.setEditState(viz.ARC_DRAG);
            viz.repaint();

	}

}

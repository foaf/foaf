package org.rdfweb.viz;

import javax.swing.*;
import java.awt.event.*;
import javax.swing.event.*;
import java.util.*;
import javax.swing.border.*;

public class VizMouseMotionListener implements MouseMotionListener{

Viz viz;

public VizMouseMotionListener (Viz viz){
this.viz=viz;
}

    /**

     mouse listeners

     */

    public void mouseMoved(MouseEvent me) {
    }


    /**

     mouseDragged -
     if a node is the focus, display the node as it changes
     as the mouse is dragged
     if an arc is the focus, display the arc as it changes

     */


    public void mouseDragged(MouseEvent me) {
        viz.updateAll();

        if (viz.getEditState() == viz.NODE_FOCUS) {//i.e. current focus is a node
            ((DrawableNode)viz.getFocussedItem()).setX(me.getX());
            ((DrawableNode)viz.getFocussedItem()).setY(me.getY());
            //if (!nodes.contains(cNode)) {
              //  nodes.addElement(cNode);
            //}
        }


        if (viz.getEditState() == viz.ARC_FOCUS) {//i.e. curent focus is an arc
            ((DrawableProperty)viz.getFocussedItem()).endX = me.getX();
            ((DrawableProperty)viz.getFocussedItem()).endY = me.getY();
            //if (!arcs.contains(cArc)) {
              //  arcs.addElement(cArc);
                //System.out.println("ADDING arc "+arcs);
            //}
            ///????
            ///	cArc.update();
        }


        viz.repaint();
    }




}


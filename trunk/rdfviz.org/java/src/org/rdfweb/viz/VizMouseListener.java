
package org.rdfweb.viz;

import com.hp.hpl.mesa.rdf.jena.model.*;
//import com.hp.hpl.mesa.rdf.jena.mem.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.text.*;
import java.awt.event.*;
import javax.swing.event.*;
import java.util.*;
import javax.swing.border.*;

public class VizMouseListener extends JComponent implements MouseListener{


Viz viz;

public VizMouseListener(Viz viz){
this.viz=viz;
}


    /**

     mouseClicked transfers the focus to the clicked object (node or arc) and
     displays the text in the object in the text area

     */

    public void mouseClicked(MouseEvent me) {

        //causing a loop
        //we are inserting a string which is updating which is reinserting

        if (viz.getNodeFromPoint(me) != null) {
            DrawableNode nn = viz.getNodeFromPoint(me);
            System.out.println("NN "+nn.getTmpText());
            String tmp = nn.getTmpText();
            viz.setFocussedItem((FocussedItem)nn);

            try {
                viz.getPropertyPane().remove(0, viz.getPropertyPane().getLength());
                viz.getPropertyPane().insertString(0, tmp, new SimpleAttributeSet());
            } catch (BadLocationException be) {
                System.err.println("bad loc1 "+be);
            }
            //	f=nn;
            viz.repaint();

        }



        if (viz.getArcFromPoint(me) != null) {
            DrawableProperty nn = viz.getArcFromPoint(me);
            System.out.println("NN "+nn.getTmpText());
            String tmp = nn.getTmpText();
            viz.setFocussedItem(nn);

            try {
                viz.getPropertyPane().remove(0, viz.getPropertyPane().getLength());
                viz.getPropertyPane().insertString(0, tmp, new SimpleAttributeSet());
            } catch (BadLocationException be) {
                System.err.println("bad loc2 "+be);
            }

            //	f=nn;
            viz.repaint();

        }


    }

    public void mouseEntered(MouseEvent me) {

    }

    public void mouseExited(MouseEvent me) {

    }


    /**

     mousePressed handles the start of dragging

     if a node is mouse-pressed on, the focus is changed to that node
     if an arc is mouse-pressed on, the focus changes to the arc, and the node is
     made the startNode of the arc

     */

    public void mousePressed(MouseEvent me) {

        System.out.println("mouse pressed state is "+viz.getEditState());

        if (viz.getEditState() == viz.NODE_FOCUS) {

            if (viz.getNodeFromPoint(me) != null) {
                viz.setFocussedItem( viz.getNodeFromPoint(me));
            }


        } else if (viz.edit_state == viz.ARC_DRAG) {

            ((DrawableProperty)viz.getFocussedItem()).startX = me.getX();
            ((DrawableProperty)viz.getFocussedItem()).startY = me.getY();
            ((DrawableProperty)viz.getFocussedItem()).endX = me.getX();
            ((DrawableProperty)viz.getFocussedItem()).endY = me.getY();

            if (viz.getNodeFromPoint(me) != null) {
                DrawableNode node = viz.getNodeFromPoint(me);
                ((DrawableProperty)viz.getFocussedItem()).startNode = node;
                ((DrawableProperty)viz.getFocussedItem()).update();
                viz.setEditState(viz.ARC_FOCUS);
                System.out.println("zz 3state "+viz.getEditState());
            } else {
                viz.setEditState(viz.ARC_DRAG);
                //		edit_state=viz.NODE_FOCUS;
                ///arcs.removeElement(f);
                System.out.println("zz 2state "+viz.getEditState());
            }

        }

    }


    /**

     mouseReleased handles the end of drag events

     if the focus is an arc, if the mouseReleased point is a node,
     the node is made the arc's endpoint.
     A new arc is created as the cArc.

     otherwise nothing happens

     */


    public void mouseReleased(MouseEvent me) {
        System.out.println("ok [0]");
        if (viz.getEditState() == viz.ARC_FOCUS) {

            System.out.println("mouse pressed - edit_state is "+viz.ARC_FOCUS);

            ((DrawableProperty)viz.getFocussedItem()).endX = me.getX();
            ((DrawableProperty)viz.getFocussedItem()).endY = me.getY();

            System.out.println("ok [1]");
            if (viz.getNodeFromPoint(me) != null) {

                System.out.println("ok [2]");

                DrawableNode node = viz.getNodeFromPoint(me);

                ((DrawableProperty)viz.getFocussedItem()).endNode = node;
                ((DrawableProperty)viz.getFocussedItem()).update();

                try {
                    DrawableProperty a = new DrawableProperty(viz.defaultURL);
                    viz.getArcs().addElement(a);//??
                    //cArc = a;
                    //		f= cArc;
                    viz.setFocussedItem(a);;
                } catch (RDFException ex) {
                    System.err.println("can't create property "+ex);
                }

                System.out.println("YEP!!!!");
                viz.setEditState(viz.NODE_FOCUS);
            } else {
                System.out.println("ok [3]");

                viz.getArcs().removeElement(viz.getFocussedItem());
                //f = cNode;
                //		state=3;
                viz.setEditState(viz.NODE_FOCUS);

                System.out.println("YNOPE!!!!");
            }

            System.out.println("ok [4]");

            viz.repaint();

        }

    }


}


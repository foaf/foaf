
package org.rdfweb.viz;

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

public class ImportActionListener implements  ActionListener {

Viz viz;

public ImportActionListener(Viz viz){
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

        if (e.getActionCommand().equals("Import file")) {


            FileInputStream in = null;
            ///...


            String inputFileName = "";

            JFileChooser jfc = new JFileChooser();
            jfc.setSize(500, 250);
            //Container parent = openItem.getParent();
            int choice = jfc.showOpenDialog(viz.getFrame());

            if (choice == JFileChooser.APPROVE_OPTION) {

                inputFileName = jfc.getSelectedFile().getAbsolutePath();
                inputFileName = "file://"+inputFileName;


                if (!inputFileName.trim().equals("")) {

                    Model m = new ModelMem();

                    try {

                        m = m.read(inputFileName);

                    } catch (Exception ez) {

                        errorMsg = errorMsg + ez;

                        //System.out.println(errorMsg);

                        JOptionPane.showMessageDialog(null, errorMsg, "alert",
                                JOptionPane.ERROR_MESSAGE);

                    }


                    try {
                        if (m.size() == 0) {
                            JOptionPane.showMessageDialog(null,
                                    errorMsg, "alert",
                                    JOptionPane.ERROR_MESSAGE);
                        } else {

                            JOptionPane.showMessageDialog(null, "url loaded",
                                    "information",
                                    JOptionPane.INFORMATION_MESSAGE);
                            viz.getSchemas().addElement(m);
                            viz.updateMenus(m);
                        }

                    } catch (Exception ii) {

                    }

                }

            }//end if choice


        } else if (e.getActionCommand().equals("Load categories and link-types from Web")) {

            String inputValue = JOptionPane.showInputDialog("Please input a url");

            if (!inputValue.trim().equals("")) {


                Model m = new ModelMem();

                try {

                    m = m.read(inputValue);


                } catch (Exception ez) {
                    errorMsg = errorMsg + ez;

                    //System.out.println(errorMsg);

                    JOptionPane.showMessageDialog(null, errorMsg, "alert",
                            JOptionPane.ERROR_MESSAGE);

                }


                try {
                    if (m.size() == 0) {
                        JOptionPane.showMessageDialog(null, errorMsg, "alert",
                                JOptionPane.ERROR_MESSAGE);
                    } else {

                        JOptionPane.showMessageDialog(null, "url loaded",
                                "information",
                                JOptionPane.INFORMATION_MESSAGE);
                        viz.getSchemas().addElement(m);
                        viz.updateMenus(m);
                    }

                } catch (Exception ii) {

                }

            }//end if not ""

        }

   }

}

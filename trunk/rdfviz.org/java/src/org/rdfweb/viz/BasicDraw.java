///next: store nodes in a model in teir correct form (resource/literal/property)


package org.rdfweb.viz;

import java.io.PrintWriter;
import javax.swing.text.*;
import com.hp.hpl.mesa.rdf.jena.model.*;
import com.hp.hpl.mesa.rdf.jena.mem.*;
import com.hp.hpl.mesa.rdf.jena.common.prettywriter.*;
import com.hp.hpl.mesa.rdf.jena.vocabulary.RDF;
import com.hp.hpl.mesa.rdf.jena.vocabulary.RDFS;

//need to join nodes and arcs
//technique:
//when a property is created, we add it to a node from where it starts using the jena addProperty
//we also need the property to know about this startnode
//when a property is terminated, we tell it about the terminal node
//we make its start and end coordinated references to the coordinates of its nodes

import java.io.*;
import java.net.*;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import javax.swing.event.*;
import java.util.*;
import javax.swing.border.*;

public class BasicDraw extends JComponent implements MouseMotionListener,
MouseListener, ActionListener, DocumentListener {


    static int NODE_FOCUS = 1;
    static int ARC_DRAG = 2;
    static int ARC_FOCUS = 3;

    int edit_state = NODE_FOCUS;

    Vector nodes = new Vector();
    Vector arcs = new Vector();
    Vector schemas = new Vector();

    DrawableNode cNode;
    DrawableProperty cArc;

    String defaultURL = "http://example.com/noproperty";

    int xoffset = 15;
    int yoffset = 60;

    int z = 20;

    int lineoffset = z / 2;

    int sqdi = 10;

    int sqoffset = sqdi / 2;

    FocussedItem f;

    ///frame stuff
    JFrame frame;//main frame
    JMenuBar menubar;//main menu
    JTextPane textPane;//for the node/prop text info
    JMenu menuNodes;
    JMenu menuArcs;

    DefaultStyledDocument lsd;//document

    //model stuff for export
    Model mem;

    /**
    constructor: initalizes the frames, menus, textareas etc

    */

    public BasicDraw() {

        edit_state = NODE_FOCUS;

        mem = new ModelMem();

        Vector nodes = new Vector();
        Vector arcs = new Vector();

        try {
            cNode = new DrawableNode(100, 100);
            cArc = new DrawableProperty(defaultURL);
        } catch (RDFException ex) {
            System.err.println("can't create property "+ex);
        }

        f = cNode;
        nodes.addElement(cNode);
        //??
        arcs.addElement(cArc);

        int xoffset = 15;
        int yoffset = 60;

        int z = 20;


        frame = new JFrame();//main frame
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);



        frame.addWindowListener(new WindowAdapter() {
                    public void windowClosing(WindowEvent e) {
                        System.exit(0);
                    }
                }
                );

        frame.setLocation(200, 200);
        frame.getContentPane().add(this);



        int frameWidth = 900;
        int frameHeight = 500;

        frame.setVisible(true);

        frame.addMouseMotionListener(this);
        frame.addMouseListener(this);

        //menu
        //new: make node a main menu item
        //can then add types of node to the menu.
        //start with anon node

        menubar = new JMenuBar();
        JMenu file = new JMenu("file");
        menubar.add(file);

        menuNodes = new JMenu("nodes");
        menuArcs = new JMenu("arcs");

        menubar.add(menuNodes);
        menubar.add(menuArcs);


        frame.getContentPane().add(menubar, BorderLayout.NORTH);

        JMenuItem node = new JMenuItem("Node");
        JMenuItem arc = new JMenuItem("Arc");
        JMenuItem imu = new JMenuItem("Import url");
        JMenuItem imf = new JMenuItem("Import file");
        JMenuItem export = new JMenuItem("Export");

        menuNodes.add(node);
        node.addActionListener(this);

        menuArcs.add(arc);
        arc.addActionListener(this);

        file.add(imu);
        imu.addActionListener(this);

        file.add(imf);
        imf.addActionListener(this);

        file.add(export);
        export.addActionListener(this);

        lsd = new DefaultStyledDocument();

        //Create the text pane and configure it
        textPane = new JTextPane(lsd);
        lsd.addDocumentListener(this);
        frame.getContentPane().add(textPane, BorderLayout.SOUTH);

        textPane.setVisible(true);
        file.setVisible(true);

        frame.setSize(frameWidth, frameHeight);

        frame.repaint();

    }//end constructor


    /**
    paint method - loops through all nodes and arcs pintint them
    if they are the focussedItem, colours them blue.

    */


    public void paint(Graphics g) {

        Graphics2D g2d = (Graphics2D) g;

        Enumeration e = nodes.elements();
        while (e.hasMoreElements()) {
            DrawableNode n = (DrawableNode) e.nextElement();

            int nodex = n.getX();
            int nodey = n.getY();

            if (n == f) {
                g2d.setColor(Color.blue);
            } else {
                g2d.setColor(Color.black);
            }

            g2d.drawOval(nodex - xoffset, nodey - yoffset, z, z);
            //System.out.println("OVAL: "+(nodex-xoffset)+" "+(nodey-yoffset)+" "+z+" "+z);

        }

        //should not draw line always?

        //g2d.drawLine(cArc.startX-xoffset+lineoffset,cArc.startY-yoffset+lineoffset,cArc.endX-xoffset+lineoffset,cArc.endY-yoffset+lineoffset);

        //int centreX=getCentreArcX(cArc.startX-xoffset+lineoffset,cArc.endX-xoffset+lineoffset);
        //int centreY=getCentreArcY(cArc.startY-yoffset+lineoffset,cArc.endY-yoffset+lineoffset);

        //g2d.drawRect((centreX-sqdi/2), (centreY-sqdi/2), sqdi,sqdi);

        //System.out.println("RECT "+(centreX-sqdi/2)+" "+(centreY-sqdi/2)+" "+sqdi+" "+sqdi);

        //System.out.println("LINE: "+(cArc.startX-xoffset+lineoffset)+" "+(cArc.startY-yoffset+lineoffset)+" "+(cArc.endX-xoffset+lineoffset)+" "+(cArc.endY-yoffset+lineoffset));

        //System.out.println("PROPS "+arcs);

        Enumeration ee = arcs.elements();
        while (ee.hasMoreElements()) {

            DrawableProperty a = (DrawableProperty) ee.nextElement();

            int arcstartx = a.startX;
            int arcstarty = a.startY;
            int arcendx = a.endX;
            int arcendy = a.endY;

            if (a == f) {
                g2d.setColor(Color.blue);
            } else {
                g2d.setColor(Color.black);
            }

            g2d.drawLine(arcstartx - xoffset + lineoffset,
                    arcstarty - yoffset + lineoffset,
                    arcendx - xoffset + lineoffset,
                    arcendy - yoffset + lineoffset);

            int centrexX = getCentreArcX(arcstartx - xoffset + lineoffset,
                    arcendx - xoffset + lineoffset);
            int centreyY = getCentreArcY(arcstarty - yoffset + lineoffset,
                    arcendy - yoffset + lineoffset);

            g2d.drawRect((centrexX - sqdi / 2), (centreyY - sqdi / 2),
                    sqdi, sqdi);

            //System.out.println("RECT "+(centrexX-sqdi/2)+" "+(centreyY-sqdi/2)+" "+sqdi+" "+sqdi);

            //System.out.println("LINE: "+(arcstartx-xoffset+lineoffset)+" "+(arcstarty-yoffset+lineoffset)+" "+(arcendx-xoffset+lineoffset)+" "+(arcendy-yoffset+lineoffset));

        }

    }//end paint method



    /**
    main just creates a Basic Draw object at the moment

    */

    public static void main(String[] args) {

        BasicDraw bb = new BasicDraw();

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
        updateAll();

        if (edit_state == NODE_FOCUS) {//i.e. current focus is a node
            cNode.setX(me.getX());
            cNode.setY(me.getY());
            if (!nodes.contains(cNode)) {
                nodes.addElement(cNode);
            }
        }


        if (edit_state == ARC_FOCUS) {//i.e. curent focus is an arc
            cArc.endX = me.getX();
            cArc.endY = me.getY();
            if (!arcs.contains(cArc)) {
                arcs.addElement(cArc);
                System.out.println("ADDING arc "+arcs);
            }
            ///????
            ///	cArc.update();
        }


        repaint();
    }


    /**

     actionPerformed captures events from the menu.
     Node creates a new node, initialises it, puts it in the nodes vector, and repaints
     Arc creates a new arc, initialises it, puts it in the arcs vector and repaints
     arc also puts the state in state2
     */

    public void actionPerformed(ActionEvent e) {

        //System.out.println("got action");
        String errorMsg = "couldn't load url ";

        if (e.getActionCommand().equals("Node")) {

            if (edit_state == NODE_FOCUS) {//i.e. state is - current focus is a node

                //System.out.println("NEW node: x "+x+" y "+y);

                DrawableNode n = new DrawableNode(100, 100);
                cNode = n;
                nodes.addElement(cNode);
            }
            repaint();

        } else if (e.getActionCommand().equals("Arc")) {
            try {

                //		cArc= new DrawableProperty(defaultURL);
                DrawableProperty p = new DrawableProperty(defaultURL);

                cArc = p;
                arcs.addElement(cArc);

            } catch (RDFException ex) {
                System.err.println("can't create property "+ex);
            }

            edit_state = ARC_DRAG;
            repaint();

        } else if (e.getActionCommand().equals("Import file")) {


            FileInputStream in = null;
            ///...


            String inputFileName = "";

            JFileChooser jfc = new JFileChooser();
            jfc.setSize(500, 250);
            //Container parent = openItem.getParent();
            int choice = jfc.showOpenDialog(frame);

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
                            schemas.addElement(m);
                            updateMenus(m);
                        }

                    } catch (Exception ii) {

                    }

                }

            }//end if choice


        } else if (e.getActionCommand().equals("Import url")) {

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
                        schemas.addElement(m);
                        updateMenus(m);
                    }

                } catch (Exception ii) {

                }

            }//end if not ""

        }


        //so now how do we get this into a jena model?
        //we have strings but need something better.
        //h'm we want a reader

        else if (e.getActionCommand().equals("Export")) {

            try {
                Enumeration en = arcs.elements();
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
        else {

            //pass all remaining commands through??




        }

    }


    /**

     mouseClicked transfers the focus to the clicked object (node or arc) and
     displays the text in the object in the text area

     */

    public void mouseClicked(MouseEvent me) {

        //causing a loop
        //we are inserting a string which is updating which is reinserting

        if (getNodeFromPoint(me) != null) {
            DrawableNode nn = getNodeFromPoint(me);
            System.out.println("NN "+nn.getTmpText());
            String tmp = nn.getTmpText();
            f = nn;

            try {
                lsd.remove(0, lsd.getLength());
                lsd.insertString(0, tmp, new SimpleAttributeSet());
            } catch (BadLocationException be) {
                System.err.println("bad loc1 "+be);
            }
            //	f=nn;
            repaint();

        }



        if (getArcFromPoint(me) != null) {
            DrawableProperty nn = getArcFromPoint(me);
            System.out.println("NN "+nn.getTmpText());
            String tmp = nn.getTmpText();
            f = nn;

            try {
                lsd.remove(0, lsd.getLength());
                lsd.insertString(0, tmp, new SimpleAttributeSet());
            } catch (BadLocationException be) {
                System.err.println("bad loc2 "+be);
            }

            //	f=nn;
            repaint();

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

        System.out.println("mouse pressed state is "+edit_state);

        if (edit_state == NODE_FOCUS) {

            if (getNodeFromPoint(me) != null) {
                cNode = getNodeFromPoint(me);
                f = cNode;
            }


        } else if (edit_state == ARC_DRAG) {

            cArc.startX = me.getX();
            cArc.startY = me.getY();
            cArc.endX = me.getX();
            cArc.endY = me.getY();
            f = cArc;

            if (getNodeFromPoint(me) != null) {
                DrawableNode node = getNodeFromPoint(me);
                cArc.startNode = node;
                cArc.update();
                edit_state = ARC_FOCUS;
                System.out.println("zz 3state "+edit_state);
            } else {
                edit_state = ARC_DRAG;
                //		edit_state=NODE_FOCUS;
                ///arcs.removeElement(f);
                System.out.println("zz 2state "+edit_state);
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
        if (edit_state == ARC_FOCUS) {

            System.out.println("mouse pressed - edit_state is "+ARC_FOCUS);

            cArc.endX = me.getX();
            cArc.endY = me.getY();

            System.out.println("ok [1]");
            if (getNodeFromPoint(me) != null) {

                System.out.println("ok [2]");

                DrawableNode node = getNodeFromPoint(me);

                cArc.endNode = node;
                cArc.update();

                try {
                    DrawableProperty a = new DrawableProperty(defaultURL);
                    arcs.addElement(a);//??
                    cArc = a;
                    //		f= cArc;
                    f = cNode;
                } catch (RDFException ex) {
                    System.err.println("can't create property "+ex);
                }

                System.out.println("YEP!!!!");
                edit_state = NODE_FOCUS;
            } else {
                System.out.println("ok [3]");

                arcs.removeElement(f);
                f = cNode;
                //		state=3;
                edit_state = NODE_FOCUS;

                System.out.println("YNOPE!!!!");
            }

            System.out.println("ok [4]");

            /*

            		try{
            		DrawableProperty a = new DrawableProperty(defaultURL);
            		arcs.addElement(a);//??
            		cArc=a;
            		f= cArc;

            		}catch(RDFException ex){
            		System.err.println("can't create property "+ex);
            		}

            */

            //if(!arcs.contains(cArc)){
            //arcs.addElement(cArc);
            //System.out.println("ADDING arc "+arcs);
            //}

            repaint();

        }

    }


    /**

     getNodeFromPoint takes a mouseEvent point and loops through all the nodes
     returning the first one it finds whose coordinates match the mouseEvent point


     */



    public DrawableNode getNodeFromPoint(MouseEvent me) {

        DrawableNode toReturn = null;
        Enumeration e = nodes.elements();

        while (e.hasMoreElements()) {

            DrawableNode n = (DrawableNode) e.nextElement();
            int nodex = n.getX();
            int nodey = n.getY();

            int mex = me.getX();
            int mey = me.getY();

            int diffx = nodex - mex;
            int diffy = nodey - mey;

            if ((diffx * diffx) + (diffy * diffy) < (z * z)) {
                //in circle

                System.out.println("got match");
                toReturn = n;

            }//end if


        }//end while

        if (toReturn != null) {
            System.out.println("GOT it!");
        } else {
            System.out.println("nope!");
        }

        return toReturn;

    }//end method


    /**

    getArcFromPoint loops through all the arcs and returns the first one
    which has been clicked on wrt the mouseEvent (i.e. in it's central square)

    */

    public DrawableProperty getArcFromPoint(MouseEvent me) {

        DrawableProperty toReturn = null;

        Enumeration e = arcs.elements();

        while (e.hasMoreElements()) {

            DrawableProperty a = (DrawableProperty) e.nextElement();

            int arcstartx = a.startX;
            int arcstarty = a.startY;
            int arcendx = a.endX;
            int arcendy = a.endY;

            //clickable box is a square
            //with its centre as the middle of the line

            int centrex = getCentreArcX(arcstartx - sqoffset,
                    arcendx - sqoffset);
            int centrey = getCentreArcY(arcstarty - sqoffset,
                    arcendy - sqoffset);

            int mex = me.getX();
            int mey = me.getY();

            //System.out.println("MEX "+mex+" MEY "+mey);
            //System.out.println("CX "+centrex+" CY "+centrey);

            int rectTopX = centrex - (sqdi / 2);
            int rectTopY = centrey - (sqdi / 2);


            if ((mex > rectTopX && mex < (rectTopX + sqdi)) &&
                    (mey > rectTopY && mey < (rectTopY + sqdi))) {
                //in the square

                System.out.println("got arc!!");
                toReturn = a;
            }//end if


        }//end while

        if (toReturn != null) {
            System.out.println("GOT it :)");
        } else {
            System.out.println("nope :(");
        }



        return toReturn;

    }//end method


    /**

    gets the central point of two coordinates

    */

    public int getCentreArcX(int arcstartx, int arcendx) {
        return (arcstartx + arcendx) / 2;
    }


    public int getCentreArcY(int arcstarty, int arcendy) {
        return (arcstarty + arcendy) / 2;
    }


    /**

     loops through all arcs, calling update on each one to ensure that all
     the start and end pojnt nodes are fully updates after and during nodes
     are being moved

     */

    public void updateAll() {

        Enumeration e = arcs.elements();
        while (e.hasMoreElements()) {
            DrawableProperty p = (DrawableProperty) e.nextElement();
            p.update();
        }//end while
    }//end method


    /**

    doclistener stuff - updates the text in the focussed item (node or arc)
    when it changes in the text area


    */

    public void changedUpdate(DocumentEvent e) {

        try {
            f.updateText( e.getDocument().getText(0,
                    e.getDocument().getLength()));
            System.out.println("changed "+ e.getDocument().getText(0,
                    e.getDocument().getLength()));
        } catch (BadLocationException bl) {
            System.err.println("text problem "+bl);
        }
    }

    public void insertUpdate(DocumentEvent e) {
        try {
            f.updateText( e.getDocument().getText(0,
                    e.getDocument().getLength()));
            System.out.println("insert"+ e.getDocument().getText(0,
                    e.getDocument().getLength()));
        } catch (BadLocationException bl) {
            System.err.println("text problem "+bl);
        }
    }

    public void removeUpdate(DocumentEvent e) {
        try {
            f.updateText( e.getDocument().getText(0,
                    e.getDocument().getLength()));
            System.out.println("remove"+ e.getDocument().getText(0,
                    e.getDocument().getLength()));
        } catch (BadLocationException bl) {
            System.err.println("text problem "+bl);
        }
    }


    public FocussedItem getFocussedItem() {
        return f;
    }


    public void updateMenus(Model m) throws RDFException {

        //get the nodes

        ResIterator classes =
                m.listSubjectsWithProperty(RDF.type, RDFS.Class);


                //get the arcs

                ResIterator properties =
                        m.listSubjectsWithProperty(RDF.type, RDF.Property);



                //build the menus

        try {

            while (classes.hasNext()) {


                String className = classes.next().toString();
                        /////////

                        menuNodes.add(new JMenuItem(className));

                        ///////////

                    }
        } catch (Exception e1) {

        }


        try {
            while (properties.hasNext()) {


                String propertyName = properties.next().toString();
                /////////

                menuArcs.add(new JMenuItem(propertyName));

                //////
            }

        } catch (Exception e1) {
        }


        //refresh menus
        menubar.repaint();
        frame.repaint();


    }



}


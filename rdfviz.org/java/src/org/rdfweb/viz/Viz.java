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

public class Viz extends JComponent implements DocumentListener, ActionListener {


    public static int NODE_FOCUS = 1;
    public static int ARC_DRAG = 2;
    public static int ARC_FOCUS = 3;

    int edit_state = NODE_FOCUS;

    Vector nodes = new Vector();
    Vector arcs = new Vector();
    Vector literals = new Vector();
    Vector schemas = new Vector();

    DrawableNode cNode;
    DrawableProperty cArc;

    public String defaultURL = "http://example.com/noproperty";

    int xoffset = 15;
    int yoffset = 60;

    int z = 20;

    int lineoffset = z / 2;

    int sqdi = 10;

    int sqoffset = sqdi / 2;

    FocussedItem f;

    ///frame stuff
    JFrame frame;//main frame
    JFrame textFrame;//main frame
    JMenuBar menubar;//main menu
    JTextArea textArea1;//for the node/prop text info
    JTextArea textArea2;//for the node/prop text info
    JTextArea textArea3;//for the node/prop text info
    JMenu menuNodes;
    JMenu menuArcs;

    DefaultStyledDocument lsd1;//document
    DefaultStyledDocument lsd2;//document
    DefaultStyledDocument lsd3;//document

    //model stuff for export
    Model mem;

    /**
    constructor: initalizes the frames, menus, textareas etc

    */

    public Viz() {

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
        textFrame = new JFrame();//main frame

        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        frame.addWindowListener(new WindowAdapter() {
                    public void windowClosing(WindowEvent e) {
                        System.exit(0);
                    }
                }
                );

        textFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        textFrame.addWindowListener(new WindowAdapter() {
                    public void windowClosing(WindowEvent e) {
                        System.exit(0);
                    }
                }
                );

        frame.setLocation(100, 100);
        textFrame.setLocation(400, 100);

        frame.getContentPane().add(this);


        int frameWidth = 900;
        int frameHeight = 500;

        frame.setVisible(true);

        frame.addMouseMotionListener(new VizMouseMotionListener(this));
        frame.addMouseListener(new VizMouseListener(this));

        //menu
        //new: make node a main menu item
        //can then add types of node to the menu.
        //start with anon node

        menubar = new JMenuBar();
        JMenu file = new JMenu("file");
        menubar.add(file);

        menuNodes = new JMenu("items");
        menuArcs = new JMenu("links");
	JMenu menuLiterals = new JMenu("Literals");
        menubar.add(menuNodes);
        menubar.add(menuArcs);
	menubar.add(menuLiterals);

        frame.getContentPane().add(menubar, BorderLayout.NORTH);

        JMenuItem node = new JMenuItem("New node");
        JMenuItem arc = new JMenuItem("New arc");
        JMenuItem imu = new JMenuItem("Load categories and link-types from Web");
        JMenuItem imf = new JMenuItem("Import file");
        JMenuItem export = new JMenuItem("Export");
	JMenuItem quit = new JMenuItem("Quit");
	JMenuItem newLiteralLink = new JMenuItem("New Literal");
	menuLiterals.add(newLiteralLink);

	newLiteralLink.addActionListener(new LiteralActionListener(this));

        menuNodes.add(node);
        node.addActionListener(new NodeActionListener(this));

        menuArcs.add(arc);
        arc.addActionListener(new ArcActionListener(this));

        file.add(imu);
        imu.addActionListener(new ImportActionListener(this));

        file.add(imf);
        imf.addActionListener(new ImportActionListener(this));

        file.add(export);
        export.addActionListener(new ExportActionListener(this));

	file.add(quit);
	quit.addActionListener(this);

        lsd1 = new DefaultStyledDocument();
        //Create the text pane and configure it
        textArea1 = new JTextArea(1,50);
//        textPane1 = new JTextArea(lsd1);
	textArea1.setSize(100,10);
        textArea1.getDocument().addDocumentListener(new VizDocTypeListener(this));
        textArea1.setVisible(true);

        lsd2 = new DefaultStyledDocument();
        //Create the text pane and configure it
        textArea2 = new JTextArea(1,50);
        textArea2.getDocument().addDocumentListener(new VizDocIDListener(this));
        textArea2.setVisible(true);
 
        lsd3 = new DefaultStyledDocument();
        //Create the text pane and configure it
        textArea3 = new JTextArea(1,50);
        textArea3.getDocument().addDocumentListener(new VizDocLabelListener(this));
        textArea3.setVisible(true);
 

///        textFrame.getContentPane().add(textPane, BorderLayout.CENTER);


//////////?????

JLabel textFieldLabel1 = new JLabel("type: ");
JLabel textFieldLabel2 = new JLabel("id: ");
JLabel textFieldLabel3 = new JLabel("label: ");
        
textFieldLabel1.setLabelFor(textArea1);
textFieldLabel2.setLabelFor(textArea2);
textFieldLabel3.setLabelFor(textArea3);
    
JPanel textControlsPane = new JPanel();

GridBagLayout gridbag = new GridBagLayout();
GridBagConstraints c = new GridBagConstraints();
    
textControlsPane.setLayout(gridbag);
textControlsPane.setPreferredSize(new Dimension(500, 50));

//JLabel[] labels = {textFieldLabel1,textFieldLabel2,textFieldLabel3};
JLabel[] labels = {textFieldLabel1,textFieldLabel2,textFieldLabel3};
/////
JTextArea[] textAreas = {textArea1, textArea2, textArea3};
//JTextPane[] textPanes = {textPane1, textPane2, textPane3};

addLabelTextRows(labels, textAreas, gridbag, textControlsPane);


textFrame.getContentPane().add(textControlsPane);

/////////
        file.setVisible(true);

        //frame.setSize(frameWidth, frameHeight);
//        textFrame.setSize(100,50);

this.setPreferredSize(new Dimension(300, 300));
//textPane.setPreferredSize(new Dimension(300, 300));

//        frame.repaint();

    }//end constructor



public void updateNodeTextFrame(DrawableNode n){

JLabel textFieldLabel1 = new JLabel("type: ");
JLabel textFieldLabel2 = new JLabel("id: ");
JLabel textFieldLabel3 = new JLabel("label: ");
        
textFieldLabel1.setLabelFor(textArea1);
textFieldLabel2.setLabelFor(textArea2);
textFieldLabel3.setLabelFor(textArea3);

textArea1.setText(n.getProp("type"));
textArea2.setText(n.getProp("uri"));
textArea3.setText(n.getProp("label"));
    
JPanel textControlsPane = new JPanel();

GridBagLayout gridbag = new GridBagLayout();
GridBagConstraints c = new GridBagConstraints();
    
textControlsPane.setLayout(gridbag);
textControlsPane.setPreferredSize(new Dimension(500, 300));
textControlsPane.setMinimumSize(new Dimension(500, 300));

JLabel[] labels = {textFieldLabel1,textFieldLabel2,textFieldLabel3};
JTextArea[] textAreas = {textArea1, textArea2, textArea3};

addLabelTextRows(labels, textAreas, gridbag, textControlsPane);


        textFrame.setVisible(true);


textFrame.getContentPane().add(textControlsPane);
textFrame.repaint();

}


public void updatePropertyTextFrame(DrawableProperty p){

JLabel textFieldLabel1 = new JLabel("type: ");

textArea1.setText(p.tmpText);        
textFieldLabel1.setLabelFor(textArea1);
    
JPanel textControlsPane = new JPanel();

GridBagLayout gridbag = new GridBagLayout();
GridBagConstraints c = new GridBagConstraints();
    
textControlsPane.setLayout(gridbag);
textControlsPane.setPreferredSize(new Dimension(500, 50));

JLabel[] labels = {textFieldLabel1};
JTextArea[] textAreas = {textArea1};

addLabelTextRows(labels, textAreas, gridbag, textControlsPane);

textFrame.getContentPane().add(textControlsPane);
textFrame.repaint();


}


/**

various get and set methoids for event and action handling classes

*/


public int getEditState(){
return edit_state;
}

public void setEditState(int state){
edit_state=state;
}


public Vector getArcs(){
return arcs;
}

public Vector getNodes(){
return nodes;
}
public Vector getLiterals(){
return literals;
}


public Vector getSchemas(){
return schemas;
}

public JFrame getFrame(){//main frame
return frame;
}

public JFrame getTextFrame(){//main frame
return textFrame;
}

public JTextArea getTypeText(){//properties for focussed node
return textArea1;///???
}

public JTextArea getIDText(){//properties for focussed node
return textArea2;///???
}

public JTextArea getLabelText(){//properties for focussed node
return textArea3;///???
}

    /**
    paint method - loops through all nodes and arcs pintint them
    if they are the focussedItem, colours them blue.

    */


    public void paint(Graphics g) {

        Graphics2D g2d = (Graphics2D) g;
//here's the problem
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

	    if(n.isLiteral()){
            g2d.drawRect(nodex - xoffset, nodey - yoffset, z, z);
	    }else{
            g2d.drawOval(nodex - xoffset, nodey - yoffset, z, z);
            //System.out.println("OVAL: "+(nodex-xoffset)+" "+(nodey-yoffset)+" "+z+" "+z);
	    }
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


int offSX=arcstartx - xoffset + lineoffset;
int offSY=arcstarty - yoffset + lineoffset;
int offEX=arcendx - xoffset + lineoffset;
int offEY=arcendy - yoffset + lineoffset;


//System.out.println("gah "+arcs);
//System.out.println("sx "+arcstartx+" sy "+arcstarty+" ex "+arcendx+" ey "+arcendy);
//System.out.println("sx "+offSX+" sy "+offSY+" ex "+offEX+" ey "+offEY);

//int segX=getSegX(z,offSX,offSY,offEX,offEY);
//int segY=getSegY(z,offSX,offSY,offEX,offEY);
int segX=getSegX(10,offSX,offSY,offEX,offEY);
int segY=getSegY(10,offSX,offSY,offEX,offEY);

double segX2=getSegX(20,offSX,offSY,offEX,offEY);
double segY2=getSegY(20,offSX,offSY,offEX,offEY);

double w=10;
double theta=java.lang.Math.atan2((offEX-offSX), (offEY-offSY));

double segLX=(segX2-w*java.lang.Math.cos(theta));
double segLY=(segY2+w*java.lang.Math.sin(theta));
double segRX=(segX2+w*java.lang.Math.cos(theta));
double segRY=(segY2-w*java.lang.Math.sin(theta));

//g2d.drawLine((int)segLX,(int)segLY,segX, segY);
//g2d.drawLine((int)segRX,(int)segRY,segX, segY);

int[] xs={(int)segLX,(int)segRX,segX};
int[] ys={(int)segLY,(int)segRY,segY};

//g2d.fillPolyline(xs,ys,3);
g2d.fillPolygon(xs,ys,3);

            g2d.drawLine(arcstartx - xoffset + lineoffset,
                    arcstarty - yoffset + lineoffset,
                    arcendx - xoffset + lineoffset,
                    arcendy - yoffset + lineoffset);

//Arrow?



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

gets coords of a point n down from the end of a line
//get length of full line (sqrt(x*x+y*y))
//work out ratio of that under ((sqrt(x*x+y*y))-r=R
//R-r/R * x= x' and *y = y'

*/


public int getSegX(double r, double sx, double sy, double ex, double ey){
//System.out.println("x "+sx+" "+ex);
//System.out.println("y "+sy+" "+ey);

double x=ex - sx;
double y=ey - sy;

//r =50;

//System.out.println("[1]x "+x);
//System.out.println("[1]y "+y);

double R=java.lang.Math.sqrt((x*x)+(y*y));
//System.out.println("R "+R);
//System.out.println("end "+(((R-r)/R)));
//return (int)( sx + (R-r) * x / R );
return (int)(sx+(  (R-r) * x / R) );
//return ex-(((x*2)/2)*ratio)-r;
}

public int getSegY(double r, double sx, double sy, double ex, double ey){

double x=ex - sx;
double y=ey - sy;

//r = 50;

double R=java.lang.Math.sqrt((x*x)+(y*y));
//System.out.println("end [2]"+(((R-r)/R)));
return (int)(sy+ ((R-r) * y / R ));
//return (int)(sy + (R-r) * y / R );
//return ey-(((y*2)/2)*ratio)-r;
}




    /**
    main just creates a Viz object at the moment

    */

    public static void main(String[] args) {

        Viz viz = new Viz();
        viz.getFrame().pack();
        viz.getTextFrame().pack();
        viz.getFrame().setVisible(true);
        viz.getTextFrame().setVisible(true);

    }


    /**

     mouse listeners are now in VizMouseMotionListener and Viz MouseListener

     */


    /**

actionperformed is now in different listeners:
NodeActionlistener (creating new nodes of various sorts)
ArcActionlistener (creating new arcs of various sorts)
ImportActionListener (importing as files or url)
ExportActionListener (exporting as RDF)
FileActionlistner (for quit)

     */



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


public void setFocussedItem(FocussedItem focus){
f=focus;
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

System.out.println("class "+className);

			JMenuItem cl=new JMenuItem(className);
                        menuNodes.add(cl);
		        cl.addActionListener(new NodeActionListener(this));

                        ///////////

                    }
        } catch (Exception e1) {

        }


        try {


            while (properties.hasNext()) {


                String propertyName = properties.next().toString();

System.out.println("props "+propertyName);

                /////////
		JMenuItem pr=new JMenuItem(propertyName);
                menuArcs.add(pr);
		pr.addActionListener(new ArcActionListener(this));

                //////
            }

        } catch (Exception e1) {
        }


        //refresh menus
        menubar.repaint();
        frame.repaint();


    }


   public void actionPerformed(ActionEvent e) {
    
        System.out.println("got action: "+ e.getActionCommand());
     
        if (e.getActionCommand().equals("quit")) {

                        System.exit(0);

	}

   }



/**
    
for laying out text
                
*/                  
private void addLabelTextRows(JLabel[] labels,
                                  JTextArea[] textAreas,
                                  GridBagLayout gridbag,
                                  java.awt.Container container) {
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.EAST;
        int numLabels = labels.length;
    
        for (int i = 0; i < numLabels; i++) {
            c.gridwidth = GridBagConstraints.RELATIVE; //next-to-last
            c.fill = GridBagConstraints.NONE;      //reset to default
            c.weightx = 0.0;                       //reset to default
            gridbag.setConstraints(labels[i], c);
            container.add(labels[i]);
            
            c.gridwidth = GridBagConstraints.REMAINDER;     //end row
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1.0;
            c.weighty = 1.0;
            gridbag.setConstraints(textAreas[i], c);
            container.add(textAreas[i]);
        }
    }         

}


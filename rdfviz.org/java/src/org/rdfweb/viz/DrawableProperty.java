package org.rdfweb.viz;

import com.hp.hpl.mesa.rdf.jena.model.*;
import com.hp.hpl.mesa.rdf.jena.common.*;

public class DrawableProperty extends com.hp.hpl.mesa.rdf.jena.common.PropertyImpl implements FocussedItem {

    String tmpText = "";

    public DrawableProperty(String nameSpace, String localName,
            Model m) throws RDFException{ super(nameSpace, localName, m);
    } public DrawableProperty(String nameSpace, String localName,
            int ordinal,
            Model m) throws RDFException{ super(nameSpace, localName,
            ordinal, m);
    } public DrawableProperty(String nameSpace,
            String localName) throws RDFException {
        super(nameSpace, localName);
    }
    public DrawableProperty(String uri, Model m) throws RDFException {
        super(uri, m);
    }
    public DrawableProperty(String uri) throws RDFException {
        super(uri);
    }


    public void update() {

        //System.out.println("STARTNODE "+startNode);
        //System.out.println("ENDNODE "+endNode);

        if (startNode != null) {
            //System.out.println("STARTNODE "+startNode.toString());
            startX = startNode.getX();
            startY = startNode.getY();
        }

        if (endNode != null) {
            //System.out.println("ENDNODE "+endNode.toString());
            endX = endNode.getX();
            endY = endNode.getY();
        }

    }


    public DrawableNode startNode = null;
    public DrawableNode endNode = null;
    public int startX = 0;
    public int endX = 0;
    public int startY = 0;
    public int endY = 0;

    public void updateText(String s) {
        tmpText = s;
    }

    public String getTmpText() {
        return tmpText;
    }

}


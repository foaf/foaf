package org.rdfweb.viz;

import com.hp.hpl.mesa.rdf.jena.common.*;
import java.awt.*;
import com.hp.hpl.mesa.rdf.jena.model.*;

public class DrawableNode implements RDFNode, FocussedItem {

    String tmpText = "";
    int x = 0;
    int y = 0;
    Color color = Color.black;


    public DrawableNode(int x, int y) {
        this.x = x;
        this.y = y;
    }


    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public int getX() {
        return x;
    }
    public int getY() {
        return y;
    }

    public void setX(int x) {
        this.x = x;
    }
    public void setY(int y) {
        this.y = y;
    }


    public void updateText(String s) {
        tmpText = s;
    }

    public String getTmpText() {
        return tmpText;
    }

}

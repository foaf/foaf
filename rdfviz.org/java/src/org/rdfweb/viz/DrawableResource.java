package org.rdfweb.viz;

import com.hp.hpl.mesa.rdf.jena.common.*;

public class DrawableResource extends ResourceImpl {

    int x = 0;
    int y = 0;


    public DrawableResource(int x, int y) {
        this.x = x;
        this.y = y;
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


}

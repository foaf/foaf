package org.rdfweb.viz;

import com.hp.hpl.mesa.rdf.jena.common.*;
import java.net.*;
import java.util.*;
import java.awt.*;
import com.hp.hpl.mesa.rdf.jena.model.*;

public class DrawableLiteral implements RDFNode, FocussedItem {

    String tmpText = "";
    int x = 0;
    int y = 0;
    Color color = Color.black;
    Hashtable properties;

    public DrawableLiteral(int x, int y) {
        this.x = x;
        this.y = y;
	properties = new Hashtable();
	properties.put("type","");;
	properties.put("ID","");;
	properties.put("label","");;
    }

    public boolean setProperty(String key, String val){
	if(val!=null&&(!val.equals("")) && key!=null && (!key.equals(""))){

	   if(properties.containsKey(key)){
	   properties.remove(key);
	   }

	   properties.put(key,val);
	   return true;
	}
	else{
	   return false;
	}
    }

    public String getProp(String key){

	if(key!=null && (!key.equals(""))){
	return (String)properties.get(key);
	}else{
	return null;
	}

   }

    public boolean setRDFType(String val){
	if(val!=null&&(!val.equals(""))){
	   properties.put("type",val);
	   return true;
	}else{
	   return false;
	}
    }

    public boolean setURI(String val){
	// Oi! Spaz! I'm a l-i-t-e-r-a-l
	return false;
    }

    public boolean setLabel(String val){
	if(val!=null&&(!val.equals(""))){
	   properties.put("label",val);
	   return true;
	}else{
	   return false;
	}
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
    public void updateType(String s) {
	setProperty("type",s);
    }
    public void updateID(String s) {
	setProperty("uri",s);
    }
    public void updateLabel(String s) {
	setProperty("label",s);
    }

    public String getTmpText() {
        return tmpText;
    }

}

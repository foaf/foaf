package org.rdfweb.viz;

import com.hp.hpl.mesa.rdf.jena.model.*;
import com.hp.hpl.mesa.rdf.jena.common.*;
import java.util.*;

public class DrawableProperty extends com.hp.hpl.mesa.rdf.jena.common.PropertyImpl implements FocussedItem {

    String tmpText = "";
    String typeText = "";
    Hashtable properties;

    public DrawableProperty(String nameSpace, String localName,
            Model m) throws RDFException{ super(nameSpace, localName, m);
	properties = new Hashtable();
	properties.put("type","");;
	properties.put("uri","http://example.com/noproperty");;
	properties.put("label","");;

    } public DrawableProperty(String nameSpace, String localName,
            int ordinal,
            Model m) throws RDFException{ super(nameSpace, localName,
            ordinal, m);
	properties = new Hashtable();
	properties.put("type","");;
	properties.put("uri","http://example.com/noproperty");;
	properties.put("label","");;

    } public DrawableProperty(String nameSpace,
            String localName) throws RDFException {
        super(nameSpace, localName);
	properties = new Hashtable();
	properties.put("type","");;
	properties.put("uri","http://example.com/noproperty");;
	properties.put("label","");;
    }
    public DrawableProperty(String uri, Model m) throws RDFException {
        super(uri, m);
	properties = new Hashtable();
	properties.put("type","");;
	properties.put("uri","http://example.com/noproperty");;
	properties.put("label","");;
    }
    public DrawableProperty(String uri) throws RDFException {
        super(uri);
	properties = new Hashtable();
	properties.put("type","");;
	properties.put("uri","http://example.com/noproperty");;
	properties.put("label","");;
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

    public void updateID(String s) {
setProperty("uri",s);
    }

    public void updateType(String s) {
        typeText = s;
setProperty("type",s);
    }
    public void updateLabel(String s) {
setProperty("label",s);
    }

    public String getTmpText() {
        return tmpText;
    }
    public String getTypeText() {
        return typeText;
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


}

/* Decompiled by Mocha from ArcNodeList.class */
/* Originally compiled from ArcNodeList.java */

import com.apple.cocoa.foundation.*;
import java.util.Enumeration;
import java.util.Vector;

public class ArcNodeList
{
    Vector array;
    ModelItem currentObject;

    public ArcNodeList()
    {
        currentObject = null;
        array = new Vector();
    }

    public void add(ModelItem anObject)
    {
        array.add(anObject);
        anObject.setMyList(this);
    }

    public void deleteCurrentObject()
    {
        if (currentObject != null)
        {
            array.removeElement(currentObject);
            currentObject.delete();
            setCurrentObject(null);
        }
    }

    public void removeObject(ModelItem anObject)
    {
        array.removeElement(anObject);
    }

    public void drawModel()
    {
        for (Enumeration enumerator = array.elements(); enumerator.hasMoreElements(); )
        {
            ModelItem anObject = (ModelItem)enumerator.nextElement();
            if (anObject == currentObject)
                anObject.drawHilight();
            else
                anObject.drawNormal();
        }
    }

    public ModelItem objectAtPoint(NSPoint point)
    {
        for (Enumeration enumerator = array.elements(); enumerator.hasMoreElements(); )
        {
            ModelItem anObject = (ModelItem)enumerator.nextElement();
            if (anObject.containsPoint(point))
                return anObject;
        }
        return null;
    }

    public void setCurrentObject(ModelItem anObject)
    {
        currentObject = anObject;
    }

    public ModelItem currentObject()
    {
        return currentObject;
    }
    
    public void showTypes(boolean value)
    {
        for (Enumeration enumerator = array.elements(); enumerator.hasMoreElements(); )
        {
            ModelItem anObject = (ModelItem)enumerator.nextElement();
            if (anObject.isNode())
            {
                ((Node) anObject).setShowType(value);
            }
        }
    }
    
    public void showIds(boolean value)
    {
        for (Enumeration enumerator = array.elements(); enumerator.hasMoreElements(); )
        {
            ModelItem anObject = (ModelItem)enumerator.nextElement();
            if (anObject.isNode())
            {
                ((Node) anObject).setShowId(value);
            }
        }
    }
    
    public void showProperties(boolean value)
    {
        for (Enumeration enumerator = array.elements(); enumerator.hasMoreElements(); )
        {
            ModelItem anObject = (ModelItem)enumerator.nextElement();
            if (!anObject.isNode())
            {
                ((Arc) anObject).setShowProperty(value);
            }
        }
    }
}

package org.desire.rudolf.rdf;

import java.util.Date;
import java.util.Vector;
import java.util.Calendar;
import java.util.Random;
import java.text.SimpleDateFormat;

/**
 * <p>
 * An RDF Triple.
 * </p>
 *
 * <pre>
 * $Log: not supported by cvs2svn $
 * Revision 1.3  2002/02/22 16:05:46  cmlm
 * er cvs gone funny
 *
 * Revision 1.2  2002/02/21 16:49:10  cmlm
 * major rewrite of QE.java, tidying of PQ2SQL.java. Parser needs lots more work.
 * see explanations in PQ2SQL and QE.
 * tests to follow.
 *
 * Revision 1.1.1.1  2002/01/24 17:37:01  cmlm
 * Imported sources
 *
 * Revision 1.4  2001/09/02 20:18:20  ccslrd
 * Various minor tweaks.
 *
 * Revision 1.3  2001/07/27 14:47:20  ccslrd
 * Tidied up javadoc, added author and version keywords
 *
 * Revision 1.2  2001/07/27 14:10:30  ccslrd
 * Refactored constructors to remove repeated code. Applied ExtractMethod to
 * produce generateTripleId, generateId, etc. Sorted methods into rough alphabetic
 * order. Deprecated get() and set() in favour of explicit accessor/mutator
 * methods. Added hasChild() convenience method.
 *
 * Revision 1.1.1.1  2001/07/27 11:04:52  ccslrd
 * Imported Sources, 0.50+fixes
 * </pre>
 *
 * @author Libby Miller
 * @version $Revision: 1.1 $*/
public class Triple
{
    public static final String TRIPLE_ID_QUALIFIER = "http://rdf.desire.org/rudolf/";
	public static final String DEFAULT_LOCUTOR = "unknown";
    public static final String DATE_STRING_FORMAT = "MM/dd/yy";
    public static final String TIME_STRING_FORMAT = "HH:mm";

    private boolean hasParent;
    private Triple parent;
    private Vector queries;
    private Triple resultsParent;
    private boolean allowsChildren;
    private String id;
    private String subject;

    private String predicate;
    private String object;
    private String subjectBinding;
    private String predicateBinding;
    private String objectBinding;
    private boolean isResource;

    private String locutor;
    private String dateString;
    private String timeString;
    private String tripleId;
    private Vector children;

    public static String generateTripleId()
    {
		return TRIPLE_ID_QUALIFIER + generateRandomLong().toString();
    }

    public static String generateId(String subject, String predicate, String object)
    {
		return subject + " " + predicate + " " + object;
    }

    private static Long generateRandomLong()
    {
        return new Long( Math.abs( new Random().nextLong() ) );
    }

    /**
     * @param id		Event identifier for the triple
     * @param locutor
     * @param subject	Subject of triple
     * @param predicate Predicate of triple
     * @param object	Object of triple
     * @param isResource Indicates whether Triple is a resource
     */
    public Triple( String id, String locutor, String subject, String predicate,
        String object, boolean isResource )
        {
            this.id = id;
            this.subject = subject;
            this.predicate = predicate;
            this.object = object;
            this.locutor = locutor;
            this.isResource = isResource;

            allowsChildren = true;
            hasParent = false;
            children = new Vector();
            queries = new Vector();

            setDateTime( Calendar.getInstance().getTime() );
            tripleId = generateTripleId();

    }

    public Triple( String id, String locutor, String subject, String predicate,
        String object )
        {
            this(id, locutor, subject, predicate, object, false);
    }

    public Triple( String subject, String predicate, String object )
    {

        this( generateId(subject, predicate, object),
              DEFAULT_LOCUTOR,
              subject,
              predicate,
              object,
              false);
    }

    public Triple( String subject, String predicate, String object,
        boolean isResource )
        {
            this(generateId(subject, predicate, object),
              	 DEFAULT_LOCUTOR,
                 subject,
                 predicate,
                 object,
              	 isResource);
    }

    /**
     * Add a child to this triple. The childs setParent methods will be
     * invoked to register this triple as its (new) parent.
     *
     * @param the new child
     */
    public void add( Triple child )
    {
        children.addElement( child );
        child.setParent( this );
    }

    /**
     * Returns the id of the triple
     */
    public String getEventId()
    {
        return id;
    }

    public String getSubject()
    {
        return subject;
    }

    public String getPredicate()
    {
        return predicate;
    }

    public String getObject()
    {
        return object;
    }

    public String getDate()
    {
        return dateString;
    }

    public String getTime()
    {
        return timeString;
    }

    public String getTripleId()
    {
        return tripleId;
    }

    public String getLocutor()
    {
        return locutor;
    }

    /**
     * @deprecated use the explicit accessor methods.
     */
    public String get( String spo )
    {
        String toreturn = null;
        if ( spo.equals( "subject" ) )
        {
            toreturn = subject;
        } //should be getsubject?
        if ( spo.equals( "predicate" ) )
        {
            toreturn = predicate;
        }
        if ( spo.equals( "object" ) )
        {
            toreturn = object;
        }
        return toreturn;
    }

    public String getObjectBinding()
    {
        return objectBinding;
    }

    /**
     * Utility method the returns a String array contains predicate, subject, object clause.
     */
	public String[] getClause()
    {
        return new String[] {getPredicate(), getSubject(), getObject()};
    }


	public Vector getChildren()
    {
        return children;
    }

    /**
     * Returns the number of levels above this node. Its depth in the
     * parent-child hierarchy. A triple with no parent is at level 0.
     * A triple with a parent is at level 1, etc.
     */
    public int getLevel()
    {
        Triple node = this;
        int level = 0;
        if ( node.parent == null )
        {
            return level;
        }
        else
        {
            while ( node.parent != null )
            {
                node = node.parent;
                level++;
            }
        }
        return level;
    }

    public Vector getUncles()
    {
        Triple t = this.getParent();
        Triple ta = null;
        if ( t != null )
        {
            ta = this.getParent().getParent();
        }
        if ( ta != null )
        {
            return ta.children;
        }
        else
        {
            return new Vector();
        }
    }

	public Triple getParent()
    {
        return parent;
    }

	public Vector getQueries()
    {
        return queries;
    }

    public String getPredicateBinding()
    {
        return predicateBinding;
    }

    public String getSubjectBinding()
    {
        return subjectBinding;
    }

    /**
     * @return true if the provided triple is a direct child of this triple
     */
    public boolean hasChild(Triple child)
    {
        return children.contains(child);
    }

    public boolean isResource()
    {
        return isResource;
    }
    /**
     * @deprecated use the explicit mutator methods.
     */
    public void set( String spo, String value )
    {
        if ( spo.equals( "subject" ) )
        {
            subject = value;
        }
        if ( spo.equals( "predicate" ) )
        {
            predicate = value;
        }
        if ( spo.equals( "object" ) )
        {
            object = value;
        }
    }

    public void setDateTime( Date date )
    {
        dateString = new SimpleDateFormat(DATE_STRING_FORMAT).format(date);
        timeString = new SimpleDateFormat(TIME_STRING_FORMAT).format(date);
    }

    /**
     * @deprecated use setDateTime which has the same effect
     */
    public void setDate( Date d )
    {
        setDateTime( d );
    }


    public void setAllowsChildren( boolean boo )
    {
        allowsChildren = boo;
    }

    /**
     * Sets the subject. Strings are automatically trimmed.
     */
	public void setSubject(String subject)
    {
        this.subject = (subject == null ? null : subject.trim());
    }

    /**
     * Sets the predicate. Strings are automatically trimmed.
     */
    public void setPredicate(String predicate)
    {
        this.predicate = (predicate == null ? null : predicate.trim());
    }

    /**
     * Sets the object. Strings are automatically trimmed.
     */
    public void setObject(String object)
    {
        this.object = (object == null ? null : object.trim() );
    }


    /**
     * sets all the bindings
     */
    public void setAllBindings(String subBind, String predBind, String
	objBind)
    {
        subjectBinding = (subBind == null ? null : subBind.trim() );
        predicateBinding = (predBind == null ? null : predBind.trim() );
        objectBinding = (objBind == null ? null : objBind.trim() );
    }


    /**
     * Sets the subject binding. Strings are automatically trimmed.
     */
    public void setSubjectBinding(String binding)
    {
        subjectBinding = (binding == null ? null : binding.trim() );
    }

    /**
     * Sets the predicate binding. Strings are automatically trimmed.
     */
    public void setPredicateBinding(String binding)
    {
        predicateBinding = (binding == null ? null : binding.trim() );
    }

    /**
     * Sets the object binding. Strings are automatically trimmed.
     */
    public void setObjectBinding(String binding)
    {
    	objectBinding = (binding == null ? null : binding.trim() );
    }

    public void setParent(Triple parent)
    {
        this.parent = parent;
    }

    public static void main( String[] args )
    {
        Triple trip = new Triple( "1111", "libby", "http://www.bized.ac.uk",
            "DC:SUBJECT", "Economics" );
        System.out.println( trip );
    }

    public String toString()
    {
		return getLocutor() + " says " + getSubject() + " " + getPredicate() + " " +
               getObject() + " at " + getTime() + " " + getDate() + " event id is " +
               getEventId() + " triple id is " + getTripleId();
    }


public void removeQMarks(){

String sb = subjectBinding;
String pb = predicateBinding;
String ob = objectBinding;

if(sb!=null && (!sb.equals("")) && sb.startsWith("?")){
subjectBinding=sb.substring(1);
}
if(pb!=null && (!pb.equals("")) && pb.startsWith("?")){
predicateBinding=pb.substring(1);
}
if(ob!=null && (!ob.equals("")) && ob.startsWith("?")){
objectBinding=ob.substring(1);
}


}

}


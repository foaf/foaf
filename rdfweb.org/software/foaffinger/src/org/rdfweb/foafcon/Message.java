/*
 * Created on Aug 15, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.rdfweb.foafcon;

/**
 * @author pldms
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Message
{
    int getIndex;
    String message;
    
    public Message(String message)
    {
        this.message = message;
    }
    
    public Message()
    {
    }
    
    public String getMessage()
    {
        return message;
    }
    
    	public Person getPerson()
    	{
    	    return null;
    	}
    
    	public int getIndex()
    	{
    	    return -1;
    	}
    	
    public static class PersonMessage extends Message
    {
        Person person;

        int index;

        public PersonMessage(Person person, int index)
        {
            super();
            this.person = person;
            this.index = index;
        }
        
        public Person getPerson()
        {
            return person;
        }
     
        public int getIndex()
        {
            return index;
        }

        public String getMessage()
        {
            return "You shouldn't see this: " + person;
        }
    }

    public static class PersonOnline extends PersonMessage
    {
        public PersonOnline(Person person, int index)
        {
            super(person, index);
        }

        public String getMessage()
        {
            return "[" + index + "] " + person.getName() + " is online.";
        }
    }

    public static class PersonOffline extends PersonMessage
    {
        public PersonOffline(Person person, int index)
        {
            super(person, index);
        }

        public String getMessage()
        {
            return "[" + index + "] " + person.getName() + " is offline.";
        }
    }
}
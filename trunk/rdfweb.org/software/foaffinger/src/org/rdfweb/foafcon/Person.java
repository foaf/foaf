package org.rdfweb.foafcon;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.ArrayList;

public class Person
{
  final static String NAME = "n";
  final static String HOMEPAGE = "h";
  final static String INTEREST = "i";
  
  String name;
  String mbox;
  String mboxHash;
  String plan;
  String homepage;
  String interest;

  ArrayList knows;
  
  public Person(String name,
		String mbox,
		String homepage)
  {
    setName(name);
    setMbox(mbox);
    setHomepage(homepage);

    knows = new ArrayList();
  }

  public Person(String name,
		String mboxHash,
		String homepage,
		String interest)
  {
    setName(name);
    setMboxHash(mboxHash);
    setHomepage(homepage);
    setInterest(interest);

    knows = new ArrayList();
  }
  
  public void setName(String name)
  {
    this.name = name;
  }

  public String getName()
  {
    return name;
  }
  
  public void setMbox(String mbox)
  {
    this.mbox = mbox;
    setMboxHash(sha1Hash(mbox));
  }

  public String getMbox()
  {
    return mbox;
  }

  public void setMboxHash(String mboxHash)
  {
    this.mboxHash = mboxHash;
  }
  
  public String getMboxHash()
  {
    return mboxHash;
  }

  public void setHomepage(String homepage)
  {
    this.homepage = homepage;
  }

  public String getHomepage()
  {
    return homepage;
  }

  public void setInterest(String interest)
  {
    this.interest = interest;
  }

  public String getInterest()
  {
    return interest;
  }
  
  public void setPlan(String plan)
  {
    this.plan = plan;
  }

  public String getPlan()
  {
    return plan;
  }

  public synchronized void addKnows(Person person)
  {
    knows.add(person);
  }
  
  public Hashtable getProps()
  {
    Hashtable toReturn = new Hashtable();

    toReturn.put(NAME, this.getName());
    if (this.getHomepage() != null)
      toReturn.put(HOMEPAGE, this.getHomepage());
    if (this.getInterest() != null)
      toReturn.put(INTEREST, this.getInterest());

    return toReturn;
  }

  public String toString()
  {
    return toString("");
  }
  
  public synchronized String toString(String indent)
  {
    String toReturn =
      indent + "Name: " + name + "\n" +
      indent + "Hash Mbox: " + mboxHash + "\n";

    if (mbox != null)
      toReturn += indent + "Mbox: " + mbox + "\n";
    if (homepage != null)
      toReturn += indent + "Homepage: " + homepage + "\n";
    if (interest != null)
      toReturn += indent + "Interest: " + interest + "\n";
    if (plan != null)
      toReturn += indent + "Plan: " + plan + "\n";

    for (Iterator i = knows.iterator(); i.hasNext();)
      {
	Person person = (Person) i.next();

	toReturn += indent + "Knows:\n";
	toReturn += person.toString("\t");
      }

    return toReturn;
  }

  public String toRDF()
  {
    String toReturn = "<?xml version=\"1.0\"?>\n";

    toReturn += "<rdf:RDF xmlns=\"http://xmlns.com/foaf/0.1/\"\n";
    toReturn += "         xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n";
    toReturn += ">\n\n";

    toReturn += this.toRDF("    ","");

    toReturn += "</rdf:RDF>";

    return toReturn;
  }

  public String toRDF(String indent, String totalIndent)
  {
    String toReturn = totalIndent + "<Person>\n";

    toReturn += totalIndent + indent +
      "<name>" + Util.escape(name) + "</name>\n";
    
    toReturn += totalIndent + indent +
      "<mbox_sha1sum>" + Util.escape(mboxHash)
      + "</mbox_sha1sum>\n";

    if (mbox != null)
      toReturn += totalIndent + indent +
	"<mbox rdf:resource=\"" + Util.escape(mbox) + "\"/>\n";
    
    if (interest != null)
      toReturn += totalIndent + indent +
	"<interest rdf:resource=\"" + Util.escape(interest)
	+ "\"/>\n";

    if (homepage != null)
      toReturn += totalIndent + indent +
	"<homepage rdf:resource=\"" + Util.escape(homepage)
	+ "\"/>\n";

    if (plan != null)
      toReturn += totalIndent + indent +
	"<plan>" + Util.escape(plan) + "</plan>\n";

    for (Iterator i = knows.iterator(); i.hasNext();)
      {
	Person person = (Person) i.next();

	toReturn += totalIndent + indent +
	  "<knows>\n";
	
	toReturn += person.toRDF(indent, totalIndent + indent);

	toReturn += totalIndent + indent +
	  "</knows>\n";
      }

    toReturn += totalIndent + "</Person>\n";
    
    return toReturn;
  }
  
  private static String sha1Hash(String string)
  {
    try
      {
	MessageDigest md = MessageDigest.getInstance("SHA");
	
	md.update(string.getBytes());
	
	byte[] digest = md.digest();
	
	BigInteger integer = new BigInteger(1, digest);

	return integer.toString(16);
      }
    catch (Exception e)
      {
	return null;
      }
  }
}


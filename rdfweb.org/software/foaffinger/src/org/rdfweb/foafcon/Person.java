package org.rdfweb.foafcon;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.ArrayList;
import java.io.PrintStream;

public class Person
{
  final static String NAME = "n";
  final static String HOMEPAGE = "h";
  final static String INTEREST = "i";
  final static String SEEALSO = "s";

  String name;
  String mbox;
  String mboxHash;
  String plan;
  String homepage;
  String interest;
  String seeAlso;

  String host;
  int port;
  
  boolean showMbox = false;
  
  ArrayList knows;
  
  public Person(String name,
		String mbox)
  {
    setName(name);
    setMbox(mbox);

    knows = new ArrayList();
  }

  public Person(String mboxHash)
  {
    this(null, mboxHash, null, null, null);
  }
  
  public Person(String name,
		String mboxHash,
		String homepage,
		String interest,
		String seeAlso)
  {
    setName(name);
    setMboxHash(mboxHash);
	
    setHomepage(homepage);
    setInterest(interest);
    setSeeAlso(seeAlso);
    
    knows = new ArrayList();
  }

  public void setFromPerson(Person person)
  {
    //System.out.println(person);
    
    setName(person.getName());
    setMboxHash(person.getMboxHash());
    setHomepage(person.getHomepage());
    setInterest(person.getInterest());
    setSeeAlso(person.getSeeAlso());
  }

  public void setHostPort(String host, int port)
  {
    this.host = host;
    this.port = port;
  }

  public String getHost()
  {
    return host;
  }
  
  public int getPort()
  {
    return port;
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
    setMboxHash(Util.sha1Hash("mailto:" + mbox));
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

  public void setShowMbox(boolean showMbox)
  {
    this.showMbox = showMbox;
  }
    
  public void setHomepage(String homepage)
  {
    this.homepage = homepage;
  }

  public String getHomepage()
  {
    return homepage;
  }

  public void setSeeAlso(String seeAlso)
  {
    this.seeAlso = seeAlso;
  }

  public String getSeeAlso()
  {
    return seeAlso;
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
    if (this.getSeeAlso() != null)
      toReturn.put(SEEALSO, this.getSeeAlso());

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

    if (showMbox && (mbox != null))
      toReturn += indent + "Mbox: " + mbox + "\n";
    if (homepage != null)
      toReturn += indent + "Homepage: " + homepage + "\n";
    if (interest != null)
      toReturn += indent + "Interest: " + interest + "\n";
    if (seeAlso != null)
      toReturn += indent + "See Also: " + seeAlso + "\n";
    if (plan != null)
      toReturn += indent + "Plan: " + "\n" + plan + "\n";
    
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
    toReturn += "         xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\"\n";
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

    if (showMbox && (mbox != null))
      toReturn += totalIndent + indent +
	"<mbox rdf:resource=\"mailto:" + Util.escape(mbox) + "\"/>\n";
    
    if (interest != null)
      toReturn += totalIndent + indent +
	"<interest rdf:resource=\"" + Util.escape(interest)
	+ "\"/>\n";

    if (homepage != null)
      toReturn += totalIndent + indent +
	"<homepage rdf:resource=\"" + Util.escape(homepage)
	+ "\"/>\n";

    if (seeAlso != null)
      toReturn += totalIndent + indent +
	"<rdfs:seeAlso rdf:resource=\"" + Util.escape(seeAlso)
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

  public boolean equals(Object obj)
  {
    if (!(obj instanceof Person))
      return false;

    return mboxHash.equals(((Person) obj).getMboxHash());
  }

  public boolean matches(String match)
  {
    if ((name != null) && (name.indexOf(match) != -1))
      return true;

    if ((homepage != null) && (homepage.indexOf(match) != -1))
      return true;

    if ((interest != null) && (interest.indexOf(match) != -1))
      return true;

    return false;
  }

  public void printLineSummary(PrintStream out)
  {
    out.print(name + "\t");

    if (homepage != null)
      out.print(homepage + "\t");
    else
      out.print("---\t\t");
    
    if (interest != null)
      out.print(interest);
    else
      out.print("---");
  }
  
}


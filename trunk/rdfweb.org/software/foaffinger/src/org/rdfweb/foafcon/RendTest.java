package org.rdfweb.foafcon;

import java.net.InetAddress;
import java.util.*;
import java.io.*;
import java.net.*;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;

public class RendTest
{
  final static String type = "_foafcon._tcp.local.";
  Person person;
  int port = 7654;
  RendListener listener;
  JmDNS rv;
  InetAddress inetaddr;
  ServiceInfo si;
  CommandLine cl;

  boolean shuttingDown = false;
    
  People people;
    
  public static void main(String[] args)
  {
    RendTest rt = new RendTest(args);

    rt.start();
  }

  public RendTest(String[] args)
  {
    if (args.length < 2)
      {
	System.
	  err.
	  println("Usage: RendTest <name> <mailbox> " +
		  "[<address>]");
	System.exit(1);
      }

    person = new Person(args[0], args[1]);

    people = new People(this);
        
    if (args.length > 2)
      {
	try
	  {
	    inetaddr = InetAddress.getByName(args[2]);
	  }
	catch (UnknownHostException e)
	  {
	    System.err.println("Problem with the address: " +
			       e.getMessage());
	    System.exit(1);
	  }
      }
    else
      {
	try
	  {
	    inetaddr = InetAddress.getLocalHost();
	    if (inetaddr.equals(InetAddress.getByName("127.0.0.1")))
	      {
		System.err.println("I can't determine the address of this " +
				   "host.\nHelp me out by supplying it as " +
				   "an argument when you run me.");
		System.exit(1);
	      }
	  }
	catch (UnknownHostException e)
	  {
	    System.err.println("Unknown host: " + e.getMessage());
	    System.exit(1);
	  }
      }

  }

  public void start()
  {
    Hashtable props = person.getProps();
    String hashBox = person.getMboxHash();
    
    listener = new RendListener(this);
    
    try
      {
	cl = new CommandLine(this);
	
	rv = new JmDNS(inetaddr);

	showMessage("Binding to: " + inetaddr);

	si = new ServiceInfo(type,
			     hashBox + "." + type,
			     port,
			     0,
			     0,
			     props);
	
	rv.registerService(si);
	
	rv.addServiceListener(type, listener);

	HttpServer httpServer =
	  new HttpServer(port, new HTTPTestHandler(person), this);

	httpServer.start();

	cl.run();
      }
    catch (Exception e)
      {
	System.err.println(e);

	System.exit(1);
      }
    
  }

  public void exit()
  {
    System.exit(0);
  }
    
  public void kickService()
    throws Exception
  {
    rv.unregisterService(si);

    Hashtable props = person.getProps();
    String hashBox = person.getMboxHash();

    si = new ServiceInfo(type,
			 hashBox + "." + type,
			 port,
			 0,
			 0,
			 props);

    rv.registerService(si);
  }

  public People getPeople()
  {
    return people;
  }

  public Person getPerson()
  {
    return person;
  }

  public Person getPerson(int index)
  {
    return people.get(index);
  }
  
  public boolean personOnline(int index)
  {
    return people.isOnline(index);
  }

  public int numberOfPeople()
  {
    return people.size();
  }

  public void find(String term)
  {
    people.find(term);
  }
    
  public void addPerson(ServiceInfo info)
  {
    people.add(info);
  }
  
  public void removePerson(String mboxHash)
  {
    people.remove(mboxHash);
  }
  
  public void showMessage(String message)
  {
    cl.addMessage(message);
  }

}

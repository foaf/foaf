package org.rdfweb.foafcon;

import java.net.InetAddress;
import java.util.*;
import java.io.*;
import java.net.*;

import com.strangeberry.rendezvous.Rendezvous;
import com.strangeberry.rendezvous.ServiceInfo;
import com.strangeberry.rendezvous.ServiceListener;

public class RendTest
{
  final static String type = "_foafcon._tcp.local.";
  List people;
  Person person;
  int port = 7654;
  TestListener listener;
  Rendezvous rv;
  InetAddress inetaddr;
  ServiceInfo si;
  CommandLine cl;
  
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
	    System.err.println("Uh-oh: " + e.getMessage());
	    System.exit(1);
	  }
      }

  }

  public void start()
  {
    Hashtable props = person.getProps();
    String hashBox = person.getMboxHash();
    
    listener = new TestListener(this);
    
    try
      {
	cl = new CommandLine(this);
	
	rv = new Rendezvous();

	showMessage("Binding to: " + inetaddr);

	si = new ServiceInfo(type,
			     hashBox + "." + type,
			     inetaddr,
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
    
    //System.out.println("You are: \n" + person + "\n");
    
  }

  public void kickService()
    throws Exception
  {
    rv.unregisterService(si);
    Hashtable props = person.getProps();
    String hashBox = person.getMboxHash();
    si = new ServiceInfo(type,
			 hashBox + "." + type,
			 inetaddr,
			 port,
			 0,
			 0,
			 props);
    
    rv.registerService(si);
  }
  
  public void findPeople()
  {
    people = listener.getPeople();
  }

  public List getPeople()
  {
    return people;
  }

  public Person getPerson()
  {
    return person;
  }

  public void showMessage(String message)
  {
    cl.addMessage(message);
  }
  
}

class TestListener implements ServiceListener
{
  List people;
  RendTest controller;
    
  public TestListener(RendTest controller)
  {
    this.controller = controller;
    
    people = new ArrayList();
  }
  
  public void addService(Rendezvous rendezvous, String type, String name)
  {
    controller.showMessage
      ("ADD: " + name);

    people.add(rendezvous.getServiceInfo(type,name));
  }

  public void removeService(Rendezvous rendezvous, String type, String name)
  {
    System.out.println("hi");
    
    
    controller.showMessage("REMOVE: " + name);

    System.out.println("hello");
    
    
    people.remove(rendezvous.getServiceInfo(type,name));
  }

  public synchronized List getPeople()
  {
    return new ArrayList(people);
  }
  
}
    

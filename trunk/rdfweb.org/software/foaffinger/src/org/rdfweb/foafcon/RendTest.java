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
  
  public static void main(String[] args)
  {
    RendTest rt = new RendTest(args);

    rt.start();

    CommandLine cl = new CommandLine(rt);

    cl.run();
  }

  public RendTest(String[] args)
  {
    if (args.length < 2)
      {
	System.
	  err.
	  println("Usage: RendTest <name> <mailbox> " +
		  "[<port>]");
	System.exit(1);
      }

    person = new Person(args[0], args[1]);

    if (args.length > 2)
      {
	try
	  {
	    port = Integer.parseInt(args[2]);
	  }
	catch (NumberFormatException e)
	  {
	    System.err.println("Port must be an integer.");
	    System.exit(1);
	  }
      }

  }

  public void start()
  {
    Hashtable props = person.getProps();
    String hashBox = person.getMboxHash();
    
    listener = new TestListener();
    
    try
      {
	rv = new Rendezvous();

	inetaddr = InetAddress.getLocalHost();
	
	System.out.println("Binding to: " + inetaddr);

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
	  new HttpServer(port, new HTTPTestHandler(person));

	httpServer.start();
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
  
    
}

class TestListener implements ServiceListener
{
  List people;
  
  public TestListener()
  {
    people = new ArrayList();
  }
  
  public void addService(Rendezvous rendezvous, String type, String name)
  {
    //System.out.println("ADD: " + rendezvous.getServiceInfo(type, name));

    people.add(rendezvous.getServiceInfo(type,name));
  }

  public void removeService(Rendezvous rendezvous, String type, String name)
  {
    //System.out.println("REMOVE: " + name);

    people.remove(rendezvous.getServiceInfo(type,name));
  }

  public synchronized List getPeople()
  {
    return new ArrayList(people);
  }
  
}
    

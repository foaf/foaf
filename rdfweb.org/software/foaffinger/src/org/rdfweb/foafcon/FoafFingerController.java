package org.rdfweb.foafcon;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Hashtable;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;

public class FoafFingerController
{
  final static String type = "_foafcon._tcp.local.";
  Person person;
  int port = 7654;
  RendListener listener;
  JmDNS rv;
  InetAddress inetaddr;
  ServiceInfo si;
  UI cl;
    
    boolean gui;

    //boolean shuttingDown = false;
    
  People people;
    
  public static void main(String[] args)
  {
    FoafFingerController rt = new FoafFingerController(args);

    rt.start();
  }

  public FoafFingerController(String[] args)
  {
    if (args.length < 2)
      {
	System.
	  err.
	  println("Usage: FoafFinger --gui|--cl <name> <mailbox> " +
		  "[<address>]");
	System.exit(1);
      }
    
    if (args[0].equals("--gui")) gui = true;
    else if (args[0].equals("--cl")) gui = false;
    else
	{
	    System.
		err.
		println("Usage: FoafFinger --gui | --cl <name> <mailbox> " +
			"[<address>]");
	    System.exit(1);
	}

    person = new Person(args[1], args[2]);

    people = new People(this);
        
    if (args.length > 3)
      {
	try
	  {
	    inetaddr = InetAddress.getByName(args[3]);
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
	  if (gui) cl = new GUI(this);
	  else cl = new CommandLine(this);
	
	rv = new JmDNS(inetaddr);

	showMessage(new Message("Binding to: " + inetaddr));

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
	
	System.exit(0);
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
    
    public boolean personOnline(Person person)
    {
	return people.isOnline(person);
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

  public void addPerson(String mboxHash)
  {
    people.add(mboxHash);
  }
    
  public void removePerson(String mboxHash)
  {
    people.remove(mboxHash);
  }
  
  public void showMessage(Message message)
  {
    cl.addMessage(message);
  }

}

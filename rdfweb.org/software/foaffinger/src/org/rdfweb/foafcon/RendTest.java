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
  static String type = "_foafcon._tcp.local.";
  
  public static void main(String[] args)
  {
    if (args.length < 3)
      {
	System.
	  err.
	  println("Usage: RendTest <name> <mailbox> <homepage> " +
		  "[<port> [<interest> [<plan>]]]");
	System.exit(1);
      }

    Person person = new Person(args[0], args[1], args[2]);

    int port = 7645;
        
    if (args.length > 3)
      {
	try
	  {
	    port = Integer.parseInt(args[3]);
	  }
	catch (NumberFormatException e)
	  {
	    System.err.println("Port must be an integer.");
	    System.exit(1);
	  }
      }

    if (args.length > 4)
      {
	person.setInterest(args[4]);
      }

    if (args.length > 5)
      {
	person.setPlan(args[5]);
      }
    
    Hashtable props = person.getProps();
    String hashBox = person.getMboxHash();
    
    TestListener listener = new TestListener();
    
    try
      {
	Rendezvous rv = new Rendezvous();

	InetAddress inetaddr = InetAddress.getLocalHost();
	
	System.out.println("Binding to: " + inetaddr);
	
	rv.registerService(new ServiceInfo(type,
					   hashBox + "." + type,
					   inetaddr,
					   port,
					   0,
					   0,
					   props)
			   );
	
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
    
    System.out.println("You are: \n" + person + "\n");
    
    Reader reader = new InputStreamReader(System.in);
    BufferedReader bufReader = new BufferedReader(reader);
    String input = null;
    List people = null;
    
    while (true)
      {
	try
	  {
	    System.out.print("> ");
	    
	    input = bufReader.readLine().trim();
	  }
	catch (Exception e)
	  {
	  }
	
	if (input.equals("exit"))
	  break;
	
	else if (input.equals("find"))
	  {
	    people = listener.getPeople();
	    
	    int n = 1;
	    
	    for (Iterator i = people.iterator();
		 i.hasNext();
		 n++)
	      {
		ServiceInfo info = (ServiceInfo) i.next();
		
		System.out.print("[" + n + "] ");
		
		System.out.
		  print(info.getPropertyString(Person.NAME) + " ");
		System.out.
		  print(info.getPropertyString(Person.HOMEPAGE) + " ");
		System.out.
		  print(info.getPropertyString(Person.INTEREST));
		System.out.print("\n");
	      }
	  }
	
	else if (input.startsWith("show "))
	  {
	    if (people == null)
	      {
		System.out.println("Try find first.");
	      }
	    else
	      {
		String numberString = input.substring(5);
		
		int number = -1;
		
		try
		  {
		    number = Integer.parseInt(numberString);
		  }
		catch (NumberFormatException e) 
		  {
		    System.out.println("show <number>");
		  }
		
		ServiceInfo info =
		  (ServiceInfo) people.get(number - 1);

		String content = Util.get("http",
					  info.getAddress(),
					  info.getPort(),
					  "/");

		System.out.println(content);
	      }
	  }

	else if (input.startsWith("I know "))
	  {
	    if (people == null)
	      {
		System.out.println("Try find first.");
	      }
	    else
	      {
		String numberString = input.substring(7);
		
		int number = -1;
		
		try
		  {
		    number = Integer.parseInt(numberString);
		  }
		catch (NumberFormatException e) 
		  {
		    System.out.println("I know <number>");
		  }
		
		ServiceInfo info =
		  (ServiceInfo) people.get(number - 1);

		Person knownPerson =
		  new Person(info.getPropertyString(Person.NAME),
			     info.getName(),
			     info.getPropertyString(Person.HOMEPAGE),
			     info.getPropertyString(Person.INTEREST));

		person.addKnows(knownPerson);
	      }
	  }
	
	else if (input.equals("set plan"))
	  {
	    System.out.println("(Use <ret><ret> to finish)");
	    
	  }

	else if (input.equals("dump"))
	  {
	    System.out.println(person.toRDF());
	  }
	
      }
    
    System.exit(0);
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
    

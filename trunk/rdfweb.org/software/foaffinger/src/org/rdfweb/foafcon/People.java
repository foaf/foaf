package org.rdfweb.foafcon;

import java.util.*;
import javax.jmdns.ServiceInfo;

public class People
{
  ArrayList people;
  ArrayList online;
  
  FoafFingerController controller;

  public People(FoafFingerController controller)
  {
    this.controller = controller;

    people = new ArrayList();
    online = new ArrayList();
  }

  public synchronized void add(ServiceInfo info)
  {
    //System.out.println("I got: " + info);
    
    Person person =
      new Person(info.getPropertyString(Person.NAME),
		 info.getName(),
		 info.getPropertyString(Person.HOMEPAGE),
		 info.getPropertyString(Person.INTEREST),
		 info.getPropertyString(Person.SEEALSO));

    if (person.equals(controller.getPerson()))
	return;

    int index = people.indexOf(person);

    if (index != -1)
      {
	Person existing = (Person) people.get(index);

	existing.setFromPerson(person);

	person = existing;
	
	online.set(index, Boolean.TRUE);
      }
    else
      {
	index = people.size();

	people.add(person);
	
	online.add(Boolean.TRUE);
      }

    person.setHostPort(info.getAddress(), info.getPort());

    controller.showMessage("[" +
			   index +
			   "] " +
			   person.getName() +
			   " is online.");
  }

  public synchronized void remove(String mboxHash)
  {
    Person person =
      new Person(mboxHash);
    
    if (person.equals(controller.getPerson()))
	return;
    
    int index = people.indexOf(person);

    if (index == -1)
      {
	System.out.println("Person removed who wasn't there?");

	return;
      }

    online.set(index, Boolean.FALSE);

    // This appears redundant, but we get a fuller person
    
    person = this.get(index);
        
    controller.showMessage("[" +
			   index +
			   "] " +
			   person.getName() +
			   " is offline.");
  }

  public int size()
  {
    return people.size();
  }

  public synchronized Person get(int index)
  {
    try
      {
	return (Person) people.get(index);
      }
    catch (Exception e)
      {
	return null;
      }
  }

  public synchronized boolean isOnline(int index)
  {
    try
      {
	return ((Boolean) online.get(index)).booleanValue();
      }
    catch (Exception e)
      {
	return false;
      }
  }

  public void find(String match)
  {
    System.out.println("\tName\t\tHomepage\t\tInterest");
    
    for (int i = 0; i < people.size(); i++)
      {
	Person checkee = (Person) people.get(i);

	if (((Boolean) online.get(i)).booleanValue() &&
	    checkee.matches(match))
	  {
	    System.out.print("[" + i + "]\t");

	    checkee.printLineSummary(System.out);
	
	    System.out.print("\n");
	  }
      }
  }
  
}

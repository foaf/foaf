package org.rdfweb.foafcon;

import java.util.*;
import com.strangeberry.rendezvous.ServiceInfo;

public class People
{
  ArrayList people;
  ArrayList online;
  
  RendTest controller;

  public People(RendTest controller)
  {
    this.controller = controller;

    people = new ArrayList();
    online = new ArrayList();
  }

  public synchronized void add(ServiceInfo info)
  {
    Person person =
      new Person(info.getPropertyString(Person.NAME),
		 info.getName(),
		 info.getPropertyString(Person.HOMEPAGE),
		 info.getPropertyString(Person.INTEREST),
		 info.getPropertyString(Person.SEEALSO));

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

  public synchronized void remove(ServiceInfo info)
  {
    System.out.println("r: " + info);
    
    Person person =
      new Person(info.getPropertyString(Person.NAME),
		 info.getName(),
		 info.getPropertyString(Person.HOMEPAGE),
		 info.getPropertyString(Person.INTEREST),
		 info.getPropertyString(Person.SEEALSO));

    int index = people.indexOf(person);

    if (index == -1)
      {
	System.out.println("Person removed who wasn't there?");

	return;
      }

    online.set(index, Boolean.FALSE);

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
	return ((Boolean) online.get(index)).equals(Boolean.TRUE);
      }
    catch (Exception e)
      {
	return false;
      }
  }
  
}

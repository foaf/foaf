package org.rdfweb.foafcon;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;

public class RendListener implements ServiceListener
{
  FoafFingerController controller;
    
  public RendListener(FoafFingerController controller)
  {
    this.controller = controller;
  }
  
  public void addService(JmDNS rendezvous, String type, String name)
  {
    ServiceInfo info = rendezvous.getServiceInfo(type,name);

    if (info != null)  controller.addPerson(info);
    else // Uh-oh, something's up
      {
	String hash = getHash(name);

	controller.addPerson(hash);
      }
  }

  public void removeService(JmDNS rendezvous, String type, String name)
  {
    controller.removePerson(getHash(name));
  }

  public void resolveService(JmDNS rendezvous,
			     java.lang.String type,
			     java.lang.String name,
			     ServiceInfo info)
  {
    // Uh - no idea;

    System.out.println("\nResolving...");
    System.out.println("Type: " + type);
    System.out.println("Name: " + name);
    System.out.println("Info: " + info);
  }

  private String getHash(String name)
  {
    int index = name.indexOf('.');

    return name.substring(0, index);
  }
  
}

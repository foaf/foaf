package org.rdfweb.foafcon;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;

public class RendListener implements ServiceListener
{
  RendTest controller;
    
  public RendListener(RendTest controller)
  {
    this.controller = controller;
  }
  
  public void addService(JmDNS rendezvous, String type, String name)
  {
    controller.addPerson(rendezvous.getServiceInfo(type,name));
  }

  public void removeService(JmDNS rendezvous, String type, String name)
  {
    int index = name.indexOf('.');

    String mboxHash = name.substring(0, index);

    controller.removePerson(mboxHash);
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
  
}

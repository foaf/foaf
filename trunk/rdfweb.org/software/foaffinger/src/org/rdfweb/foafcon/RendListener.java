package org.rdfweb.foafcon;

import com.strangeberry.rendezvous.Rendezvous;
import com.strangeberry.rendezvous.ServiceInfo;
import com.strangeberry.rendezvous.ServiceListener;

public class RendListener implements ServiceListener
{
  RendTest controller;
    
  public RendListener(RendTest controller)
  {
    this.controller = controller;
  }
  
  public void addService(Rendezvous rendezvous, String type, String name)
  {
    controller.addPerson(rendezvous.getServiceInfo(type,name));
  }

  public void removeService(Rendezvous rendezvous, String type, String name)
  {
    controller.removePerson(rendezvous.getServiceInfo(type,name));
  }

  public void resolveService(Rendezvous rendezvous,
			     java.lang.String type,
			     java.lang.String name,
			     ServiceInfo info)
  {
    // Uh - no idea;

    System.out.println(rendezvous + "\n" +
		       type + "\n" +
		       name + "\n" +
		       info + "\n");
  }
  
}

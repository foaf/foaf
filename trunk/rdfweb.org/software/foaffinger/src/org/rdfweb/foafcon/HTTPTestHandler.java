package org.rdfweb.foafcon;

public class HTTPTestHandler implements HTTPRequestHandler
{
  Person person;
  
  public HTTPTestHandler(Person person)
  {
    this.person = person;
  }
  
  public String get(String object)
  {
    return person.toString();
  }

  public String contentType(String object)
  {
    return "text/plain";
  }
}

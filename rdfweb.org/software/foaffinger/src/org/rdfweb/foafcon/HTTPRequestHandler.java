package org.rdfweb.foafcon;

public interface HTTPRequestHandler
{
  public String get(String object);
  public String contentType(String object);
}

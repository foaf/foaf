package org.rdfweb.foafcon;

import java.net.*;
import java.io.*;
import java.util.*;
import java.lang.*;


public class HttpServer implements Runnable
{
  int port;
  HTTPRequestHandler reqHandler;
  Thread listenThread;
  FoafFingerController controller;
  
  public HttpServer(int port, HTTPRequestHandler reqHandler,
		    FoafFingerController controller)
  {
    this.port = port;
    this.reqHandler = reqHandler;
    this.controller = controller;
  }

  public void start()
  {
    listenThread = new Thread(this);

    listenThread.start();
  }

  public void run()
  {
    ServerSocket socket;
    
    try
      {
	socket = new ServerSocket(port);
	controller.showMessage("HttpServer running on port " + 
			   socket.getLocalPort());
	
	while(true)
	  {
	    Socket requestSocket = socket.accept();
	    /*
	    System.out.println("New connection accepted " +
			       requestSocket.getInetAddress() +
			       ":" + requestSocket.getPort());
	    */
	    try
	      {
		HttpConnectionHandler request = 
		  new HttpConnectionHandler(requestSocket, reqHandler);
		
		Thread thread = new Thread(request);
		
		thread.start();
	      }
	    catch(Exception e)
	      {
		System.out.println(e);
	      }
	  }
      }
    
    catch (IOException e)
      {
	System.out.println(e);
      }
  }
}

class HttpConnectionHandler implements Runnable
{
  final static String CRLF = "\r\n";
  Socket socket;
  InputStream input;
  OutputStream output;
  BufferedReader br;

  HTTPRequestHandler reqHandler;
  
  public HttpConnectionHandler(Socket socket, HTTPRequestHandler reqHandler)
    throws Exception 
  {
    this.socket = socket;
    this.input = socket.getInputStream();
    this.output = socket.getOutputStream();
    this.br = 
      new BufferedReader(new InputStreamReader(socket.getInputStream()));
    this.reqHandler = reqHandler;
  }
  
  public void run()
  {
    try
      {
	processRequest();
      }
    catch(Exception e)
      {
	System.out.println(e);
      }
  }
  
  private void processRequest() throws Exception
  {
    while(true)
      {
	String headerLine = br.readLine();
	//System.out.println(headerLine);
	if(headerLine.equals(CRLF) || headerLine.equals("")) break;
	
	StringTokenizer s = new StringTokenizer(headerLine);
	String temp = s.nextToken();
	
	if(temp.equals("GET"))
	  {
	    String object = s.nextToken();
	    
	    String serverLine = "Server: fpont simple java httpServer";
	    String statusLine = null;
	    String contentTypeLine = null;
	    String contentLengthLine = "error";
	    
	    String content = reqHandler.get(object);
	    
	    if (content != null)
	      {
		statusLine = "HTTP/1.0 200 OK" + CRLF ;
		contentTypeLine = "Content-type: " + 
		  reqHandler.contentType(object) + CRLF ;
		contentLengthLine = "Content-Length: " +
		  content.length() +
		  CRLF;
	      }
	    else
	      {
		statusLine = "HTTP/1.0 404 Not Found" + CRLF ;
		contentTypeLine = "text/html" ;
		content = "<HTML>" + 
		  "<HEAD><TITLE>404 Not Found</TITLE></HEAD>" +
		  "<BODY>404 Not Found" 
		  +"<br>usage:http://yourHostName:port/"
		  +"fileName.html</BODY></HTML>" ;
	      }
	    
	    output.write(statusLine.getBytes());
	    
	    output.write(serverLine.getBytes());
	    
	    output.write(contentTypeLine.getBytes());
	    
	    output.write(contentLengthLine.getBytes());
	    
	    output.write(CRLF.getBytes());
	    
	    output.write(content.getBytes());
	  }
      }
    
    try
      {
	    output.close();
	    br.close();
	    socket.close();
      }
    catch(Exception e) {}
  }
  
}







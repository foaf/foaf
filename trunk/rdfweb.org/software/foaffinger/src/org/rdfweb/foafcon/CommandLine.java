package org.rdfweb.foafcon;

import java.io.*;
import java.util.*;

import com.strangeberry.rendezvous.*;

public class CommandLine
{
  RendTest controller;
  List messageQueue;
  
  public CommandLine(RendTest controller)
  {
    this.controller = controller;
    this.messageQueue =
      Collections.synchronizedList(new ArrayList());
  }
  
  public void run()
  {
    Reader reader = new InputStreamReader(System.in);
    BufferedReader inputReader = new BufferedReader(reader);
    String input = null;
    StreamTokenizer inputTokeniser = null;
    
    while (true)
      {
	try
	  {
	    flushMessages();
	    
	    System.out.print("> ");

	    input = inputReader.readLine();
	    
	    inputTokeniser =
	      new StreamTokenizer(new StringReader(input));

	    // This lets URIs work
	    inputTokeniser.wordChars(35, 126);

	    List tokens = Util.tokenise(inputTokeniser);

	    for (Iterator it = tokens.iterator(); it.hasNext();)
	      {
		Object obj = it.next();

		System.out.println(obj.getClass() +
				   ":\t" +
				   obj);
	      }
	    
	    
	    int returned = inputTokeniser.nextToken();

	    if ((returned == StreamTokenizer.TT_EOL) ||
		(returned == StreamTokenizer.TT_EOF))
	      continue;

	    else if (returned == StreamTokenizer.TT_NUMBER)
	      {
		unrecognised();
		continue;
	      }

	    else // A Word
	      {
		String command = inputTokeniser.sval.toLowerCase();
		
		if (command.equals("find"))
		  find(inputTokeniser);
		else if (command.equals("show"))
		  show(inputTokeniser);
		else if (command.equals("iknow"))
		  iknow(inputTokeniser);
		else if (command.equals("dump"))
		  dump(inputTokeniser);
		else if (command.equals("exit"))
		  exit();
		else if (command.equals("set"))
		  set(inputTokeniser);
		else if (command.equals("help") ||
			 command.equals("?"))
		  help();
		else
		  unrecognised();
	      }
	  }
	catch (Exception e)
	  {
	    System.out.println("Error: " + e.getMessage());
	  }
      }
  }

  public void find(StreamTokenizer it)
    throws Exception
  {
    System.out.println("\tName\t\tHomepage\t\tInterest");

    int noOfPeople = controller.numberOfPeople();

    if (noOfPeople == 0)
      return;
    
    for (int n = 0; n < noOfPeople; n++)
      {
	if (!controller.personOnline(n)) continue;
	
	Person p = controller.getPerson(n);
	
	System.out.print("[" + n + "]\t");

	p.printLineSummary(System.out);
	
	System.out.print("\n");
      }	
  }

  public void show(StreamTokenizer it)
    throws Exception
  {
    
    while (true)
      {
	int tt = it.nextToken();

	if ((tt == StreamTokenizer.TT_EOF) ||
	    (tt == StreamTokenizer.TT_EOL))
	  return;

	if (tt == StreamTokenizer.TT_WORD)
	  throw new Exception("Usage: show <number>");

	int num = (int) it.nval;

	Person person =
	  controller.getPerson(num);

	if (person == null)
	  {
	    System.out.println("No such person: " + num);
	    continue;
	  }

	if (!controller.personOnline(num))
	  {
	    System.out.println(num + " not online");
	    continue;
	  }
		
	String content = Util.get("http",
				  person.getHost(),
				  person.getPort(),
				  "/");
	
	System.out.println(content);
      }
  }

  public void iknow(StreamTokenizer it)
    throws Exception
  {
    while (true)
      {
	int tt = it.nextToken();

	if ((tt == StreamTokenizer.TT_EOF) ||
	    (tt == StreamTokenizer.TT_EOL))
	  return;

	if (tt == StreamTokenizer.TT_WORD)
	  throw new Exception("Usage: iknow <number>");

	int num = (int) it.nval;

	Person person =
	  controller.getPerson(num);

	if (person == null)
	  {
	    System.out.println("No such person: " + num);
	    continue;
	  }
	
	controller.getPerson().addKnows(person);
      }
  }

  public void dump(StreamTokenizer it)
    throws Exception
  {
    int tt = it.nextToken();

    if (tt != StreamTokenizer.TT_WORD)
      System.out.println(controller.getPerson().toRDF());

    else
      {
	String filename = it.sval;
	Util.toFile(filename,
		    controller.getPerson().toRDF());

	System.out.println("Information saved to " + filename);
      }
  }

  public void exit()
  {
    controller.exit();
  }

  public void unrecognised()
    throws Exception
  {
    throw new Exception("Unrecognised command");
  }

  public void set(StreamTokenizer it)
    throws Exception
  {
    int tt = it.nextToken();

    if (tt != StreamTokenizer.TT_WORD)
      throw new Exception("Usage: set <var>");

    String var = it.sval.toLowerCase();

    String val = null;
        
    if (!var.equals("plan"))
      {
	tt = it.nextToken();

	if ((tt != StreamTokenizer.TT_WORD) &&
		(tt != '"'))
	  throw new Exception("Usage: set " + var + " <val>");
	
	val = it.sval;

	if (val.equals("\"\""))
	  val = null;
      }
    
    Person person = controller.getPerson();
        
    if (var.equals("name"))
      {
	if (val == null)
	  throw new Exception("Name cannot be empty");
	
	person.setName(val);
	controller.kickService();
      }
    else if (var.equals("homepage"))
      {
	person.setHomepage(val);
	controller.kickService();
      }
    else if (var.equals("seealso"))
      {
	person.setSeeAlso(val);
	controller.kickService();
      }
    else if (var.equals("interest"))
      {
	person.setInterest(val);
	controller.kickService();
      }
    else if (var.equals("plan"))
      person.setPlan(getMultiInput());
    else if (var.equals("showmbox"))
      {
	val = val.toLowerCase();

	person.setShowMbox(val.equals("true") ||
			   val.equals("yes"));
      }
    else
      throw new Exception("Unknown variable: " + var);
  }
  
  private String getMultiInput()
    throws Exception
  {
    StringBuffer sb = new StringBuffer();

    System.out.println("(Enter text, and finish with two blank lines)");

    Reader reader = new InputStreamReader(System.in);
    BufferedReader inputReader = new BufferedReader(reader);
    String input;

    boolean lastLineEmpty = false;
        
    while (true)
      {
	input = inputReader.readLine();

	if (input.equals(""))
	  {
	    if (lastLineEmpty)
	      break;
	    else
	      lastLineEmpty = true;
	  }
	else
	  lastLineEmpty = false;
	
	sb.append(input);
	sb.append("\n");
      }

    // Remove trailing newline used to indicate finished
    sb.deleteCharAt(sb.length() - 1);
    
    String toReturn = sb.toString();

    if (toReturn.equals(""))
      return null;
    else
      return toReturn;
  }

public synchronized void addMessage(String message)
{
messageQueue.add(message);
}

public synchronized void flushMessages()
{
for (Iterator i = messageQueue.iterator();i.hasNext();)
{
System.out.println((String) i.next());
}

messageQueue.clear();
}

  public void help()
  {
    System.out.println("Commands:\n\n" +

		       "help -- well, you found it :-)\n" +
		       "find -- show people on the local network.\n" +
"show <number> [<number>...] -- show detailed information on person (people).\n" +
		       "iknow <number> [<number>...] -- indicate that you know person (people)\n" +
		       "set <var> <val> -- set information. If <val> is \"\" unsets <var>.\n" +
		       "\t<var> can be:\n" +
		       "\tname -- your name (can't be unset)\n" +
		       "\thomepage -- your homepage [URL]\n" +
		       "\tinterest -- a URL indicating an interest, eg http://rdfweb.org/foaf/.\n" +
		       "\tseealso -- more information about you in rdf (eg output of \"dump\") [URL]\n" +
		       "\tshowmbox -- by default your email address is private. Using \"true\"\n" +
		       "\t\tor \"yes\" means people can see it (other values mean they can't).\n" +
		       "\tplan -- No value needed as you'll enter a simple editor.\n" +
		       "\t\tPlan is like the old unix .plan file. Let people know what you're\n" +
		       "\t\tup to, and maybe where you are (eg on IRC). Dumping ground :-)\n" +
		       "dump <filename> -- produces RDF/XML serialised form of information about you,\n" +
		       "\tsaving it to <filename>. Without a filename shows you RDF/XML.\n" +
		       "exit -- exit\n");
    
  }
    
}
/* $Id: Squish2SQL.java,v 1.2 2002-08-02 17:35:21 libby Exp $ */


/*

Copyright (C) 2002 University of Bristol 

This file is part of Inkling <http://swordfish.rdfweb.org/rdfquery>.

Inkling is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

Inkling is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

Libby Miller <libby.miller@bristol.ac.uk>

*/

// This stuff no longer in a package
//package org.desire.rudolf.query;

import java.util.*;
import java.security.MessageDigest;

import org.desire.rudolf.util.ErrorLog;
import org.desire.rudolf.rdf.Triple;
import org.desire.rudolf.query.squishparser.*;

import java.io.RandomAccessFile;

import java.security.MessageDigest;

/**

need to work out how to supply datatyping via the jdbc framework
note that every a1.... is a triple
could we return a shadow datatype with each object

maybe we could return an object ?var.type for each ?var
shame don't seemt to be able to use rsmd


See docs/rewriter.html for Libby's docs (trimmed from here for now)

This is basically a text filter, though it currently has smarts for
talking to databases too (and associated dependencies). See docs/
HTML for writeup of the dependencies.

Commandline interface:

  java Squish2SQL 'squishquery' [search] [dbname]

 */


public class Squish2SQL {

    boolean debug = false;

    public static void main(String args[]) throws Exception {

        String serverhost = "localhost";
        String dbname = "codepict";
        boolean genhtml = false;
        boolean genreport = false;
        boolean showsql = false;

        String mode = "onlysql";

        String squish_query =
                "SELECT ?thumb, ?name, ?dd, ?mbox, ?uri WHERE "+ "(foaf::depiction ?x ?uri) "+
                "(foaf::depiction ?z ?uri) "+ "(foaf::mbox ?x mailto:libby.miller@bristol.ac.uk) "+
                "(foaf::mbox ?z ?mbox) "+ "(foaf::name ?z ?name) "+ "(foaf::thumbnail ?uri ?thumb) "+
                "(dc::description ?uri ?dd) "+ " USING foaf for http://xmlns.com/foaf/0.1/ "+
                " dc for http://purl.org/dc/elements/1.1/ ";

        if (args.length > 0) {
            try {
                squish_query = textFromFile(args[0]);
            } catch (Exception e) {
                System.err.println("Reverting to default. Error opening file "+
                        args[0]);
                throw (e);
            }
        }

        if (args.length > 1) {
            if (args[1].equals("search")) {
                System.out.println("Searching...");
                mode = args[1];
            }
        }

        if (args.length > 2) {
            dbname = args[2];
            System.out.println("dbname: "+dbname);
        }


        SquishParser pq = SquishParser.parse(squish_query);
        String sql_query = Squish2SQL.convert(pq);

        if (mode.equals("onlysql")) {
            System.out.println("\n"+sql_query + "\n");
            System.exit(0);
        }

        System.err.println("QUERYING DATABASE:");


        String summary =
                "Squish: "+squish_query + "\n SQL: "+sql_query + "\n dbname: "+
                dbname + "\n serverhost: "+serverhost + "\n\n";
        System.err.println("Debug summary: "+summary);


        // ************* database query and connectivity code **********

        java.sql.Driver mydriver = null;
        java.sql.Connection myconn = null;
        java.sql.ResultSet results = null;
        try {

            mydriver = (java.sql.Driver) Class.forName(
                    "org.postgresql.Driver").newInstance();
            myconn =
                    java.sql.DriverManager.getConnection("jdbc:postgresql://"+
                    serverhost + ":5432/"+dbname + "?user=postgres");
            java.sql.Statement s = myconn.createStatement();
            results = s.executeQuery(sql_query);


            int numcols = results.getMetaData().getColumnCount();
            String colname;
            String value;
            String table = "<html><head><title>query results</title></head><body>\n<h1>rdf query results</h1>\n\n";

            System.out.println("summary: "+summary);
            table += summary + "<table><tr>\n";


            for (int i = 0; i < numcols; i++) {
                colname = results.getMetaData().getColumnLabel(i + 1);
                table += " <th>"+colname + "</th>\n";
            }
            table += "</tr>\n";

            while (results.next()) {

                table += "<tr>\n";
                for (int i = 0; i < numcols; i++) {
                    colname = results.getMetaData().getColumnLabel(i + 1);
                    value = results.getString(colname);
                    System.out.println(" "+ colname + ": "+ value);
                    table += "<td>"+value + "</td>\n";
                }
                table += "</tr>\n";
                System.out.println("\n");
            }
            table += "</table>\n\n";
            if (genhtml) {
                System.out.println (table);
            }
        }
        catch (Exception e) {
            System.err.println("Error in database query operation " + e);
        }
        finally { try {
                myconn.close();
            } catch (Exception e) {}
        }
    }





    //bcount is the part which accesses the 'resources' table
    //this list is the list of SQL variable names to access for the resources table
    Vector sqlVariableNamesB = new Vector();

    //index of clauses: number used to name the clauses in sql for triples table
    int clause_idxA = 1;

    //index of clauses: number used to name the clauses in sql for triples table
    int clause_idxB = 1;


    //this is just for the a* variables
    Hashtable realToSqlVariableNameMapA = new Hashtable();

    //table names
    String main_table = "triples";
    String res_table = "resources";

    //strings for building teh query
    String sss = "subject";
    String ppp = "predicate";
    String ooo = "object";

    //trying to make both versions work, but only the SHA1 one does
    //simple would be just a strings bucket
    boolean SHA1 = true;
    boolean SIMPLE = false;

    //this is for matching the numbers to the clauses between
    //the triples and resources table,
    //there will be an SQL name for the triples table and
    //another for the resource table - we need to keep track
    //of these and force them to refer to the same thing
    //when we build the SQL query.
    //we need a1.subject=b1.subject - that's what this hash does.
    Hashtable sqlVariableMatchAB;


    public Squish2SQL() {
        clause_idxA = 1;
        clause_idxB = 1;
        sqlVariableNamesB = new Vector();
        realToSqlVariableNameMapA = new Hashtable();
        sqlVariableMatchAB = new Hashtable();
    }


    /**

       This method produces a string SQL query from a minimum  of

        * a (full) list of variables  (i.e. not just the ones you want returned (actually
       this could be a problem));

        * the variables you actually want

        * a vector of arrays of clauses: each array: (pred, sub, obj).

        * a vector of arrays of constraints (as pseudo triples at the moment (e.g. array
       is (~ ?y bla)).

       */


    public String triple_sql(Vector allQueryVariables,
            Vector variablesWeWant, Vector clauses, Vector constraints) {



        //allQueryVariables are all the variables in this query.
        //variableswewant are the variable names that we want to return the value of


        //the string to return
        StringBuffer sql = new StringBuffer("SELECT DISTINCT ");

        try {


            //used for creating 'virtual' SQL tables: e.g. triples as a1, resources as b2
            clause_idxA = 1;

            //we need to keep track fo the real variable names and what we call them
            //in the sql query.
            realToSqlVariableNameMapA = new Hashtable();

            int clause_count = clauses.size();

            //for storing the 'where' parts of the SQL query
            //for the triples and resources tables respectively
            Vector whereA = new Vector();
            Vector whereB = new Vector();

            Enumeration clauseIterator = clauses.elements();



            //---------------------
            //
            // creates a mapping between real and SQl varibales names for A (triples)
            // fills realToSqlVariableNameMapA with these
            //
            //---------------------



            //for each clause
            while (clauseIterator.hasMoreElements()) {


                String[] clause = (String[]) clauseIterator.nextElement();

                //here's our first triple from the list of clauses
                String pred = clause[0];
                String subject = clause[1];
                String object = clause[2];


                int pred_bound = 0;
                int subject_bound = 0;
                int object_bound = 0;



                //this section creates a mapping between the real
                //variables names and their equivalents in the SQL query for the triples table.


                Enumeration queryVariablesIterator =
                        allQueryVariables.elements();

                while (queryVariablesIterator.hasMoreElements()) {

                    String variableNameToMatch =
                            (String) queryVariablesIterator.nextElement();

                    try {


                        //i.e. if variable names are the same in each case
                        if (subject.equals(variableNameToMatch)) {

                            //this adds a mapping from the real name of the variable to its sql name
                            realToSqlVariableNameMapA.put(
                                    variableNameToMatch,
                                    "a"+clause_idxA + "."+sss + "");

                            subject_bound = 1;

                        }

                        if (pred.equals(variableNameToMatch)) {
                            pred_bound = 1;

                            realToSqlVariableNameMapA.put(
                                    variableNameToMatch,
                                    "a"+clause_idxA + "."+ppp + "");

                        }

                        if (object.equals(variableNameToMatch)) {
                            object_bound = 1;

                            realToSqlVariableNameMapA.put(
                                    variableNameToMatch,
                                    "a"+clause_idxA + "."+ooo + "");

                        }

                    } catch (Exception pp) {
                        System.err.println("ERROR "+pp);
                        pp.printStackTrace();
                    }

                }


                //---------------------

                //adding actual clauses to the 'where' part of the query
                //for the triples table e.g. a1.subject='426897987'

                //---------------------

                //sha1 stuff: creating sha1s of s, p, o


                String sh1Sub = "";
                String sh1Pred = "";
                String sh1Obj = "";

                try {
                    sh1Sub = sh1Sub + sha1(subject);
                    sh1Pred = sh1Pred + sha1(pred);
                    sh1Obj = sh1Obj + sha1(object);
                } catch (Exception es) {
                    System.err.println("could not compute sha1 hashcode "+es);
                }


                if (subject_bound == 0) {
                    whereA.addElement("a"+clause_idxA + "."+sss + " = '"+
                            sh1Sub + "'");
                }

                if (pred_bound == 0) {
                    whereA.addElement("a"+clause_idxA + "."+ppp + " = '"+
                            sh1Pred + "'");
                }

                if (object_bound == 0) {
                    whereA.addElement("a"+clause_idxA + "."+ooo + " = '"+
                            sh1Obj + "'");
                }

                clause_idxA++;

            }

            //---------------------
            //
            // creates a vector of SQL variables names for A (triples)
            // fills sqlVariableNamesList with these
            // later we generate paths from this list
            // e.g. a1.subject=a4.object etc
            //
            //---------------------



            Vector sqlVariableNamesList = new Vector();

            int cl_idx = 1;


            //looping through the allQueryVariables again, this time to get the
            //links _between_ the triples variables in the sql query
            //e.g. a1.object=a2.subject and a2.subject=a3.subject
            Enumeration variables = allQueryVariables.elements();
            while (variables.hasMoreElements()) {
                String variableNameToMatch =
                        (String) variables.nextElement();
                Vector sqlVariablesNamesA = new Vector();

                //this is going to be a list of matches from clauses of one variable, and
                //will eventualy be added to bindings

                Enumeration eq = clauses.elements();

                while (eq.hasMoreElements()) {

                    String[] clause = (String[]) eq.nextElement();

                    String pred = clause[0];
                    String subject = clause[1];
                    String object = clause[2];


                    int pred_bound = 0;
                    int subject_bound = 0;
                    int object_bound = 0;

                    if (subject.equals(variableNameToMatch)) {
                        sqlVariablesNamesA.addElement("a"+cl_idx + "."+
                                sss + "");
                    }

                    if (pred.equals(variableNameToMatch)) {
                        sqlVariablesNamesA.addElement("a"+cl_idx + "."+
                                ppp + "");
                    }

                    if (object.equals(variableNameToMatch)) {
                        sqlVariablesNamesA.addElement("a"+cl_idx + "."+
                                ooo + "");
                    }

                    cl_idx++;

                }//end of loop through clauses - still in var loop

                cl_idx = 1;

                if (sqlVariablesNamesA.size() > 1) {
                    sqlVariableNamesList.addElement(sqlVariablesNamesA);
                }


            }//end of loop through var loop



            // adding some more stuff to whereA - these are
            // the sqlVariableNamesList - used to generate links between A-variable names

            for (int i = 0; i < sqlVariableNamesList.size(); i++) {

                Vector bindings =
                        (Vector) sqlVariableNamesList.elementAt(i);

                for (int j = 0; j < bindings.size() - 1; j++) {

                    whereA.addElement((String) bindings.elementAt(j) +
                            "="+(String) bindings.elementAt(j + 1));
                }

            }




            //------------------
            //
            // this part sorts out the mappings from values
            // to the resources table
            // puts these in sqlVariableMatchAB; also adds stuff to the whereB part
            //
            //------------------


            //selectvars is the list of b variables and the actual variables that match them:
            //e.g b1.value as ?x

            Vector selectvars = new Vector();

            //realToSqlVariableNameMapA is a mapping from the real names of the
            //variables: ?x, ?sal etc to the SQL names of the
            //variables: a1.subject, a2.object etc - just the a* (i.e. just the triples table)


            Enumeration qq = realToSqlVariableNameMapA.keys();

            while (qq.hasMoreElements()) {

                String key = (String) qq.nextElement();
                String val = (String) realToSqlVariableNameMapA.get(key);
                String kk = key;

                //the keys should not start with "?" any more
                if (key.startsWith("?")) {
                    kk = key.substring(1);
                }


                //the key stays the same but
                //kk should be equal to something in a later clause, and
                //val should be the resource.value, i.e. the value of the key
                //val in the resources table.

                if (variablesWeWant.contains(key)) {

                    selectvars.addElement("b"+ clause_idxB + ".value AS "+kk);


                    //.....literals attempt
                    //change this - put the boolean literal in resources table not triples
                    //leave this for now
                    /*
                    		    selectvars.addElement("a"+ clause_idxB + ".isresource AS "+kk+"type");

                                        selectvars.addElement("b"+ clause_idxB + ".isresource AS "+kk+"type");
                    */

                    //.....literals attempt


                    sqlVariableNamesB.addElement("b"+ clause_idxB);

                    whereB.addElement("b"+ clause_idxB + ".keyhash="+val);

                    //need to add in the constraints stuff here
                    //hash of vectors?

                    Vector ttemp;
                    if (!sqlVariableMatchAB.containsKey(val)) {
                        ttemp = new Vector();
                        ttemp.addElement("b"+ clause_idxB + ".value");
                        sqlVariableMatchAB.put(val, ttemp);
                    } else {
                        ttemp = (Vector) sqlVariableMatchAB.get(val);
                        ttemp.addElement("b"+ clause_idxB + ".value");
                    }
                }
                clause_idxB++;
            }





            //------------------
            //
            // building the actual query.
            //
            //
            //------------------



            //real variable names 'x, y' etc

            /*
            	    ///attempt at literal
            	    Enumeration litloop = literalToSqlVariableName.keys();

            	    while(litloop.hasMoreElements()){
            		String key=(String)litloop.nextElement();
            		String val=(String)literalToSqlVariableName.get(key);

                            //the keys should not start with "?" any more
                            if (key.startsWith("?")) {
                                key = key.substring(1);
                            }

            		sql.append(" "+val+" as "+key+", ");
            	    }
            	    ///attempt at literal
            */

            for (int q = 0; q < selectvars.size(); q++) {

                if (q == selectvars.size() - 1) {
                    sql.append(selectvars.elementAt(q).toString() + " ");
                } else {
                    sql.append(selectvars.elementAt(q).toString() + ", ");
                }

            }


            //SQL from clause
            sql.append(" FROM ");

            //from

            for (int z = 1; z < clause_idxA; z++) {
                sql.append(" "+main_table + " a"+z + ", ");
            }


            //variables to get from the resources table

            Enumeration zq = sqlVariableNamesB.elements();

            for (int z = 0; z < sqlVariableNamesB.size(); z++) {

                if (z == (sqlVariableNamesB.size() - 1)) {

                    sql.append(" "+res_table + " "+
                            zq.nextElement().toString() + " ");
                } else {

                    sql.append(" "+res_table + " "+
                            zq.nextElement().toString() + ", ");
                }


            }


            //where for the triples table
            sql.append(" WHERE ");

            for (int r = 0; r < whereA.size(); r++) {

                sql.append(whereA.elementAt(r).toString() + " ");
                sql.append(" AND ");
            }//end for loop

            //where for the resources table

            for (int r = 0; r < whereB.size(); r++) {

                sql.append(whereB.elementAt(r).toString() + " ");

                //constraints

                if (constraints == null || constraints.size() == 0) {

                    if (r == whereB.size() - 1) {
                        sql.append(";");
                    } else {
                        sql.append(" AND ");
                    }
                } else {
                    sql.append(" AND ");
                }
            }//end for loop


            if (constraints != null) {


                for (int zz = 0; zz < constraints.size(); zz++) {

                    String[] tr = (String[]) constraints.elementAt(zz);
                    String key = tr[1].trim();

                    String s = (String) realToSqlVariableNameMapA.get(key);

                    String ss = tr[0];

                    if (SHA1) {

                        //hack. what about multiple values?

                        Vector oo = (Vector) sqlVariableMatchAB.get(s);

                        if (oo != null) {
                            s = oo.elements().nextElement().toString();
                        }
                    }


                    //we need to try to cast this to the right sort of object for the database
                    //(Postgres - don't know about other systems yet)
                    //INT, timestamp, float, date, time
                    //all except numbers must be surrounded by single quotes
                    //should do something clever here with xml datatypes....

                    //** dates not workig yet - needs further thought

                    //ordering? int, float, datetime, date, time?

                    String tocast = tr[2];

                    //shoudl have proper equals for strings - not sure of syntax


                    if (ss.trim().equals("~") ||
                            ss.trim().toLowerCase().equals("like")) {
                        //assume a string
                        //System.out.println("~ or like "+tocast);

                        sql.append("lower("+s + ") like lower('%"+
                                tocast + "%')");
                    } else if (ss.trim().equals("eq")) {

                        sql.append(s + "='"+tocast + "'");

                        //System.out.println("eq "+tocast);
                    } else if (ss.trim().equals("ne")) {

                        //assume a string
                        sql.append(s + "!='"+tocast + "'");
                        //                        sql.append(" not(lower("+s + ") like lower('%"+tocast + "%'))");
                        //System.out.println("neq "+tocast);

                    } else {

                        if (ss.trim().equals("==") ||
                                ss.trim().equals("=")) {
                            ss = "=";
                        }

                        //System.out.println("= "+tocast);

                        try {
                            Integer test = new Integer(tocast);


                            sql.append("CAST(CAST("+s + " as TEXT) as INTEGER) "+
                                    ss + " "+tocast);


                        } catch (Exception e1) {
                            ErrorLog.write("Not an Integer");

                            try {
                                Float test = new Float(tocast);

                                sql.append("CAST(CAST("+s + " as TEXT) as FLOAT) "+
                                        ss + " "+tocast);

                            } catch (Exception e2) {


                                ErrorLog.write("Not a Float");


                                ErrorLog.write("Defaulting to string");

                                //may fail...last resort
                                sql.append(s + " "+ss + " '"+tocast + "'");


                            }//end catch

                        }//end catch

                    }
                    //		    }else{

                    //	               	sql.append(s+" "+ss+" '"+tocast+"'");

                    //                  }//end else


                    if (zz == constraints.size() - 1) {
                        sql.append(";");
                    } else {
                        sql.append(" AND ");
                    }


                }//end for


            }//end if
        }
        catch (Exception yt) {
            yt.printStackTrace();
        }

        if (debug) {
            System.out.println("SQL query is "+sql.toString());
        }
        return sql.toString();

    }



    public Vector processQueryVariables(Vector r) {

        Vector v = new Vector();

        for (int y = 0; y < r.size(); y++) {

            String s = ((String) r.elementAt(y)).trim();
            if (s.startsWith("?")) {
                s = s.substring(1);
                v.addElement(s);
            }
        }

        return v;

    }


    /** a utility method: squish query string to SQL query string*/

	public String convertString(String squish_query){

        SquishParser pq = SquishParser.parse(squish_query);
        String sql_query = Squish2SQL.convert(pq);

	return sql_query;

	}


    /**
       basically the main method: takes a parsed query and returns a
       converted SQL query as a string
       */


    public static String convert(SquishParser p) {


        //all the variables
        Vector allQueryVariables = new Vector();

        //just the ones we want
        Vector variablesWeWant = new Vector();

        //clauses of the query (as arrays)
        Vector clauses = new Vector();

        //constraints of the query (as arrays)
        Vector constraints = new Vector();

        String query = null;
        String query1 = null;

        Enumeration e = p.triples.elements();


        //we get the clauses as triples - here we convert to arrays
        while (e.hasMoreElements()) {
            Triple tt = (Triple) e.nextElement();
            String pp = tt.getPredicate();
            String ss = tt.getSubject();
            String oo = tt.getObject();

            if (tt.getPredicate() == null) {
                pp = tt.getPredicateBinding();
            }
            if (tt.getSubject() == null) {
                ss = tt.getSubjectBinding();
            }
            if (tt.getObject() == null) {
                oo = tt.getObjectBinding();
            }

            String[] clause = {pp, ss, oo};
            clauses.addElement(clause);
        }

        constraints = new Vector();

        variablesWeWant = p.variables;
        allQueryVariables = p.listVariablesRaw();

        Squish2SQL t = new Squish2SQL();

        //we get the constraints as triples - here we convert to arrays
        Enumeration e1 = p.constraints.elements();
        while (e1.hasMoreElements()) {
            Triple tt = (Triple) e1.nextElement();
            String[] clause = {tt.getPredicate(), tt.getSubject(),
            tt.getObject()};
            constraints.addElement(clause);
        }


        String sqlQuery = t.triple_sql(allQueryVariables, variablesWeWant,
                clauses, constraints);


        return sqlQuery;


    }

    public static int sha1 (String sha1) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA1");
        md.update(sha1.getBytes());
        byte[] digest = md.digest();
        return ((int) digest[0] & 0xff) | (((int) digest[1] & 0xff) << 8) |
                (((int) digest[2] & 0xff) << 16) |
                (((int) digest[3] & 0xff) << 24);
    }


    public static String textFromFile(String fn) throws Exception {
        //System.out.println("file: "+ fn);
        RandomAccessFile in = new RandomAccessFile(fn, "r");
        String text = "";
        String line = "";
        while (line != null) {
            text += line;
            line = in.readLine();
        }
        return text;
    }
}

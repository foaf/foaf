package org.desire.rudolf.query;

import java.util.*;
import java.io.*;
import java.sql.*;
import java.net.*;
import org.desire.rudolf.util.ErrorLog;
import org.desire.rudolf.util.SearchReplace;
import org.desire.rudolf.rdf.Triple;

/**
 *a generic representation of a parsed query. bits we need: triples
 * constraints namespaces variables graphs 
								*/

public class ParsedQuery {
    static String query;
    static int type = 0;
    //0=squish, 1=algae, 2=rdfpath
    public static Vector triples = new Vector();
    public static Vector triplesOR = new Vector();
    public static Vector variables = new Vector();
    public static Vector constraints = new Vector();
    public static Vector graphs = new Vector();
    public static Hashtable ns = new Hashtable();
    static String triplesPart = null;
    static String graphsPart = null;
    static String endPart = null;
    static String usingPart = null;
    static String variablesPart = null;
    static boolean debug = false;
    static ParsedQuery p = null;

    /** parses query into the relevent parts. */
    public static ParsedQuery parse(String query) {
        p = new ParsedQuery();
        p.setQuery(query);
        triples = new Vector();
        variables = new Vector();
        constraints = new Vector();
        graphs = new Vector();
        ns = new Hashtable();
        if (query != null) {
            if (debug) {
                System.out.println("\nquery is " + query + "\n");
            }
            if (query.trim().toLowerCase().startsWith("select")) {
                if (debug) {
                    System.out.println("SQUISH query");
                }
                type = 0;
                p.parseQuerySquish();
            } else {
                if (query.trim().toLowerCase().startsWith("resource") ||
                        query.trim().toLowerCase().startsWith("literal")
                        || query.trim().toLowerCase().startsWith("graph")) {
                    //rdfpath!?
                    if (debug) {
                        System.out.println("RDFPath query");
                    }
                    type = 2;
                    p.parseQueryRDFPath();
                } else {
                    if (debug) {
                        System.out.println("ALGAE query");
                    }
                    type = 1;
                    p.parseQueryAlgae();
                }
            }
        } else {
            ErrorLog.write("PQ: no query");
        }
        return p;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public void parseQuerySquish() {
        //error logging here
        //triples = new Vector();
        //remove select...from part - at the moment other code handles this
        int aaa = query.toLowerCase().indexOf("from");
        int bbb = query.toLowerCase().indexOf("where");
        if (bbb != -1) {
            if (aaa != -1) {
                ErrorLog.write("PQ: no from clause in query - not fatal");
                graphsPart = query.substring((aaa + 4), bbb);
                getGraphs();
            }
        } else {
            ErrorLog.write("PQ: no 'where' clause in query " + query);
        }
        //get the triples part of the query
        int a = query.indexOf("(");
        int aa = query.lastIndexOf(")");
        if (a == -1) {
            ErrorLog.write("PQ: malformed query - no first bracket - fatal");
            return;
        }
        if (aa == -1) {
            ErrorLog.write("PQ: malformed query - no last bracket - fatal");
            return;
        }
        triplesPart = query.substring(a, aa);
        //get the last part of the query (namespaces and constraints)
        endPart = query.substring(aa + 1);
        if (debug) {
            System.out.println("***endPart ..." + endPart + "...");
        }
        if (endPart != null && (!endPart.equals(""))) {
            parseEnd();
        } else {
            ErrorLog.write("PQ: no constraints or namespaces - not fatal");
        }

        /*

                   Enumeration e = lastConstraints.elements();
                   while(e.hasMoreElements()){
                   org.desire.rudolf.rdf.Triple t = (org.desire.rudolf.rdf.Triple)e.nextElement();

                   System.out.println("***"+t.subject+"***"+t.predicate+"***"+t.object);

                   }

          */

        variablesPart = query.substring(0, a);
        getSquishVars();
        Vector v = SearchReplace.splitByString(triplesPart, ")");
        if (v.size() == 0) {
            ErrorLog.write("PQ: no subqueries - fatal");
            return;
        }
        //each element of the vector is a subquery
        Enumeration eno = v.elements();
        while (eno.hasMoreElements()) {
            //split again to find the predicate
            String subquery = eno.nextElement().toString();
            if (debug) {
                System.out.println("SUBQ is " + subquery);
            }
            Vector spo = new Vector(3);

            /*
                        int yy = subquery.indexOf(",");


                        if (yy != -1) {

                            spo = SearchReplace.splitByString(subquery, ",");

                            Enumeration yt = spo.elements();
                            while (yt.hasMoreElements()) {

                                String sw = yt.nextElement().toString();

                                int qq = sw.indexOf(",");


                                if (qq != -1) {
                                    SearchReplace.replace(sw, ",", " ");

                                    sw.trim();


                                }


                            }

                        } else {
            */

            spo = SearchReplace.splitByString(subquery, " ");
            //            }
            //parsing sucks - breaks if there are brackets in the urls etc.
            //for now...
            String predicate = null;
            String subject = null;
            String object = null;
            try {
                predicate = spo.elementAt(0).toString().trim();
                subject = spo.elementAt(1).toString().trim();
                object = spo.elementAt(2).toString().trim();
            } catch (Exception e) {
                ErrorLog.write(
                        "PQ: parsing error - fatal: probably brackets in the urls: " +
                        spo);
                return;
            }
            int y1 = predicate.indexOf("(");
            if (y1 != -1) {
                predicate = predicate.substring(y1 + 1).trim();
            }
            int y2 = object.indexOf(")");
            if (y2 != -1) {
                object = object.substring(0, y1).trim();
            }
            int ff = predicate.indexOf("::");
            if (ff != -1) {
                String names = predicate.substring(0, ff);
                String val = predicate.substring(ff + 2);
                predicate = ns.get(names) + val;
            }
            int gg = subject.indexOf("::");
            if (gg != -1) {
                String names = subject.substring(0, gg);
                String val = subject.substring(gg + 2);
                subject = ns.get(names) + val;
            }
            int hh = object.indexOf("::");
            if (hh != -1) {
                String names = object.substring(0, hh);
                String val = object.substring(hh + 2);
                object = ns.get(names) + val;
            }

            ////changing to have binding separate

            String sb = null;
            String pb = null;
            String ob = null;

            if (subject.startsWith("?")) {
                sb = subject;
                subject = null;
            } else {
                sb = subject;
            }
            if (predicate.startsWith("?")) {
                pb = predicate;
                predicate = null;
            } else {
                pb = predicate;
            }
            if (object.startsWith("?")) {
                ob = object;
                object = null;
            } else {
                ob = object;
            }

            org.desire.rudolf.rdf.Triple trip =
                    new org.desire.rudolf.rdf.Triple(subject,
                    predicate, object);
            trip.setAllBindings(sb, pb, ob);

            triples.addElement(trip);


            if ( debug )
             {
            System.out.println("\nadding elements to triples " +
                    subject + " " + predicate + " " + object + " sb "+
                    sb + " pb "+pb + " ob "+ob);
            }
        }
        if (variables.size() == 0) {
            variables = listVariablesRaw();
        }
    }

    public void getGraphs() {
        String graphString = null;
        if (graphsPart != null && (!graphsPart.equals(""))) {
            graphString = graphsPart.trim();
            graphs = SearchReplace.splitByString(graphString, ",");
        } else {
            ErrorLog.write("PQ: no graphs. query fragment is " +
                    graphsPart);
        }
    }

    void getSquishVars() {
        String s = "";
        int i = variablesPart.toLowerCase().indexOf("select");
        int j = variablesPart.toLowerCase().indexOf("where");
        int k = variablesPart.toLowerCase().indexOf("from");
        if (j == -1) {
            ErrorLog.write("PQ: no where part - fatal ");
            return;
        }
        if (i != -1) {
            if (k != -1) {
                s = variablesPart.substring(i + 6, k).trim();
            } else {
                s = variablesPart.substring(i + 6, j).trim();
                ErrorLog.write("PQ: no from part - non-fatal");
            }
        } else {
            ErrorLog.write("PQ: no select part - fatal");
            return;
        }
        Vector v = null;
        if (s.trim().equals("*")) {
            v = new Vector();
        } else {
            v = SearchReplace.splitByString(s, ",");
        }
        //removing ?s from variable names (to be consistent with sql)

        /*

        	Enumeration eq = v.elements();
        	while(eq.hasMoreElements()){

        	String sr = (String)eq.nextElement();

        		if(sr.trim().startsWith("?")){
        		sr = sr.trim().substring(1);
        		}


        	}
        */

        variables = v;
    }

    void parseEnd() {
        String s = endPart;

        int q = endPart.toLowerCase().indexOf("using");
        if (q != -1) {
            usingPart = endPart.substring(q + 5).trim();
            s = s.substring(0, q);
            createUsingClause();
        } else {
            ErrorLog.write("PQ: no using part - non-fatal");
        }
        Vector v = null;
        int d = s.toLowerCase().indexOf("and");
        boolean wasAND = false;

        if (s.toLowerCase().trim().startsWith("and")) {
            s = s.substring(d + 3).trim();
            wasAND = true;
        }
        int f = s.toLowerCase().indexOf(" and ");

        //spaces to make sure doesn't pick up works with 'and' in them
        if (f != -1) {
            //may have more than one clause
            Vector tmp = SearchReplace.splitByStringNoChomp(
                    s.toLowerCase(), " and ");
            v = SearchReplace.splitByStringNoChomp(s, " AND ");


            if (v.size() < tmp.size()) {
                v = SearchReplace.splitByStringNoChomp(s, " and ");
            }
            if (debug) {
                Enumeration e = v.elements();
                while (e.hasMoreElements()) {
                    System.out.println("v is " + e.nextElement());
                }
            }
        } else {
            ErrorLog.write("PQ: assuming one constrain clause");
            //may in fact have constrain clauses - just one
            if (wasAND) {
                v = new Vector();
                v.addElement(s);
            } else {
                if (debug) {
                    System.err.println("PQ: no constrain clauses " + s);
                }
            }
        }
        if (v != null) {
            Enumeration eze = v.elements();
            while (eze.hasMoreElements()) {
                String sez = (String) eze.nextElement();
                int a = sez.indexOf(">");
                int b = sez.indexOf("<");
                int l = sez.indexOf("=");
                int dd = sez.indexOf("<=");
                int ff = sez.indexOf(">=");
                int ddd = sez.indexOf("=<");
                int fff = sez.indexOf("=>");
                int g = sez.indexOf("~");
                int h = sez.indexOf(" like ");
                int i = sez.indexOf(" ne ");
                int j = sez.indexOf(" eq ");
                int k = sez.indexOf("==");
                int c = sez.indexOf("!=");

                org.desire.rudolf.rdf.Triple t =
                        new org.desire.rudolf.rdf.Triple("", "", "");
                if (dd != -1 || dd != -1) {
                    if (dd == -1) {
                        dd = ddd;
                    }
                    t.setPredicate("<=");
                    t.setSubject(sez.substring(0, dd).trim());
                    t.setObject(sez.substring(dd + 2).trim());
                } else if (ff != -1 || fff != -1) {
                    if (ff == -1) {
                        ff = fff;
                    }
                    t.setPredicate (">=");
                    t.setSubject(sez.substring(0, ff).trim());
                    t.setObject(sez.substring(ff + 2).trim());
                } else if (a != -1) {
                    t.setPredicate(">");
                    t.setSubject(sez.substring(0, a).trim());
                    t.setObject(sez.substring(a + 1).trim());
                } else if (b != -1) {
                    t.setPredicate("<");
                    t.setSubject(sez.substring(0, b).trim());
                    t.setObject(sez.substring(b + 1).trim());
                } else if (c != -1) {
                    t.setPredicate("!=");
                    t.setSubject(sez.substring(0, c).trim());
                    t.setObject(sez.substring(c + 2).trim());
                } else if (g != -1) {
                    t.setPredicate("~");
                    t.setSubject(sez.substring(0, g).trim());
                    t.setObject(sez.substring(g + 1).trim());
                

                } else if (h != -1) {
                    t.setPredicate("like");
                    t.setSubject(sez.substring(0, h).trim());
                    t.setObject(sez.substring(h + 5).trim());
                
                } else if (i != -1) {
                    t.setPredicate("ne");
                    t.setSubject(sez.substring(0, i).trim());
                    t.setObject(sez.substring(i + 3).trim());
                
                } else if (j != -1) {
                    t.setPredicate("eq");
                    t.setSubject(sez.substring(0, j).trim());
                    t.setObject(sez.substring(j + 3).trim());

                } else if (k != -1 ) {
                    t.setPredicate("==");
                    t.setSubject(sez.substring(0, l).trim());
                    t.setObject(sez.substring(l + 2).trim());

                } else if (l != -1 ) {
                    t.setPredicate("=");
                    t.setSubject(sez.substring(0, l).trim());
                    t.setObject(sez.substring(l + 1).trim());

                }

                if ((!t.getSubject().equals("")) &&
                        (!t.getPredicate().equals("")) &&
                        (!t.getObject().equals(""))) {
                    constraints.addElement(t);
                }


            } //end while
        } //end if v not null
    }

    void createUsingClause() {
        Vector v = SearchReplace.splitByString(usingPart, " ");
        String k = null;
        String val = null;
        for (int i = 3; i < v.size() + 3; i++) {
            if (i % 3 == 0) {
                k = v.elementAt(i - 3).toString();
            }
            if (i % 3 == 2) {
                val = v.elementAt(i - 3).toString();
            }
            if (k != null && val != null) {
                ns.put(k, val);
                k = null;
                val = null;
            }
        }
    }

    public void parseQueryAlgae() {
        int a = query.indexOf("'"); //start of query
        int b = query.indexOf("(", a + 1); //first (outer) bracket
        int c = query.indexOf("'", a + 1); //last part of query
        int d = query.indexOf("collect"); //last part of query
        if (a == -1) {
            ErrorLog.write(
                    "PQ:Algae: syntax error - needs to start with ' " +
                    query);
        }
        if (c == -1) {
            ErrorLog.write(
                    "PQ:Algae: syntax error - needs to end with ' " +
                    query);
        }
        if (b == -1) {
            ErrorLog.write(
                    "PQ:Algae: syntax error - enclose query in backets " +
                    query);
        }
        if (d == -1) {
            ErrorLog.write(
                    "PQ:Algae: syntax error - query requires a collect clause " +
                    query);
        }
        if (a != -1 && b != -1 && c != -1 && d != -1) {
            //send the rest to be split
            triplesPart = query.substring(b, d);
            endPart = query.substring(d);
            int dd = endPart.indexOf("?");
            int ddd = endPart.indexOf(")");
            if (dd != -1 && ddd != -1) {
                endPart = endPart.substring(dd, ddd);
            } else {
                ErrorLog.write(
                        "PQ:Algae: Syntax error - no variables " + query);
            }
            Vector display = SearchReplace.splitByString(endPart, " ");
            Vector v = SearchReplace.splitByString(triplesPart, "(");
            Enumeration e = v.elements();
            while (e.hasMoreElements()) {
                String sub = e.nextElement().toString();
                //replace ) with space
                sub = SearchReplace.replace(sub, ")", " ");
                //tokenize for spaces
                Vector v1 = SearchReplace.splitByString(sub, " ");
                String subject;
                String predicate;
                String object;
                if (v1.size() > 2) {
                    predicate = (String) v1.elementAt(0);
                    subject = (String) v1.elementAt(1);
                    object = (String) v1.elementAt(2);
                    //put into triples
                    org.desire.rudolf.rdf.Triple trip =
                            new org.desire.rudolf.rdf.Triple(subject,
                            predicate, object);
                    triples.addElement(trip);
                    if (debug) {
                        System.out.println( "\nadding elements to triples" +
                                subject + " " + predicate + " " + object);
                    }
                }
                variablesPart = query.substring(d);
                getAlgaeVars();
            }
        } else {
            ErrorLog.write("PQ:Algae:Syntax error " + query);
        }
    }

    public void getAlgaeVars() {
        Vector v = new Vector();
        int d = query.indexOf("collect"); //last part of query
        if (type == 1 && d != -1) {
            //got an algae query with a collect clause
            String tmp = query.substring(d);
            int i = tmp.lastIndexOf("?");
            int j = tmp.indexOf(")");
            if (i != -1 && j != -1) {
                tmp = tmp.substring(i, j);
                v = SearchReplace.splitByString(tmp, ",");
            } else {
                if (j == -1) {
                    ErrorLog.write(
                            "PQ:Algae: no final bracket: query fragment is " +
                            tmp);
                }
                if (i == -1) {
                    ErrorLog.write(
                            "PQ:Algae: variable syntax incorrect: query fragment is " +
                            tmp);
                }
                v = listVariables();
            }
        } else {
            ErrorLog.write("PQ:Algae: not an algae query");
        }
        variables = v;
    }

    public Vector listVariables() {
        Vector v = new Vector();
        Enumeration tr = triples.elements();
        while (tr.hasMoreElements()) {
            org.desire.rudolf.rdf.Triple t =
                    (org.desire.rudolf.rdf.Triple) tr.nextElement();
            if (t.getSubjectBinding() != null) {
                if (t.getSubjectBinding().startsWith("?") &&
                        (!v.contains(t.getSubjectBinding()))) {
                    v.addElement(t.getSubjectBinding());
                }
            }
            if (t.getObjectBinding() != null) {
                if (t.getObjectBinding().startsWith("?") &&
                        (!v.contains(t.getObjectBinding()))) {
                    v.addElement(t.getObjectBinding());
                }
            }
            if (t.getPredicateBinding() != null) {
                if (t.getPredicateBinding().startsWith("?") &&
                        (!v.contains(t.getPredicateBinding()))) {
                    v.addElement(t.getPredicateBinding());
                }
            }
        }
        return v;
    }

    public Vector listVariablesRaw() {
        Vector v = new Vector();
        Enumeration tr = triples.elements();
        while (tr.hasMoreElements()) {
            org.desire.rudolf.rdf.Triple t =
                    (org.desire.rudolf.rdf.Triple) tr.nextElement();

            if (t.getSubjectBinding() != null) {
                t.setSubjectBinding(t.getSubjectBinding().trim());
                if (t.getSubjectBinding().startsWith("?") &&
                        (!v.contains(t.getSubjectBinding()))) {
                    v.addElement(t.getSubjectBinding());
                }
            }
            else if (t.getSubject() != null) {
                t.setSubjectBinding(t.getSubject().trim());
                t.setSubject(t.getSubject().trim());
                if (t.getSubjectBinding().startsWith("?") &&
                        (!v.contains(t.getSubjectBinding()))) {
                    v.addElement(t.getSubjectBinding());
                }
            }




            if (t.getObjectBinding() != null) {
                t.setObjectBinding(t.getObjectBinding().trim());
                if (t.getObjectBinding().startsWith("?") &&
                        (!v.contains(t.getObjectBinding()))) {
                    v.addElement(t.getObjectBinding());
                }
            }

            else if (t.getObject() != null) {
                t.setObject(t.getObject().trim());
                t.setObjectBinding(t.getObject().trim());
                if (t.getObjectBinding().startsWith("?") &&
                        (!v.contains(t.getObjectBinding()))) {
                    v.addElement(t.getObjectBinding());
                }
            }


            if (t.getPredicateBinding() != null) {
                t.setPredicateBinding(t.getPredicateBinding().trim());
                if (t.getPredicateBinding().startsWith("?") &&
                        (!v.contains(t.getPredicateBinding()))) {
                    v.addElement(t.getPredicateBinding());
                }
            }


            if (t.getPredicate() != null) {
                t.setPredicate(t.getPredicate().trim());
                t.setPredicateBinding(t.getPredicate().trim());
                if (t.getPredicateBinding().startsWith("?") &&
                        (!v.contains(t.getPredicateBinding()))) {
                    v.addElement(t.getPredicateBinding());
                }
            }

        }
        return v;
    }

    public static void main(String[] args) {
        Vector tests = new Vector();
        //tests.addElement("graph('http://rdfweb.org/~pldab/rdfweb/danbri.wot.rdf')");
        tests.addElement("graph('danbri.wot.rdf')/resource()[wot:identity]/foaf:mbox");
        tests.addElement("graph('http://rdfweb.org/~pldab/rdfweb/danbri.wot.rdf')/wot:identity/foaf:mbox");
        tests.addElement("graph('http://rdfweb.org/~pldab/rdfweb/danbri.wot.rdf')/resource(mailto:daniel.brickley@bristol.ac.uk)");
        tests.addElement("graph('uri1.rdf')");

        /* return the graph */

        tests.addElement("graph('uri1.rdf')/resource()");

        /* return all resources of graph uri1.rdf */

        tests.addElement("graph('uri1.rdf')/resource(rdf:Bag)");

        /* return the resource rdf:Bag of graph uri1.rdf */

        tests.addElement("graph('uri1.rdf')/resource()[rdf:type=rdf:Bag]");

        /* return all instances of rdf:Bag */

        //tests.addElement("resource()[rdf:type=rdf:Bag]/elements()";
        tests.addElement("resource()[rdf:type=rdf:Bag]");

        /* return all elements of all bags /*


        tests.addElement("resource()[rdf:type=dct:Person]/vCard:N/vCard:Given");

        /* return the name of all 'persons' */

        tests.addElement("literal('stefan')/parent(dc:creator)");

        /* return docs written by stefan */

        tests.addElement("resource('doc.html')[dc:creator]");

        /* return doc.html if it has a dc:creator*/

        /*
        tests.addElement("select ?z, ?a from "+
        "http://ilrt.org/discovery/2000/11/rss-query/jobs-rss.rdf, "+
        "http://yaddle.ilrt.bris.ac.uk/~libby/rss/jobs.rss "+
        "where (job::advertises ?x ?y) (job::salary ?y ?z) (job::title ?y ?a) "+
        "and ?z > 60000 and ?z < 100000 "+
        "using job for "+
        "http://ilrt.org/discovery/2000/11/rss-query/jobvocab.rdf#");


        tests.addElement(" (ask '( "+
                      " (http://...#type ?annot http://...#Annotation) "+
                      " (http://...#Author ?annot Joe) "+
                      " (http://...#Annotates ?annot ?doc) "+
                     " ):collect '(?doc)) ");


        */

        Enumeration zzz = tests.elements();
        while (zzz.hasMoreElements()) {
            String testquery = (String) zzz.nextElement();
            ParsedQuery p = ParsedQuery.parse(testquery);

            /*
            System.out.println( "\nQuery is " + p.query );
             System.out.println("\nGraphs part is "+p.graphsPart);
             System.out.println("\nVariables part is "+p.variablesPart);
             System.out.println("\nTriples part is "+p.triplesPart);
             System.out.println("\nEnd part is "+p.endPart);
             System.out.println("\nUsing part is "+p.usingPart);
             */

            System.out.println("\nGraphs vector is " + p.graphs);
            System.out.println("\nVariables vector is " + p.variables);
            System.out.println("\nTriples vector is ");

            Enumeration e = p.triples.elements();
            while (e.hasMoreElements()) {
                org.desire.rudolf.rdf.Triple t =
                        (org.desire.rudolf.rdf.Triple) e.nextElement();
                System.out.println("\n!!" + t.toString());
            }
            System.out.println("\nConstraints vector is " + p.constraints);
            System.out.println("\nNamespaces hash is " + p.ns);
        } //end while
    }

    /////////////////////////////////////////////////
    ////////////// experimental RDRFPath parsing
    ///////////// see http://zoe.mathematik.Uni-Osnabrueck.DE/QAT/qat.html
    //////////// RDFPath notes by Stefan Kokkelink
    String getRDFPathGraph(String q) {
        int i = q.indexOf("graph(");
        int j = q.indexOf(")");
        if (i != -1 && j != -1) {
            graphsPart = q.substring(i + 6, j).trim();
            if (graphsPart.startsWith("'")) {
                graphsPart = graphsPart.substring(1);
            }
            if (graphsPart.endsWith("'")) {
                graphsPart =
                        graphsPart.substring(0, graphsPart.length() - 1);
            }
            graphs.addElement(graphsPart);
            String tmp = q.substring(j + 1).trim();
            if (tmp.startsWith("/")) {
                tmp = tmp.substring(1);
            }
            return tmp;
        } else {
            ErrorLog.write(
                    "PG:RDFPath: [1]syntax problem with 'graph()' clause " + q);
            return q;
        }
    }

    org.desire.rudolf.rdf.Triple getRDFPathResource(String q, int i) {
        return getRDFPathVals(q, "resource", i);
    }

    org.desire.rudolf.rdf.Triple getRDFPathLiteral(String q, int i) {
        return getRDFPathVals(q, "literal", i);
    }

    void getRDFPathAll(String q, int i) {
        getRDFPathVals(q, "*", i);
    }

    void getRDFPathChild(String q, int count) {
        String subject = "?var" + (count - 1);
        String predicate = "?pred" + (count);
        String object = "?var" + (count);
        if (currentV != null) {
            ////
            subject = currentV;
        }
        currentV = object;
        int i = q.indexOf("child(");
        int j = q.indexOf(")");
        if (i != -1 && j != -1) {
            String tripleClause = q.substring(i + 8, j);
            ///check if resource
            if (resClause != null) {
                org.desire.rudolf.rdf.Triple tt =
                        getRDFPathResLit(resClause, "resource", count);
                if (tt.getSubject() != null) {
                    subject = tt.getSubject();
                }
                if (tt.getPredicate() != null) {
                    predicate = tt.getPredicate();
                }
                if (tt.getObject() != null) {
                    object = tt.getObject();
                }
                resClause = null;
            } else if (litClause != null) {
                org.desire.rudolf.rdf.Triple tt =
                        getRDFPathResLit(litClause, "literal", count);
                if (tt.getSubject() != null) {
                    subject = tt.getSubject();
                }
                if (tt.getPredicate() != null) {
                    predicate = tt.getPredicate();
                }
                if (tt.getObject() != null) {
                    object = tt.getObject();
                }
                litClause = null;
            }
            if (!tripleClause.trim().equals("")) {
                predicate = tripleClause;
            }
        } else {
            ErrorLog.write(
                    "PG:RDFPath: [2] syntax problem with 'graph()' clause " + q);
        }
        org.desire.rudolf.rdf.Triple t =
                new org.desire.rudolf.rdf.Triple(nsTransform(subject),
                nsTransform(predicate), nsTransform(object));
        triples.addElement(t);
    }

    void getRDFPathPredicate(String q, int i) {
        getRDFPathVals(q, "", i);
    }

    void getRDFPathParent(String q, int count) {
        String subject = "?var" + (count - 1);
        String predicate = "?pred" + (count);
        String object = "?var" + (count);
        if (currentV != null) {
            ////
            subject = currentV;
        }
        currentV = subject;
        int i = q.indexOf("parent(");
        int j = q.indexOf(")");
        if (i != -1 && j != -1) {
            String tripleClause = q.substring(i + 7, j);
            ///check if resource
            if (resClause != null) {
                org.desire.rudolf.rdf.Triple tt =
                        getRDFPathResLit(resClause, "resource", count);
                if (tt.getSubject() != null) {
                    object = tt.getSubject(); //????
                }
                if (tt.getPredicate() != null) {
                    predicate = tt.getPredicate();
                }
                //		if(tt.object!=null){
                //		object=tt.object;
                //		}
                resClause = null;
            } else if (litClause != null) {
                org.desire.rudolf.rdf.Triple tt =
                        getRDFPathResLit(litClause, "literal", count);
                if (tt.getSubject() != null) {
                    object = tt.getSubject(); //??
                }
                if (tt.getPredicate() != null) {
                    predicate = tt.getPredicate();
                }
                //if(tt.object!=null){
                //object=tt.object;
                //}
                litClause = null;
            }
            if (!tripleClause.trim().equals("")) {
                predicate = tripleClause;
            }
        } else {
            ErrorLog.write(
                    "PG:RDFPath: [2] syntax problem with 'graph()' clause " + q);
        }
        org.desire.rudolf.rdf.Triple t =
                new org.desire.rudolf.rdf.Triple(nsTransform(subject),
                nsTransform(predicate), nsTransform(object));
        triples.addElement(t);
    }

    //checks to see if we know about the namespace
    public String nsTransform(String s) {
        if (s != null) {
            int uiu = s.indexOf(":");
            if (uiu != -1) {
                String key = s.substring(0, uiu);
                String vv = s.substring(uiu + 1);
                if (ns.containsKey(key)) {
                    return (ns.get(key) + vv);
                } else {
                    return null;
                }
            } else {
                return s;
            }
        } else {
            return null;
        }
    }

    org.desire.rudolf.rdf.Triple getRDFPathVals(String q, String val,
            int count) {
        //for OR queries
        String taObject = null;
        //not sure what this variable shoudl be called
        String taSubject = "?var" + (count - 1);
        String subject = "?var" + (count - 1);
        String predicate = "?pred" + (count);
        String object = "?var" + (count);
        String mys = "?var" + (count - 2);
        String myp = "?pred" + (count - 1);
        String myo = "?var" + (count - 1);
        String ss = null;
        String pp = null;
        String oo = null;
        int i = q.indexOf(val + "(");
        int j = q.indexOf(")");
        int k = q.indexOf("[");
        int l = q.indexOf("]");
        ///check if resource/literal stored
        ///??
        if (resClause != null) {
            org.desire.rudolf.rdf.Triple tt =
                    getRDFPathResLit(resClause, "resource", count);
            if (tt.getSubject() != null) {
                mys = tt.getSubject();
            }
            if (tt.getPredicate() != null) {
                myp = tt.getPredicate();
            }
            if (tt.getObject() != null) {
                myo = tt.getObject();
            }
            triples.addElement(
                    new org.desire.rudolf.rdf.Triple(mys, myp, myo));
            resClause = null;
        } else if (litClause != null) {
            org.desire.rudolf.rdf.Triple tt =
                    getRDFPathResLit(litClause, "literal", count);
            if (tt.getSubject() != null) {
                mys = tt.getSubject();
            }
            if (tt.getPredicate() != null) {
                myp = tt.getPredicate();
            }
            if (tt.getObject() != null) {
                myo = tt.getObject();
            }
            triples.addElement(
                    new org.desire.rudolf.rdf.Triple(mys, myp, myo));
            litClause = null;
        }
        //_now_ set currentV
        if (currentV != null) {
            ////
            subject = currentV;
            ////???
            mys = currentV;
        }
        //make sure have brackets; if not then qname
        if (i != -1 && j != -1) {
            //System.out.println("VAL is "+val);
            //System.out.println("Q is "+q);
            //System.out.println("VAL LEN  is "+val.length());
            String tripleClause = q.substring((i + val.length() + 1), j);
            ///check if resource
            ///??
            if (!tripleClause.trim().equals("")) {
                subject = tripleClause;
                ss = subject;
                taObject = tripleClause;
            }
            //now look for filter stuff
            if (k != -1 && l != -1) {
                int m = q.indexOf("=");
                //i.e. if a sub and a pred
                if (m != -1 && (m < l && m > k)) {
                    predicate = q.substring(k + 1, m);
                    object = q.substring(m + 1, l);
                    oo = object;
                    //I think....
                    currentV = subject;
                } else {
                    //acts like a qname
                    String ppp = q.substring(k + 1, l);
                    predicate = ppp;
                    pp = predicate;
                    currentV = object;
                }
            } else {
                //		object="?fixme";
                currentV = object;
            }
            //////////////////
        }
        else {
            System.err.println("PG:RDFPath: [1]assuming q name ..." +
                    predicate + "...");
            if (q.equals("*")) {
                pp = predicate;
            } else {
                predicate = q;
                pp = predicate;
            }
            currentV = object;
        }
        //System.out.println("TRIPLExxxztest "+subject+" "+predicate+" "+object);
        org.desire.rudolf.rdf.Triple t =
                new org.desire.rudolf.rdf.Triple(nsTransform(subject),
                nsTransform(predicate), nsTransform(object));
        org.desire.rudolf.rdf.Triple tt =
                new org.desire.rudolf.rdf.Triple(nsTransform(ss),
                nsTransform(pp), nsTransform(oo));
        triples.addElement(t);
        /////////for now
        if (taObject != null) {
            triplesOR.addElement( new org.desire.rudolf.rdf.Triple(
                    nsTransform(taSubject), nsTransform(predicate),
                    nsTransform(taObject)));
        }
        //System.out.println("***cv[3] "+currentV);
        return tt;
    }

    org.desire.rudolf.rdf.Triple getRDFPathResLit(String q, String val,
            int count) {
        String subject = "?var" + (count - 2);
        String predicate = "?pred" + (count - 1);
        String object = "?var" + (count - 1);
        String ss = null;
        String pp = null;
        String oo = null;
        //System.out.println("*** cv [6] "+currentV);
        if (currentV != null) {
            ////
            subject = currentV;
        }
        int i = q.indexOf(val + "(");
        int j = q.indexOf(")");
        int k = q.indexOf("[");
        int l = q.indexOf("]");
        //System.out.println("!!!! i is "+i);
        //System.out.println("!!!! val is "+val);
        //System.out.println("!!!! q is "+q);
        if (i != -1 && j != -1) {
            //System.out.println("VAL is "+val);
            //System.out.println("Q is "+q);
            //System.out.println("VAL LEN  is "+val.length());
            String tripleClause = q.substring((i + val.length() + 1), j);
            ///check if resource
            //System.out.println("TC[1] is "+tripleClause);
            if (!tripleClause.trim().equals("")) {
                subject = tripleClause;
                ss = subject;
            }
            //now look for filter stuff
            if (k != -1 && l != -1) {
                int m = q.indexOf("=");
                //i.e. if a sub and a pred
                if (m != -1 && (m < l && m > k)) {
                    predicate = q.substring(k + 1, m);
                    object = q.substring(m + 1, l);
                    pp = predicate;
                    oo = object;
                    //I think
                    currentV = subject;
                    //System.out.println("*** cv [5] "+currentV);
                } else {
                    //acts like a qname
                    String ppp = q.substring(k + 1, l);
                    //System.out.println("PPP is "+ppp);
                    predicate = ppp;
                    pp = predicate;
                    currentV = object;
                }
            } else {
                //		object="?fixme";
                currentV = object;
            }
        } else {
            System.err.println("PG:RDFPath: [2]assuming q name");
            predicate = q;
            pp = predicate;
            currentV = object;
        }
        org.desire.rudolf.rdf.Triple t =
                new org.desire.rudolf.rdf.Triple(nsTransform(subject),
                nsTransform(predicate), nsTransform(object));
        org.desire.rudolf.rdf.Triple tt =
                new org.desire.rudolf.rdf.Triple(nsTransform(ss),
                nsTransform(pp), nsTransform(oo));
        /////////////////not sure about this....
        ////////////triples.addElement(tt);
        //System.out.println("TTTTTTT "+tt.toString());
        return tt;
    }

    String currentV = null;
    String resClause = null;
    String litClause = null;

    void parseQueryRDFPath() {
        currentV = null;
        ns.put("rss", "http://purl.org/rss/1.0/");
        ns.put("job", "http://ilrt.org/discovery/2000/11/rss-query/jobvocab.rdf#");
        ns.put("wn", "http://xmlns.com/wordnet/1.6/");
        ns.put("web", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        ns.put("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        ns.put("dc", "http://purl.org/dc/elements/1.1/");
        ns.put("abc", "http://ilrt.org/discovery/harmony/abc-0.1#");
        ns.put("xabc", "http://ilrt.org/discovery/harmony/abc-0.1#");
        ns.put("foaf", "http://xmlns.com/foaf/0.1/");
        ns.put("wot", "http://xmlns.com/wot/0.1/");
        //	String q = query.trim().toLowerCase();
        String q = query.trim();
        int yy = q.indexOf("/");
        String firstp = null;
        String rest = null;
        resClause = null;
        litClause = null;
        if (q.startsWith("graph(")) {
            q = getRDFPathGraph(q);
        }
        //case one clause
        String fp = q;
        Vector clauses = SearchReplace.splitByString(q, "/");
        //System.out.println("Q "+q);
        //System.out.println("[1]CLAUSES "+clauses);
        for (int z = 0; z < clauses.size(); z++) {
            fp = clauses.elementAt(z).toString();
            //System.out.println("FP "+fp);
            if (!fp.trim().equals("")) {
                //locStepAxis
                if (fp.startsWith("resource")) {
                    //System.out.println("1a z "+z+" cl si "+clauses.size());
                    if (z == 0 || z == 1) {
                        if (z == 0 && clauses.size() == 1) {
                            //System.out.println("1b - good");
                            getRDFPathResource(fp, z);
                        } else if (z == 1 && clauses.size() == 2) {
                            //System.out.println("1b - bad");
                            getRDFPathResource(fp, z);
                        } else {
                            //System.out.println("1c");
                            resClause = fp;
                        }
                    } else {
                        System.err.println(
                                "PQ:RDFPath:syntax error - resource only on first or second clause " + z);
                    }
                } else if (fp.startsWith("literal")) {
                    if (z == 0 || z == 1) {
                        //System.err.println("z is "+z);
                        //System.err.println("clauses size is "+clauses.size());
                        if (z == 0 && clauses.size() == 1) {
                            getRDFPathLiteral(fp, z);
                        } else if (z == 1 && clauses.size() == 2) {
                            getRDFPathLiteral(fp, z);
                        } else {
                            litClause = fp;
                        }
                    } else {
                        System.err.println(
                                "PQ:RDFPath:syntax error - literal only on first or second clause " + z);
                    }
                } else if (fp.startsWith("child")) {
                    getRDFPathChild(fp, z);
                } else if (fp.startsWith("parent")) {
                    getRDFPathParent(fp, z);
                }

                /*
                  //not done yet

                  	else if(fp.startsWith("element")){

                  	getRDFPathElement(fp,z);

                  	}
                  	else if(fp.startsWith("container")){

                  	getRDFPathContainer(fp,z);

                  	}
                  */

                //qname
                else if (fp.indexOf(":") != -1) {
                    getRDFPathPredicate(fp, z);
                }
                //.
                else if (fp.startsWith(".")) {
                    getRDFPathAll(fp, z); //???
                }
                //* */
                else if (fp.startsWith("*")) {
                    getRDFPathAll(fp, z);
                }
                //resource/literal
            }
            //System.out.println("\n\n***currentv is "+currentV+"\n");
        } //end loop
    } //end method
}


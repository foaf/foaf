#!/bin/sh
#
# genscutterstats.sh $Id: gen_scutterstats.sh,v 1.1 2002-07-11 13:18:26 danbri Exp $ danbri@w3.org
#
# a few simple queries that describe the contents of 
# some RDF query service. Note that we use raw SQL 
# instead of RDF query. This makes the script less
# generic, but allows us to make use of various features 
# rarely seen in RDF query.
#
# TODO: an RDF representation of all this
#
# see also: http://www.xml.com/lpt/a/2002/06/26/vocabularies.html



echo 'RDF Properties:'
echo 'select value, count(value)   from resources, triples where resources.key = triples.predicate group by value order by count(value)' | psql rdfweb1
    

echo 'RDF Classes:'
echo "select r2.value, count(r2.value) from resources r1, resources r2, triples t1 where r1.key = t1.predicate AND r1.value='http://www.w3.org/1999/02/22-rdf-syntax-ns#type' AND r2.key = t1.object group by r2.value order by count(r2.value);" | psql  rdfweb1
 

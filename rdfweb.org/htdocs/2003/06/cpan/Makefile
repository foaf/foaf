RDF=01mailrc.foaf.rdf

clean: zap 01mailrc.foaf.rdf

zap:
	rm -f $(RDF)

01mailrc.foaf.rdf:
	perl cpan2foaf.pl > $(RDF)


load:
	webdata --scutter=http://rdfweb.org/2003/06/cpan/perl-blog-dir.rdf
	webdata --scutter=http://rdfweb.org/2003/06/cpan/01mailrc.foaf.rdf


RDF=01mailrc.foaf.rdf

clean: zap 01mailrc.foaf.rdf weblogs.txt

weblogs.txt:
	ruby lookups.rb > weblogs.txt

zap:
	rm -f $(RDF)
	rm -f weblogs.txt

01mailrc.foaf.rdf:
	perl cpan2foaf.pl > $(RDF)


load:
	webdata --scutter=http://rdfweb.org/2003/06/cpan/perl-blog-dir.rdf
	webdata --scutter=http://rdfweb.org/2003/06/cpan/01mailrc.foaf.rdf


RDF=01mailrc.foaf.rdf

clean: zap 01mailrc.foaf.rdf

zap:
	rm -f $(RDF)

01mailrc.foaf.rdf:
	perl cpan2foaf.pl > $(RDF)

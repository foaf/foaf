check:
	gpg --quiet --verify ../foafsig index.html


sign:
	@echo "Sign your Schema!"
	gpg -sab index.rdf 
	mv index.rdf.asc ../foafsig

	gpg -sab index.html 
	mv index.html.asc ../htmlfoafsig
#


# should this be the rdf version i'm signing? both?


PARSER=/home/pldab/working/w3c/perl/modules/W3C/Rdf/bin/xrdfparser

test:
	$(PARSER) index.html



# generate the RDF version:
# http://xmlns.com/2002/07/extractrdf.xsl

#<danbri> oh
#<danbri> interesting problem
#<zool> best problems are :)
#<danbri> if the schema is content negotiable, and I sign either or both
#representation... how best to point to the sigs?
#<danbri> right now ../foafsig is what I get from signing the xhtml
#<danbri> not huge prob, just an admin nuisance

check:
	gpg --quiet --verify ../foafsig index.html
V=../../2003/vocabtable.rb

sign:
	@echo "Sign your Schema!"
	gpg -sab index.rdf 
	mv index.rdf.asc ../foafsig

	gpg -sab index.html 
	mv index.html.asc ../htmlfoafsig
#

vocabtable:
	rdfdump -o ntriples -q file:index.rdf  | $(V)  > table.html

getrdf:
	xsltproc ../../2002/07/extractrdf.xsl index.html > index.rdf

# should this be the rdf version i'm signing? both?


# generate the RDF version:
# http://xmlns.com/2002/07/extractrdf.xsl

#<danbri> oh
#<danbri> interesting problem
#<zool> best problems are :)
#<danbri> if the schema is content negotiable, and I sign either or both
#representation... how best to point to the sigs?
#<danbri> right now ../foafsig is what I get from signing the xhtml
#<danbri> not huge prob, just an admin nuisance

#V=../../2003/vocabtable.rb
V=./specgen.rb
GENFILES=../foafsig ../htmlfoafsig *.asc -f index.html table.html rdf_vdl.nt

# todo: vocabtable.rb is kinda primitive, and expects different input
# to the way we invoke it in this Makefile.

all: clean colour.html index.html
clean: tidyup 

tidyup:
	rm -f $(GENFILES)
	
rdf_vdl.nt:
	rapper -o ntriples -q file:index.rdf > rdf_vdl.nt

index.html:
	ruby ./specgen.rb > index.html

xxxindex.html: index.rdf rdf_vdl.nt table.html wip.html 
	rm -f index2.html
	cat wip.html > index2.html
	cat table.html >> index2.html
	cat index.rdf >> index2.html
	echo "</body></html>" >> index2.html
	mv -f index2.html index.html

check:
	gpg --quiet --verify ../foafsig index.html

sign:
	@echo "Sign your Schema!"
	gpg --quiet -sab index.rdf 
	gpg --quiet -sab index.html 

sigfiles: sign
	mv -f index.rdf.asc ../foafsig
	mv -f index.html.asc ../htmlfoafsig

# We need Redland installed, currently.
table.html:
	rapper -o ntriples -q file:index.rdf  | $(V)  > table.html

# We used to keep master copy in the HTML, now we do the opposite.
getrdf:
	xsltproc ../../2002/07/extractrdf.xsl index.html > index.rdf


publish:
	ssh xmlns.com make slaves


htaccess:
	ruby ../../2003/apacheconf.rb > .htaccess

colour.html:
	rm -f colour.html
	xsltproc colour-xml-serializer.xsl index.rdf > colour.html




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

#V=../../2003/vocabtable.rb
V=./specgen.rb
GENFILES=../foafsig ../htmlfoafsig *.asc -f index.html table.html rdf_vdl.nt


# GET http://xmlns.com/foaf/0.1/ | gpg --verify ../htmlfoafsig -

all: clean colour.html index.html
clean: tidyup 

#site:	all sigfiles
site:	all
	cd ../; cvs commit -m 'regenerated foaf spec docs';ssh rdfweb.org make slaves

tmpsite:
	make; cp index.html _tmp.html; cvs commit -m 'regen' _tmp.html;ssh rdfweb.org make slaves


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

# todo: need to get agent working...
sign:
	@echo "Sign your Schema!"
	rm -f index.rdf.asc index.html.asc
	gpg --no-use-agent --quiet -sab index.rdf 
	gpg --no-use-agent --quiet -sab index.html 

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
	ssh xmlns.com "cd /var/web/sites/slave/xmlns.com/; cvs update -d"


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

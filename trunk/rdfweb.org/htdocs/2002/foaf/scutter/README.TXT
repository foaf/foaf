
SCUTTER - an RDF harvesting and indexing package.


This is Scutter, a package that harvests, indexes, stores and queries
RDF collected from the Web.

It it written in Ruby, and requires various other bits of software to 
run. QUICKSTART.TXT gives you the basics. doc/scutter.html provides a bit
more detail.


The main script is bin/scutter

To set it harvesting, use 'bin/scutter --scutter'. If you pass it 
a URL, it'll start there, if not, it'll start at the author's 
FOAF self-description file and explore the Web of files 
connected from there.




XSLT RDF parser

You can also try using the XSLT RDF parser by Max Froumentin, which is 
included in the distribution (in conf/). To use this:

 	bin/scutter --use-xslt --scutter

Note that this requires 'xsltproc' to be installed. 
On debian, 'apt-get install xsltproc' will do this.

This parser (and its integration into RubyRDF) hasn't been well tested.
There may be some RDF files that, for example, generate N-Triples that 
the RubyRDF N-Triples parser can't deal with. Multi-line output etc.
-- work in progress.





INTERESTING DATA

These might be useful...

 http://www.kanzaki.com/info/rss.rdf     
  - RSS feed. mix of Japanese and English.

 http://jibbering.com/rdf/highlight-3.svg?url=http://jibbering.com/rdfweb/1020892863024.rdf
  - SVG image annotations, see RDF URL too.





--
Dan Brickley
July 2002

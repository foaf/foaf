
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


Win32 Usage
===========

bin/scutter --dbdriver=Mysql --use-xslt

...should work, given various as-yet undocumented constraints (cygwin?
xsltproc...). A schema for MySQL is in conf/ subdirectory. PostgreSQL
isn't available for Windows, so use Win32 MySQL.

Known problems:

  * the field in table 'resources' called 'key' is now 'hashkey', since
    'key' was a controlled name. TODO: push this change back into PG version.
  * SQL variants. Use query.toSQLQuery({'quotevars'=>1}) to generate 
    MySQL-happy queries.
  * The (pragmatic programmers') Win32 Ruby I'm using is core dumping 
    periodically. Investigating.

Win32 Redland usage:

 ruby bin/scutter --use-raptor=./redparse/rdfdump --dbdriver=Mysql --scutter
 ...also works.

 (assuming you have a binary of the 'rdfdump' utility in the 
  redparse directory. this isn't provided (ahem, though for temp convenience
  look http://tux.w3.org/~danbri/redland/ here, and rename the .exe).




INTERESTING DATA

These might be useful...

 http://www.kanzaki.com/info/rss.rdf     
  - RSS feed. mix of Japanese and English.

 http://jibbering.com/rdf/highlight-3.svg?url=http://jibbering.com/rdfweb/1020892863024.rdf
  - SVG image annotations, see RDF URL too.





--
Dan Brickley
July 2002

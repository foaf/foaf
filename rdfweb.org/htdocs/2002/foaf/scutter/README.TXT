
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



Debian

Currently, the debian installer is putting files in:
	/usr/lib/ruby/site_ruby/1.6/basicrdf.rb
...instead of wherever else they're supposed to go. Investigating.

	sudo cp -R /usr/lib/ruby/site_ruby/1.6/RDF4R* /usr/lib/ruby/1.6/
  	sudo cp /usr/lib/ruby/site_ruby/1.6/basicrdf.rb /usr/lib/ruby/1.6/
  	sudo cp /usr/lib/ruby/site_ruby/1.6/squish.rb /usr/lib/ruby/1.6/

For DBI storage, we want 
	apt-get install libdbi-ruby

...and the DBI driver for postgres:
	apt-get install libdbd-pg-ruby

For XSLT,
	apt-get install xsltproc

To set up PostgresSQL
	apt-get install postgresql

...and we'll want a new user, in my case called 'danbri', to work as:

	sudo su postgres /usr/lib/postgresql/bin/createuser danbri

	/usr/lib/postgresql/bin/createdb -E UNICODE rdfweb1
	/usr/bin/psql rdfweb1 < conf/scutterdb.sql

Raptor RDF parser

	Grab from http://www.redland.opensource.ac.uk/raptor/

	apt-get install libxml2 libxml2-dev
	...use libxml in preference to Expat
	For raptor:
		./configure
		make
		make test
		make install

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


PATHFINDER


try:

bin/paths --input=doc/sample_pathdata.txt --search=mailto:libby.miller@bristol.ac.uk
bin/paths --input=doc/sample_pathdata.txt --search=mailto:amy@w3.org  

(or see Makefile for more demos)

...code to find paths thru the data. needs documenting.


--
Dan Brickley
July 2002


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


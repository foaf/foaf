XSLT=xsltproc
H2R=news/home2rss.xsl
HTML=index.html

BASE="'http://rdfweb.org/'"
CHANNEL="'http://rdfweb.org/news/home.rss'"
PAGE="'http://rdfweb.org/'"


homepage: build commitnews

commit:	build commitall 

commitnews: 
	cvs commit news/home.rss
	cvs commit index.html

commitall: cvs commit


build:	rssfeed

rssfeed:	home.rss

home.rss:
	$(XSLT) --param Base $(BASE) --param Page $(PAGE)  \
         --param Channel $(CHANNEL) $(H2R) $(HTML) > news/home.rss


find:
	find . -name \*html  -exec echo {} \; -exec grep yahoo {} \;

SCRIPTS=.
INDEXPAGE=names.html


index:	$(SCRIPTS)/imgquery.pl $(INDEXPAGE)


names.html: 
	$(SCRIPTS)/imgquery.pl > $(INDEXPAGE)

clean:
	rm $(INDEXPAGE)

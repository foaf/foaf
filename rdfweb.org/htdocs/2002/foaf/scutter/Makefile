VERSION=pre-alpha

dist:	dirs
	cd ../; tar -zcvf software/scutter_$(VERSION).tar.gz `xargs<scutter/FILES`

clean:
	rm *.bak
	rm *.tmp

dirs:
	mkdir -p ./webcache/_nt/


destroydata:
	rm -f webcache/*.rdf
	rm -f webcache/*.meta
	rm -f webcache/_nt/*.nt
	rm -f webcache/_nt/*.rdf
	

harvest:
	bin/scutter --scutter

xsltharvest:
	bin/scutter --use-xslt --scutter

stats:
	bin/vocabstats.rb


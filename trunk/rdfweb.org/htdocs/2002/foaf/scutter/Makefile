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
	

harvest:
	bin/scutter --scutter

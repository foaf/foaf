VERSION=pre-alpha

dist:	dirs
	cd ../; tar -zcvf software/scutter_$(VERSION).tar.gz `xargs<scutter/FILES`

clean:
	rm *.bak
	rm *.tmp

dirs:
	mkdir -p ./webcache/_nt/

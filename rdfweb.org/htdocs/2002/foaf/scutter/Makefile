VERSION=pre-alpha

dist:	dirs
	cd ../; tar -zcvf software/scutter_$(VERSION).tar.gz `xargs<scutter/FILES`


install:
	ruby ./install.rb

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

winharvest:
	bin/scutter --dbdriver=Mysql --use-xslt --scutter

redwin:
	ruby bin/scutter --use-raptor=./redparse/rdfdump --dbdriver=Mysql --scutter

 
stats:
	bin/vocabstats.rb


foafpathdemo:
	bin/paths --input=doc/sample_pathdata.txt 

foafcorpdemo:
	bin/paths --input=doc/sample_foafcorp.txt --search=microsoft  

dot:
	dot -Tsvg seealso.dot > seealso.svg

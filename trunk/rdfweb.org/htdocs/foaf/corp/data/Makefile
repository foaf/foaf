# a bunch of ugly hack!


rdf:	corprdf ntriples

corprdf:
	./rule2foaf.rb > _corpdata.rdf

links:	dumplinks preplinks

dumplinks:
	cat exportcorplinks.sql | mysql foafcorp --user=foaf --pass=foafcorp > _corplinks.dat

preplinks:
	 ./link2pathfind.rb > _corplinks-simple.dat
	cp _corplinks-simple.dat ../java/
           



ntriples:
	redparse _corpdata.rdf > _corpdata.nt



mklinkschema:
	 cat onboard-table.txt | mysql foafcorp --user=foaf --pass=foafcorp

loadlinktable:
	 cat onboard.txt | mysql foafcorp --user=foaf --pass=foafcorp

mklinksql:
	./rule2foaf.rb | grep insert> onboard.txt 


CP=.:lib/jena.jar:lib/rdffilter.jar:lib/sax2.jar:lib/xerces.jar 

all:	classes test

classes:
	CLASSPATH=$(CP) \
	javac org/rdfweb/viz/*java


test:
	CLASSPATH=$(CP) \
	java org.rdfweb.viz.BasicDraw



test2:
	CLASSPATH=$(CP) \
	 ./run.sh

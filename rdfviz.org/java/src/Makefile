
BASE=./
CP=$(BASE):$(BASE)lib/jena.jar:$(BASE)lib/rdffilter.jar:$(BASE)lib/sax2.jar:$(BASE)lib/xerces.jar 

all:	classes test

#
# export CLASSPATH=`make cp`
cp:
	@echo $(CP)

classes:
	CLASSPATH=$(CP) \
	javac org/rdfweb/viz/*java


test:
	CLASSPATH=$(CP) \
	java org.rdfweb.viz.BasicDraw



test2:
	CLASSPATH=$(CP) \
	 ./run.sh

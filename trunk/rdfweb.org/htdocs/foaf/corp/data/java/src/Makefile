all:	compile dist

compile:
	javac *.java

dist:
	jar -cvfm ../foafpath.jar  META-INF/MANIFEST.MF  . 

test:
	java -jar ./foafpath.jar





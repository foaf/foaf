#!/bin/sh

PLATFORM=`uname`

CLASSPATH=build:lib/jmdns.jar:lib/jface.jar:lib/runtime.jar

JAVA=java

MAINCLASS=org.rdfweb.foafcon.FoafFingerController

if [ x$PLATFORM == xLinux ]
then
	LIB_PATH=lib_linux
	CLASSPATH=$CLASSPATH:$LIB_PATH/swt.jar:$LIB_PATH/swt-pi.jar
elif [ x$PLATFORM == xDarwin ]
then
	LIB_PATH=lib_mac
	CLASSPATH=$CLASSPATH:$LIB_PATH/swt.jar:$LIB_PATH/swt-pi.jar
	JAVA=$LIB_PATH/java_swt
else
	echo "I don't work on the platform: '$PLATFORM'" >&2
	exit 1
fi

exec $JAVA -cp $CLASSPATH -Djava.library.path=$LIB_PATH $MAINCLASS $1 "$2" $3 $4

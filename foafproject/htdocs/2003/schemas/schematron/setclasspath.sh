for i in xalan-jars/*.jar
do
CLASSPATH=$CLASSPATH:$i
done

export CLASSPATH

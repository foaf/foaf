for i in ../xalan-jars/*.jar
do
CLASSPATH=$CLASSPATH:$i
done

export CLASSPATH


# bash usage: . setclasspath.sh 

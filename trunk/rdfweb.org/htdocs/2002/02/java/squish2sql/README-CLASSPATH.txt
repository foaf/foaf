# Set up your classpath right. You need some JDBC stuff for PostgreSQL
# and Inkling for representing queries.
# for easy setup, source this file: . ./README-CLASSPATH.txt

# which version of Java do we need? java.security sha1 for example...
# 
# http://java.sun.com/j2se/1.4/download.html


#old version, entire Inkling library used 
#export CLASSPATH=$CLASSPATH:.:./jars/rdfquery.jar:./jars/pgjdbc2.jar

# slimline version, minimalistic. (note: doesn't include previous classpath)
#
export CLASSPATH=:.:./jars/rewrite-helpers.jar:./jars/pgjdbc2.jar

# starkminimal:

#export CLASSPATH=:./jars/rewrite-helpers.jar:./jars/pgjdbc2.jar

# Or run with no CLASSPATH. If you're not doing actual PostgreSQL/JDBC queries
# the following works:
#
#java -cp jars/rewrite-helpers.jar Squish2SQL sql/test1.squish           


# Then you need some data...
# I dumped and locally resorred postgres database using:
#	 pg_dump -h 10.0.2.17 codepict > codepict.SQL      
#	createdb codepict; psql codepict < codepict
# 
# to query it, see Makefile examples




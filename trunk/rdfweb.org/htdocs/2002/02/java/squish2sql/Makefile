

rewriter:
	javac Squish2SQL.java


packagejars:
	jar -cvf jars/rewrite-helpers.jar org/ Squish2SQL.*

jartest:
	java -cp jars/rewrite-helpers.jar Squish2SQL sql/test1.squish           



query1:
	@java Squish2SQL 'sql/test1.squish' 

search1:
	@java Squish2SQL 'sql/test1.squish' search codepict


query2:
	@java Squish2SQL 'sql/test2.squish'

search2:
	@java Squish2SQL 'sql/test2.squish' search test1
 

query3: 
	@java Squish2SQL 'sql/test3.squish'

search3: 
	@java Squish2SQL 'sql/test3.squish' search test1



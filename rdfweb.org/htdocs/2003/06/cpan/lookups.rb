#!/usr/bin/ruby
#
# $Id: lookups.rb,v 1.2 2003-06-07 22:04:55 danbri Exp $
require 'squish'
require 'basicrdf'
require 'dbi'

oldq = SquishQuery.new.parseFromText "SELECT ?name, ?homepage, ?mb, ?x, 
	WHERE 	(foaf::name ?x ?name)
		(foaf::myersBriggs ?x ?mb)
		(foaf::homepage ?x ?homepage)
	USING 	foaf for http://xmlns.com/foaf/0.1/"

q = SquishQuery.new.parseFromText "SELECT ?name, ?homepage, ?weblog, ?x, 
	WHERE 	(foaf::name ?x ?name)
		(dc::contributor ?x http://www.cpan.org/)
		(foaf::homepage ?x ?homepage)
		(foaf::weblog ?x ?weblog)
	USING 	dc for http://purl.org/dc/elements/1.1/
		foaf for http://xmlns.com/foaf/0.1/"

##		(dc::contributor ?x http://www.cpan.org/)



q = SquishQuery.new.parseFromText "SELECT ?name, ?homepage, ?weblog, ?x, 
	WHERE 	(foaf::name ?x ?name)
		(dc::contributor ?x http://www.cpan.org/)
		(foaf::homepage ?x ?homepage)
		(foaf::weblog ?x ?weblog)
	USING 	dc for http://purl.org/dc/elements/1.1/
		foaf for http://xmlns.com/foaf/0.1/"

dbi_driver = 'DBI:Pg:scutter1'
DBI.connect(  dbi_driver, 'danbri' ,  '' ) do | dbh |
  dbh.select_all( q.toSQLQuery  ) do | x |
    p=ResultRow.new x
    puts "#{p.name} (#{p.homepage} weblog: #{p.weblog})\n";
  end
end


#!/usr/bin/ruby
#
# $Id: lookups.rb,v 1.4 2003-06-11 09:34:33 danbri Exp $
#
# Lookup data from local SQL-backed RDF store.

require 'squish'
require 'basicrdf'
require 'dbi'

q = SquishQuery.new.parseFromText "SELECT ?name, ?homepage, ?weblog, ?x, ?feed, 
	WHERE 	(foaf::name ?x ?name)
		(dc::contributor ?x http://www.cpan.org/)
		(foaf::homepage ?x ?homepage)
		(foaf::weblog ?x ?weblog)
		(foaf::rssfeed ?weblog ?feed)
	USING 	dc for http://purl.org/dc/elements/1.1/
		foaf for http://xmlns.com/foaf/0.1/"

DBI.connect('DBI:Pg:scutter1', 'danbri' ,  '') do |dbh|
  dbh.select_all(q.toSQLQuery) do |res|
    p=ResultRow.new res
    puts "#{p.name} homepage: #{p.homepage} weblog: #{p.weblog} rss: #{p.feed}\n";
  end
end 


# Other stuff to filter on?
# 		(foaf::myersBriggs ?x ?mb)


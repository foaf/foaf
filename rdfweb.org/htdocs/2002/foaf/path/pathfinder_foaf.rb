#!/usr/bin/env ruby
#
# RDFWeb CoDepiction index generator
#
# Look up photos do-depicting some person, via their mailbox.
# Generate HTML.
# Assumes data has been drafragmented / smushed.
#
# danbri $Id: pathfinder_foaf.rb,v 1.3 2002-07-11 13:19:05 danbri Exp $

require 'squish'
require 'dbi'

# see http://rdfweb.org/people/damian/2002/02/foafnation/


# todo: fix squish parser so final comma not needed here:
query=<<EOQ;

	SELECT ?mbox1, ?mbox2, ?uri,  
	WHERE
       (foaf::depiction ?x ?img)
       (foaf::depiction ?z ?img)
       (foaf::thumbnail ?img ?uri)
       (foaf::mbox ?x ?mbox1)
       (foaf::mbox ?z ?mbox2)
	USING foaf for http://xmlns.com/foaf/0.1/

EOQ

DBI_DRIVER = 'DBI:Pg:rdfweb1'
DBI_USER = 'danbri'
DBI_PASS=''

q = SquishQuery.new.parseFromText query

DBI.connect( DBI_DRIVER, DBI_USER, DBI_PASS) do | dbh |
  dbh.select_all( q.toSQLQuery  ) do | row |
    puts row.inspect
 
   end
end


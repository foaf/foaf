#!/usr/bin/env ruby
#
# $Id: myScutter.rb,v 1.2 2002-12-12 17:10:11 danbri Exp $
# A sample scutter implementation, using the framework defined in ayf.rb
# (ayf.rb is still usable as a script, and includes a few default handlers)
#
#

require './ayf'

def go(uri)
  foaf='http://xmlns.com/foaf/0.1/' 
  contact="http://www.w3.org/2000/10/swap/pim/contact#" 
  air= 'http://www.megginson.com/exp/ns/airports#'

  ayf = SimpleScutter.new uri
  pagecount=0

  # a code block to output basic info about each RDF page encountered
  # 
  page_summary = Proc.new do |crawler,page|  
    puts "RDFDOC: count='#{pagecount}': uri:#{crawler.uri} gave RDF graph #{page} \
	with #{page.size} triples\n" 
    pagecount=pagecount+1
  end

  # a code block to see if some page provides nearestAirport information:  
  #
  #  <contact:nearestAirport><wn:Airport air:icao="EGGD" air:iata="BRS"/>...
  #
  airports = Proc.new do |crawler,page| 

    #page.reg_xmlns 'http://www.megginson.com/exp/ns/airports#', 'air'
   
    rs = page.ask Statement.new(nil, air+"iata", nil)
    rs.objects.each do |a|
      a.graph=page
      puts "AIRPORT: #{a} got airport code in #{crawler.uri})" if (a.to_s =~ /\S/) 
    end					# the 'if' is fix for parser bug
  end

  # stats to be output at start of each loop      
  #
  loopstats = Proc.new do |s|
    puts "INIT: s.left.size=#{s.left.size} s.seen.size=#{s.seen.size} current: #{s.uri}"
  end

  error_logger = Proc.new {|e| puts "ERROR: #{e}" }


#trying to find src of memory bloat:
#objstats=nil
#ayf.pagehandlers.push Proc.new {|c,page| 
#  objstats=Hash.new(0) # empty and recount each loop
#  ObjectSpace.each_object{|x| 
#    objstats[x.class.name]=objstats[x.class.name]+1
#  }
#  puts "ObjectStats: #{objstats.inspect}\n"
#}


  # register some handlers:
  ayf.pagehandlers.push page_summary, airports
  ayf.inithandlers.push loopstats
  ayf.errorhandlers.push error_logger 
  ayf.run  # set crawler running!
end 

################################################################# 

start_uri = 'http://rdfweb.org/people/danbri/rdfweb/danbri-foaf.rdf' 
start_uri = ARGV.shift if ARGV.length > 0
go(start_uri)

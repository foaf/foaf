#!/usr/bin/env ruby
# who.rb - a very simple rdf crawler - $Id: who.rb,v 1.2 2002-12-12 17:10:11 danbri Exp $ 
require './ayf'
#require 'profile'

uri = 'http://rdfweb.org/people/danbri/rdfweb/danbri-foaf.rdf' 
uri = ARGV.shift if ARGV.length > 0
ayf = SimpleScutter.new uri
FOAF='http://xmlns.com/foaf/0.1/'
i=1

ayf.inithandlers.push Proc.new {|s|
#  puts "INIT: size=#{s.left.size} seen:=#{s.seen.size} uri: #{s.uri}"
}
rs=nil
objstats=nil
ayf.pagehandlers.push Proc.new {|c,page| 
#  rs = page.ask Statement.new(nil, FOAF+"name", nil)
#  rs.objects.each {|a|
#    puts "#{a}  \t\t\t uri: #{c.uri}" 
#  }
  objstats=Hash.new(0) # empty and recount each loop
  ObjectSpace.each_object{|x| 
    objstats[x.class.name]=objstats[x.class.name]+1
  }
  puts "ObjectStats: #{objstats.inspect}\n"
}
ayf.pagehandlers.push Proc.new {|c,page|  
  puts "RDF: no.:'#{i}': uri:#{c.uri} size:#{page.size}"; i=i+1
}
ayf.errorhandlers.push Proc.new {|e| puts "ERROR: #{e}" }

ayf.run

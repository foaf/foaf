#!/usr/bin/env ruby
# who.rb - a very simple rdf crawler - $Id: who.rb,v 1.3 2002-12-16 20:52:18 danbri Exp $ 

require './ayf'

uri = 'http://rdfweb.org/people/danbri/rdfweb/danbri-foaf.rdf' 
uri = ARGV.shift if ARGV.length > 0
ayf = SimpleScutter.new uri
FOAF='http://xmlns.com/foaf/0.1/'
i=1
ayf.inithandlers.push Proc.new {|s|
  puts "INIT: size=#{s.left.size} seen:=#{s.seen.size} uri: #{s.uri}"
}
rs=nil
ayf.pagehandlers.push Proc.new {|c,page| 
  rs = page.ask Statement.new(nil, FOAF+"name", nil)
  rs.objects.each {|a|
    puts "#{a}  \t\t\t uri: #{c.uri}" 
  }
}
ayf.pagehandlers.push Proc.new {|c,page|  
  puts "RDF: no.:'#{i}': uri:#{c.uri} size:#{page.size}"; i=i+1
}
ayf.errorhandlers.push Proc.new {|e| puts "ERROR: #{e}" }
ayf.run

#!/usr/bin/env ruby

require 'date'
require 'dbi'

driver = 'Mysql'

# Ruby script to query a (certain shape) SQL-backed RDF store, generate summary
# $Id: vocabstats.rb,v 1.3 2002-12-16 20:52:18 danbri Exp $
# author: dan brickley

today = Date.today
yyyy = today.year
mm = today.month
dd = today.day


# config info
# todo: load from elsewhere
#
logfilename = "log/vocabstats-#{yyyy}-#{mm}-#{dd}.log"		# where to log
cfg = {'dbi_driver' => 'DBI:'+driver+':rdfweb1', 'dbi_user'=>'danbri'}  # db to analyse

def table (q=nil, cfg={})
  hits=[]
  DBI.connect(  cfg['dbi_driver'], cfg['dbi_user'], cfg['dbi_pass']  ) do | dbh |
    dbh.select_all( q ) do | row |
      # puts "Row: #{row.inspect} \n" 
      hits.push row.clone
    end
  end
  return hits
end

propq = 'select value, count(value)   from resources, triples where resources.keyhash = triples.predicate group by value order by  count(value)' 
propinfo =  table propq, cfg

classq = "select r2.value, count(r2.value) from resources r1, resources r2, triples t1 where r1.keyhash = t1.predicate AND r1.value='http://www.w3.org/1999/02/22-rdf-syntax-ns#type' AND r2.keyhash  = t1.object group by r2.value order by count(r2.value)"
classinfo =  table classq, cfg 
 

log = <<EOT
<rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
	xmlns:stat="http://xmlns.com/2002/07/stat#">
EOT

log += "<!-- stats for an RDFWeb aggregation point. todo: add service description info here. -->\n\n"


propinfo.each do |prop, stat| 
  log += "<stat:PropertyCount>\n"
  log += "<stat:property rdf:resource=\"#{prop}\" />\n"
  log +=  "<stat:count>#{stat}</stat:count>\n"
  log += "</stat:PropertyCount>\n\n"
end
puts classinfo.inspect

classinfo.each do |cl, stat| 
    log += "<stat:ClassCount>\n"
    log += "<stat:class rdf:resource=\"#{cl}\" />\n"
    log += "<stat:count>#{stat}</stat:count>\n"
    log += "</stat:ClassCount>\n\n"
end 

log += "\n</rdf:RDF>\n\n"

begin

 if File::exists? logfilename
    File::delete logfilename # one log per day 
 end

  logfile = File::new( logfilename, File::CREAT|File::RDWR, 0644)   
  logfile.puts log
  logfile.close
rescue
  puts "Error logging vocabulary stats"
end



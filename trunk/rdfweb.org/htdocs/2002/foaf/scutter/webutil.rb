#!/usr/bin/env ruby

require 'basicrdf'
require 'squish' 			
require 'dbi'
require 'net/http'
require 'sha1'
require 'getoptlong'


# webutil.rb 
# 
# $Id: webutil.rb,v 1.15 2002-07-16 21:03:25 danbri Exp $
#
# Copyright 2002 Dan Brickley 
#
# 
#    This program is free software; you can redistribute it and/or modify
#    it under the terms of the GNU General Public License as published by
#    the Free Software Foundation; either version 2 of the License, or
#    (at your option) any later version.
#
#    This program is distributed in the hope that it will be useful,
#    but WITHOUT ANY WARRANTY; without even the implied warranty of
#    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#    GNU General Public License for more details.
#
#    You should have received a copy of the GNU General Public License
#    along with this program; if not, write to the Free Software
#    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307 USA





#########################################################################################
#
# fetch and cache: derference a uri, store locally, return its local handle (numeric string)
#
def fetch_and_cache (uri, cache_dir='./', proxy=true, opts={} )

  user_agent = 'RDFWeb-Scutter-200207;http://rdfweb.org/foaf/'
  my_headers = {'Accept' => 'application/rdf+xml', 'User-agent' => user_agent } 

  proxy_addr = 'cache-edi.cableinet.co.uk' # todo: add to config
  proxy_port = 8080

  raise "scutter: fetch_and_cache: fetch remote: warning, bNode URI (not fetchable)" if uri =~ /^\[/
  raise "scutter: fetch_and_cache: fetch remote: unsupported URI scheme (http: only, sorry) #{uri}" if ! uri =~ /^http:/

  uri_hash = hashcodeIntFromString(uri)
  rdf_fn =  "#{cache_dir}webcache/rdf-#{uri_hash}.rdf" 

  uri =~ /:\/\/([^\/]+)(\/.*)$/
  gzipped=false
  host = $1
  res = $2
  data=''
  resp=''
  h = Net::HTTP::new host

  begin 
    if proxy
      puts "Fetching (via proxy). HTTP GET: host=#{host} res=#{res}" 
      Net::HTTP::Proxy(proxy_addr, proxy_port).start( $1 ) do |http|
        resp, data = http.get(res, my_headers)
      end
      gzipped = true if resp['Content-encoding'] =~ /gzip/
    else
      resp, data = h.get(res, my_headers)
    end

    puts "URI: #{uri}\n"
    puts "response: #{resp.inspect}\n"
    #puts "data: #{data.inspect}\n"

#    raise "HTTP error" if !(resp.to_s =~ /200/) 
#hmmxxx

    File::delete rdf_fn if File::exists? rdf_fn  # should use CVS/RCS
 
    puts "Writing to file: rdf_fn=#{rdf_fn}"
    if !gzipped
      begin 
        cf = File::new( rdf_fn, File::CREAT|File::RDWR, 0644)
        puts "Writing to file: #{cf.inspect} rdf_fn=#{rdf_fn}"
        cf.write data  
        cf.close
      rescue
        puts "Error writing HTTP'd data to file. data was: #{data} \n"
        raise "scutter: system error (cache unwriteable? disk full?)"
      end
    end


      # sometimes we see stuff gzip'd that shouldn't be.

    if gzipped
      begin 
        require 'zlib'  # special handling of gzipped content
        cf = File::new( rdf_fn + ".gz", File::CREAT|File::RDWR, 0644)
        cf.write data  
        cf.close
        f = open( rdf_fn + ".gz" )
        gz = GzipReader.new(f)
        unzipped=gz.read
        gz.close
        cf = File::new( rdf_fn, File::CREAT|File::RDWR, 0644)
        cf.write unzipped 
        cf.close
      rescue
        puts "Error writing GZIP'd HTTP'd data to file or unzipping. data was: #{data} \n"
        raise "scutter: system error (cache unwriteable? disk full? gzip error?)"
      end
    end


    ## Store meta/cfg info: 				(yeah, should in RDF/XML. sue me...)
    ##
    mf = File::new("#{cache_dir}webcache/rdf-#{uri_hash}.meta", File::CREAT|File::RDWR, 0644)
    base = uri.clone
    base.gsub!(/\/([^\/])+$/, "/")
    mf.puts "#baseuri: #{base}"
    mf.puts "#uri: #{uri}"
    mf.puts "#Last-Visit: <notrecorded>" #+Date::today::to_s
    mf.close
    return uri_hash
  rescue
    puts "Scutter: fetch_and_cache: error with URI #{uri} msg: #{$!}"
    raise "(re-throwing fetch_and_cache exception $!)"
  end


  
  puts "Written #{uri_hash} / #{uri} data to #{rdf_fn}"
  return uri_hash   
end




##########################################################################
#
# given a local RDF file (cached, in effect, parse and load)
# todo: * pass in datasource info
def load_graph_from_cache (file, base_uri='lookup:', opts={})
  #todo: get base uri from cache meta in .meta 
  
  meta={}
  meta_fn = "#{cache_dir}webcache/rdf-#{file}.meta"
  if File::exists? meta_fn
    metainfo = File::new meta_fn 
    metainfo.each do |line|
      line =~ /#(\w+)+:\s+(.*)$/
      key = $1
      value = $2
      meta[key]=value # ignore repeated vals for now.  
    end
  else
    # return nil or raise an exception? use typed exceptions? todo...
    raise "No meta entry in cache for #{file}" 
  end

  base_uri = meta['baseuri'].chomp if base_uri == 'lookup:'
  raise "No base URI found for #{file}" if !base_uri
  puts "baseuri: #{base_uri}"

  # config info
  cache_dir = opts['cache-dir']
  use_xslt= opts['use-xslt'] 
  redparse = opts['use-raptor'] 
  redparse=false if use_xslt

  nt_cache = "#{cache_dir}webcache/_nt/rdf-#{file}.nt" # normal home for ntriples
  puts "Parsing cached RDF file: #{cache_dir} file: #{file} xslt: #{use_xslt} base: #{base_uri}"
 
  parsed_ok = false
  puts "Scutter: parsing."


  # Run Redland/Repat parser
  #
  if redparse
    pmsg=`rdfdump -q -r -o ntriples 'file:#{cache_dir}webcache/rdf-#{file}.rdf'  '#{base_uri}' `
    # puts "Got N-Triples: #{pmsg} "
    nt_cache = "#{cache_dir}webcache/_nt/rdf-#{file}.red.nt"
    red_nt = File::new( nt_cache, File::CREAT|File::RDWR, 0644 )
    red_nt.puts pmsg
    red_nt.close
    # puts "Scutter: just parsed w/ redland: #{pmsg}"
    if pmsg =~ /\w/
      parsed_ok = true 
    else
      puts "scutter: redparse error: no triples! " # todo: raise exception
    end
  end




  # NOTE: *plug in alternate RDF parsers here*
  # (yes, this isn't as configurable as it should be)


  if use_xslt 
    puts "\n\nRunning XSLT PARSER_#5:\n\n"
	
    c14n_fn = "#{cache_dir}webcache/_nt/rdf-#{file}.c14.rdf"
    nt_cache = "#{cache_dir}webcache/_nt/rdf-#{file}.nt"    
    ctext= `xsltproc '#{cache_dir}conf/rdfc14n.xsl' '#{cache_dir}webcache/rdf-#{file}.rdf' `

    begin 
      c14n = File::new( c14n_fn, File::CREAT|File::RDWR, 0644 )
      c14n.puts ctext
      c14n.close
    rescue
      puts "Problem storing canonicalised RDF data in cache. $!"
    end

    puts "Stored canonicalised RDF."
    nt_text = `xsltproc  --stringparam base '#{base_uri}' '#{cache_dir}conf/rdfc2nt.xsl' '#{c14n_fn}'`
    puts "N-Triples: #{nt}"

    begin
      nt = File::new( nt_cache, File::CREAT|File::RDWR, 0644 )
      nt.puts nt_text
      nt.close
    rescue
      puts "Problem storing N-Triples data in cache. $!"
    end

    parsed_ok = true
    puts "\n==#5 done.\n\n"
  end 

  return Loader.ntfile2graph nt_cache
end


#################################################################################################
# store a graph
#
# given an RDF graph, store it locally 
#
def store_graph graph, cache_id, opts={}

  dbdriver = opts['dbdriver'] 
  dbdriver = 'Pg' if !dbdriver # default to PostgreSQL

  ## CONFIG INFO (TODO: move this into options {}
  ##
  dbname='rdfweb1'                # database name
  dbi_driver = 'DBI:'+dbdriver+':'+dbname   # DBI driver 
  dbi_user = 'danbri'		    # user
  dbi_pass=''	                    # autho

  sql_script = nil
  if  true # remnant   
    sql_inserts = graph.toSQLInserts ("uri=#{cache_id}")
     # puts "GOT SQL: #{sql_inserts} \n\n====\n\n"

    if !sql_inserts.empty?
      puts "updating query server."
      DBI.connect ( dbi_driver, dbi_user, dbi_pass ) do |dbh|
        # clean out last triples from this src
        # TODO: this risky? make sure won't accidentially zap the db.
        puts "-  #dbi.do delete from triples where assertid = 'uri=#{cache_id}';"
        begin 
          dbh.do "delete from triples where assertid = 'uri=#{cache_id}'"
        rescue 
          puts "DBI: Error in sql delete, msg: #{$!}"
        end
        puts "+"
        sql_inserts.each do |sql_insert|
          begin 
            print '.'
	    sql_insert.gsub! /;\s*$/, "" # mysql barfs on ';'
	      # print "INSERT: '#{sql_insert}' "
            dbh.do sql_insert 
          rescue 
            #puts "DBI: Error in sql insert #{cache_id} sql: #{sql_insert} msg: #{$!}"
	    # we need an --debug= verbosity level here. @@todo
            # this will be really verbose (lots of inserts into fields where dups not allowed)
          end
        end
        puts
      end
    else 
      puts "skipping reload (no triples)."
    end
  else 
    puts "Error parsing: #{pmsg}"
  end
  return graph
end






##########################################################################


def scutter (todo = ['http://rdfweb.org/people/danbri/rdfweb/webwho.xrdf'], cache_dir= './', 
	crawl=true, proxy=true, opts ={} )

  ## RDF vocab URIs:
  ##
  rdfs = 'http://www.w3.org/2000/01/rdf-schema#'	
  wot = 'http://xmlns.com/wot/0.1/'	
  opts['cache-dir']='./'  if !opts['cache-dir']       
  opts['max']='500'  if !opts['max']   # debug limit!

  seeAlsoRef = {} # from -> to
  done = {}
  count=0

  # note that todo list may grow during harvesting
  #
  todo.each do |uri|

    count = count+1
   if count > opts['max'].to_i
      puts "Max retrieval count reached. Exiting harvester."
      break 
   end
    uri_hash = hashcodeIntFromString uri

    begin 
      cache_id = fetch_and_cache (uri, cache_dir, proxy)
    rescue
      puts "Exception: harvesting problem in fetch_and_cache: $!"
      next
    end
    loaded = load_graph_from_cache cache_id
    
    begin 
      store_graph loaded, cache_id, opts
    rescue
      puts "scutter: problem storing data, #{cache_id}"
      next # can we do this?
    end

    # puts "SCUTTER: Searching for seeAlsos... loaded='#{loaded.inspect}' "    
    # puts "URI: #{uri} SeeAlso: #{seeAlso.inspect} " if !seeAlso.empty?
    # TODO: should search for 'uri --seeAlso-> nil'; but doesn't seem to work.
    seeAlso = loaded.ask(Statement.new(nil,rdfs+'seeAlso',nil)).objects
    seeAlsoRef[uri]=seeAlso if !seeAlso.empty? # store referer info for seeAlso graph
    seeAlso.each do |doc|
      puts "Scutter: adding to TODO list: #{doc} from URI: #{uri}"
      if (!done[doc.to_s])
        todo.push doc.to_s 
        done[doc.to_s]=1 # should this be be a counter
      end
    end

    # look for signatures  
    # puts "WOT: looking for <#{uri}> <#{wot+'assurance'}> <?>"
    # puts "IN: "+loaded.toNtriples
    assurances = loaded.ask(Statement.new(uri,wot+'assurance',nil)).objects
    if !assurances.empty?
      # puts "WOT assurances: #{assurances.inspect} "
      mf = File::new("#{cache_dir}webcache/rdf-#{uri_hash}.meta", File::WRONLY|File::APPEND|File::CREAT, 0644)
      #puts "Re-Opened RDF .meta file to store assurance ptr."
      assurances.each do |sig|
        # puts "Scutter: invoking GPG : #{sig} "
        mf.puts "WOT-Assurance: #{sig.inspect} "
	# gpg --quiet --verify sigfile contentfile # do here or elsewhere?
      end
      mf.close
    end

    # keep a graphical log of where we're up to 
    #
    seealso_dot = './seealso.dot'
    File::delete seealso_dot if File::exists? seealso_dot
    puts "Writing new seealso dot graph"
    dotdata = seeAlsoDotGraph(seeAlsoRef)
    begin 
      s = File::new( seealso_dot, File::CREAT|File::RDWR, 0644)
      s.puts dotdata
      s.close
    rescue
      puts "Error writing seealso .dot graph: "+ $!
      puts "Data was: #{dotdata}"
    end

  end # todo list is empty

#  puts "\nseeAlsoRef: \n#{seeAlsoRef.inspect}\n"
#  puts seeAlsoDotGraph seeAlsoRef
 
end # scutter method




def seeAlsoDotGraph (seeAlsoRef={})
  dot = "digraph seealso { "
  dot += " rankdir=\"LR\"; "
  lookup={}
  seeAlsoRef.each_key do |from|
    dot += "doc_#{gvNode(from)} [label=\"#{from}\", shape=box fontsize=10]; \n"
    lookup[from]="doc_#{gvNode(from)}"
    targets = seeAlsoRef[from]
    t=0
    targets.each do |to|
    t=t+1
    break if t>5 # graph becomes unreadable
      if !lookup[to.to_s]
        l= "doc_#{gvNode(to)} [label=\"#{to} \", shape=box fontsize=10]; \n"
        dot += l
        lookup[to.to_s]=l
      end
      dot += " doc_#{gvNode(from)} -> doc_#{gvNode(to)} [label=\"references\", color=red, weight=100];\n"
    end
  end
  dot += "}"
  return dot
end

def gvNode (data)
  data=data.to_s
  return hashcodeIntFromString(data).to_s.gsub!(/-/,"_")
end





##########################################################################
##########################################################################
#
# Misc utilities


# assumes \uNNNN escape codes
# convert into XML entity escape codes
#
def esc_utf8 (unicode_string)
  escaped_utf8 = unicode_string.gsub! ( /\\u(....)/ )  {|s| "\&#x#{$1};" } != nil
  return unicode_string
end


##########################################################################
#
# from the RAA/soap experiment. Needs updating:

def raa_load
  ### RAA example.
  ###
  Dir['raa-dump/*.xml'].each do |file| 
    file.gsub!("^raa-dump/","")
    sb= `sabcmd soap2rdf.xsl 'raa-dump/#{file}' > 'web_cache/#{file}.rdf' 2>&1`
    # todo: add .meta files
    if ! (sb =~ /\w/)  
    load_graph_from_cache(file, 'http://www.ruby-lang.org/xmlns/raa/test1-ns#', './')
    else
      # puts "Skipping #{file} due to XSLT / filename error"
    end
  end
end

####

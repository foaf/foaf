#!/usr/bin/env ruby

require 'basicrdf'
require 'squish' 			
require 'dbi'
require 'net/http'
require 'sha1'
require 'getoptlong'


# webutil.rb 
# 
# $Id: webutil.rb,v 1.9 2002-07-13 16:37:02 danbri Exp $
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




##########################################################################

# assumes \uNNNN escape codes
# convert into XML entity escape codes
#
def esc_utf8 (unicode_string)
  escaped_utf8 = unicode_string.gsub! ( /\\u(....)/ )  {|s| "\&#x#{$1};" } != nil
  return unicode_string
end


# given a local RDF file (cached, in effect, parse and load)
# todo: * pass in datasource info
def scutter_local (file, base_uri, opts={})

  # config info
  cache_dir = opts['cache-dir']
  use_xslt= opts['use-xslt'] 
  # redparse = false
  redparse = opts['use-raptor'] 
	# use redland parser 'rdfdump' ?
  dbdriver = opts['dbdriver'] 
  dbdriver = 'Pg' if !dbdriver # default to PostgreSQL
 
  nt_cache = "#{cache_dir}webcache/_nt/rdf-#{file}.nt" # normal home for ntriples

  puts "Local: #{cache_dir} file: #{file} xslt: #{use_xslt} "
 
  ## CONFIG INFO (TODO: move this into options {}
  ##
  dbname='rdfweb1'                # database name
  dbi_driver = 'DBI:'+dbdriver+':'+dbname   # DBI driver 
  dbi_user = 'danbri'		    # user
  dbi_pass=''	                    # autho

  parsed_ok = false

  # Run Redland/Repat parser
  #
  if redparse
    pmsg=`rdfdump -q -r -o ntriples 'file:#{cache_dir}webcache/rdf-#{file}.rdf'  '#{base_uri}' `
    red_nt = File::new( '#{cache_dir}webcache/_nt/rdf-#{file}.red.nt', File::CREAT|File::RDWR, 0644 )
    red_nt.puts pmsg
    red_nt.close
  end


  # NOTE: *plug in alternate RDF parsers here*
  # (yes, this isn't as configurable as it should be)


  if use_xslt 
    puts "\n\nRunning XSLT PARSER_#5:\n\n"
	
    c14n_fn = "#{cache_dir}webcache/_nt/rdf-#{file}.c14.rdf"
    nt_cache = "#{cache_dir}webcache/_nt/rdf-#{file}.nt"    
    ctext= `xsltproc '#{cache_dir}conf/rdfc14n.xsl' '#{cache_dir}webcache/rdf-#{file}.rdf' `
    c14n = File::new( c14n_fn, File::CREAT|File::RDWR, 0644 )
    c14n.puts ctext
    c14n.close
    puts "Stored canonicalised RDF."
    nt_text = `xsltproc  --stringparam base '#{base_uri}' '#{cache_dir}conf/rdfc2nt.xsl' '#{c14n_fn}'`
    puts "N-Triples: #{nt}"
    nt = File::new( nt_cache, File::CREAT|File::RDWR, 0644 )
    nt.puts nt_text
    nt.close
    parsed_ok = true
    puts "\n==#5 done.\n\n"
  end 

  puts "N-Triples cache: #{nt_cache}"
#  parsed_ok = (pmsg=='')

  graph = nil
  sql_script = nil
  if  parsed_ok   
    graph = Loader.ntfile2graph( nt_cache )
    sql_inserts = graph.toSQLInserts ("uri=#{file}")
     # puts "GOT SQL: #{sql_inserts} \n\n====\n\n"
    if !sql_inserts.empty?
      puts "updating query server."
      DBI.connect ( dbi_driver, dbi_user, dbi_pass ) do |dbh|
        # clean out last triples from this src
        # TODO: this risky? make sure won't accidentially zap the db.
        puts "-  #dbi.do delete from triples where assertid = 'uri=#{file}';"
        begin 
          dbh.do "delete from triples where assertid = 'uri=#{file}'"
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
            #puts "DBI: Error in sql insert #{file} sql: #{sql_insert} msg: #{$!}"
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

def scutter_remote (uri, base=uri, cache_dir='./', proxy=true)

  proxy_addr = 'cache-edi.cableinet.co.uk'
  proxy_port = 8080

  if uri =~ /^\[/
    puts "Warning, bNode URI."
    return nil
  end

  uri_hash = hashcodeIntFromString(uri)

  uri =~ /:\/\/([^\/]+)(\/.*)$/

  h = Net::HTTP::new $1 

  begin 
    # puts "Getting: #{$2}"

    # TODO: Make this configurable elsewhere. 

    data=''
    resp=''
    gzipped=false
    if proxy
      Net::HTTP::Proxy(proxy_addr, proxy_port).start( $1 ) do |http|
        resp, data = http.get $2 , {'Accept' => 'application/rdf+xml' } 
        # puts "Proxied GET."
      end
      if resp['Content-encoding'] =~ /gzip/
        gzipped = true 
      end

    else
        # puts "Un-Proxied GET."
      resp, data = h.get ($2, {'Accept' => 'application/rdf+xml'} )
    end

    # puts "Response: "+data.to_s
    # puts "Storing in webcache URI: #{uri} as #{uri_hash} .rdf / .meta"
    # delete (todo: rcs/cvs archive) previous cached data
    rdf_fn =  "#{cache_dir}webcache/rdf-#{uri_hash}.rdf" 
    if File::exists? rdf_fn 
      File::delete rdf_fn
    end
    # store current data
 
    if !gzipped
      cf = File::new( rdf_fn, File::CREAT|File::RDWR, 0644)
      cf.write data  
      cf.close
    else

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

    end

    # puts "Stored RDF"

    mf = File::new("#{cache_dir}webcache/rdf-#{uri_hash}.meta", File::CREAT|File::RDWR, 0644)

    puts "Opened RDF .meta file"

    mf.puts "#baseuri: #{base} "
    mf.puts "#uri: #{uri}"
    mf.puts "#Last-Visit: <notrecorded>" #+Date::today::to_s
    # todo: use .nt or .rdf for this. Investigate soap/date clash.
    mf.close
    return uri_hash
   rescue
     # puts "Scutter: #Error with URI #{uri} msg: #{$!}"
   end

   return uri_hash   
end


def raa_load
  ### RAA example.
  ###
  Dir['raa-dump/*.xml'].each do |file| 
    file.gsub!("^raa-dump/","")
    sb= `sabcmd soap2rdf.xsl 'raa-dump/#{file}' > 'web_cache/#{file}.rdf' 2>&1`
    # todo: add .meta files
    if ! (sb =~ /\w/)  
    scutter_local(file, 'http://www.ruby-lang.org/xmlns/raa/test1-ns#', './')
    else
      # puts "Skipping #{file} due to XSLT / filename error"
    end
  end
end

####


def scutter (todo = ['http://rdfweb.org/people/danbri/rdfweb/webwho.xrdf'], cache_dir= './', crawl=true,proxy=true, opts ={} )

  if !opts['cache-dir']
    opts['cache-dir']='./' 
  end

  rdfs = 'http://www.w3.org/2000/01/rdf-schema#'	
  wot = 'http://xmlns.com/wot/0.1/'	
  done = {}
  todo.each do |uri|
    uri_hash = hashcodeIntFromString(uri)
    if crawl
      fetched = scutter_remote (uri, proxy)
    else
      fetched = RDFGraph.new # todo: load from webcache/.nt
    end

    if (fetched != nil)
      loaded = scutter_local(fetched, uri, opts )
      puts "load failed " if loaded == nil

      seeAlso = loaded.ask(Statement.new(nil,rdfs+'seeAlso',nil) ).objects
      # puts "SeeAlso: #{seeAlso.inspect} " if !seeAlso.empty?
      seeAlso.each do |doc|
        # puts "Scutter: adding to TODO list: #{doc} "
        if (!done[doc.to_s])
          todo.push doc.to_s 
          done[doc.to_s]=1
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
    end
  end
end

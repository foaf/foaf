#!/usr/bin/env ruby

require 'basicrdf'
require 'squish' 			
require 'dbi'
require 'net/http'
require 'sha1'
require 'getoptlong'


# webutil.rb 
# 
# $Id: webutil.rb,v 1.5 2002-07-11 17:25:56 danbri Exp $
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
def scutter_local (file, base_uri='', cache_dir='./' )



  ## CONFIG INFO (TODO: move this elsewhere)
  ##
  dbname='rdfweb1'                # database name
  dbi_driver = 'DBI:Pg:'+dbname   # DBI driver 
  dbi_user = 'danbri'		    # user
  dbi_pass=''	                    # autho
  #  puts "scutter_local: file=#{file} with base=#{base_uri} :"
  pmsg=`rdfdump -q -r -o ntriples 'file:#{cache_dir}webcache/rdf-#{file}.rdf'  '#{base_uri}' > '#{cache_dir}webcache/_nt/rdf-#{file}.nt'`
  # puts pmsg
  #
  #  puts "\n\nPARSER_#5:\n\n"

# this is wrong, especially for a library.
# quick hack for debugging the xslt parser.

  p5_msg_c = `xsltproc '#{cache_dir}conf/rdfc14n.xsl' '#{cache_dir}webcache/rdf-#{file}.rdf' > '#{cache_dir}webcache/_nt/rdf-#{file}.c14.rdf'`
  p5_msg = `xsltproc  --stringparam base '#{base_uri}' '#{cache_dir}conf/rdfc2nt.xsl'   '#{cache_dir}webcache/_nt/rdf-#{file}.c14.rdf' > '#{cache_dir}webcache/_nt/rdf-#{file}.p5.nt'`
  puts "\n==#5\n\n"
  
  #../../xsltrdf/rdfc14n.xsl
  #../../xsltrdf/rdfc2nt.xsl

  nt_cache = "#{cache_dir}webcache/_nt/rdf-#{file}.nt"
  puts "N-Triples cache: #{nt_cache}"
  parsed_ok = (pmsg=='')
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
        #
        puts "-  #dbi.do delete from triples where assertid = 'uri=#{file}';"
        begin 
          dbh.do "delete from triples where assertid = 'uri=#{file}';"
        rescue 
          puts "DBI: Error in sql delete, msg: #{$!}"
        end
        puts "+"

        sql_inserts.each do |sql_insert|
          begin 
            print '.'
            dbh.do sql_insert 
          rescue 
            # puts "DBI: Error in sql insert #{file} sql: #{sql_insert} msg: #{$!}"
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

   ### stopgap

#    sql_cache = "#{cache_dir}webcache/_nt/rdf-#{file}.sql"
#    if File::exists? sql_cache 
#      File::delete sql_cache
#    end
#    mf = File::new(sql_cache, File::CREAT|File::RDWR, 0644)
#    mf.puts sql_script
#    mf.close
#    `cat #{sql_cache} | psql rdfweb1`
   ### end stopgap

  return graph
end

def scutter_remote (uri, base=uri, cache_dir='./', proxy=true)
  puts "Scuttering remote: #{uri}"

  #### PROXY SETTINGS
  proxy_addr = 'cache-edi.cableinet.co.uk'
  proxy_port = 8080

  #######################################

  uri_hash = hashcodeIntFromString(uri)
  uri =~ /:\/\/([^\/]+)(\/.*)$/
  #  puts "Host: #{$1} Resource: #{$2} "
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


def scutter (todo = ['http://rdfweb.org/people/danbri/rdfweb/webwho.xrdf'], cache_dir= './', crawl=true, proxy=true)
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
      loaded = scutter_local(fetched, uri)
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

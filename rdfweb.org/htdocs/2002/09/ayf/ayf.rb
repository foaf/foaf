#!/usr/bin/env ruby
#
# ayf.rb 
# $Id: ayf.rb,v 1.16 2002-12-11 21:11:35 danbri Exp $
# AllYourFoaf... see http://rdfweb.org/2002/09/ayf/intro.html
# 
# This is a basic RDF harvester that traverses rdfs:seeAlso links
# and calls application-specific handlers before and after it 
# retrieves each RDF document. It comes with some demo code blocks that
# collect photo information, look out for mentions of airports, and 
# generate basic logging records.
#
# TODO list:
#
# - parser may have a fixme re genid generation from ntriples
#   bug is probably in the parser wrapping code I wrote.
#   a good point at which to write syntax tests?
#
# - parser also generates \n triples -- need to investigate.
#
# some possible starting points:
#   http://rdfweb.org/people/danbri/rdfweb/danbri-foaf.rdf
#   http://www.perceive.net/xml/googlescutter.rdf
#   http://www.perceive.net/xml/googlescutterNoChatlogs.rdf
#
# nearby:
#   wordnet/ruby hacking, 
#   http://fireball.danbri.org/people/danbri/2002/07/xmlns-wordnet/dantest.rb


require 'net/http'
require 'RDF4R/Driver/XMLParser'
require 'RDF4R/Consumer/Standard'
require 'basicrdf'

###############################################################################
#
# Test script that creates, configures and starts a SimpleScutter with some URI
#
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
  #    page.reg_xmlns 'http://www.megginson.com/exp/ns/airports#', 'air'
  #
  airports = Proc.new do |crawler,page| 
    rs = page.ask Statement.new(nil, air+"iata", nil)
    rs.objects.each do |a|
      a.graph=page
      puts "AIRPORT: #{a} -got airport code in #{crawler.uri})" if (a.to_s =~ /\S/) 
    end					# the 'if' is fix for parser bug
  end


  # a code block that writes an html page based on the crawler's .out property 
  #
  htmler = Proc.new do |crawler,page|
      html = "<html><head><title>all your foaf depictions...</title></head>\n<body>\n"
      html += "<h1>AllYourFoaf Image Index</h1>\n"  
      html += "<p><strong>stats:</strong>: left.size=#{crawler.left.size} \
	seen.size=#{crawler.seen.size} seenpic.size=#{crawler.seenpic.size} current:{crawler.uri} </p> "
      html += "<hr />\n\n" + crawler.out
      html += "</body></html>\n\n"
      SimpleScutter.writefile(crawler.outfile,html)
  end

  # basic image metadata harvesting
  #
  mugshots = Proc.new do |crawler,page|
    images=[]
    img = page.ask Statement.new(nil,  foaf+'img', nil)
    img.objects.each {|a| images.push a.to_s }
    img = page.ask Statement.new(nil,  foaf+'depiction',nil)
    img.objects.each {|a| images.push a.to_s }
		    # todo: store this state locally instead of inside crawler
    images.each do |pic|
      next if (!pic =~ /\S/) #bug in Liber RDF parser.
      if crawler.seenpic[pic]==0  ### how to do this as a Proc? fixme
        crawler.out += "<img src='#{pic}' width='128' height='128' />" 
        crawler.out += "<!-- linked at: #{crawler.uri} -->\n\n"
      end
      crawler.seenpic[pic]=crawler.seenpic[pic]+1
    end
    # todo: some thought needed w.r.t. outfile/html generation and state
  end

  # stats to be output at start of each loop      
  #
  loopstats = Proc.new do |s|
    puts "INIT: s.left.size=#{s.left.size} s.seen.size=#{s.seen.size} current: #{s.uri}"
  end

  error_logger = Proc.new {|e| puts "ERROR: #{e}" }

  # register some handlers:
  ayf.pagehandlers.push page_summary, airports, mugshots, htmler
  ayf.inithandlers.push loopstats
  ayf.errorhandlers.push error_logger 

  ayf.run  # set crawler running!
end 

   

#############################################################################
#############################################################################


class SimpleScutter

  attr_accessor :start, :seen, :seealso, :out, :seenpic, :debug, \
	:uri, :out, :outfile, :left, :pagehandlers, :inithandlers, :errorhandlers

  def initialize(start_uri='',outfile="_allyourfoaf.html")
    @left=[]
    @uri=start_uri 		# todo: should allow for a list?
    @left.push uri if uri != '' # todo: do we need this qualifier?
    @debug=true
    @pagehandlers=[]
    @inithandlers=[]
    @errorhandlers=[]
    @seen=Hash.new(0) 		# counter for whether a rdf uri has been seen
    @seealso=Hash.new(0) 	# all the seealso uris we've seen, counted
    @seenpic=Hash.new(0) 	# counter for whether a pic been seen
    @outfile=outfile 		# output filename
    @out=""                     # output content
  end

  def run
    rdfs='http://www.w3.org/2000/01/rdf-schema#'
    while left.size>0
      @uri = @left.pop.to_s
      page = nil
      @inithandlers.each {|handler| handler.call(self)} # call inithandlers

      # Try fetching some RDF:
      #
      seen[uri]=seen[uri]+1  # increment per-uri encounter
      begin 
        page = rdfget(uri)
        raise "#{$!} (rdfget returned nil)" if page==nil
      rescue
        err_msg="FAILED URI: '#{uri}' MSG: #{$!}" 
        errorhandlers.each {|handler| handler.call err_msg }
        next
      end
      next if page.size==0 # skip to next URI if empty graph

      # We have some RDF; inspect it for seeAlso links to more RDF:
      # 
      also = page.ask Statement.new(nil,  rdfs+'seeAlso',nil)
      also.objects.each do |a|
        a=a.to_s
        if seen[a]==0
          seealso[a]=seealso[a]+1
          left.push a unless a==nil           # stash this unseen link
        end
      end
      self.left=[] # reset and rebuild
      seealso.each_key {|k| left.push(k) if seen[k]==0 }

    # Call any pagehandlers:
    pagehandlers.each {|handler| handler.call(self,page)} # call pagehandlers
  end 
  puts "RDF crawl complete. Exiting!" if @debug
end




#########################################################################
#########################################################################
#
#
# temporary stuff that should be in a library...


def SimpleScutter.writefile(fn,html)
      begin 
        File::delete fn if File::exists? fn
      rescue Exception
        puts "HTML logfile locked? Skipping. #{$!}"
        next
      end 
      begin
        cf = File::new( fn, File::CREAT|File::RDWR, 0644)
        cf.write html
        cf.close
      rescue Exception
        puts "ERROR: can't write HTML logfile #{cf} "
      end
end



  def SimpleScutter.parse(filename, base_uri)
    consumer = RDF4R::Consumer::Standard.new
    File.open(filename, "r") do |file|
      begin
        return RDF4R::Driver::XMLParser.process(file, base_uri, consumer)
      rescue Exception 
        raise "Expat setup error. url=#{base_uri} error: #{$!}"
      end
    end
  end


  def rdfget(uri)
    uri=uri.to_s
    uri.chomp!
    uri =~ /:\/\/([^\/]+)(\/*.*)$/
    host = $1
    res = $2
    h = Net::HTTP::new host
    user_agent = 'RDFWeb-SimpleScutter-200212;http://rdfweb.org/foaf/'
    rdfdata=Graph.new([])
    my_headers = {'Accept' => 'application/rdf+xml', 'User-agent' => user_agent }  
    h.open_timeout = 10   
    h.read_timeout = 60
    begin 
    resp, data = h.get(res, my_headers)
    rescue
      error_msg="rdfget: HTTP GET failed. Returning empty graph. error:#{$!}"
      errorhandlers.each {|handler| handler.call error_msg } 
      return rdfdata
    end

    base=uri
    fn="_local.rdf" # temporary file; shouldn't need this :(
    File::delete fn if File::exists? fn
    cf = File::new( fn, File::CREAT|File::RDWR, 0644)
    cf.write data  
    cf.close

    begin 
      models = SimpleScutter.parse(fn, base)
    rescue
      raise "RDF parser error. #{$!}\n"	
    end
    return(rdfdata) if models==nil 

    if models.size == 0		# messed up remains of a liber script
    elsif models.size > 1	# todo: tidy up. just grab 1st model if any
    else
      model = models.shift
      model.statements.each do |s|
      s.each do |bit|
        if bit.type.to_s =~ /RDF4R/
          nt = bit.to_ntriple
          begin
            Loader.parseline nt.to_s, rdfdata, {} # ugly. fixme!
          rescue
            puts "rdfget: error w/ parseline. #{$!} "
          end
        end
      end
    end
  end
  return rdfdata
end	
end


################################################################# 

start_uri = 'http://rdfweb.org/people/danbri/rdfweb/danbri-foaf.rdf' 
start_uri = ARGV.shift if ARGV.length > 0
go(start_uri)

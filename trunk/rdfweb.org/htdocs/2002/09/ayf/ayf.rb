#!/usr/bin/env ruby
#

# HEALTH WARNING: This version I'm checking in is BROKEN. 
# -- the loop/control/seen stuff is hosed. sorry.


# ayf.rb 
# $Id: ayf.rb,v 1.7 2002-12-10 02:30:40 danbri Exp $
# AllYerFoaf... see http://rdfweb.org/2002/09/ayf/intro.html
# 
# This is a basic RDF harvester that traverses rdfs:seeAlso links
# and creates an HTML page _allyourfoaf.html that links each image 
# mentioned in the RDF it finds.
#
# some possible starting points:
#   http://rdfweb.org/people/danbri/rdfweb/danbri-foaf.rdf
#   http://www.perceive.net/xml/googlescutter.rdf
#   http://www.perceive.net/xml/googlescutterNoChatlogs.rdf

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

  ayf = SimpleScutter.new  
  ayf.left.push uri
  pagecount=0

  # a code block to output basic info about each RDF page encountered
  # 
  page_summary = Proc.new do |uri,page|  
    puts "RDF doc count='#{pagecount}': uri:#{uri} gave RDF graph #{page} \
	with #{page.size} triples\n" 
    pagecount=pagecount+1
  end

  # a code block to see if some page provides nearestAirport information:  
  #
  airports = Proc.new do |uri,page|  
    rs = page.ask Statement.new(nil, air+"iata", nil)
    rs.objects.each do |a|
      a.graph=page
      puts "Found airport code: #{a}" if (a.to_s =~ /\S/) 
    end					# the 'if' is fix for parser bug
  end


      
  loopstats = Proc.new do |uri,s|
    puts "init: s.left.size=#{s.left.size} s.seen.size=#{s.seen.size} current: #{uri}"
  end

  error_logger = Proc.new {|e| puts "ERROR: #{e}" }

  # register some handlers:
  ayf.pagehandlers.push page_summary, airports
  ayf.inithandlers.push loopstats
  ayf.errorhandlers.push error_logger 

  ayf.run  # set it going! our handlers will get called for each RDF doc

end 

#############################################################################
#############################################################################


class SimpleScutter

  attr_accessor :start, :seen, :seealso, :out, :seenpic, :debug, \
	:outfile, :left, :pagehandlers, :inithandlers, :errorhandlers


  # Set up state needed for each crawler
  # 
  def initialize

    @debug=true

    @pagehandlers=[]
    @inithandlers=[]
    @errorhandlers=[]

    @seen=Hash.new(0) 		# counter for whether a rdf uri has been seen
    @seealso=Hash.new(0) # counter
    @left=[]

    @seenpic=Hash.new(0)	# counter for whether a pic been seen

    # state relating to output; really belongs elsewhere
    @outfile="_allyourfoaf.html" # output filename
    @out=""                      # output content

  end



#################################################################

  def run

    rdfs='http://www.w3.org/2000/01/rdf-schema#'

    while left.size>0

      uri = @left.pop.to_s
      page = nil
      if uri == nil
        puts "Nothing left todo. Exiting... (???fixme)"
        exit 0
      end

      # call initialization handlers 
      #
      @inithandlers.each do |handler|
        handler.call(uri,self) 
      end

     
      ################################################################
      # Try fetching some RDF

      seen[uri]=seen[uri]+1  # increment per-uri encounter
      begin 
       page = rdfget(uri)
       raise "#{$!} (rdfget returned nil)" if page==nil
     rescue
       errorhandlers.each do |handler|
         handler.call( "FAILED URI: '#{uri}' MSG: #{$!}" )
       end
       next
      end

      next if page.size==0 # skip to next URI if empty graph


      ################################################################
      #
      # From here on in, we have an rdf graph
  
      

      # look in page for seeAlso and store details
      #
      also = page.ask Statement.new(nil,  rdfs+'seeAlso',nil)
      also.objects.each do |a|
        a=a.to_s
        if seen[a]==0
          seealso[a]=seealso[a]+1
          left.push a 			# stash this unseen link
        end
      end

      self.left=[] # reset and rebuild
      seealso.each_key do |k|
        left.push(k) if seen[k]==0 
      end





    #############################################################
    # Things we do with each RDF 'page' we find 
    #
    # (could do all this via handlers/blocks?)

    # look in page for foaf:img
    foaf='http://xmlns.com/foaf/0.1/'
    img = page.ask Statement.new(nil,  foaf+'img', nil)
    img.objects.each do |a|
      gotpic(a.to_s,uri)
    end

    # look in page for foaf:depiction
    foaf='http://xmlns.com/foaf/0.1/'
    img = page.ask Statement.new(nil,  foaf+'depiction',nil)
    img.objects.each do |a|
      gotpic(a.to_s,uri)
    end



    #################################################################
    #
    # (Re)write an HTML page 
    #
    html = "<html><head><title>all your foaf depictions...</title></head>\n<body>\n"
    html += "<h1>AllYourFoaf Image Index</h1>\n"  
    html += "<p><strong>stats:</strong>: left.size=#{left.size} \
	seen.size=#{seen.size} seenpic.size=#{seenpic.size} current: #{uri} </p> "
    html += "<hr />\n\n" + out
    html += "</body></html>\n\n"
    fn='_allyourfoaf.html'

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
  



    # Call the pagehandlers (if there was one)

    pagehandlers.each do |handler|
      handler.call(uri,page)
    end
 
  end # big loop

  puts "RDF crawl complete. Exiting!" if @debug

end


#########################################################################



  def SimpleScutter.parse(filename, base_uri)
    consumer = RDF4R::Consumer::Standard.new
    File.open(filename) do |file|
      begin
        return RDF4R::Driver::XMLParser.process(file, base_uri, consumer)
      rescue Exception 
        puts "Expat setup error. error: #{$!}"
      end
    end
  end


  def gotpic(pic,u="")
    #    return '' if $pic =~ m/mpg/i; 
    #    return '' if $pic =~ m/svg/i; # nasty; but inline SVG doesn't work 
    pic=pic.to_s # warn if a non-string object, or just deal?
    return if (!pic =~ /\S/) #bug in Liber RDF parser.
    if (@seenpic[pic]==0) # here we're using a counter for times seen

      @out += "<img src='#{pic}'   width='128' height='128' />" 
      @out += "<!-- from #{u} -->\n\n"

    else
      # puts "gotpic: already seen #{pic} "
    end
    @seenpic[pic]=@seenpic[pic]+1
    return ""
  end




  
##################################################################
#
# temporary stuff that should be in a library...
#
  def rdfget(uri)
    uri=uri.to_s
    fn="_local.rdf"
    uri.chomp!
    uri =~ /:\/\/([^\/]+)(\/*.*)$/
    host = $1
    res = $2
    h = Net::HTTP::new host
    user_agent = 'RDFWeb-Scutter-200207;http://rdfweb.org/foaf/'
    rdfdata=Graph.new([])

    my_headers = {'Accept' => 'application/rdf+xml', 'User-agent' => user_agent }  
    h.open_timeout = 30   
    h.read_timeout = 60   

    begin 
    resp, data = h.get(res, my_headers)
    rescue
      puts "rdfget: HTTP GET failed. Returning empty graph. error:#{$!} "
      # fixme: should raise an error?
      return rdfdata
    end

    # puts "Got data: #{data.inspect} from host:#{host} res:#{res} uri:#{uri}\n\n"
    base=uri
    File::delete fn if File::exists? fn
    cf = File::new( fn, File::CREAT|File::RDWR, 0644)
    cf.write data  
    cf.close

    begin 
      models = SimpleScutter.parse(fn, base)
    rescue
      puts "RDF parser error. #{$!}\n"	
    end

    return(rdfdata) if models==nil 

    # cruft: fixme / delete?:
    #
    if models.size == 0
      # puts "no models found"
      # exit 0
    elsif models.size > 1
      # puts "i got multiple models, you probably didn't want that"
      # exit 0
    else
      model = models.shift
      model.statements.each do |s|
      s.each do |bit|
        if bit.type.to_s =~ /RDF4R/
          nt = bit.to_ntriple
          begin
            # this is nasty way to hook apis together.
            Loader.parseline nt.to_s, rdfdata, {}
 
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


start_uri = ARGV.shift if ARGV.shift
if ARGV.shift
  start_uri=ARGV.shift
else
  start_uri = 'http://rdfweb.org/people/danbri/rdfweb/danbri-foaf.rdf' 
end

go start_uri





#<contact:nearestAirport><wn:Airport air:icao="EGGD" air:iata="BRS" />...
#    page.reg_xmlns 'http://www.megginson.com/exp/ns/airports#', 'air'
#
# TODO
# PARSER may have a fixme re genid generation from ntriples
# bug is probably in the parser wrapping code I wrote.
# a good point at which to write syntax tests?

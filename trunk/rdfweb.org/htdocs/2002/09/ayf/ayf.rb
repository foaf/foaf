#!/usr/bin/env ruby
#
# ayf.rb 
# $Id: ayf.rb,v 1.3 2002-12-09 21:47:39 danbri Exp $
# AllYerFoaf... see http://rdfweb.org/2002/09/ayf/intro.html
# 
# This is a basic RDF harvester that traverses rdfs:seeAlso links
# and creates an HTML page _allyourfoaf.html that links each image 
# mentioned in the RDF it finds.
#
# some possible starting points:
# http://rdfweb.org/people/danbri/rdfweb/danbri-foaf.rdf
# http://www.perceive.net/xml/googlescutter.rdf
# http://www.perceive.net/xml/googlescutterNoChatlogs.rdf

require 'RDF4R/Consumer/Standard'
require 'RDF4R/Driver/XMLParser'
require 'net/http'
require 'basicrdf'

###############################################################################
#
# Test script (creates, configures and starts a SimpleScutter with some URI
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
    puts "\n\nHandler '#{pagecount}': uri:#{uri} gave graph #{page} \
	with #{page.size} triples\n\n " 
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

  # register some handlers:
  ayf.pagehandler.push page_summary, airports

  ayf.run  # set it going! our handlers will get called for each RDF doc

end 



class SimpleScutter

  attr_accessor :start, :seen, :seealso, :out, :seenpic, :debug, :outfile, :left, :pagehandler

  def initialize
    @seen={} 
    @seealso={}
    @out=""
    @seenpic=Hash.new(0)
    @left=[]
    @debug=true
    @outfile="_allyourfoaf.html"
    @pagehandler=[]
  end

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

  def gotlink(more,p=nil)
    #puts "Seealso: #{more} FROM #{p}\n" if @debug
    if (!@seen[more]) 
      @seealso[more]=@seealso[more]+1  if @seealso[more]
      left.push more
    end
    return ''
  end

  def gotpic(pic,u="")
    #    return '' if $pic =~ m/mpg/i; 
    #    return '' if $pic =~ m/svg/i; # nasty; but inline SVG doesn't work 
    pic=pic.to_s # warn if a non-string object, or just deal?
    return if (pic=="\n") #bug in Liber RDF parser.
    if (@seenpic[pic]==0) # here we're using a counter for times seen
		# fixme: make this consistent w/ @seen  
      @out += "<img src='#{pic}'   width='128' height='128' /> \n" 
      @out += "<!-- #{pic} from #{u} -->\n\n"
    else
      puts "gotpic: already seen #{pic} "
    end
    @seenpic[pic]=@seenpic[pic]+1
    return ""
  end




  

#########################################################################

  def run
  while (1) 
    @seealso.each_key do |k|
      puts "Starting with: #{k}\n"
      left.push(k) if (!@seen[k]) 
    end
    if (left.size==0) 
      if (@debug)
        puts "FOAF harvester complete: no more links to explore. exiting...\n" 
      end
      exit 0
    end
    uri = @left.pop
    uri = uri.to_s
    print "rdfget-ing uri: '#{uri}'\n" if @debug
    page = nil
    if (uri != nil) 
      puts "Beginning loop. left.size=#{left.size} seen.size=#{seen.size}  current: #{uri} "
      begin 
       page = rdfget(uri)
      rescue
       puts "RDFGET/parse failed. skipping. error: #{$!}"
       next
      end
      next if page==nil
      next if page.size==0 # skip if empty graph
      puts "rdfget got triples: count=#{page.size} \n\n" 

      # look in page for seeAlso
      rdfs='http://www.w3.org/2000/01/rdf-schema#'
      also = page.ask(Statement.new(nil,  rdfs+'seeAlso',nil))
      also.objects.each do |a|
        puts "seealso: #{a}\n"
        gotlink(a.to_s)
      end
    else
      puts "Nothing left todo. Exiting..."
      exit 0
    end
    if @seen[uri] 
      @seen[uri]=@seen[uri]+1 
    else
      @seen[uri]=1 
    end


    #############################################################
    # Things we do with each RDF 'page' we find 
    # (could do all this via handlers/blocks?)

    # look in page for foaf:img
    foaf='http://xmlns.com/foaf/0.1/'
    img = page.ask(Statement.new(nil,  foaf+'img',nil))
    img.objects.each do |a|
      puts "img: #{a}\n"
      gotlink(a.to_s,uri)
    end

    # look in page for foaf:depiction
    foaf='http://xmlns.com/foaf/0.1/'
    img = page.ask(Statement.new(nil,  foaf+'depiction',nil))
    img.objects.each do |a|
      puts "depiction: #{a}\n"
      gotpic(a.to_s,uri)
    end


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
  

  # Call the pagehandler (if there was one)
  #

  #yield(uri,page) # pass to pagehandler callback

  @pagehandler.each do |handler|
    handler.call(uri,page)
  end

  end #/while

end


#########################################################################
#
# temporary stuff that should be in a library...
#
  def rdfget(uri)
    puts "RDFGET called with URI='#{uri}' uritype=#{uri.class}"
    uri=uri.to_s
    fn="_local.rdf"
    uri.chomp!
    uri =~ /:\/\/([^\/]+)(\/*.*)$/
    # puts "GETTING: h=#{$1} r=#{$2} uri=#{uri}"
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
      puts "GET failed. Returning empty graph. error:#{$!} "
      return data
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

    if models.size == 0
      puts "no models found"
      exit 0
    elsif models.size > 1
      puts "i got multiple models, you probably didn't want that"
      exit 0
    else
      model = models.shift
      model.statements.each do |s|
      s.each do |bit|
        if bit.type.to_s =~ /RDF4R/
          nt = bit.to_ntriple
          begin
            Loader.parseline nt.to_s, rdfdata, {}
          rescue
            puts "Error w/ parseline. #{$!} "
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

#!/usr/local/bin/ruby
#
# RDFWeb Ruby RDF stuff
# danbri@w3.org


## NOTE: basicrdf.rb bundled with scutter, but
## is an external package (and W3C software), and will ultimately require a
## separate install. danbri@W3.org
##



# Overview:
# we use the classes 'Graph', 'Node', and 'Statement'
# 
# todo: 
#  - associate Nodes with Graphs DONE
#  - implement ask(template_statement) method (slog through the variations!)
#  - implement ntriples-based i/o INPROGRESS
#  - use method_missing to catch property queries on nodes DONE
#  - figure out how to test this and Perl version at same time /?
#  - document the stuff it doesn't do w.r.t. RDF specs /?
#  - find out about various Ruby features I'm unclear on (see 'todo:' notes)
#  - continue migrating ask() to return graphs not nodes DONE
#  - add Mozilla-based graph API 
#  - fix the worst inefficiencies (eg. all those new graphs / statements)
#  - make basicrdf into a module, http://www.rubycentral.com/book/tut_modules.html 

# Currently:
# Nodes and Graphs
#
# a node has a graph; this relationship may be transient, fleeting etc
# default is for node to use the Graph.default graph.
# we only ever have one node per URI, so this could be a source of
# confusion if the graph is seen as dominating the node's state, since
# nodes may be in several graphs, and graphs may mention nodes that
# appear elsewhere. Is this just a matter of better documentation needed?


### utilities

#!/usr/bin/env ruby
   

def hashcodeIntFromString (data)
  require 'sha1'
  sh = SHA1::new(data)
  dig = sh.digest()
  r = (dig[0])|((dig[1]) << 8)|((dig[2]) << 16)|((dig[3]) << 24) 
  # restrict to signed 32 bit int (didn't need this in java)
  if  ( r > ( ( 1 << 31 ) -1 ) )  
    return ( r - ( 1 << 32 ) )
  end
  return r
end


##############################################################################
#
class Node
  @@nodes = {}
  @db
  @graph
  attr_accessor :content, :graph
   
  # constructor (make this private? use get* instead)
  # N-Triples parser seems to still be using this. @@todo
  def initialize (content)
    ## puts "NEW NODE: #{content}"
    srand()
    @content = content
  end
  def inspect
    "#@content "
  end
    
  # Get a Node given its URI, recycling where available
  def Node.getResource( content, graph=Graph.default() )
    return @@nodes[content] if @@nodes[content] 
    # puts "NEW NODE: #{content}"
    node = Node.new(content)
    node.graph=graph
    #puts "NEW NODE: #{content}"
    @@nodes[content]=node
    return node 
  end

  # get a fresh blank node
  # notes: couldn't see how to have Node.getResource() work 
  def Node.getBlank(graph=Graph.default, id=nil)
    if (id == nil)     
      content = '[local:bnodeid:' + rand(100000000).to_s() 
    else
      content = id
#      puts  "cached genid Node.getBlank: '#{id}' " 
    end 

    node = Node.new(content) 
    node.graph=graph
    @@nodes[content]=node
    return node
  end

  def Node.getLiteral(content, graph=Graph.default)
    return @@nodes[content] if @@nodes[content]
    node = Node.new("\""+content)
    node.graph=graph
    @@nodes["\""+content]=node
    return node
  end

  # a stringified node doesn't include '"' around literals (genids? @@ISSUE)
  def to_s 
    string = "#@content"
    # print "*"
    string.gsub!("^\"","")
    string.gsub!("\"$","")
    string.gsub!("\"-ja$","")  # todo: lang codes from ntriples
    return string
  end

  # unicode encoding for XML output
  def to_utf8_xml
   return self.to_s.gsub ( /\\u(....)/ )  {|s| "\&#x#{$1};"  }  
  end


  def is_resource
    if (@content =~ /^"/)  
      return false 
    end
    return true
  end 

  def to_nt_term
    return Node.wrap(@content)
  end


  def sha1 
    #    puts "SHA1ing #{@content} "
    return hashcodeIntFromString(@content)
  end

  # wrap literals in quotes, data in <> blanks in ???
  # for NTriples/N3 export
  # 
  def Node.wrap(data)

    wrap = "#{data}"    if (data =~ /^"/)
    wrap = ":_#{data}"  if (data =~ /^\[/) 
    wrap = "<#{data}>" unless wrap
    return wrap
    # todo: multiline, XML content, charset etc...
  end

  # catch missing method calls, assume they're rdf property queries
  # todo: think about error handling; what would be sane here?
  #
  def method_missing(methid)
    str = methid.id2name
    # print "Missing method ",str,"\n" # for rdf property access
    lhs,rhs = str.split(/_+/,2)
    # print "LHS: '#{lhs}' RHS: '#{rhs}' \n"
    uri = self.graph.ns(lhs)
    # print "URI: '#{uri}'\n"
    propuri = uri+rhs
    # print "Should query node for property of type #{propuri}\n"
    ans = self.graph.ask(Statement.new(self,Node.getResource(propuri),nil))
    #todo: scalar or list?
    raw= ans.objects
    ret=[]
## TODO: we're returning statement objects wrapped in "

#    raw.each do |o| 
#      o.gsub!(/^"/,"")
#      o.gsub!(/"$/,"")
#      ret.push o      
#    end
    return raw
  end
end


#############################################################################
#

class Graph

  @@defaultdb=0 # reference to default database, made on demand in default()
  def initialize( statements )
    @db={}	  # counter(notyet, just flag) for each statement we've met
    @fp={}
    @bp={}  
    @ns={}
    return unless statements
    statements.each { |statement| @db[statement]=1 } 
    statements.each {|statement| tell(statement) } 
  end


  def Graph.default()
    if (@@defaultdb==nil)
      print "Getting default graph\n"
      @@defaultdb=Graph.new([]) ## should put the basic RDF/RDFS stuff here
      return @@defaultdb
    end
  end


  # XML Namespace registration
  #
  def reg_xmlns(uri, prefix)
    # print "registering an xml namespace: #{uri} as #{prefix} \n"
    @ns[prefix]=uri 
    return uri
  end

  def ns(prefix)
    # print "Looking up '#{prefix}' in registry, #{@ns.inspect} \n"
    return @ns[prefix]
  end
    

  # Add a bunch of statements (merge into tell() maybe?)
  # 
  def tell_all ( statements )
    statements.each { |s| tell(s) }
  end

  # tell the graph something (add and index a statement)
  #
  def tell ( statement )

    @db[statement]=1

    # store objects under subject+predicate
    #
    sp_list = @fp["#{statement.subject} | #{statement.predicate}"]
    if (sp_list)
      # puts "Storing object under EXISTING s/p, statement= #{statement}"
      # puts "Inspecting existing sp_list: #{sp_list.inspect} \n"
      sp_list.push(statement.object) #todo: we should order this list (and po)
    end
    if (!sp_list)
      # puts "Storing object under NEW s/p, statement = #{statement}"
      sp_list = [statement.object]
      @fp["#{statement.subject} | #{statement.predicate}"]=sp_list
    end

    # store subjects under predicate+object
    #
    po_list = @bp["#{statement.predicate} | #{statement.object}"]    
    if (po_list)
      # puts "Storing subject under EXISTING p/o, statement= #{statement}"
      po_list.push(statement.subject) 
    end					# todo: lookup else syntax for Ruby
    if (!po_list)
      # puts "Storing subject under NEW p/o, statement= #{statement}"
      po_list = [statement.subject]
      @bp["#{statement.predicate} | #{statement.object}"]=po_list
    end
  end


  def toNtriples()
    out = "\n# Ruby-RDF NTriple serializer $Id: basicrdf.rb,v 1.1 2002-07-17 11:45:11 danbri Exp $ \n#\n\n"
    # forward pointers -- from subject+predicate to object(s)
    @fp.each_key { |key|
      s,junk, p = key.split(/ | /,3)
      stem = "<#{s}> <#{p}>"
      # print "Value: #{@fp[key]} \n"
      @fp[key].each { |value| out += "#{stem} #{value.to_nt_term} .\n" }
    }
    return out
  end

# A simple dump to the Dot format used by GraphViz, http://www.graphviz.org/
#
def toDotGraph()
    out =""
# "\n# Ruby-RDF GraphViz *.dot serializer $Id: basicrdf.rb,v 1.1 2002-07-17 11:45:11 danbri Exp $ \n#\n\n"

out += 'digraph G {
  size="25,25";
  ratio=auto;
  node [shape=ellipse,fontsize=11];
  rankdir=LR;'

# Now we want this:

# r1 [label="_:0"];
# r2 [label="file:/Users/aaronsw/Projects/cwm/soap.n3#Thing"];
# r1 -> r2 [label="rdf::type" fontsize=12];
#etc

res={}
arc=[]
predicates={}
types={}
lookup={}
count=1
    # forward pointers -- from subject+predicate to object(s)
    @fp.each_key { |key|
      s,junk, p = key.split(/ | /,3)
      @fp[key].each { |value| 
      o=value.to_s # stringify?

      if (!lookup[s]) 
        res[count] = s
 #       puts "S: #{s} = #{count}\n" 
        lookup[s] = count
        count += 1
      end
      if (!lookup[p]) 
        res[count] = p
 #       puts "P: #{p} = #{count}\n" 
        lookup[p] = count
        predicates[p]=1
        count += 1
      end
      if (!lookup[o]) 
        res[count] = o
 #       puts "O: #{o} = #{count}\n" 
        lookup[o] = count

	if (p =~ /#type/) 
	  #puts "#{o} may be a type"
	  types[o]=1
        end
        count += 1
      end
 
#      puts "Storing: p:#{p} s:#{s} o:#{o} \n"
    
      #BUG: mayn't have stored p and o. should make a register() function
      arc.push [lookup[s],lookup[p],lookup[o]]
      }
    }

    lookup.each_key do |text|
      n = lookup[text]
      l = res[n]
      l.gsub!(/"/,"")   

      if (!predicates[l]) 
#trim nodes!        l.gsub(/
        out += "r#{n} [label=\"#{l}\"]; \n"
      end
    end

    arc.each do |edge|
      arclabel=res[edge[1]]
      arclabel.gsub!(/http:\/\/www\.w3\.org\/1999\/02\/22-rdf-syntax-ns#/,"")
      out += "r#{edge[0]} -> r#{edge[2]} [label=\"#{arclabel}\"]; \n"
    end       

    out += "\n\n}\n\n"
#    out += "debug: \n RES: #{res.inspect} \nARC:\n#{arc.inspect} \nLOOKUP:\n#{lookup.inspect}\n"
    return out
  end






  def toSQL()
    # We generate SQL script based on a SHA1 dump, same as java code
    # see Node.sha1 method
    rdfsha1={}
    out = " --- SQL-RDF dump of RDF database $Id: basicrdf.rb,v 1.1 2002-07-17 11:45:11 danbri Exp $ \n"
    # sub pred obj person src asserted 
    @fp.each_key do |key|
      s,junk, p = key.split(/ | /,3)
      sub = Node.getResource(s, self)
      pred = Node.getResource(p, self)      
      rdfsha1[s] = sub.sha1
      rdfsha1[p]=pred.sha1
      stem = "insert into triples values ('#{sub.sha1}', '#{pred.sha1}', "
      @fp[key].each do  |value| 
        isres = (value.is_resource)? 't':'f'
        out += "#{stem} '#{value.sha1}','assertid-src-notyet:ruby-rdf:$Id: basicrdf.rb,v 1.1 2002-07-17 11:45:11 danbri Exp $','personidid:notyet','#{isres}'); \n" 
        object = "#{value}"
	object.gsub!("'","") # zapping not ideal, but "\'" and "\\'" failed
	object.gsub!("^\"","")
	object.gsub!("\"$","")
        rdfsha1[ "#{object}" ] = value.sha1
      end
    end
    rdfsha1.each_key do |thing|
      puts "insert into resources values ( '#{rdfsha1[thing]}', '#{thing}' );\n"
    end
    
    return out
  end

 
  def toSQLInserts(src='assertid-src-notyet:ruby-rdf:$Id: basicrdf.rb,v 1.1 2002-07-17 11:45:11 danbri Exp $')
    # We generate SQL script based on a SHA1 dump, same as java code
    # see Node.sha1 method
    rdfsha1={}
    out=[]
    # sub pred obj person src asserted 
    @fp.each_key do |key|
      s,junk, p = key.split(/ | /,3)
      sub = Node.getResource(s, self)
      pred = Node.getResource(p, self)      
      rdfsha1[s] = sub.sha1
      rdfsha1[p]=pred.sha1
      stem = "insert into triples values ('#{sub.sha1}', '#{pred.sha1}', "
      @fp[key].each do  |value| 
        isres = (value.is_resource)? 't':'f'

        out.push "#{stem} '#{value.sha1}', '#{src}','' ,'#{isres}'); \n" 

        object = "#{value}"
	object.gsub!("'","") # zapping not ideal, but "\'" and "\\'" failed
	object.gsub!("^\"","")
	object.gsub!("\"$","")
#	object.gsub!("\"-ja$","")
	object.gsub!(/\\/, '\&\&')
       rdfsha1[ "#{object}" ] = value.sha1
      end
    end
    rdfsha1.each_key do |thing|
      out.push "insert into resources values ( '#{rdfsha1[thing]}', '#{thing}' );\n"
    end
    
    return out
  end



  # return all the blunt ends of the arcs in this graph
  def subjects()
    ans=[]
    @db.each_key{ |statement| ans.push(statement.subject) } 
    return ans
  end

  # return all the sharp ends of the arcs in this graph
  def objects()
    ans=[]
    @db.each_key{ |statement| ans.push(statement.object) }
    return ans
  end

  # return all the label nodes for the arcs in this graph
  def predicates()
    ans=[]
    @db.each_key{ |statement| ans.push(statement.predicate) }
    return ans
  end
    
  def size()
    return @db.length 
  end

  def statements
    return @db.keys
  end


  # Graph.ask
  #
  # Basic query method for our RDF graph. passed a (template) statement w/ nils
  # ...and returns a graph (which we can probe with subjects(), predicates() etc
  #
  # Notes: this is all pretty inefficient, creating new graphs all
  # over the shop when we needn't, new statements etc etc.
  # todo: change @fp and @bp to store refs to statements not nodes
  #       ...and think about how we can return sub-graph matches
  # without having to go index them. maybe do indexing on demand?
  # (see also tell()

  def ask(query)
    # puts "Vapourware ask/query method called, template statement: #{query} "

    # ooo: dump all statements in the graph
    if (query.predicate==nil && query.subject==nil && query.object==nil) 
      dump =[]
      @db.each_key() {|k| dump.push(k)}
      return Graph.new(dump) 
    end

    # oxo: dump all statements in the graph with fixed predicate
    # notes: Statement or Node class should be more useful for comparisons
    if (query.subject==nil && query.object==nil) 
      dump =[]
      @db.each_key() do |k| 
        # puts "TRIPLE: #{k.predicate.inspect} vs #{query.predicate}\n"
        dump.push(k) if k.predicate.to_s == query.predicate.to_s
      end
      return Graph.new(dump) 
    end
   
    # xxx: is this statement in the graph? 
    if (query.predicate && query.subject && query.object)
      if ( @db[query] == nil) 
        # puts "Test failed: statement #{query.inspect} is not in graph\n"
      return Graph.new( [ ] ) 
      end
      if ( @db[query] >0)
        # puts "Test succeeded: statement #{query.inspect} is in graph\n"
        return Graph.new([query]) if (@db[query])
      end
    end

    # xxo
    if (query.predicate && query.subject && query.object==nil)
      # puts "xxo: get value(s) given sp"
      # puts "subject = '#{query.subject}' predicate= '#{query.predicate}' \n"
      # puts "Answer lookup: "
      #old:     return @fp["#{query.subject} | #{query.predicate}"]
      obs = @fp["#{query.subject} | #{query.predicate}"]
      response=[]
      query.subject.graph=self	# Is this wrong?
      query.predicate.graph=self

      if obs
        # suppress duplicates (on node identity not strings...)
        obs.uniq!
        seen={}
        new=[]
        obs.each {|i| new.push(i) if !(seen[i.to_s]); seen[i.to_s]=1 }
        obs = new.uniq
      end

      obs.each { |object| object.graph=self; response.push(Statement.new(query.subject,query.predicate, object)) } if obs
      ans = Graph.new(response)
      return ans
    end
 
    ## TODO: THIS IS INEFFICIENT. STORE STATEMENTS IN FP and BP!!!
    if (query.predicate && query.subject==nil && query.object)
      # puts "oxx: get subjects(s) given po: #{query.inspect}"
      subs = @bp["#{query.predicate} | #{query.object}"]
      # puts "Subs: #{subs.inspect} \n\nbp: \n\n#{@bp.inspect} \n\n"
      # puts "fp\n\n#{@fp.inspect} \n\n"
      response=[] 
      query.predicate.graph=self
      query.object.graph=self

      if subs
        # suppress duplicates (on node identity not strings...)
        subs.uniq!
        seen={}
        new=[]
        subs.each {|i| new.push(i) if !(seen[i.to_s]); seen[i.to_s]=1 }
        subs = new.uniq
      end

      subs.each { |subject| subject.graph=self; response.push(Statement.new(subject,query.predicate,query.object)) } if subs
      ans = Graph.new(response)
      # puts "Returning a graph! details: #{ans.inspect} \n"
      return ans 
    end 
  
  ## more query facilities needed here

  # done: xxx ooo xxo oxx oxo
  # todo: xox oox xoo 

    if (query.predicate==nil && query.subject && query.object==nil)
      raise "Graph.ask() doesn't implement xox matches"
    end

    if (query.predicate==nil && query.subject==nil && query.object)
      raise "Graph.ask() doesn't implement oox matches"
    end

    if (query.predicate==nil && query.subject && query.object==nil)
      raise "Graph.ask() doesn't implement xoo matches"
    end

  end

  # nodes that know... (about a graph)
  def getResource(content)
    return Node.getResource(content, self)
  end

  def getBlank()
    return Node.getBlank(self)
  end

  def getLiteral(content)
    return Node.getLiteral(content, self)
  end

end

###########################################################################
#
class Statement
  attr_accessor :predicate, :subject, :object
  def initialize (subject, predicate, object)

    subject   = Node.getResource(subject) if subject.type == String 
    predicate = Node.getResource(predicate) if predicate.type == String
    object = Node.getResource(object) if object.type == String #todo
    #print "In statement init. #{subject} -- #{predicate} -> #{object} \n"

    @subject = subject
    @predicate = predicate
    @object = object
  end
#  def inspect
#    "<#@subject> <#@predicate> <#@object> "
#  end 
  def to_s
  " #@subject> <#@predicate> <#@object> .\n"
  end

end

############################################################################
#    A rather basic NTriples parser, cut down from:
#    http://www.w3.org/2000/10/swap/n-triples2kif.pl
#    http://www.w3.org/TR/rdf-testcases/#ntriples
# seeAlso: 
#     http://www.rubycentral.com/book/tut_stdtypes.html

class Loader

  attr_accessor :base, :files

  # clean up a term t and return
  # litOK: flag whether literals acceptable or not
  def Loader.term (t, litOK)
    t.chomp()
    t=t.sub(/^\s*</,'')
    t=t.sub(/>\s*$/,'') 
    # a whole bunch more stuff see perl script above
    # print "Modified t: #{t.inspect} \n" 
    return t
  end

  # call out to an RDF2Ntriples parser
  def Loader.fn2nt (fn)
    print "Running external parser on filename #{fn} \n"
    ##TODO!
    return 
  end

  def initialize (files='../..')
    @files = '../..'
    @@files = @files
  end

  # filename, returning ntriples as text
  #
  def Loader.xsltrdf (fn, baseuri='file:/dev/null/nobaseuri/') 
    xsltparser='rdf2nt-mf.xsl'
    parser=RDFParser.xslt
    xsl = parser
    xml =  open(fn){|f| f.read}  

    require 'sablot' #exception handling if this is missing?
    sab = Sablot.new()
    arg = {"a"=>xsl, "b"=>xml}
    param = {"base-uri" => baseuri}
    begin
      sab.runProcessor("arg:/a", "arg:/b", "arg:/c",  param, arg)
    rescue
      puts "Error: "+$!
    end
    res=  sab.resultArg("arg:/c")
    return res
  end


  def Loader.xsltstring2rdf (xml, baseuri)
    require 'sablot' #exception handling if this is missing?
    sab = Sablot.new()
    arg = {"a"=>RDFParser.xslt, "b"=>xml}
    param = {"base-uri" => baseuri}
    begin
      sab.runProcessor("arg:/a", "arg:/b", "arg:/c",  param, arg)
    rescue
      puts "Error: "+$!
    end
    res=  sab.resultArg("arg:/c")
    return res
  end



  # pull NTriples from somewhere, return them as a new Graph
  #
  def Loader.rdf2nt (input, data=Graph.new([]))
    # print "NT reader: #{input.inspect}\n"
    gets.each {|l| Loader.parseline(l,data) } # defaults STDIN
    return data
  end

  # URI (just a filename currently), returns a data graph (somehow)
  # 
   # this is all broke: shouldn't need to pass in the RDF parser
  def Loader.rdf2graph(uri, base, data = Graph.new([]) )
    nt = Loader.xsltrdf(uri,base ) # we'll use XSLT
			       # could use web service, commandline etc too
    nt = "#{nt}"
    nt.each { |l| Loader.parseline(l,data) } 
    # print "Got graph in NT: #{data.inspect} \n"
    return data
   end


  def Loader.rdfdata2graph(rdfdata, base, data = Graph.new([]) )
    nt = Loader.xsltstring2rdf(rdfdata,base ) # we'll use XSLT
    nt = "#{nt}"
    nt.each { |l| Loader.parseline(l,data) } 
    return data
   end


  def Loader.nt2graph(input='stdin', data=Graph.new([]) )
    # default to STDIN; should allow forfiles too
    bNodeIDCache={}
    while (gets)
      Loader.parseline($_,data, bNodeIDCache)
    end
    return data
  end
  
  def Loader.ntfile2graph(file='_default.nt', data=Graph.new([]) )
    begin 
    File.open(file) do |f|
      bNodeIDCache={}
      f.each do |line|
        Loader.parseline line ,data, bNodeIDCache
      end
    end

    rescue 
      puts "Error opening file '#{file}', no data loaded." # stderr?
    end
  return data
  end

  def Loader.parseline(line,data,bNodeIDCache={})  

      $_ = line # need to tidy this up
      return if /^#/		# re IO see 
      return unless /\S/	# http://www.rubycentral.com/book/intro.html
      $_ = $_.sub(/^ */, '')	# http://www.rubycentral.com/book/tut_io.html
      $_ = $_.sub(/\s*\.\s*$/, '')	# 
      chomp
      #print "LOADER: data is now: '#{$_}' \n"
   

      ## TODO: literals with spaces
      parts = $_.split(/\s+/) # other ws? tricky as need to re-assemble
			    # this is the wrong way to parse ntriples
      #puts "LOADER: Summary: #{parts.inspect}\n"
      return unless parts.length>=3 # literals > 3? 
      parts[2] += " "+parts[3..parts.length].join(' ') if(parts.length >3)
      st = Loader.term(parts[0], 0)
      pt = Loader.term(parts[1], 0)
      ot = Loader.term(parts[2], 1)
      #puts "LOADER: output: s:#{st} p:#{pt} o:#{ot} \n"# xxxxdanbri
	## todo: here we should behave differently for genid'd values
	## this in NTriple stuff _: so code lives here.

	# this is clearly a bit longwinded and in need of re-org
	# don't think we do literals-vs-resources right (unless Node.new handes it)
	if st =~ /^_:/
	  s_node = Node.getBlank(data, bNodeIDCache[st]) 
          bNodeIDCache[st]=s_node.to_s
	else 
          s_node = Node.new(st)
        end

	if pt =~ /^_:/
	  p_node = Node.getBlank(data, bNodeIDCache[pt]) 
          bNodeIDCache[pt]=p_node.to_s # should only do if needed
	else 
          p_node = Node.new(pt)
        end

	if ot =~ /^_:/
	  o_node = Node.getBlank(data, bNodeIDCache[ot]) 
          bNodeIDCache[ot]=o_node.to_s
	else 
          o_node = Node.new(ot)
	end

      s = Statement.new( s_node, p_node, o_node )
      data.tell(s)
  end
end


class RDFParser
# by MaxF, copied here so we can find it easily!
# todo:
# add link to home URI for this...

def RDFParser.xslt 

return %{<?xml version="1.0" encoding="iso-8859-1"?>


<!-- 
Parser_#5, an RDF to N-triples XSLT transform

Copyright Max Froumentin, 2002.

Use and distribution of this code are permitted under the terms of the <a
href="http://www.w3.org/Consortium/Legal/copyright-software-19980720"
>W3C Software Notice and License</a>.
-->


<stylesheet xmlns="http://www.w3.org/1999/XSL/Transform"
            xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
            version="1.0">


  <output method="text" encoding="US-ASCII"/>

  <strip-space elements="*"/>

  <param name="base" select="''"/>

  <variable name="debug" select="0"/>
  
  <!-- if there's an xml:base attribute use it as the base URI, otherwise -->
  <!-- use the one passed as parameter $base -->
       
  <variable name="baseURI">
    <choose>
      <when test="rdf:RDF/@xml:base">
        <choose>
          <!-- if the baseURI has a fragment identifier, remove it -->
          <when test="contains(rdf:RDF/@xml:base,'#')">
            <value-of select="substring-before(rdf:RDF/@xml:base,'#')"/>
          </when>
          <otherwise>
            <value-of select="rdf:RDF/@xml:base"/>
          </otherwise>
        </choose>
      </when>
      <otherwise>
        <value-of select="$base"/>
      </otherwise>
    </choose>
  </variable>


  <variable name="rdfnsURI" select="'http://www.w3.org/1999/02/22-rdf-syntax-ns#'"/>

  <!-- ================================================================= -->
  <template match="/">
    <apply-templates/>
  </template>

  <!-- ================================================================= -->
  <template match="rdf:RDF">
    <apply-templates mode="node"/>
  </template>

  <!-- ================================================================= -->

  <template match="*" mode="node">
    <if test="$debug > 0">
      <text># N-triples for node: </text>
      <value-of select="concat(local-name(),'&#xa;')"/>
    </if>

    <if test="namespace-uri() = $rdfnsURI and 
                  (@about or @resource or @type or @ID or @value)">
      <message>Warning: rdf attribute used without prefix</message>
    </if>


    <if test="not(self::rdf:Description)">
      <!-- n-triple for the type of this element -->
      <for-each select="@*[not(namespace-uri() = $rdfnsURI)]">
        <!-- subject: parent element's generate-id  -->
        <value-of select="concat('_:',generate-id(..),' ')"/>

        <!-- predicate: attribute name -->
      <value-of select="concat('&lt;',namespace-uri(),local-name(),'&gt;')"/>
      <!-- object: attribute value -->
      <value-of select="concat(' ',.,' .&#xa;')"/>

      </for-each>

      <!-- subject -->
      <choose>
        <when test="@rdf:about">
          <variable name="s">
            <call-template name="expand">
              <with-param name="base" select="$baseURI"/>
              <with-param name="there" select="@rdf:about"/>
            </call-template>
          </variable>
          <value-of select="concat('&lt;',$s,'&gt; ')"/>
        </when>
        <otherwise>
          <value-of select="concat('_:',generate-id(),' ')"/>
        </otherwise>
      </choose>

      <!-- predicate -->
      <value-of select="concat('&lt;',$rdfnsURI,'type&gt; ')"/>

      <!-- object -->
      <value-of select="concat('&lt;',namespace-uri(),local-name(),'&gt; .&#xa;')"/>
    </if>

    <if test="self::rdf:Description">
      <for-each select="@*[not(namespace-uri() = $rdfnsURI)]">

        <!-- subject, rdf:ID of parent element -->
        <value-of select="concat('&lt;',$baseURI,'#',../@rdf:ID,'&gt;')"/>
        
        <!-- predicate: name of attribute -->
        <value-of select="concat('&lt;',namespace-uri(),local-name(),'&gt;')"/>

        <!-- object: value of attribute -->
        <value-of select="concat('&quot;',.,'&quot; .&#xa;')"/>
      </for-each>
    </if>

    <!-- process the arcs -->
    <apply-templates mode="arc"/>
  </template>
    
  <!-- ==================================================================== -->

  <template match="*" mode="arc">
    <if test="$debug > 0">
      <text># N-triples for arc: </text>
      <value-of select="concat(local-name(),'&#xa;')"/>
    </if>

    <!-- 1st element of triple: subject (id of parent) -->
    <variable name="subject">
      <!-- we put the subject in a variable as it could be used later -->
      <!-- if we reify the statement -->
      <choose>
        <when test="../@rdf:about">
          <value-of select="concat('&lt;',../@rdf:about,'&gt;')"/>
        </when>

        <when test="../@rdf:ID">
          <value-of select="concat('&lt;',$baseURI,'#',../@rdf:ID,'&gt;')"/>
        </when>

        <otherwise>
          <!-- no ID or parent about: this is probably a bNode -->
          <value-of select="concat('_:',generate-id(..))"/>
        </otherwise>
      </choose>
    </variable>

    <!-- 2nd element: predicate (URI of node) -->
    <variable name="predicate">
      <choose>
        <when test="self::rdf:li">
          <value-of select="concat('&lt;',namespace-uri(),'_',count(preceding-sibling::rdf:li|.),'&gt;')"/>
        </when>
        <otherwise>
          <value-of select="concat('&lt;',namespace-uri(),local-name(),'&gt;')"/>
        </otherwise>
      </choose>

      
    </variable>
    

    <!-- 3rd element: object (URI or literal) -->
    <variable name="object">
      <choose>
        <when test="@rdf:parseType='Literal'">
          <value-of select="concat('xml&quot;',.,'&quot;')"/>
        </when>
        
        <when test="@rdf:parseType='Resource'">
          <value-of select="concat('_:',generate-id(.))"/>
        </when>
        
        <when test="@rdf:resource">
          <!-- target is a resource -->
          <variable name="r">
            <call-template name="expand">
              <with-param name="base" select="$baseURI"/>
              <with-param name="there" select="@rdf:resource"/>
            </call-template>
          </variable>

          <value-of select="concat('&lt;',$r,'&gt;')"/>

          <!--
          <value-of select="concat('&lt;',$baseURI,@rdf:resource,'&gt;')"/>
          -->
        </when>
        
        <when test="text()">
          <!-- target is a literal -->
          <value-of select="concat('&quot;',.,'&quot;')"/>
        </when>
        
        <!-- target is an RDF container rdf:Resource, rdf:Bag, -->
        <!-- rdf:Seq, rdf:Alt -->
        <!-- I assume those never have rdf:about -->
        <!--        <when test="namespace-uri(child::*)=$rdfnsURI">-->
        <when test="*">
          <value-of select="concat('_:',generate-id(child::*))"/>
        </when>

        <!-- arc has a child: it is the object, a bNode -->
        <!--
        <when test="*">
          <value-of select="concatgenerate-id(*[1])"/>
        </when>
-->
        <!-- arc has no target attributes or children -->
        <!-- object is then empty string -->
        <when test="not(@*[not(namespace-uri()=$rdfnsURI)])">
          <text>""</text>
        </when>
      </choose>
    </variable>

    <!-- if there's a reason to print this, do it -->
    <!-- how helpful the above line is -->
    <!-- basically, this test means: it the value of $object has been -->
    <!-- computed above, output the triple -->
    <!-- (there must be a more elegant way to do this) -->
         
    <if test="* or text() or @rdf:resource or not(@*[not(namespace-uri()=$rdfnsURI)])">
      <value-of select="concat($subject,' ')"/>
      <value-of select="concat($predicate,' ')"/>
      <value-of select="concat($object,' .&#xa;')"/>
    </if>
    
    <!-- if the current arc has non-rdf attributes, they are targets -->
    <!-- and a couple of n-triples must be generated for each -->
    <for-each select="@*[not(namespace-uri()=$rdfnsURI)]">
      <value-of select="concat($subject,' ')"/>
      <value-of select="concat($predicate,' ')"/>
      <value-of select="concat('_a:',generate-id(),' .&#xa;')"/>

      <value-of select="concat('_a:',generate-id(),' ')"/>
      <value-of select="concat('&lt;',namespace-uri(),local-name(),'&gt; ')"/>
      <value-of select="concat('&quot;',.,'&quot; .&#xa;')"/>
    </for-each>



    <!-- if target has an rdf:ID, the statement itself should be -->
    <!-- reified it seems. Not sure why (from example test0005.rdf) -->
    
    <if test="@rdf:ID">
      <!-- First n-triple: type -->
      
      <!-- subject -->
      <value-of select="concat('&lt;',$baseURI,'#',@rdf:ID,'&gt; ')"/>
      <!-- predicate -->
      <value-of select="concat('&lt;',$rdfnsURI,'type&gt; ')"/>
      <!-- object -->
      <value-of select="concat('&lt;',$rdfnsURI,'Statement&gt; .&#xa;')"/>

      <!-- Second n-triple: subject -->

      <!-- subject -->
      <value-of select="concat('&lt;',$baseURI,'#',@rdf:ID,'&gt; ')"/>
      <!-- predicate -->
      <value-of select="concat('&lt;',$rdfnsURI,'subject&gt; ')"/>
      <!-- object -->
      <value-of select="concat($subject,' .&#xa;')"/>

      <!-- Second n-triple: predicate -->

      <!-- subject -->
      <value-of select="concat('&lt;',$baseURI,'#',@rdf:ID,'&gt; ')"/>
      <!-- predicate -->
      <value-of select="concat('&lt;',$rdfnsURI,'predicate&gt; ')"/>
      <!-- object -->
      <value-of select="concat($predicate,' . &#xa;')"/>

      <!-- Third n-triple: object -->
      <!-- subject -->
      <value-of select="concat('&lt;',$baseURI,'#',@rdf:ID,'&gt; ')"/>
      <!-- predicate -->
      <value-of select="concat('&lt;',$rdfnsURI,'object&gt; ')"/>
      <!-- object -->
      <value-of select="concat($object,' . &#xa;')"/>

    </if>

    <!-- children could be one node (@@or more than one?)  or arcs -->
    <choose>
      <when test="@rdf:parseType[.='Resource']">
        <apply-templates mode="arc"/>                
      </when>
      <otherwise>
        <apply-templates select="*" mode="node"/>        
      </otherwise>
    </choose>
  </template>

<!--########################################################################-->
<!--########################################################################-->
<!--########################################################################-->
<!--########################################################################-->

  <!-- From here on, the templates are from Dan Connoly's URI
       absolutizer. They used to be <include>d but have been included
       to make users lifes simpler (few bugs fixed too)
   -->
  
<!--
<div xmlns="http://www.w3.org/1999/xhtml">

<h2>Share and Enjoy</h2>

<p>$ uri.xsl,v 1.6 2000/09/08 08:06:31 connolly Exp $</p>

<p>Copyright (c) 2000 W3C (MIT, INRIA, Keio), released under the <a
href="http://www.w3.org/Consortium/Legal/copyright-software-19980720">
W3C Open Source License</a> of August 14 1998.  </p>

<h2>Reference</h2>

<p><cite><a href="http://www.ietf.org/rfc/rfc2396.txt">Uniform
    Resource Identifiers (URI): Generic Syntax</a></cite> (RFC 2396)
    T. Berners-Lee, R. Fielding, L. Masinter August 1998 </p>

</div>
-->

<variable name="lowalpha"
	      select='"abcdefghijklmnopqrstuvwxyz"'/>
<variable name="upalpha"
	      select='"ABCDEFGHIJKLMNOPQRSTUVWXYZ"'/>
<variable name="digit"
	      select='"01234567890"'/>
<variable name="alpha"
	      select='concat($lowalpha, $upalpha)'/>

<param name="Debug" select="0"/>

<template name="expand">
  <!-- 5.2. Resolving Relative References to Absolute Form -->
  <param name="base"/> <!-- an absolute URI -->
  <param name="there"/> <!-- a URI reference -->

  <!-- @@assert that $there contains only URI characters -->
  <!-- @@implement the unicode->ascii thingy from HTML 4.0 -->

  <variable name="fragment" select='substring-after($there, "#")'/>
		<!-- hmm... I'd like to split after the *last* #,
		     but substring-after splits after the first occurence.
		     Anyway... more than one # is illegal -->

  <variable name="hashFragment">
    <choose>
      <when test="string-length($fragment) > 0">
        <value-of select='concat("#", $fragment)'/>
      </when>
      <otherwise>
        <value-of select='""'/>
      </otherwise>
    </choose>
  </variable>
  <variable name="rest"
		select='substring($there, 1,
			          string-length($there)
				  - string-length($hashFragment))'/>

  <if test="$Debug"><message>
     [<value-of select="$there"/>]
     [<value-of select="$fragment"/>]
     [<value-of select="$hashFragment"/>]
     [<value-of select="$rest"/>]
  </message></if>

  <choose>
    <!-- step 2) -->
    <when test="string-length($rest) = 0">
      <if test="0">
      <message>expand called with reference to self-same document.
			     should this be prohibited? i.e.
			     should the caller handle references
			     to the self-same document?</message>
      </if>
      <value-of select="concat($base, $hashFragment)"/>
    </when>

    <otherwise>
      <variable name="scheme">
        <call-template name="split-scheme">
	  <with-param name="ref" select="$rest"/>
	</call-template>
      </variable>

      <choose>
        <when test='string-length($scheme) > 0'>
	  <!-- step 3) ref is absolute. we're done -->
	  <value-of select="$there"/>
	</when>

        <otherwise>
	  <variable name="rest2"
			select='substring($rest, string-length($scheme) + 1)'/>

	  <variable name="baseScheme">
	    <call-template name="split-scheme">
	    <with-param name="ref" select="$base"/>
	    </call-template>
	  </variable>

	  <choose>
	    <when test='starts-with($rest2, "//")'>
	      <!-- step 4) network-path; we're done -->

	      <value-of select='concat($baseScheme, ":",
					   $rest2, $hashFragment)'/>
            </when>

	    <otherwise>

	      <variable name="baseRest"
			    select='substring($base,
				 string-length($baseScheme) + 2)'/>

	      <variable name="baseAuthority">
		<call-template name="split-authority">
		  <with-param name="ref" select="$baseRest"/>
		</call-template>
	      </variable>

	      <choose>
	        <when test='starts-with($rest2, "/")'>
		  <!-- step 5) absolute-path; we're done -->

		  <value-of select='concat($baseScheme, ":",
					       $baseAuthority,
					       $rest2, $hashFragment)'/>
		</when>

		<otherwise>
		  <!-- step 6) relative-path -->
		  <!-- @@ this part of the implementation is *NOT*
		       per the spec, because I want combine(wrt(x,y))=y
		       even in the case of y = foo/../bar
		       -->

		  <variable name="baseRest2"
			    select='substring($baseRest,
				 string-length($baseAuthority) + 1)'/>

		  <variable name="baseParent">
		    <call-template name="path-parent">
		      <with-param name="path" select="$baseRest2"/>
		    </call-template>
		  </variable>

		  <variable name="path">
		    <call-template name="follow-path">
		      <with-param name="start" select="$baseParent"/>
		      <with-param name="path" select="$rest"/>
		    </call-template>
		  </variable>

		  <if test="$Debug"><message>
		    step 6 rel
		     [<value-of select="$rest2"/>]
		     [<value-of select="$baseRest2"/>]
		     [<value-of select="$baseParent"/>]
		     [<value-of select="$path"/>]
		  </message></if>

		  <value-of select='concat($baseScheme, ":",
					       $baseAuthority,
					       $path,
					       $hashFragment)'/>
		</otherwise>
	      </choose>
	    </otherwise>
	  </choose>

        </otherwise>
      </choose>
    </otherwise>
  </choose>
</template>


<template name="split-scheme">
  <!-- from a URI reference -->
  <param name="ref"/>

  <variable name="scheme_"
		    select='substring-before($ref, ":")'/>
  <choose>
    <!-- test whether $scheme_ is a legal scheme name,
	 i.e. whether it starts with an alpha
	 and contains only alpha, digit, +, -, .
	 -->
    <when
      test='string-length($scheme_) > 0
            and contains($alpha, substring($scheme_, 1, 1))
	    and string-length(translate(substring($scheme_, 2),
			                concat($alpha, $digit,
					       "+-."),
				        "")) = 0'>
	  <value-of select="$scheme_"/>
    </when>

    <otherwise>
      <value-of select='""'/>
    </otherwise>
  </choose>
</template>


<template name="split-authority">
  <!-- from a URI reference that has had the fragment identifier
       and scheme removed -->
       <!-- cf 3.2. Authority Component -->

  <param name="ref"/>

  <choose>
    <when test='starts-with($ref, "//")'>
      <variable name="auth1" select='substring($ref, 3)'/>
      <variable name="auth2">
        <choose>
          <when test='contains($auth1, "?")'>
	    <value-of select='substring-before($auth1, "?")'/>
	  </when>
	  <otherwise><value-of select="$auth1"/>
	  </otherwise>
	</choose>
      </variable>

      <variable name="auth3">
        <choose>
          <when test='contains($auth2, "/")'>
	    <value-of select='substring-before($auth1, "/")'/>
	  </when>
	  <otherwise><value-of select="$auth2"/>
	  </otherwise>
	</choose>
      </variable>

      <value-of select='concat("//", $auth3)'/>
    </when>

    <otherwise>
      <value-of select='""'/>
    </otherwise>
  </choose>
</template>

<template name="follow-path">
  <param name="start"/> <!-- doesn't end with / ; may be empty -->
  <param name="path"/> <!-- doesn't start with / -->

  <if test="$Debug"><message>
    follow-path
     [<value-of select="$start"/>]
     [<value-of select="$path"/>]
  </message></if>

  <choose>
    <when test='starts-with($path, "./")'>
      <call-template name="follow-path">
        <with-param name="start" select="$start"/>
	<with-param name="path" select='substring($path, 3)'/>
      </call-template>
    </when>

    <when test='starts-with($path, "../")'>
      <call-template name="follow-path">
        <with-param name="start">
	  <call-template name="path-parent">
	    <with-param name="path" select="$start"/>
	  </call-template>
	</with-param>
	<with-param name="path" select='substring($path, 4)'/>
      </call-template>
    </when>

    <otherwise>
      <value-of select='concat($start, "/", $path)'/>
    </otherwise>
  </choose>
</template>


<template name="path-parent">
  <param name="path"/>

  <if test="$Debug"><message>
    path parent
     [<value-of select="$path"/>]
  </message></if>

  <choose>
	      <!-- foo/bar/    => foo/bar    , return -->
    <when test='substring($path, string-length($path)) = "/"'>
      <value-of select='substring($path, 1, string-length($path)-1)'/>
    </when>

	      <!-- foo/bar/baz => foo/bar/ba , recur -->
	      <!-- foo/bar/ba  => foo/bar/b  , recur -->
	      <!-- foo/bar/b   => foo/bar/   , recur -->
    <when test='contains($path, "/")'>
      <call-template name="path-parent">
        <with-param name="path"
		   select='substring($path, 1, string-length($path)-1)'/>
      </call-template>
    </when>

	      <!-- '' => '' -->
	      <!-- foo => '' -->
    <otherwise>
      <value-of select='""'/>
    </otherwise>

  </choose>

</template>

</stylesheet>


}
end

end

#!/usr/bin/env ruby
#
# Ruby code to generate a spec from namespace + per-term docs.
# Dan Brickley <danbri@w3.org>


# http://www.w3.org/2000/06/webdata/xslt?xslfile=http%3A%2F%2Fwww.w3.org%2F2003%2F02%2Fcolour-xml-serializer.xsl&xmlfile=http%3A%2F%2Fxmlns.com%2Ffoaf%2F0.1%2Findex.rdf&transform=Submit

require 'squish'
require 'basicrdf'




class Vocabulary

FOAF = 'http://xmlns.com/foaf/0.1/'
DC = 'http://purl.org/dc/elements/1.1/'
RDF = 'http://www.w3.org/1999/02/22-rdf-syntax-ns#'
RDFS = 'http://www.w3.org/2000/01/rdf-schema#'
OWL = 'http://www.w3.org/2002/07/owl#'
VS = 'http://www.w3.org/2003/06/sw-vocab-status/ns#'

attr_accessor :spec, :termlist, :ranges, :domains

def initialize 
  # STDERR.puts "Vocabulary class starting up..."
  ranges={}
  domains={}
end

# todo: shouldn't be foaf specific
def termlink(text)
   text.gsub(/<code>foaf:(\w+)<\/code>/){ defurl($1) }
end

# todo: shouldn't be foaf specific
def defurl(term)
  #STDERR.puts "defurl Term was: '#{term}' \n"
  return "<code><a href=\"#term_#{term}\">foaf:#{term}</a></code>"
end

def shortname (uri)
  uri = uri.to_s
  uri.gsub!('http://www.w3.org/1999/02/22-rdf-syntax-ns#', 'rdf:')
  uri.gsub!(/http:\/\/www.w3.org\/2000\/01\/rdf-schema#/, 'rdfs:')
  return uri
end

def owlInfo(t)
  types=t.rdf_type
  ifp=false
  types.each do |d|
    u=d.to_s
    ifp=true if u==OWL+'InverseFunctionalProperty'
  end
#  return("an InverseFunctionalProperty (uniquely identifying property)\n<br/>") if ifp
  return("\t<tr><th>OWL Type:</th>\n\t<td>An InverseFunctionalProperty (uniquely identifying property)</td></tr>\n") if ifp

  return ''
end


def rdfsInfo(term, doc='')
  # domain and range stuff (properties only)
  d= term.rdfs_domain.to_s
  r= term.rdfs_range.to_s
  if (d!=nil) 
    d.gsub!(/http:\/\/xmlns.com\/foaf\/0.1\/(\w+)/){ "<a href=\"#term_#{$1}\">foaf:#{$1}</a>" }
#    doc += "domain: #{d}<br />"
    doc += "\t<tr><th>Domain:</th>\n\t<td>#{d}</td></tr>\n"
#    domains[term.to_s]=[d]   
  else
    doc += "-"
  end
  if (r!=nil) 
    r.gsub!(/http:\/\/xmlns.com\/foaf\/0.1\/(\w+)/){ "<a href=\"#term_#{$1}\">foaf:#{$1}</a>" }
#    doc += "range: #{r}<br />"
    doc += "\t<tr><th>Range:</th>\n\t<td>#{r}</td></tr>\n"
#    ranges[term.to_s]=[r]   
  else
    doc += "-"
  end
  return doc
end 


# todo: keep config state somewhere?
#
def htmlDocInfo(t, termdir='../doc/', doc='')
  fn=termdir+t+".en"
  if (File.exists?(fn))
    termdoc=File.new(fn).read
    doc += termlink(termdoc) 
  else
    STDERR.puts "No detailed documentation for term '#{t}'"
  end

  # CDATA handling no workie! help! not using this for now...
  doc.gsub!(/(.*)<\!\[CDATA\[([^[]+)\]\]>(.*)/) {  
    x=$2
    x=x.gsub(/&/,'&amp;');
    x=x.gsub(/</,'&lt;');
    x=x.gsub(/>/,'&gt;');
    STDERR.puts "\nCDATA fixed: #{x} \n\n"
    return ("#{$1}#{x}#{$3}")
  }
#  STDERR.puts "\nDOC fixed: #{doc} \n\n"
  return doc
end

# document a list of terms (where category is 'Class' or 'Property')
# spec is an RDF graph, and doc is string we're appending to.
#
def docTerms(category, list, spec, doc='')
nspre='foaf' # hardcoded for now
list.each do |t|
  term = Node.getResource(FOAF+t, spec)
  doc += "<div class=\"specterm\" id=\"term_#{t}\">\n<h3>#{category}: #{nspre}:#{t}</h3>\n"
  # almost vocab neutral. todo: 'foaf' shouldn't be hardcoded.
  l= term.rdfs_label.to_s
  c= term.rdfs_comment.to_s

  status= term.vs_term_status.to_s
  # MM: Listing the term name twice?
  doc += "<em>#{l}</em> - #{c} <br />"
  doc += "<table>\n\t<tr><th>Status:</th>\n\t<td>#{status}</td></tr>\n"
  doc += owlInfo(term)
  doc += rdfsInfo(term) if category=='Property'
  doc += "</table>\n"
  doc += htmlDocInfo(t)
  doc += "<p style=\"float: right; font-size: small;\">[<a href=\"#glance\">back to top</a>]</p>\n\n"

  doc += "\n<br/>\n</div>\n\n"
end
return doc
end



## Former main method, and shows!

def makeSpec 

spec.reg_xmlns FOAF, 'foaf'
spec.reg_xmlns DC, 'dc'
spec.reg_xmlns RDF,'rdf'
spec.reg_xmlns RDFS,'rdfs'
spec.reg_xmlns OWL, 'owl'
spec.reg_xmlns VS,'vs'


# Get all the URIs of properties and classes, and store in plist and clist.
# (must be a more concise way of doing this...)
classes = spec.ask(Statement.new (nil, RDF+'type', RDFS+'Class'))
props = spec.ask(Statement.new (nil, RDF+'type', RDF+'Property'))

clist=[]
classes.subjects.each do |classuri|
  c = Node.getResource classuri, spec
  clist.push c.to_s
end

plist=[]
props.subjects.each do |propuri|
  p = Node.getResource propuri, spec
  plist.push p.to_s
end




### OK a basic a-z table of contents

azlist = "<div style=\"padding: 5px; border: dotted; background-color: #ddd;\">\n"
azlist += "\n<p>Classes: |"
clist.sort.each do |t| 
  t.gsub!(FOAF,'') 
  azlist += " <a href=\"#term_#{t}\">#{t}</a> | "
end
azlist += "</p>\n"

azlist += "\n<p>Properties: |"
plist.sort.each do |t|
  t.gsub!(FOAF,'') 
  azlist += "<a href=\"#term_#{t}\">#{t}</a> | "
end
azlist += "</p>\n"
azlist += "</div>\n"




# fix ordering



## Full details in HTML

termlist = "<h3>Classes and Properties (full detail)</h3>"
termlist += docTerms('Property',plist,spec)
termlist += docTerms('Class',clist,spec)


# Output 

template=File.new('wip.html').read
rdfdata=File.new('index.rdf').read

colour=File.new('colour.html').read
#STDERR.puts("Colour: "+colour)
#colour =~ /<pre>(.*)<\/pre>/i
pretty = '<p>not available</p>'
pretty = $1 if $1
pretty=colour

nslist=<<EOT
xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#" xmlns:owl="http://www.w3.org/2002/07/owl#" xmlns:vs="http://www.w3.org/2003/06/sw-vocab-status/ns#" xmlns:foaf="http://xmlns.com/foaf/0.1/" xmlns:wot="http://xmlns.com/wot/0.1/" xmlns:dc="http://purl.org/dc/elements/1.1/"
EOT

nslist.gsub!(/ /,"\n  ")
pretty.gsub!(/&lt;rdf:RDF/, "\n&lt;rdf:RDF "+nslist)

template.gsub!(/<!--AZLIST-->/,azlist)
template.gsub!(/<!--TERMLIST-->/,termlist)
template.gsub!(/<!--RDFDATA-->/,rdfdata)
template.gsub!(/<!--PRETTY-->/,pretty)

puts template

#<!--ATOZ-->
#<!--TERMLIST-->


end

end #endVocab

##########################################################################


voc = Vocabulary.new()
voc.spec = Loader.get_rdf('index.rdf')
voc.makeSpec()


#puts "Ranges: "+voc.ranges.inspect

#!/usr/bin/env ruby
# RDF query (in memory version), Dan Brickley <danbri@w3.org>

require 'squish'
require 'basicrdf'


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
  return("an InverseFunctionalProperty (uniquely identifying property)\n<br/>") if ifp
  return ''
end


def rdfsInfo(term, doc='')
  # domain and range stuff (properties only)
  d= term.rdfs_domain.to_s
  r= term.rdfs_range.to_s
  if (d!=nil) 
    d.gsub!(/http:\/\/xmlns.com\/foaf\/0.1\/(\w+)/){ "<a href=\"#term_#{$1}\">foaf:#{$1}</a>" }
    doc += "domain: #{d}<br />"
  else
    doc += "-"
  end
  if (r!=nil) 
    r.gsub!(/http:\/\/xmlns.com\/foaf\/0.1\/(\w+)/){ "<a href=\"#term_#{$1}\">foaf:#{$1}</a>" }
    doc += "range: #{r}<br />"
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
  return doc
end

# document a list of terms (where category is 'Class' or 'Property')
# spec is an RDF graph, and doc is string we're appending to.
#
def docTerms(category, list, spec, doc='')
list.each do |t|
  term = Node.getResource(FOAF+t, spec)
  doc += "<div class=\"specterm\">\n<h3>#{category}: <a  name=\"term_#{t}\">foaf:#{t}</h3>\n"
  # almost vocab neutral. todo: 'foaf' shouldn't be hardcoded.
  l= term.rdfs_label.to_s
  c= term.rdfs_comment.to_s
  status= term.vs_term_status.to_s
  doc += "<em>#{l}</em> - #{c} <br />"
  doc += "status: #{status}<br />"
  doc += owlInfo(term)
  doc += rdfsInfo(term) if category=='Property'
  doc += htmlDocInfo(t)
  doc += "<br/><br/></div>\n\n"
end
return doc
end


##########################################################################

spec = Loader.get_rdf('index.rdf')


FOAF = spec.reg_xmlns 'http://xmlns.com/foaf/0.1/', 'foaf'
DC = spec.reg_xmlns 'http://purl.org/dc/elements/1.1/', 'dc'
RDF = spec.reg_xmlns 'http://www.w3.org/1999/02/22-rdf-syntax-ns#','rdf'
RDFS = spec.reg_xmlns 'http://www.w3.org/2000/01/rdf-schema#','rdfs'
OWL = spec.reg_xmlns 'http://www.w3.org/2002/07/owl#', 'owl'
VS = spec.reg_xmlns 'http://www.w3.org/2003/06/sw-vocab-status/ns#','vs'


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

azlist = "<h4>Classes and Properties (A-Z index)</h4>"
azlist += "<div style=\"padding: 5px; border: dotted; background-color: #ddd;\">\n"
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

termlist = "<h4>Classes and Properties (full detail)</h4>"
termlist += docTerms('Property',plist,spec)
termlist += docTerms('Class',clist,spec)


# Output 

template=File.new('wip.html').read
rdfdata=File.new('index.rdf').read

template.gsub!(/<!--AZLIST-->/,azlist)
template.gsub!(/<!--TERMLIST-->/,termlist)
template.gsub!(/<!--RDFDATA-->/,rdfdata)

puts template

#<!--ATOZ-->
#<!--TERMLIST-->



#!/usr/bin/env ruby
# RDF query (in memory version), Dan Brickley <danbri@w3.org>

require 'squish'
require 'basicrdf'

termdir='../doc/'


style=<<EOT;
     <style type="text/css" media="screen">
                dt {
                    font-weight: bold;
                }

                .class-table {
                    margin: 1px;
                    padding: 3px;
                }

                .tableheading     { background: #CCCCFF; font-weight:  bold; font-size: 120%;} /* Dark mauve */

                .float-right {
                  float:right;
                }
            </style>
EOT


def shortname (uri)
  uri = uri.to_s
  uri.gsub!('http://www.w3.org/1999/02/22-rdf-syntax-ns#', 'rdf:')
  uri.gsub!(/http:\/\/www.w3.org\/2000\/01\/rdf-schema#/, 'rdfs:')
  return uri
end

spec = Loader.get_rdf('index.rdf')

FOAF = spec.reg_xmlns 'http://xmlns.com/foaf/0.1/', 'foaf'
DC = spec.reg_xmlns 'http://purl.org/dc/elements/1.1/', 'dc'
RDF = spec.reg_xmlns 'http://www.w3.org/1999/02/22-rdf-syntax-ns#','rdf'
RDFS = spec.reg_xmlns 'http://www.w3.org/2000/01/rdf-schema#','rdfs'

#VDL = spec.reg_xmlns 'http://www.w3.org/2000/01/rdf-schema#','vdl'


s1=Statement.new (nil, RDF+'type', RDFS+'Class')
s2=Statement.new (nil, RDF+'type', RDF+'Property')
classes = spec.ask s1
props = spec.ask s2

clist=[]
plist=[]

puts '<?xml version="1.0" encoding="utf-8"?>
<html xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" 
xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#" 
xmlns:ver="http://www.ldodds.com/ns/ver/" 
xmlns:owl="http://www.w3.org/2002/07/owl#" 
xmlns:dc="http://purl.org/dc/elements/1.1/">
   <head>
      <title>FOAF Vocabulary Reference</title>'


puts style

puts "</head><body>"

puts "<table class=\"class-table\" border=\"1\" summary=\"RDF classes\" 
><tr><th>Class name</th><th>comment</th></tr>\n"

classes.subjects.each do |classuri|
  c = Node.getResource classuri, spec
  clist.push c.to_s
  # puts "C is #{c.inspect} \n"
  name = shortname(classuri)		#+" label="+c.rdfs_label.to_s 
  puts "<tr>\n <td>#{name}</td> \n"
  puts " <td>#{c.rdfs_comment}</td></tr> \n"  
end
puts "\n</table>\n\n"

puts "<table border=\"1\" summary=\"RDF properties\" ><tr><th>Property name</th><th>comment</th><th>domain</th><th>range</th></tr>\n"

# todo: are there any multi-valued domain or range declarations?

props.subjects.each do |propuri|
  p = Node.getResource propuri, spec
  plist.push p.to_s
  r = p.rdfs_range
  rt = r.to_s
  if (r.to_s == "") 
    rt = '<em>not specified</em>'
  end
  d= p.rdfs_domain
  dt = d.to_s
  if (d.to_s == "")
    dt = '<em>not specified</em>'
  end
  rt = shortname rt
  dt = shortname dt

  name = shortname (propuri) # + " label="+p.rdfs_label.to_s
  puts "<tr>\n <td>#{name}</td> \n"
    puts " <td>#{p.rdfs_comment.to_s}</td><td>#{dt}</td><td>#{rt}</td></tr> \n"  
end

puts "\n</table>\n\n"

clist.each { |c| c.gsub!(FOAF,'') }
plist.each { |c| c.gsub!(FOAF,'') }

# todo: shouldn't be foaf specific
def termlink(text)
   text.gsub(/<code>foaf:(\w+)<\/code>/){ defurl($1) }
end

# todo: shouldn't be foaf specific
def defurl(term)
  STDERR.puts "defurl Term was: '#{term}' \n"
  return "<code><a href=\"#term_#{term}\">foaf:#{term}</a></code>"
end


puts "Classes:\n"+clist.join("\n")+"\n"
puts "Properties:\n"+plist.join("\n")+"\n"
 
plist.each do |p|

  puts "<h3><a name=\"term_#{p}\">#{p}</h3>\n"
  prop = Node.getResource(FOAF+p, spec)
  d= prop.rdfs_domain.to_s

  if (d!=nil) 
    puts "<dl><dt>Domain:</dt><dd>#{d}</dd></dl>"
    #puts "Got d: #{d.inspect}"
  end

  if (File.exists?(termdir+p))
    termdoc=File.new(termdir+p).read
    puts termlink(termdoc) 
  else
    puts "No documentation for term '#{p}'"
  end

end

puts "</body></html>"

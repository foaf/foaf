#!/usr/bin/env ruby
# RDF query (in memory version), Dan Brickley <danbri@w3.org>

require 'squish'
require 'basicrdf'

#vdl = Loader.rdf2graph 'rdf_vdl.xml', 'http://www.w3.org/2000/01/rdf-schema#'
#`rdfdump -o ntriples file:rdf_vdl.xml > rdf_vdl.nt`

def shortname (uri)
  uri = uri.to_s
  uri.gsub!('http://www.w3.org/1999/02/22-rdf-syntax-ns#', 'rdf:')
  uri.gsub!(/http:\/\/www.w3.org\/2000\/01\/rdf-schema#/, 'rdfs:')
  return uri
end

vdl = Loader.ntfile2graph 'rdf_vdl.nt'
FOAF = vdl.reg_xmlns 'http://xmlns.com/foaf/0.1/', 'foaf'
DC = vdl.reg_xmlns 'http://purl.org/dc/elements/1.1/', 'dc'
RDF = vdl.reg_xmlns 'http://www.w3.org/1999/02/22-rdf-syntax-ns#','rdf'
VDL = vdl.reg_xmlns 'http://www.w3.org/2000/01/rdf-schema#','vdl'

puts "<html><head><title>RDF Vocab</title></head><body><h1>RDF Vocabulary</h1>\n"

s1=Statement.new (nil, RDF+'type', VDL+'Class')
s2=Statement.new (nil, RDF+'type', RDF+'Property')
classes = vdl.ask s1
props = vdl.ask s2


puts "<table border=\"1\" summary=\"RDF classes\" ><tr><th>Class name</th><th>comment</th></tr>\n"

classes.subjects.each do |classuri|
  c = Node.getResource classuri, vdl
  # puts "C is #{c.inspect} \n"
  name = shortname(classuri)		#+" label="+c.vdl_label.to_s 
  puts "<tr>\n <td>#{name}</td> \n"
  puts " <td>#{c.vdl_comment}</td></tr> \n"  
end
puts "</table>\n\n"


puts "<table border=\"1\" summary=\"RDF properties\" ><tr><th>Property name</th><th>comment</th><th>domain</th><th>range</th></tr>\n"

# todo: are there any multi-valued domain or range declarations?

props.subjects.each do |propuri|
  p = Node.getResource propuri, vdl
  r = p.vdl_range
  rt = r.to_s
  if (r.to_s == "") 
    rt = '<em>not specified</em>'
  end
  d= p.vdl_domain
  dt = d.to_s
  if (d.to_s == "")
    dt = '<em>not specified</em>'
  end
  rt = shortname rt
  dt = shortname dt

  name = shortname (propuri) # + " label="+p.vdl_label.to_s
  puts "<tr>\n <td>#{name}</td> \n"
    puts " <td>#{p.vdl_comment.to_s}</td><td>#{dt}</td><td>#{rt}</td></tr> \n"  
end



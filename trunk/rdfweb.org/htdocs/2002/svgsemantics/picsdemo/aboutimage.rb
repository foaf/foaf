#!/usr/bin/env ruby

# SVG/RDF demo
#
# reads RDF via N-Triples
# outputs XML summary including a chunk of SVG filtering rude bit(s) of a photo.
# for educational purposes only...
#
# danbri

require "basicrdf"  

# gotta get a real rdf parser:
`rdfdump -q -o ntriples file:rdf-taboo.rdf > rdf-taboo.nt`

data = Loader.ntfile2graph 'rdf-taboo.nt'

rdf 	= data.reg_xmlns 'http://www.w3.org/1999/02/22-rdf-syntax-ns#', 'rdf'
foaf 	= data.reg_xmlns 'http://xmlns.com/foaf/0.1/', 'foaf'
dc 	= data.reg_xmlns 'http://purl.org/dc/elements/1.1/', 'dc'
wn 	= data.reg_xmlns 'http://xmlns.com/wordnet/1.6/', 'wn'
rsvg 	= data.reg_xmlns "http://jibbering.com/2002/3/svg/#", 'rsvg'


def genSVG (url, path)
  return <<EOF
  <svg width="100%" height="100%" enable-background="new" >
   <defs><filter id="pixelize" x="-0.5" y="-0.5" width="2" height="2" filterRes="8"><feOffset in="BackgroundImage"/></filter></defs>
   <image opacity="1" width="640" height="480" x="0" y="0" xmlns:xlink="http://www.w3.org/1999/xlink" xlink:href="#{url}"/>
   <path stroke-width="10" filter="url(#pixelize)"  id="pathref0" d="#{path}" />
  </svg>
EOF
end

images = data.ask( Statement.new(nil,rdf+'type',foaf+'Image') ).subjects.each do |i|
  puts "<!-- got an image, URI: #{i} -->"
  parts = data.ask( Statement.new(i, rsvg+'hasPart', nil) ).objects.each do |p|
    puts "<!-- \n\n"
    puts "\n\tImage description tells of an SVG region: #{p}"
    puts "\tSVG path type: #{p.rdf_type} "
    depictee = Node.getResource p.foaf_regionDepicts, data
    puts "\tRegion depicts: #{depictee} "
    thingtype = depictee.rdf_type[0] # get a type 
    puts "\t\t...depicted object type: '#{thingtype}' foaf:rudeThing=#{thingtype.foaf_rudeThing} "
	
  puts "\n\n -->\n\n"
 
   if thingtype.foaf_rudeThing.to_s =~ /true/ 
      puts "<!-- **** foaffamilyfilter activated! **** -->"
      puts genSVG i, p.rsvg_polypath 
   end

    if (thingtype.to_s =~ wn.to_s) 	# yuck. sort out equality testing of nodes...
      puts "<!-- \tThis is a wordnet class; todo: lookup more info!... -->\n\n"
    end
  end
end

#!/usr/bin/env ruby
#
# script to read theyrule.net data from MySQL and 
# convert to RDF (FOAFCorp vocabulary).
#
#

require "mysql"
m = Mysql.new('localhost', 'foaf', 'foafcorp', 'foafcorp')

header = <<END
<?xml version="1.0" encoding="UTF-8"?>
<web:RDF xmlns:web="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
        xmlns:rss="http://purl.org/rss/1.0/"
        xmlns="http://xmlns.com/foaf/0.1/"
        xmlns:foaf="http://xmlns.com/foaf/0.1/"
        xmlns:fc="http://xmlns.com/foaf/corp#"
        xmlns:dc="http://purl.org/dc/elements/1.1/" >
END

makesql=nil
onboard = {}

puts (header)

companies = m.query("select * from companies")
companyfields = companies.fetch_fields.filter do |f| f.name end
companies.each do |row|
  puts ("<fc:Company fc:name=\"#{row[1]}\">\n")
  puts (" <homepage>\n  <Document dc:title=\"#{row[1]} homepage\" web:about=\"#{row[4]}\" /> \n ")
  puts (" </homepage>\n")
  puts (" <fc:board>\n   <fc:Committee>\n")
  corp = row[0]
  bods = row[2].split(',') 
  bods.each do |bod|
    name = m.query("select distinct directors.firstname, directors.lastname from onboard, directors where directors.id = '#{bod}'")
    name.each do |namerecord|
      fullname = "#{namerecord[0]} #{namerecord[1]}\n".chomp!;
      personuri = "web:about=\"http://rdfweb.org/2002/02/theyrule#pid_#{bod}\" "
      puts ("    <fc:member><Person #{personuri} fc:name=\"#{fullname}\"/></fc:member>\n")
    end
  end  
  puts ("  </fc:Committee>\n")
  puts (" </fc:board>\n")
  puts ("</fc:Company>\n")
end
puts ("\n</web:RDF>\n\n")



########################### SQL Schema info ############################
#
# directors and companies are the tables that Josh exports
# they both contain fields (boards, bod) that have arrays containing
# links to the other table. We use these to generate a 3rd table, 'onboard'
# making it convenient to do joins between these tables in SQL.
# (aside: maybe MySQL lets us do this anyway? I don't know how)

#directors: id      firstname       lastname        gender  boards  url  
#companies:  id      companyname     bod     rank2001        url   
#onboard: corpid personid



########################### RDF Schema info ############################
#
# The FOAFCorp RDF vocab we export to is an extension of RDFWeb's FOAF 
# vocabulary, containing classes and properties related to companies.
#
#http://rdfweb.org/people/danbri/2001/09/foafcorp/intro.html
#http://rdfweb.org/people/danbri/2001/09/foafcorp/cola-corps-sample.xml

# target example:
#
pepsi = <<END
<fc:Company fc:name="PepsiCo Investor Relations">
 <homepage>
  <Document dc:title="Pepsiworld" web:about="http://www.pepsico.com/"/>
 </homepage>
 <fc:stock>PEP</fc:stock>
 <fc:board>
  <fc:Committee>
    <fc:member><Person fc:name="Cynthia M. Trudell, Ph.d."/></fc:member>
    <fc:member><Person fc:name="Sharon P. Rockefeller"/></fc:member>
    ...
  </fc:Committee>
 </fc:board>
</fc:Company>

END









# Ruby MySQL code:
# http://www.tmtm.org/en/mysql/ruby/README



##
# random fragments
#

#directors = m.query("select * from directors")
#directorfields = directors.fetch_fields.filter do |f| f.name end
#puts directorfields.join("\t")
#directors.each do |row|
# puts row.join("\t")
#end



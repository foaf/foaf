#!/usr/bin/perl -w
#
# RDFWeb Image database query
# Queries a remote RDF data-service 
# (in this case, Libby's Apache/Axis-based wrapper for the RDFWeb db)
#
# we send an rdf graph-match query, plugging in a mailbox of the person we're 
# searching for. The SOAP service returns bindings in a table, one row per hit.

use SOAP::Lite;

my $who= shift || 'mailto:dturner@microsoft.com';

my $query = "SELECT ?personid, ?img, ?name 
       WHERE 
       (foaf::depiction ?personid ?img) 
       (foaf::mbox ?personid $who)
       (foaf::name ?personid ?name) 
 USING   
       foaf for http://xmlns.com/foaf/0.1/";

my $servicetype= 'http://rdfweb.org/RDF/RDFWeb/SOAPDemo';
my $dbconf     = 'http://swordfish.rdfweb.org/~libby/chump/rdfwebconf.rdf';
my $serviceuri = 'http://swordfish.rdfweb.org:8080/axis/servlet/AxisServlet';


# talk to the RDFWeb Service    
#
my $service = SOAP::Lite  -> uri($servicetype) -> proxy($serviceuri);
my $response = $service->squish($query, $dbconf, undef )->result();

print "<html><head><title>RDFWeb image index</title></head>\n";
print "<ul>\n";

foreach my $row ( @{ $response } ) {
  printf "<li><a href=\"%s\"> %s </a></li>", $$row{'img'},$$row{'name'} ;
  print "\n";
}
print "</ul>\n</body></html>";

#!/usr/bin/perl
#
# RDFWeb Image database query
# Queries a remote RDF data-service 
# (in this case, Libby's Apache/Axis-based wrapper for the RDFWeb db)
#
use SOAP::Lite;


my $query = 'SELECT ?personid, ?img, ?mbox, ?name 
       WHERE 
       (foaf::depiction ?personid ?img) 
       (foaf::mbox ?personid ?mbox) 
       (foaf::name ?personid ?name) 
 USING   
       foaf for http://xmlns.com/foaf/0.1/';

my $servicetype= 'http://rdfweb.org/RDF/RDFWeb/SOAPDemo';
my $dbconf     = 'http://swordfish.rdfweb.org/~libby/chump/rdfwebconf.rdf';
my $serviceuri = 'http://swordfish.rdfweb.org:8080/axis/servlet/AxisServlet';


# talk to the RDFWeb Service    
#
my $service = SOAP::Lite  -> uri($servicetype) -> proxy($serviceuri);
my $response = $service->squish($query, $dbconf, undef )->result();

foreach my $row ( @{ $response } ) {

  printf "%s %s %s", 
		 $$row{'img'},
		 $$row{'name'},
		 $$row{'mbox'} ;
  print "\n";
}

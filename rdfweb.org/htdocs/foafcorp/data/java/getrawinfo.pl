#!/usr/bin/perl -w
#
# RDFWeb Image database query
# Queries a remote RDF data-service 
# (in this case, Libby's Apache/Axis-based wrapper for the RDFWeb db)
#
use SOAP::Lite;


my $qfile = shift || 'pathinfo.squish';
my $query = `cat $qfile`;

my $servicetype= 'http://rdfweb.org/RDF/RDFWeb/SOAPDemo';
my $dbconf     = 'http://swordfish.rdfweb.org/~libby/chump/rdfwebconf.rdf';
my $serviceuri = 'http://swordfish.rdfweb.org:8080/axis/servlet/AxisServlet';


# talk to the RDFWeb Service    
#
my $service = SOAP::Lite  -> uri($servicetype) -> proxy($serviceuri);
my $response = $service->squish($query, $dbconf, undef )->result();


foreach my $row ( @{ $response } ) {

  print
		 $$row{'mbox1'}. " ".
		 $$row{'mbox2'}. " ". 
		 ($$row{'uri'} || '') ;
  print "\n";
}

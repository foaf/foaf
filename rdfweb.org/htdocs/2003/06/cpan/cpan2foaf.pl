#!/usr/bin/perl -w
#
# A crude Perl script to emit RDF/XML FOAF description of CPAN contributors
# 
# Author: dan brickley <danbri@rdfweb.org>, <danbri@w3.org>
#
# This reads the 01mailrc.txt list of authors and their cpan IDs and 'real' mailboxes.
# It puts both through sha1sum so we don't create a spam harvesting goldmine and emits
# a very basic RDF description on STDOUT.
#
# When RDF-merged with other data sources, this should provide fodder for queries such 
# as "Find me weblog posts by CPAN contributors". 
# For example, a match of:
#
# (foaf:weblog ?x ?y)
# (dc:contributor ?x http://www.cpan.org/)
# (foaf:name ?x ?z)
#
# ...using Squish RDF query syntax here would return us a table of ?x, ?y, ?z 
# results where ?y and ?z were the weblog URI and name of contributors to CPAN.
# 
# Note that our dataset here is inadequate to service this query, it solely provides
# information about who contributes, and a couple of their mailboxes. Assuming the 
# contributor had a FOAF description that mentioned (a) one of his/her mailboxes as 
# listed in CPAN authors file (b) their name and weblog, we'd have enough info.
# For the query to match, we'd need an RDF harvester that loaded up all the relevant 
# data, and that 'smushed' it together, ideally after computing the extra 
# mbox_sha1sum properties in case they weren't mentioned explicitly in the src data.
#
# origins: see #foaf chat,
# http://ilrt.org/discovery/chatlogs/foaf/2003-06-07#T13-32-31
#
# <crysflame> http://mirrors.cpan.org/
#.cpan/sources/authors/01mailrc.txt.gz can be downloaded?
# <danbri> ah alias ZUMMO is the last person, then there is a bunch of groups 
# listed. could trim manually.

# TODO: 
# - fix documentation to be perldoc friendly
# - load mailrc.txt from cpan if not held locally
# - derrive collaboration graph as foaf:knows for http://www.foafnaut.org/ visualisation

use Digest::SHA1 qw(sha1_hex);
use LWP::Simple;

my $local=`zcat ~/.cpan/sources/authors/01mailrc.txt.gz`;

#todo:
#my $remote=get('ftp://ftp.perl.org/pub/CPAN/authors/01mailrc.txt.gz');
#print $remote;


#my $sha1sum = sha1_hex($data);

my $out=''; # add headers
foreach my $person (split(/\n/,$local)) {
 
# alias RSOD       "Richard Soderberg <perl-pause@crystalflame.net>
  chomp $person;
  $person =~ m/alias (\w+)\s*"(.*)<(.*)>"/i;
  #  print "ID: $1 name: $2 email: $3 \n\n";
  my $name=$2;
  my $cpanid=$1;
  my $cpanmail='mailto:' . lc($cpanid);
  my $mbox=$3;
  next unless $cpanid && $mbox;
  my $cpansha=sha1_hex($cpanmail);
  my $mainsha=sha1_hex($mbox);
  $out .= "<foaf:Person>\n";
  $out .= "<foaf:mbox_sha1sum>$cpansha</foaf:mbox_sha1sum>\n";
  $out .= "<foaf:mbox_sha1sum>$mainsha</foaf:mbox_sha1sum>\n";
  $out .= "<foaf:interest rdf:resource=\"http://www.perl.org/\"/>\n";
  $out .= "<foaf:interest rdf:resource=\"http://www.cpan.org/\"/>\n";
  $out .= "<dc:contributor rdf:resource=\"http://www.cpan.org/\"/>\n";
  $out .= "<dc:contributor rdf:resource=\"http://search.cpan.org/author/$cpanid/\"/>";
  $out .= "</foaf:Person>\n\n";
  # TODO: get homepage from CPAN site. Other stuff too? Link to specific packages?
}

my $top='<rdf:RDF xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" xmlns:foaf="http://xmlns.com/foaf/0.1/" xmlns="http://xmlns.com/foaf/0.1/">';
print $top . "\n\n". $out . "</rdf:RDF>\n";


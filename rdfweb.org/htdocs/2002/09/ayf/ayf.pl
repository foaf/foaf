#!/usr/bin/env perl
#
# ayf.pl (all your foaf...)
# $Id: ayf.pl,v 1.2 2002-09-28 17:27:19 danbri Exp $ by danbri@rdfweb.org
#
# A quick perl script that harvests and traverses RDF FOAF files
# I blame Bob for my calling this 'all your foaf'.
#
# Bugs and issues (where to begin!):
#
#  * please note: this a nasty hack, to see how far grep and hyperlinks get us
#  * i18n support is likely dire
#  * parses RDF as text, not as XML/RDF; namespace ignorant, etc etc.
#  * some seeAlso files don't point at RDF in any form - rdfs spec feedback?
#  * foaf:depiction is being used liberally (svg, mpeg etc...): kinda nuisance
#  * uses horrible heuristics to omit SVG, MPEG depictions
#  * doesn't respect robots.txt (for shame!)
#  * doesn't (unsuprise suprise) seem to harvest most of the foaf web,
#    presumably as others use different syntax variants that the regexes miss
#
#   Usage: 
#   ./ayf.pl some-url
#   ...will generate HTML page listing images in $outfile

use LWP::Simple;

my $start = shift || 'http://rdfweb.org/people/danbri/rdfweb/danbri-foaf.rdf';
my %seen, %seealso, $out, %seenpic;
$seealso{$start}=1;
my $debug=1;
my $outfile="_allyourfoaf.html";

while (1) {
  my @left=();
  foreach my $k (sort keys %seealso) {
    push(@left, $k) if (!$seen{$k}) 
  }
  if (scalar @left==0) {
    print "FOAF harvester complete: no more links to explore. exiting...\n" if $debug; 
    exit 0;
  }
   
  print "Unseen todo list: ". join('; ',@left) ."\n\n" if $debug;
  my $todo = pop @left;
  print "Fetching uri: $todo ... \n" if $debug;
  $page = get $todo;
  $seen{$todo}++; 
  


  ### hack-parse out the seeAlso pointers  ###############
  #
  #  yes, this is a horrible idiom. We should use an XML parser 
  #  at least, and really an RDF/XML parser. Ho hum... :)
  #
  while($page =~ s!rdfs:seeAlso\srdf:resource="([^"]+)"\s*/>!gotlink($1,$todo)!e) {};

  sub gotlink { 
    my $more=shift;  my $p=shift;
    print "Seealso: $more FROM $p\n" if $debug;
    if (!$seen{$more}) {
      $seealso{$more}++;
    }
    return '';
  }




  ##########################
  # look for pictures :)
  
  while($page =~ s!depiction\s+rdf:resource="([^"]+)"\s*/>!gotpic($1,$todo)!e) {};
  while($page =~ s!img\s+rdf:resource="([^"]+)"\s*/>!gotpic($1,$todo)!e) {};

  sub gotpic{
    my $pic = shift; 
    my $u = shift;

    return '' if $pic =~ m/mpg/i; 
    return '' if $pic =~ m/svg/i; # nasty; but inline SVG doesn't work 
                                  # and some depictions are SVG. @@todo
   				  # clarify FOAF schema.
			
    if (!$seenpic{$pic}) {
      $out .= "<img src='$pic'   width='128' height='128' /> \n\n " ;
      $out .= "<!-- $pic from $u ; not using ALT as some imgs 404 badly -->\n\n";
    }
    $seenpic{$pic}++;
    return '';
  }

  ##########################
  


  #### Update the world on our findings... 
 

  open (OUT, ">$outfile" ) || die "Can't write outfile: $outfile  ";
  print OUT "<html><head><title>all your foaf depictions...</title></head>\n<body>\n";
  print OUT $out;
  print OUT "</body></html>\n\n";
  
  print "(re)writing output: $outfile\n\n" if $debug;
  close OUT;

  ##########################

} # endless loop




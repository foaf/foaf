#!/bin/perl

# this is obsolete, but I want to rewrite it (in ruby...)
# danbri@rdfweb.org

# RDF MOO

package RDF::RDFWeb::TestMoo;

use lib '../';
use RDF::RDFWeb::Node;
use RDF::API::Database;
use RDF::RDFdb; # needed? should be hidden...
use RDF::RDFWeb::XRDFDataSource;
use RDF::RDFWeb::MooSW;
use strict;

my %cmd;  # commandline args from --arg-name=value-passed
while ($_ = shift()) {
  next unless ($_ =~ s/^--//);
  my($lhs,$rhs)=split(/=/,$_,2);
  $lhs=lc($lhs);
  chomp $rhs;
  $cmd{$lhs}=$rhs if ($rhs);
}
foreach (keys %cmd) { print "arg: $_ value $cmd{$_} \n";}

my $module=$cmd{'module'};
my $datadir=$cmd{'datadir'} || './tmp/';
my @mods=split(/,/,$module);
my $data;

## get database
eval {  
      $data = new RDF::RDFdb (undef, $datadir.'default') ; # rw?
      $data->open( 'rw'=>'1' ); # open database
};    
if ($@) {    
  print STDERR "Failed to open rdfdb default in $datadir \n\t".join(' > ',$@)."\n";
} else {
  print "Got default db: $data\n"; 
}

## database bug workaround @@todo: Bug: should be no need for this.
  $data->index();
  $data->close();
  $data->open();

## Declare and register namespaces (and their nicknames)
##
my $FOAF 	= ns $data 'FOAF', 'http://xmlns.com/foaf/0.1/';
my $DC 		= ns $data 'DC', 'http://purl.org/dc/elements/1.1/';
my $RSS 	= ns $data 'RSS', 'http://purl.org/rss/1.0/';
my $WOT 	= ns $data 'WOT', 'http://xmlns.com/wot/0.1/';
my $RDF 	= ns $data 'RDF', 'http://www.w3.org/1999/02/22-rdf-syntax-ns#';
my $SWIPE 	= ns $data 'SWIPE', '$http://rdfweb.org/2001/01/swipe-ns#';
my $SY 		= ns $data 'SWIPE', 'http://purl.org/rss/1.0/modules/syndication/';
my $FRBR 	= ns $data 'FRBR', 'http://ilrt.org/discovery/2001/04/frbr-lite#';
my $VCARD 	= ns $data 'VCARD', 'http://www.w3.org/TR/vcard-rdf#';
my $WN 		= ns $data 'WN', 'http://xmlns.com/wordnet/1.6/';
my $XYZ 	= ns $data 'XYZ', 'http://example.com/xyz#';
my $MOO 	= ns $data 'MOO', 'http://xmlns.com/2001/04/moosw#';


#########################################################################
# 
#  MOO-SemanticWeb Prototype
#
###########################################################################

# initialise variables
my $input;
my $loc='001'; # an rdf literal, oid of current location
my $oldloc; # where we were before
my $here; # an rdf node (plus database)

my @dirs = ('north:n','south:s','east:e','west:w','open:o',
		'up:u','down:d'); # hardcoded basic directions. ne/sw etc?

my $BLUE='[0m[01;34m'; # for color terminals
my $GREEN='[01;32m';     # should detect terminal capabilities
my $OFF='[0m';  

moosw();




###########################################################################
# 
# moo semantic web prototype
#
sub moosw {

  # Startup message
  print "MOO-SW: You are in a maze of crappy prototypes, all alike. Exit: ctrl-d\n\n";
  eval { $here = location( $loc ); }; # not needed each time
  if ($@) { print "$BLUE MOO-SW ERROR initialising location: $@ $OFF\n"; } 

  print "The first thing you see: \n";

  look($here);

  # toy moo command interpreter
  # 
  # Main loop: get input, display input, process move, display status
  
  while ( $input=<STDIN> ) {

    # echo user input
    chomp $input;
    next unless $input;
    if (!$input) { print look($here); next; } 

    print "[location: $loc ]: $BLUE $input $OFF \n";

    # here we show the workings of the parser (which currently doesn't...)
    print "Command parser got: ". RDF::RDFWeb::MooSW::parseline($input)."\n\n";
    # then we ignore the parser and blunder on hackily

    if ($input =~ /^help/) { 
      print "Help: use n,e,s,w, ne,se,sw,nw, open, up, down to move.\n";
      next;
    } elsif ($input =~ /^look/ ) {
      print "You look around...\n";
      look($here);
      next;
    } elsif ($input =~ /^[nsew]/i ) {
      print "I assume by '$input' you are trying to move.\n";
      print move();
      look($here);
        next;
    } elsif ($input =~ /^do (\w+) on (\w+)/i ) {
      print "you try to '$1' the '$2'\n";
      print "you do not see a '$2' here! (honest... ;-)\n";

	## todo: we need rules for finding out what stuff is 'here'
	## then getting shortnames for each so we can match $2 and 
	## see if there is a verb on it or a parent class
	
      
      look($here);
        next;
    } else {
      print "Command '$input' not understood. Try again.\n";
    }
  }#end while
} # end moosw()

 
###########################################################################
   
sub move ($) {
    my $move = shift;

    # if the user is trying to move...
    foreach my $d (@dirs) {
    my ($ln,$sn)=split(/:/,$d,2); # short and long name for possible moves

    if ($input =~ m/^$sn$/ or $input =~ m/^(go )*$ln$/ ) {
      print "You try to head $ln ($sn) .\n";

      my @out = $here->moo__locexit;
      foreach my $exit (@out) {

        my $x = new RDF::RDFWeb::Node($exit, $data);
        print "Considering exit: $x ".join(" ; ", $x->moo__direction )."\n";
        foreach my $outname ($x->moo__direction) {

        if ( $outname =~ m/$ln/ ) {
          print "Leaving:  $outname // $ln via ".$x->moo_name."...\n";
          print $x->moo_name ,"\n : ", $x->moo_player_msg, "\n";
          $oldloc = $loc;
          $loc = $x->moo_to; 
          print "Location is now: $loc\n";
        } # got a genuine direction
        }

      }#end each exit
    }
   }#end dirs

    print "Finalising move.\n";   
    eval { $here = location( $loc ); }; # not needed each time
    if ($@) { print "$BLUE MOO-SW ERROR: $@ $OFF\n"; } # fell off world?
}

############# end movement code


###########################################################################
sub look($) {
  my $here = shift;
  print "You look around. You can see: $GREEN", 
		$here->moo_name, "\n", 
		$here->moo_description, " $OFF\n";

  my @stuff = moo__contents $here;     
  print "Items in this location include: \n";
  foreach (@stuff) {
    my $item = new RDF::RDFWeb::Node( $_, $here->{'CONTEXT'}  );
    print sprintf " %s  (#%s) \n", moo_name $item, moo_oid $item;
  }
  my $player = new RDF::RDFWeb::Node('http://danbri.org/',$data);
  $data->assert($MOO.'name','http://danbri.org/','DanBri');
  runverb($here, $player, 'look','around' );# todo xxx
}




###########################################################################
# MOO location. given a location oid, '"foo"' return an RDF Node
# (or throw an exception... we assume database knows about it.
# do we need a default, 'limbo' location instead?)
#
sub location($) {
  my $loc = shift;
  my $newhere =  new RDF::RDFWeb::Node( $data->GetSource("\"$loc\"",
$MOO.'oid'),$data);
  return $newhere; # throw exceptions here? or when calling this sub?
}

sub runverb {
  my $this = shift();
  my $player = shift();

  my $coderef = $this->{'view'};

  my $RDFS='http://www.w3.org/2000/01/rdf-schema#';
  my $data = $this->{'CONTEXT'};
  my @classes = rdf_type $this ; # get its direct type(s)

  my @verbs;
  foreach my $c (@classes) {
    my $class = new RDF::RDFWeb::Node ($c,$data);
    print "Got class: $class ";
    my $verb = new RDF::RDFWeb::Node ( moo_verb $class, $data );
   print "Got verb val: ", moo_verb $class ;
   print "Got verb: $verb \n";
    my $verbscript = $verb->moo_rdfscript;
#   print "Verb rdfscript : [[ $verbscript ]]\nEval'd:\n";
    eval {
      $verbscript =~ s/^"//;
      $verbscript =~ s/"$//;
      eval ($verbscript); # should we eval line by line? ugh.
      if ($@) { print "moo verb scripting error: ", join(' ; ',$@),"\n\n"; }
    };
    if ($@) { print "Verb handling error: ", join(' ; ',$@),"\n\n"; }
  }
  return $_;
}  

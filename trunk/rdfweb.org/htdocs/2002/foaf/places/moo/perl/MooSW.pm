#!/bin/perl 

# danbri
# moo parser 0.01
# parse a line of input into phrase/units, for w/space quotes group 
# "foo bar" fee fi fo
# is 4 

use strict;
package RDF::RDFWeb::MooSW;

#!/bin/perl

#`this', `any', or `none'. 
#The preposition specifier is `none', `any':

my @phrases;

my %preplist=(
	'p1' => 'with/using',
	'p2' => 'at/to',
	'p3' => 'in front of', 
	'p4' => 'in/inside/into',
	'p5' => 'on top of/on/onto/upon', 
	'p6' => 'out of/from inside/from',
	'p7' => 'over', 
	'p8' => 'through', 
	'p9' => 'under/underneath/beneath',
	'p10' => 'behind',
	'p11' => 'beside',
	'p12' => 'for/about',
	'p13' => 'is',
	'p14' => 'as', 
	'p15' => 'off/off of'
	);

my %term2prep; # rearrange data structure
foreach my $p (keys %preplist){
  my @t = split(/\//,$preplist{$p}) ;
  foreach my $t(@t) {
    $term2prep{"$t"} = $p ;
  #hmm:  print "Storing $t in $p \n";
    push (@phrases, $t);
  }
}

# print "\n\nprep phrases: \n";

# foreach my $phrase (@phrases) {
#   print "phrase: $phrase\n";
# }


  my $test = 'foo bar "fi foop" fing'; # example input
  my $in;
  print parseline($test);
  1;
 

sub parseInput {
  my $in;
  while ($in = <STDIN>) {
    chomp $in;
    print "Parsing $in : " . parseline($in);
  }
}


  sub parseline($) {
    my $in = shift;
    my @phrases;

    $in =~ s/the//g;
    $in =~ s/an//g;
    @phrases = @{ mooparse($in) } ; 
 
    # initialise verblist
    my %verbs = (
    'look' => 'player',
    'hit' => 'multi',
    'put' => 'multi',
    'examine' => 'multi',
    'inv' => 'player',
    'n' => 'location',
    's' => 'location',
    'e' => 'location',
    'w' => 'location',
    'go' => 'location',
    'play' => 'mullti',
    'listen' => 'player'
    );

    print "Parts: ". join(" ; ",@phrases) ."\n\n";

    my $verb = shift @phrases;
    print "Verb: $verb \n";
    # print "Verblist: ".%verbs;
    if ($verbs{$verb} =~ 'self') {
      print "calling \$player->$verb( ". join (' , ',@phrases ). " ) \n";
    }

    if ($verbs{$verb} eq 'location') {
     print "calling \$player->moo_location()->$verb( ". join (' , ',@phrases ). " ) \n";
    }

    if ($verbs{$verb} eq 'multi') {
      print "calling \$???->$verb( ". join (' , ',@phrases ). " ) \n";
    }

    my $subject = shift @phrases;
    print "Subject: $subject \n";
    my $rem = shift @phrases;
    print "remainder: $rem \n";
    my $rem2 = shift @phrases;
    print "remainder2: $rem2 \n";
  }


# notes: 

## now we have a phrase list, match one of...
# - verb 		(eg: 'look','open')
# - verb noun 	(eg: 'examine music', 'play track4')
# - verb noun subordinate-thingy (eg: 'throw money down')  ???
# - vern noun subordinate-thingy sub-thingy-2 ('put food in microwave')
# complex case:
# load "music player" with "dan's mp3 archive"


#############################################################################
###	 
###	 simple phrase parser
###	 
#############################################################################
# 
sub mooparse($) {
my $in = shift;
my (@phrases, $tmp, @parts, $chunk, $quoted);

@parts = split (/\s+/,$in);
# print "Got input: $in\n";
# print "part array: ", join (" ; ",@parts),"\n\n";
foreach $chunk (@parts) {
  chomp $chunk;
#  print "Got: $chunk \n";
  if ($quoted) {
#    print "In a quote.\n";
    if ($chunk =~ s/"$//) {
#      print "Closing quote in '$chunk' \n";
      if ($tmp) { $tmp .= " $chunk";} else { $tmp = $chunk; }
      push (@phrases, $tmp); # flush tmp list to phrase list
      $tmp="";
      $quoted=0; # closing quote 
     } else {
#       print "In quote; no close tag in '$chunk' \n";
       $tmp .= " $chunk";
     }
   } else {
#     print "Not in a quote.\n";

    if ($chunk =~ s/^"//) {
#      print "start of quote found. \n";
      $quoted=1;
       $tmp .= " $chunk";
    } else {
#      print "No start quote found. \n";
      push (@phrases, $chunk);
    }
  }
}

return \@phrases;
}


#!/bin/perl

my $raw = `cat base.txt`;

my @parts = ('help','top','who','dev');

my %html;
$html{'dev'} = '<b>developers</b>';

foreach my $section (@parts) {

#print STDERR "doing section '$section' html is: ".frag($section)." ... \n";
$raw =~ s/%%MAIN%%/'xxx'.frag($section).'yyy'/ge;

sub frag {
my $b=shift;
print "lookup $b\n";
return $html{$b};
}

open (OUT,">$section.txt" || die);
print OUT $raw;
close OUT;

}

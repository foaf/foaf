#!/usr/bin/perl 
#
# generate index.html from index.html.orig 
# using these two colours (in-page highlights and top-page navbar)
# ./color.pl ffcc66 cccccc
#
# http://www.visibone.com/colorlab/big.html
# http://rogers.fiendsreunited.com/people/danbri/remote-cvs/fireball.danbri.org/rdfweb.org/htdocs/
# http://www.stone.com/java/cc/ColorCoordinator.html

my $orig = 'index.html.orig';
my $page = `cat $orig`;

#<!-- fffacd yellow, 000000, black -->

my $c=shift || die 'no new colour';

$page =~ s/#fffacd/#$c/gi;

my $b = shift || 'cccccc';

$page =~ s/#cccccc/#$b/gi;

open(OUT,">index.html") || die "can't write file $_";
print OUT $page;
close OUT;

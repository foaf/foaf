#!/usr/bin/perl 
#
# http://www.visibone.com/colorlab/big.html
# http://rogers.fiendsreunited.com/people/danbri/remote-cvs/fireball.danbri.org/rdfweb.org/htdocs/

my $orig = 'index.html.orig';
my $page = `cat $orig`;

#<!-- fffacd yellow, 000000, black -->

my $c=shift || die 'no new colour';

$page =~ s/#fffacd/#$c/gi;

my $b = shift || '000000';

$page =~ s/#000000/#$b/gi;

open(OUT,">index.html") || die "can't write file $_";
print OUT $page;
close OUT;

#!/bin/perl
#
#
# $Id: examplepath.pl,v 1.1 2002-02-09 18:42:41 danbri Exp $
#

my $eg='1';
my $dir = 'eg'.$eg;
my @todo = `cd $dir; ls p*.jpg | sort -n`;

foreach my $img (@todo) {
  chomp $img;
  my $new = "sm-".$img;
  my $cmd = "cd $dir; convert -geometry 100 '$img' '$new'";
  print "<img src=\"$dir/$img\" alt=\"codepiction image $img\" height=\"100\" />\n\n";
 `$cmd`;
}

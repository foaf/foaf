#!/usr/bin/perl

my $raw = `cat index.html`;

my $GET = 1;

use LWP::Simple;

my $height=100;

$raw =~ s/\n/ /g;
$x = $raw;

my $i;
my $d=1;
while ($x =~ s#<li><a href="([^"]+)">([^<]+)</a></li>##) {
  my $url=$1;
  my $type = 'jpg';
  my $fn;
  my @imgdata;

  if ($GET) {
    @imgdata = get($url);
    $type = 'gif' if ($url =~ m/gif$/i);
    $type = 'png' if ($url =~ m/png$/i);
    $fn =  "img_"  . $d . '.' . $type;

    print STDERR "Writing $fn for $url\n";
    open(OUT, ">$fn") || die "Can't write $fn" ;
    foreach (@imgdata) { print OUT $_; }
    close OUT;
  }

  print STDERR "Running: convert -geom 100x $fn sm_$fn \n";
  print `convert -geometry 100x $fn sm_$fn`;
  $i .= "<img src=\"sm_$fn\" alt=\"$2\" height=\"$height\" />\n";

  $d++;
}

print "<p>$i</p>";

#!/usr/bin/perl

my $raw = `cat index.html`;

my $height=100;

$raw =~ s/\n/ /g;
$x = $raw;
my $i;
while ($x =~ s#<li><a href="([^"]+)">([^<]+)</a></li>##) {
  $i .= "<img src=\"$1\" alt=\"$2\" height=\"$height\" />\n";
}

print "<p>$i</p>";

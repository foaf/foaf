#!/usr/bin/perl -w
        
use CPANPLUS::Backend;
my $cp = new CPANPLUS::Backend;

#my $module_obj  = $cp->module_tree()->{'Dir::Purge'};
my $all_authors = $cp->author_tree();
my $all=$all_authors->{'JV'}->modules();

foreach my $w( keys %{$all_authors}) {
    my $a= $$all_authors{$w};
    print "Name: $a->{'name'} mbox: mailto:$a->{'email'} id: $a->{'cpanid'}\n";
    my %mods = %{$cp->modules(authors => [$a->{'cpanid'}])->{'rv'}};
    print "mods: $mods\n"; 
	
    foreach $m (keys %mods) {
	print "Module: $m\n";
    }
#print "authors: ". $all_authors;
}


#my $mods_by_same_auth = $cp->modules(authors => ['RSOD']);
#print "mods: ".$mods_by_same_auth;


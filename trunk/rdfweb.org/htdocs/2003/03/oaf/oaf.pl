#!/usr/bin/perl -w

# Wikibot 0.21, by Earle Martin.
# ...forked and f****d with by Dan Brickley
# 
# original:
# http://downlode.org/perl/wikibot/
#
# This is free software with no warranty of any kind whatsoever.
# This program is released under the same terms as Perl itself.


# OAF fork (danbri trying to add POE support (and learn POE (and learn IRC)))
#
# this version has Net::IRC removed, and POE code starting to appear
# note that when run, it runs a POE app which copies text between irc 
# channels.  this stems from my having copied the example code in
# http://www.funkplanet.com/POE/cnnbot.pl -- the idea is to write a bit
# of glue code that calls the functions which Net::IRC would, were it 
# still in the loop. Which it isn't. Hope that's clear :)
#
# Feel free to refork. I may not do any more on the code.  
#
# Notes:
#  - the old app copies text from one channel to another, expecting '>>' in format.
#    ...see regex for details. Ah, a full stop terminates multiline content.
#    so we can test easily enough. Finish a single line with >>.
#  - normalise indenting
#  - if it works, my tests should scrawl into http://rdfweb.org/rweb/wiki/wiki?ScratchPad
#
# cvs version: $Id: oaf.pl,v 1.6 2003-03-30 00:33:09 danbri Exp $
# cvsweb: http://rdfweb.org/viewcvs/viewcvs.cgi/rdfweb.org/htdocs/2003/03/oaf/
# Recent changes:
# $Log: not supported by cvs2svn $
# Revision 1.5  2003/03/30 00:27:47  danbri
# tidying mynick code
#
# Revision 1.4  2003/03/30 00:24:46  danbri
# We now extract userid
# this isn't the nick, or is it? Confused, I should check the docs.
# Anyway it gets the bit at the start of userid!~foo@ip.addr.here from the
# $who variable. Seems OK for now.
#
# Revision 1.3  2003/03/29 23:26:42  danbri
# It basically works. Need to track nick of who made comment.
#
# Revision 1.2  2003/03/29 23:02:49  danbri
# Working on gluing POE events into the Wiki-oriented code.
# Focussing on new on_poe_public function, to replace Net::IRC-based on_public.
#
# We now are at stage where it'll try to write to Wiki. Seems to fail silently,
# maybe my Wiki formatting is not what it expects.
#
# Revision 1.1  2003/03/29 22:19:16  danbri
# First cut at forked POE-happy wikibot
#
# 

use strict;
use POE;
use POE::Component::IRC;
use LWP::UserAgent;
use HTTP::Request::Common;
use HTML::TokeParser;


use constant MAX_LINE_LENGTH => 250;

my $ua = LWP::UserAgent->new;
my $version = "0.21-OAF-Forkv0.0";
$ua->agent('Wikibot/$version');

my $listen = {
    nick => 'oafbot',
    chan => '#foaf',
    feed => 'FOAF',
    server => 'irc.freenode.net',
};


my $speak = {
    nick => 'foafwiki',
    chan => '#oaf',
    server => 'irc.freenode.net',
};

my $buffer = '';
my %conn;

# =============================================================================
# configuration section 


# danbri TODO: this is for the old Net::IRC code
# the POE app had its own cfg details, need to converge
# these are not currently used

my $mynick = "foafwiki";					# bot's nick
my $owner = "danbri";					# bot's owner's nick

my $server = "irc.freenode.net";				# IRC  server to use
my $port = "6667";					# port to use on server
	
my $ircname = "foafwiki";					# real name for  bot
my $username = "foafwiki";					# username for bot
my $quitmsg = "We get signal!";				# quit message
my $channel = "#foaf";				# channel to join

# URL of the wiki script
my $baseurl = "http://rdfweb.org/rweb/wiki/wiki?";
# type of wiki in use: current valid options are "usemod" and "moinmoin"
my $wikitype = "usemod";
my $wikipage = "ScratchPad";				# page to edit on wiki

# danbri added:
$baseurl =~ s/\?$// if ($wikitype eq "usemod"); # normalise to no trailing '?' 


# format strings for text to be added to wiki page - outputs the following
# so you can apply different styles to the text for the nick and comment:
#
# $formatstart nickname $formatmid comment $formatend
#
# you can also disable the display of nicks by setting $shownicks to 0;
# note that $formatstart will not then be used either

my $shownicks = "1";

my $formatstart = "<strong>[";
my $formatmid = "]</strong> ";
my $formatend = "";

# =============================================================================

# whole lotta variables

my ($self,$newedit,$raw,$tag,$title,$oldtime,$oldconflict,$text,
    $cgifields,$datestamp,$savetext,$request,$response);

# startup info

print "Wikibot version $version.\n";
print "Using wiki at URL: $baseurl\n";
print "Wiki type specified: $wikitype\n\n";

my $conn;


# danbri TODO: replace these with POE event stuff

#$conn->add_handler('msg',    \&on_msg);
#$conn->add_handler('public', \&on_public);
#$conn->add_global_handler([ 251,252,253,254,302,255 ], \&on_init);
#$conn->add_global_handler('disconnect', \&on_disconnect);
#$conn->add_global_handler(376, \&on_connect);
#$conn->add_global_handler(433, \&on_nick_taken);
pogo();



############   functions from wikibot original (based on Net::IRC)

sub on_connect {
	$self = shift;

	print "Joining $channel.\n";
	$self->join("$channel");
}

sub on_disconnect {
	# reconnect if disconnected
#	$conn = $irc->newconn(Nick   	=> $mynick,
#			Server  	=> $server,
#			Port    	=> $port,
#			Ircname 	=> $ircname,
#			Username	=> $username);
# danbri TODO: this was Net::IRC code, how to do this via POE?
}

sub on_init {
	my ($self, $event) = @_;
	my (@args) = ($event->args);
	shift (@args);
    
	print "*** @args\n";
}

sub on_msg {
	my ($self, $event) = @_;
	my $nick = $event->nick;
	my ($arg) = ($event->args);
	my $fullurl;

	# help text

	if ($arg eq "help") {
		$self->privmsg($nick, "Hello, I am Wikibot version $version.");
		$self->privmsg($nick, "I append text to a wiki which is located at:");	

		# get correct URL to send

		if ($wikitype eq "moinmoin") {
			$fullurl = $baseurl . "/" . $wikipage;
		}
		elsif ($wikitype eq "usemod") {
			$baseurl =~ s/\?$//; # normalise to no trailing '?'
			print STDERR "[oaf] Writing to baseurl:  $baseurl\n";
			$fullurl = $baseurl . "?" . $wikipage;
			$fullurl =~ s/ /_/;
		}

		$self->privmsg($nick, $fullurl);
		$self->privmsg($nick, "To append something, simply address me with some text.");
	}
}

sub on_public {
	my ($self, $event) = @_;
	my @to = $event->to;
	my ($nick, $mynick) = ($event->nick, $self->nick);
	my ($arg) = ($event->args);

	if (($nick eq $owner) && ($arg =~ /^$mynick, leave/i)) {
		print "Quitting.\n";
		$self->quit("$quitmsg");
		exit 0;
	}

	# pick up text addressed to bot, apply format strings
	# pass it over to the writing routine
	# bot can be addressed "bot, " or "bot: "

	elsif (($arg =~ /^$mynick, /i) || ($arg =~/^$mynick: /i)) {
		my $comment = $arg;
		$comment =~ s/$mynick, //;
		$comment =~ s/$mynick: //;

		# list new addition in STDOUT
		print $nick . " added \"" . $comment . "\"\n";

		if ($shownicks == "1") {
			# apply formatting strings
			$comment = $formatstart . $nick . $formatmid . $comment . $formatend;
			&write($comment);
		}
		
		else {
			# apply formatting strings, leaving out nick
			$comment = $formatmid . $comment . $formatend;
			&write($comment);
		}
	}

}


# this is on_public, to be modified to be called from the POE code 
# danbri TODO: xxx
# 
sub on_poe_public {

	# already extracted from POE wierdness:
	my ($msg, $sender, $who, $chan)=@_;
	# [on_poe_public]: msg=werwer . 
	# sender=POE::Session=ARRAY(0x85838e0) 
	# who=danb_lap!
	#~danbri@pc-80-192-52-217-az.blueyonder.co.uk 
	# chan=ARRAY(0x8587ba0)

	# print STDERR "[on_poe_public]: msg=$msg sender=$sender who=$who chan=$chan\n\n"; # xxx
	my ($self, $event) = @_;
	my @to=('oaf'); # danbri TODO: this is wrong! FIXME

        $who =~ s/(.*)!.*/$1/;
        my ($nick, $mynick)=($who,'oafbot'); # FIXME: don't hardcode mynick
        my $arg=$msg;
        print "[oaf] from nick:'$nick'  considering '$arg'\n\n"; 
	if (($nick eq $owner) && ($arg =~ /^$mynick, leave/i)) {
		print "Quitting.\n";
		$self->quit("$quitmsg");
		exit 0;
	}

	# pick up text addressed to bot, apply format strings
	# pass it over to the writing routine
	# bot can be addressed "bot, " or "bot: "

	elsif (($arg =~ /^$mynick, /i) || ($arg =~/^$mynick: /i)) {
		my $comment = $arg;
		$comment =~ s/$mynick, //;
		$comment =~ s/$mynick: //;

		# list new addition in STDOUT
		print "[oaf]" . $nick . " added \"" . $comment . "\"\n";

		if ($shownicks == "1") {
			# apply formatting strings
			$comment = $formatstart . $nick . $formatmid . $comment . $formatend;
			&write($comment);
		}
		
		else {
			# apply formatting strings, leaving out nick
			$comment = $formatmid . $comment . $formatend;
			&write($comment);
		}
	}

}


sub write {
	$newedit = shift;
	my $editurl;
 
        print STDERR "[oaf] DEBUG! writing $newedit \n\n";

	# construct url for editing specified wiki page,
	# depending on type of wiki software being run

	if ($wikitype eq "usemod") {
		$wikipage =~ s/ /_/g;			
		$editurl = $baseurl . "?action=edit&id=" . $wikipage;
	}
	
	elsif ($wikitype eq "moinmoin") {
		$editurl = $baseurl . "/" . $wikipage . "?action=edit";
	}

	else {
		&error_unknown_wiki();
	}

	$request = HTTP::Request->new('GET', $editurl);
	$response = $ua->request($request); 
	if ($response->is_success) {

		$raw = $response->content;

		# scrape the necessary values from the wiki edit page
		
		if ($wikitype eq "usemod") { 
			&getfields_usemod();
		}

		elsif ($wikitype eq "moinmoin") {
			&getfields_moinmoin();
		}

		else {
			&error_unknown_wiki();
		}

		# post form data back to the wiki with the new text

		if ($wikitype eq "usemod") {
			&postdata_usemod();
		}

		elsif ($wikitype eq "moinmoin") {
			&postdata_moinmoin();
		}

		else {
			&error_unknown_wiki();
		}

	}
 
	else {
		print "Error: $!";
	}
}

sub getfields_moinmoin {

	# parse MoinMoin-style edit page HTML and get necessary fields

	my $stream = HTML::TokeParser->new( \$raw ) or die $!;
	while ( $tag = $stream->get_tag("form") ) {

		# skip "action" input
		$tag = $stream->get_tag('input'); 
		
		$tag = $stream->get_tag('input');
		$datestamp = $tag->[1]{value} || "--";

		$tag = $stream->get_tag('textarea');
		$savetext = $stream->get_text('/textarea');
	}
}

sub getfields_usemod {

	# parse UseMod-style edit page HTML and get necessary fields

	my $stream = HTML::TokeParser->new( \$raw ) or die $!;
	while ( $tag = $stream->get_tag("form") ) {

		$tag = $stream->get_tag('input'); 
		$title = $tag->[1]{value} || "--";
		$tag = $stream->get_tag('input');
		$oldtime = $tag->[1]{value} || "--";

		$tag = $stream->get_tag('input');
		$oldconflict = $tag->[1]{value} || "0";

		$tag = $stream->get_tag('textarea');
		$text = $stream->get_text('/textarea');

		# skip past some unwanted <input>s
		$tag = $stream->get_tag('input');
		$tag = $stream->get_tag('input');
		$tag = $stream->get_tag('input');
		$tag = $stream->get_tag('input');

		$tag = $stream->get_tag('input');
		$cgifields = $tag->[1]{value} || "--";
	}
}

# data posting subroutines for different wiki types

sub postdata_moinmoin {
	$savetext = $savetext . "\n" . $newedit;

	# MoinMoin does URLs differently
	my $newurl = $baseurl . "/" . $wikipage;

	print $newurl . "\n";

	$response = $ua->request(POST $newurl, [
					action => "savepage",
					datestamp => $datestamp,
					savetext => $savetext
				]);

}

sub postdata_usemod {
	$text = $text . "\n" . $newedit;

	$response = $ua->request(POST $baseurl, [
					title => $title, 
					oldtime => $oldtime,
					oldconflict => $oldconflict,
					text => $text
				]);

}

sub error_unknown_wiki {
	print "Error: unrecognised wiki type in configuration.\n";
	$self->privmsg($channel, "Sorry, I haven't been configured properly. Please check the 'wikitype' setting.");
}




############################ POE STUFF ################################
#
# should wire these events to handlers above
#
# all the new stuff is here....


sub _start {
  my ($kernel) = $_[KERNEL];

  foreach ($listen, $speak) {
    $conn{ $kernel->alias_resolve( $_ ) } = $_;
    $kernel->post( $_, 'register', 'all');
    print "[$_->{nick}] Connecting to $_->{server}...\n";
    $kernel->post( $_, 'connect', { Debug    => 0,
				    Nick     => $_->{nick},
				    Server   => $_->{server},
				    Port     => $ARGV[0] || 6667,
				    Username => 'neenio',
				    Ircname  => "http://funkplanet.com/POE/cnnbot.pl", }
		  );
  }
}

sub irc_001 {
  my ($kernel, $sender) = @_[KERNEL, SENDER];

  print "[$conn{$sender}->{nick}] Connected.\n";
  $kernel->post( $sender, 'away', 'Note: I am just a bot.' );
  $kernel->post( $sender, 'join', $conn{$sender}->{chan} );
}

sub irc_disconnected {
  my ($sender, $server) = @_[SENDER, ARG0];
  die "[$conn{$sender}->{nick}] Lost connection to server $conn{$sender}->{server}.\n";
}

sub irc_error {
  my ($sender, $err) = @_[SENDER, ARG0];
  die "[$conn{$sender}->{nick}] Server error occurred! $err\n";
}

sub irc_socketerr {
  my ($sender, $err) = @_[SENDER, ARG0];
  die "[$conn{$sender}->{nick}] Couldn't connect to server: $err\n";
}

sub _stop {
  my ($kernel) = $_[KERNEL];

  foreach ($listen, $speak) {
    $kernel->call( $_, 'quit', 'cnnbot: http://funkplanet.com/POE/cnnbot.pl' );
  }
  print "Control session stopped.\n";
}

sub irc_public {
  my ($kernel, $sender, $who, $chan, $msg) = @_[KERNEL, SENDER, ARG0 .. ARG2];

  # danbri's olde style debugin'
  print STDERR "[STDERR!] irc_public got msg: $msg \n\n";

  #  leftover logic from cnn app. keeping for reference only
  #  return unless exists $conn{$sender}->{feed} and
  #                $chan->[0] eq $conn{$sender}->{chan} and
  #                $who =~ /^$conn{$sender}->{feed}!/i;
  # something to do with the CNN app, multi line content etc.
 
  # danbri TODO: 
  # we got a msg event from listener, time to call Wiki code somehow
  # hmm this is where POE-ese gets turned into Net::IRC-ese. Hopefully.
  # zzz
  on_poe_public($msg, $sender, $who, $chan);


  while ($msg =~ /^(.*?)\s*>>\s*(.*)$/) {
    $buffer .= $1;
    $msg = $2;
    flush_buffer();
  }

  $buffer .= $msg . " ";
  flush_buffer() if length $buffer > MAX_LINE_LENGTH or $msg =~ /[\.\?\!]\s*$/;
}

# we copy text across to other channel
sub flush_buffer {
  my $session = $poe_kernel->alias_resolve( $speak );
  $poe_kernel->call( $session, 'privmsg', $speak->{chan}, $buffer );
  $buffer = "";
}

sub irc_433 {
  my ($kernel, $sender) = @_[KERNEL, SENDER];

  warn "$conn{$sender}->{nick} couldn't get its nickname!\n";
  $conn{$sender}->{nick} = '\\' . $conn{$sender}->{nick};
  $kernel->post( $sender, 'nick', $conn{$sender}->{nick} );
}


# Hook it up and run...
# 
sub pogo {
  foreach ($listen, $speak) {
    POE::Component::IRC->new( $_ ) or die "Can't create component  \"$_\"!\n";
  }
  POE::Session->new( 'main' => [qw(_start _stop irc_001 irc_433 irc_disconnected
                                 irc_socketerr irc_error irc_public)] );
  $poe_kernel->run();
  exit 0;
}

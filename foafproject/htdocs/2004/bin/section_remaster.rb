#!/usr/bin/ruby

require '../2004/bin/section_util'

# author: Dan Brickley <danbri@w3.org>
#
# Script reconciles a normal page against a master template
#
# log:
# $Log: not supported by cvs2svn $
# Revision 1.1  2004/08/14 20:38:13  danbri
# working on remaster script now
#

file = './web/index.html'
master_file = './master/web_section_master.html'

master = DocFile.new master_file
doc = DocFile.new file
doc.defers_to = master
doc.remasters = ['SecondaryNavigation','MainNavigation']
	
master.scan
doc.scan

puts doc.refresh_from_master!

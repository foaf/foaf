#!/usr/bin/ruby

require './section_util'

# navtest1.rb
#
# This script shows use of section_util.rb library
# Specifically we read in HTML content, and find the marked sections
# TODO: 
#
#   - read these also from a master template
#   - rewrite chosen subsections using content from a master file
#   - figure out how to test our functionality
#   - test UTF-8 issues and line endings (\n vs \r)
#   - test roundtripping with diff
#
# author: Dan Brickley <danbri@w3.org>
# version: $Id: navtest1.rb,v 1.2 2004-08-14 20:05:04 danbri Exp $
# log:
# $Log: not supported by cvs2svn $
# Revision 1.1  2004/08/14 19:45:07  danbri
# coding
#
#
# 

# just a quick example script; we assume we're run in htdocs/2004/bin/

section_names = Hash.new

todo = `find ../../new/ -name \*.html` # or whatever gets us a list of files

todo.each do |file|
  file.chomp!
  doc = DocFile.new(file)  
  puts "Scanning file: #{file} title: #{doc.title} "
  doc.scan
  doc.sections.each do |name,content| 
    section_names[name] = true
    #  puts "Found a section called: '#{name}' "
  end

  secnav = doc.sections['SecondaryNavigation']
  if secnav 
#    puts "2ndary nav section is: "+secnav # uncomment for text spew
#    puts "\n\n"
  else  
#    puts "No 2ndary navigation section in marked comments."
  end
end

puts "All section names found: "
section_names.each_key do |name| 
  puts "named page section: #{name}"
end

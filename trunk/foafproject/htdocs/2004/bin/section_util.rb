#!/usr/bin/ruby
# 
# section_util.rb - hypertext templating utilities
# author: Dan Brickley <danbri@w3.org>
# status: Work in progress. Parsing works; no rewriting yet. 
# see also: navtest1
#
# version: $Id: section_util.rb,v 1.2 2004-08-14 20:38:13 danbri Exp $
# Log:
#   $Log: not supported by cvs2svn $
#   Revision 1.1  2004/08/14 19:45:07  danbri
#   coding
#
#
# Utility to parse files on FOAF site 
# It reads an HTML page from disk, 
# parses the commented sections into "sections" hashtable
#
# Basic idea is that bits of your HTML doc are marked up with 
# surrounding comments, such as:
#
# 	<!-- InstanceBeginEditable name="HelpText" -->
# 	<p>help yourself</p> 
# 	<!-- InstanceEndEditable -->
#
#  ...so each page implicit has a set of name/content pairs, 
#  where the name is something like "HelpText" or "CopyrightFooter" 
#  and the content is a chunk of (hopefully UTF-8) markup text.
#
#  The intent is that these utilities could allow for some of these 
#  bits of markup to be refreshed / rewritten based on the content of 
#  a "master" template stored elsewhere in the filetree. 
#
# IMPLEMENTATION:
# 
# The class DocFile represents an HTML file in the local tree.
#
# mydoc = DocFile.new('../files/mypage.html') # initialize
#
# redone = mydoc.scan  # parse and reserialize to text
#
# puts mydoc.sections['SecondaryNavigation'] # examine named bits
#
# 
# TODO:
# things to lookup in manual
# 1. 'find' functionality
# 2. 'load file to string'
# line 18 column 76 - Warning: nested emphasis <span>
# 

class DocFile

  def initialize(fn)
    if fn 
      @text = `cat #{fn}`
    else
      @text=''
    end
    @restrung='' # for result of parsing
  end

  attr_accessor :text, :restrung, :sections

  def title
    t=''
    text =~ /<title>([^<]*)<\/title/ # quick parse of title
    t1=$1
    return t1
  end

  def scan
    t1=''
    textcopy = text.clone.gsub(/\n/," ")

    require 'html/htmltokenizer'
    @sections = Hash.new
    @restrung=''
 
    page=textcopy
    tokenizer = HTMLTokenizer.new(page)

    current_section=''
    current_section_name=''

    while token = tokenizer.getNextToken
    # puts "\n# Token type is: "+token.class().to_s;

      # Check for HTML comments, including hidden Dreamweaver-style begin and end markers:
      #
      if token.class.to_s=="HTMLComment"
#        puts "\nBEGINCOMMENT #{token.to_s} ENDCOMMENT\n\n"
        mycomment = token.to_s.clone  # We got a comment...

        # <!-- InstanceBeginEditable name="SecondaryNavigation" -->
        if mycomment =~ /InstanceBeginEditable/
#          puts "# Found beginning of section...'"

          current_section = '' # reset accumulated content

          mycomment =~ /name="([^"]*)"/
          if $1 
#            puts "# named section: '#{$1}'"
            current_section_name=$1
          else
#            puts "# anonymous section."
          end
        # <!-- InstanceEndEditable -->
        elsif mycomment =~ /InstanceEndEditable/
#          puts "# Closed section. Storing content under sectionID"
          sections[current_section_name]=current_section

 
        # Add recognition of other Dreamweaver MX tags here?

        # elsif mycomment =~ / .... /

        # elsif mycomment =~ / .... /

        else 
#          puts "# Normal HTML comment? ie. unrecognised."
        end


        # pass thru the comment tagging
        out=token.to_s
        out.gsub!(/\r/,"\n")
        @restrung += out

        # end comment processing

      else
        out=token.to_s
        current_section += out # other tokens get appended to current accumulation
        out.gsub!(/\r/,"\n")
        @restrung += out
      end
    end # done HTML parsing

    return @restrung    

  end


  def section_debug
    txt=''
    txt += "############# DUMPING MARKED SECTIONS:\n\n" 
    sections.each do  |a,b| txt += "#{a} --->>>> #{b.gsub(/\r/," ")} \n\n" end
    txt += "############# END MARKED SECTION DUMP.\n\n\n" 
   return txt
  end

end # Class definition

###############################################################################

#todo = `find . -name \*.html`

#todo.each do |file|
 # file.chomp!
 # doc = DocFile.new(file)  
 # STDERR.puts "Scanning file: #{file} title: #{doc.title} "
 # mangled=doc.scan
#  print mangled
 # print doc.section_debug
#end



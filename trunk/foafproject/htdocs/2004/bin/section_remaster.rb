#!/usr/bin/ruby

require '../2004/bin/section_util'

# author: Dan Brickley <danbri@w3.org>
#
# Script reconciles a normal page against a master template
#
# log:
# $Log: not supported by cvs2svn $

#
#
# 

file = './web/index.html'
master_file = './master/web_section_master.html'

to_restore = ['SecondaryNavigation','MainNavigation']

doc = DocFile.new(file)  
puts "Scanning file: #{file} title: #{doc.title} "

master = DocFile.new(master_file)
puts "Scanning file: #{master_file} title: #{master.title} "

doc.scan
master.scan

doc.sections.each do |name,content| 
    puts "Found a section called: '#{name}' "
      puts "...which is also in the master" if master.sections[name]
      if to_restore.member?(name)
        puts "...restore requested!" 
        if doc.sections[name].eql?(master.sections[name])
          puts "...but they're the same already; nothing to be done."
        else
          puts "They're different!: "
          puts " compare: doc=#{doc.sections[name]} master=#{master.sections[name]}"
        end
      end
end



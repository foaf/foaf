#!/usr/bin/env ruby


data = `cat _corplinks.dat`



data.each do |link|
  link.chomp!
  link.gsub!(" ", "_") 
  fields = link.split(/\t+/)
  from = fields[5]
  to = fields[6]
  who = "#{fields[1]}_#{fields[2]}"
  who.chomp!
  puts "#{from} #{to} #{who}\n"

end


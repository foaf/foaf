#!/usr/bin/env ruby


require 'sha1'

uri = 'http://www.omcl.org/~spcoltri/ruby/ruby-sha1.html'

sh = SHA1::new(uri)

bytes = sh.digest()

puts "Hexdigest: "+ sh.hexdigest()

## http://www.linuxgazette.com/issue32/rogers.html
## try %02x %02d etc
## %02d won't change anything; decimal int.
ret = ''
bytes.each_byte {|i| ret << sprintf("%02d", i) }
puts "Output: "+ ret


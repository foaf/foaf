#!/usr/bin/env ruby

require 'rbconfig'
require 'find'
require 'ftools'
require 'getoptlong'

include Config

$destdir = ARGV.shift
$srcdir = CONFIG["srcdir"]
$version = CONFIG["MAJOR"]+"."+CONFIG["MINOR"]
$libdir = File.join(CONFIG["libdir"], "ruby", $version)
$archdir = File.join($libdir, CONFIG["arch"])
$site_libdir = CONFIG["sitedir"]
if !$site_libdir
  $site_libdir = $:.find {|x| x =~ /site_ruby$/}#/
end
if !$site_libdir
  $site_libdir = File.join($libdir, "site_ruby")
end

def install_rb(libdir = "lib", files = nil)
  path = []
  dir = []
  if files
    path = files
    dir |= [libdir]
  else
    Find.find(libdir) do |f|
      next if (f = f[libdir.length+1..-1]) == nil
      path.push f if File.ftype(File.join(libdir, f)) == 'file'
      dir |= [File.dirname(f)]
    end
  end
  for f in dir
    if f == "."
      File::makedirs(File.join($destdir, $libdir))
    else
      File::makedirs(File.join($destdir, $libdir, f))
    end
  end
  for f in path
    File::install(File.join(libdir, f), File.join($destdir, $libdir, f), nil, true)
  end
end

# run tests here?

install_rb("./lib", ["scutter.rb","pathfinder.rb","basicrdf.rb","squish.rb"])

puts "Ruby install.rb: INSTALLING INTO:"+ Config::CONFIG["rubylibdir"]
puts "For Scutter: see http://rdfweb.org/foaf/"
puts "Supporting library (RubyRDF) see http://www.w3.org/2001/12/rubyrdf/intro.html"

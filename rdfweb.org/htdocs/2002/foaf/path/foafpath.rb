#!/usr/bin/env ruby

# foafpath (cargo cult edition) 
#
# This is a Ruby transliteration of a collection of Java classes 
# originally by Damian Steer and Libby Miller.
# see http://rdfweb.org/people/damian/2002/02/foafnation/
#
# $Id: foafpath.rb,v 1.2 2002-07-10 21:31:12 danbri Exp $ danbri@rdfweb.org
#
# 
# classes: TElement, TEdge, TNode, TGraph
#
# todo: translate BuildPath.java machinery for storing paths in RDBMS
#   -- we don't yet store the paths in an SQL db, just dump the graph to rdf
#   -- maybe we could use a DBM or BerkeleyDB to store paths? SQL seems overkill


#################################################################
######################### TElement

# This class represents a simple tree element. It is only used for
# the 'shortest paths' routine as a simple way to store paths in a
# (hopefully) efficient manner.

class TElement 
  attr_accessor :parent, :node, :edge
  def initialize (node, parent, edge = nil) 
    @node = node
    @parent = parent
    @edge = edge
  end
  def to_s
    return @node.to_s
  end
end


###########################################################
######################### TEdge

class TEdge

    attr_accessor :node1, :node2, :labels, :hash

    def initialize(node1, node2, label)
	@node1 = node1
	@node2 = node2
	@labels = []
	@labels.push(label)
	@node1.addEdge self
	@node2.addEdge self
    end

    def to_s
    	return @labels.to_s
    end

    def addLabel (label)
	@labels.push(label)
    end

    def  getLabel()
	if (@labels.size() == 1)
		return @labels[0].to_s
	else
		i = 0
		#todo: (Math.random() * (double) labels.size()).to_int
		return  @labels[i]
	end
    end

    def getOtherNode(node)    
#     return @node.equals(node1)?node2:node1 ## what's  Ruby idiom for this? verbosely:
      if node.equals( @node1 )
        return @node2
      else
        return @node1
      end

    end 
end


###########################################################
######################### TNode

class TNode

  attr_accessor :edges, :text, :name, :hash, :visited
  # todo: strip out java-esque accessors

  def initialize(text='nothing')
    @text = text
    @edges = []
    @name="noone"
    @hash = 0 #int
    @visited= false #boolean
  end

  def setName name
    @name=name
  end

  def getName
    return @name
  end

  def setVisited visited
    @visited = visited
  end

  def notVisited
    return !@visited #boolean
  end

  def addEdge edge
    @edges.push edge
  end

  def getEdges
    return @edges # []
  end

  def getText 
    return @text
  end
    
  def to_s
    return @text
  end    

  def setText(text)
    @text=text
  end

  def sameNode (obj)
    ## this was .equals() in java...
    ## todo:Should ret false unless obj is a TNode
    ## hmm, is this how we do string eq testing?
    if obj.to_s =~ self.to_s # todo: inefficient?
      # puts "node compared equal "+self.to_s
      return true
    else 
      # puts "node '"+obj.to_s+"' compared not equal "+self.to_s
      return false
    end

  end 


  # Note: we add 'from' here to reduce the cloning of Vectors
  # ret void; sends Vector, Vector, tEdge, int
  def getPaths (paths, pathToMe, from, ttl)
    if (pathToMe.contains(self)) 
      return
    end

    ttl = ttl-1 # ?
    if (ttl == 0) 
      return # Kill it off if the path is too long
    end

    pathIncludingMe = pathToMe.copy #or clone() per java orig?
	
    # if this is the start node 'from' will be nil
    if (from != nil) 
      pathIncludingMe.push(from)
    end
    pathIncludingMe.addElement(self) 	# new path with me in it
    paths.add(pathIncludingMe) 		# add it to the other paths

    edges.each do |e| 
      edge = e.pop
      if (edge == from) 
        continue 
      end

      otherEnd = edge.getOtherNode(self);

      # add paths for edges
      otherEnd.getPaths(paths, pathIncludingMe, edge, ttl);
      end
  end
    
  def pathsFromWithTTL(ttl)
    # takes vector, returns int
    paths = []
    pathToMe = []
    self.getPaths(paths, pathToMe, nil, ttl)
	
    # We aren't interested in the first path found
    # (it is just this node)
    paths.removeElementAt(0) # todo! Odd, this hasn't caused a problem yet. junk?
    return paths
  end
end

###########################################################
######################### TGraph

require 'basicrdf'

class TGraph
  attr_accessor :edges, :nodes, :nodeLookup, :edgeLookup, :complex

  def TGraph.main(args={})
    @complex=true # verbose RDF output
    all=""

    graph = TGraph.new
    graph.buildGraph(graph)

     puts "# of nodes: " + graph.getNodes.size.to_s
     puts "# of edges: " + graph.getEdges.size.to_s
     puts "Nodelookup table: " 
     graph.nodeLookup.each_key do |k| 
       puts "node: #{k} "
     end 	 

     puts "\n\n\n"

     danpath = graph.findShortestPathsFromNodeNamed 'mailto:danbri@w3.org'
     puts "\n\nDanpaths: " 
     danpath.each do |e|
       puts "Path:\n"
       while (e.parent != nil) 
         puts "\tpath element: "+e.to_s
         e = e.parent
       end
     end
     puts "\n\n\n"


     # see BuildPath for how we might store this graph in SQL
     # for fast retrieval

    topPerson = nil
    avLen = 0

    all += ("<?xml version=\"1.0\" ?>\n")
    all += ("<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n")
    all += (" xmlns:foaf=\"http://xmlns.com/foaf/0.1/\">\n")
    graph.edges.each do |edge|
      node1 = edge.node1
      node2 = edge.node2
      if(!@complex)
                  all += ("<!-- simple description of a codepiction --> \n")
 		  all += ("<rdf:Description rdf:about=\""+node1+"\">\n")
		  all += ("<foaf:codepiction rdf:resource=\""+node2+"\" />\n")
		  all += ("</rdf:Description>\n")
      else
                  all += ("<!-- complex description of a codepiction --> \n")
  		  all += ("<rdf:Description rdf:about=\""+node1.getText()+"\">\n")
		  all += ("<foaf:depiction rdf:resource=\""+edge.getLabel()+"\" />\n")
		  all += ("</rdf:Description>\n")
		  all += ("<rdf:Description rdf:about=\""+node2.getText()+"\">\n")
		  all += ("<foaf:depiction rdf:resource=\""+edge.getLabel()+"\" />\n")
		  all += ("</rdf:Description>\n\n\n")
      end
    end
    all += ("</rdf:RDF>")
    puts all;
  end

  def initialize 
    @edges = []
    @nodes = []
    @nodeLookup = {}
    @edgeLookup = {}
    @complex = true
  end

  def addPath( node1, node2, edgeLabel)
    addPathWithName(node1,node2,edgeLabel, nil, nil)
  end

 def addPathWithName(node1, node2, edgeLabel, name1, name2)
   node1.downcase!
   node2.downcase!

   if (node1 == node2) ## string names for nodes, how to test string eq?
     return
   end

    edgeObj = @edgeLookup[node1 + node2] #todo: + ???
    if (edgeObj == nil) 
      edgeObj = @edgeLookup[node2 + node1]
    end

     nodeObj1 = @nodeLookup[node1]
     nodeObj2 = @nodeLookup[node2]

     if (nodeObj1 == nil)
       nodeObj1 = TNode.new node1
       if(name1!=nil)
   	 nodeObj1.setName name1
       end
       @nodes.push(nodeObj1)
       @nodeLookup[node1]= nodeObj1
     end

     if (nodeObj2 == nil)
       nodeObj2 = TNode.new node2
       if(name2!=nil)
         nodeObj2.setName(name2)
       end
       @nodes.push(nodeObj2)
       @nodeLookup[node2] = nodeObj2
     end

     if (edgeObj == nil) 
       edgeObj = TEdge.new(nodeObj1, nodeObj2, edgeLabel)
       @edges.push(edgeObj)
       @edgeLookup[node1 + node2]= edgeObj
     else
       edgeObj.addLabel(edgeLabel)
       end
    end

  def to_s
    string = "[";
    @edges.each do |edge| 
      node1 = edge.node1
      node2 = edge.node2
      string += "["+node1+","+edge+","+node2+"]"
    end
    string += "]"
    return string
  end

  def getEdges
    return @edges
  end

  def getNodes
    return @nodes
  end

# 
#  This method finds the shortest paths between a node in the graph
#  and all other (reachable) nodes. It returns a Vector of tree
#  elements. To find each path start with each element of the
#  vector, find its parent, and repeat until parent == nil. That
#  gives you all the shortest paths. Easy, huh? And hopefully fast

  def findShortestPathsFromNode(root)
  # Clear graph for search
    @nodes.each do |node|
      node.setVisited false
    end

    # But (of course) we start with a node, so let's say it's been visited

    root.setVisited true

    # Set up the root of the tree
    treeRoot = TElement.new(root, nil)
    # Set up parents vector (just treeRoot) and leaves (empty)
    parents = []
    parents.push treeRoot
    leaves = []
    # indicates whether to keep going
    continuing = true
    # todo: this is going to look rather different in Ruby! double-check w/ original java
    while (continuing)
      children = []
      continuing = false
      parents.each do |parent| 
        parentNode = parent.node
        puts "Doing parent: #{parentNode} " 
        parentNode.getEdges.each do |edge|
          puts "Doing edge(s): #{edge} " 
          childNode = edge.getOtherNode parentNode
          if childNode.notVisited
 	    childNode.setVisited true 
            puts "visited: #{childNode}"
            child = TElement.new (childNode, parent, edge)
            leaves.push child
            children.push child
            continuing = true
          end
	  parents = children
	end
      end
    end
    #puts "returning: #{leaves}"
    return leaves 
  end

  def findShortestPathsFromNodeNamed (name)
    node = @nodeLookup[name]
    if node == nil
      puts ("No node named '" + name + "'") ## todo: STDERR!
      return nil
    end
    return (findShortestPathsFromNode node)
  end

  def paths (leaves)
    pathString = ""
    leaves.each do |leaf|
      while (leaf.parent != nil)
        pathString += leaf.node + " , "
	leaf = leaf.parent
   	pathString += leaf.node + "\n"
      end
    end 
    return pathString
  end

  def pathLength leaf
    length = 1;
    while (leaf.parent != nil)
      leaf = leaf.parent
      length = length+1
    end
    return length
  end

  def averagePathLength (paths)
    length = 0
    paths.each do |p|
      length += pathLength(p)
    end
    return (length / paths.size) # floats: todo, to_float?
  end

  def buildGraph (graph)	
    require 'squish' 
    require 'dbi'
    query =<<EOQ;

	SELECT ?mbox1, ?mbox2, ?uri,  
	WHERE
       (foaf::depiction ?x ?img)
       (foaf::depiction ?z ?img)
       (foaf::thumbnail ?img ?uri)
       (foaf::mbox ?x ?mbox1)
       (foaf::mbox ?z ?mbox2)
	USING foaf for http://xmlns.com/foaf/0.1/

EOQ

    q = SquishQuery.new.parseFromText query
    DBI.connect ('DBI:Pg:test1','danbri','') do | dbh |
      dbh.select_all( q.toSQLQuery  ) do | row |
        puts row.inspect
        p = ResultRow.new(row)
	graph.addPath(p.mbox1, p.mbox2, p.uri)
      end 
    end
  end
end # /class TGraph



###########################################################



puts "Running pathfinder..."

TGraph.main 

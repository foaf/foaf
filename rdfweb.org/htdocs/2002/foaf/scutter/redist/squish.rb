#!/usr/bin/env ruby
#
# Ruby-RDF 
# RDF Query support
# $Id: squish.rb,v 1.1 2002-07-17 11:45:11 danbri Exp $

## NOTE: squish.rb bundled with scutter, but 
## is an external package (and W3C software), and will ultimately require a 
## separate install. danbri@W3.org
## 


=begin

This is a basic library to support RDF query applications in Ruby.

It complements (but doesn't yet depend on) Ruby-RDF. 

It provides a SquishQuery class, representing simple "SQL-ish" queries 
against RDF datasets. A goal is to allow for multiple textual
representations of the same logical query. The toAlgae() method, for
example, shows how a Squish query can be exported in Algae syntax.

The class also provides a toSQLQuery() method which will generate an SQL 
query suitable for use against a PostgreSQL database that stores RDF
in a certain way. The Ruby-RDF's Graph.toSQL() method provides a
corresponding mechanism for turning RDF graphs into such databases.

acknowledgements:
The SQL functionality is a pretty literal translation of 
Libby Miller's original Java version, which in turn was heavily
inspired by Matt Biddulph's PHP implementation. It is currently one
large method; I might reorganise it once the tests are in place. The
Squish parser too is pretty rough, but worked well enough to get the
SQL code working. Squish is based on Guha's original RDF query
language (see RDFdb), but has a cuter name and a few new features
(USING clauses, for example). The tests will have to replace
documentation, for now. The hashcode function was written by Damian
Steer, based on the Stanford RDF API SHA1 code.

Note that this code is pretty undocumented and messy in its first
release, but it WorksForMe. There are some test files in samples/. 
Investigate the runtests.rb script (or the last few lines of
squish.rb) to see how it can be used.

Dan Brickley <danbri@w3.org> 


 useful refs:
  http://www.io.com/~jimm/downloads/rubytalk/talk.html
  http://www.daml.org/listarchive/joint-committee/0856.html
  http://www.hpl.hp.com/semweb/rdql.html
  http://www.hpl.hp.com/semweb/rdql-grammar.html


Random Notes: a fresh query object always needed (since state isn't
blanked down). So we see a lot of this sort of thing: query = SquishQuery.new()


=end




#
# A class representing a simple, SQL-ish RDF query
#

=begin

SquishQuery highlights:

	    query = SquishQuery.new()
	    query.parseFromText(squish_text) 

	    puts "Parsed and reprinted: "+query.toSquish
	    puts "Variables: "+ query.all_vars.keys
	    puts "Export as Algae: "+ query.toAlgae
	    puts "Squish2SQL: "+ query.toSQLQuery

=end
  
class SquishQuery


  # incl parsed query state
  attr_accessor :select_args, :from_args, :using_args, :where_args,  :clauses, :xmlns, :full_clauses, :all_vars

#, :select_arglist

  ## Hmm, don't understand why needed a method for an array field
  ## todo: read docs on attr_accessor
  def sels
    return @select_arglist
  end 
 
  def initialize   
    @clauses  = []
    @full_clauses = []
    @xmlns = {}
    @loginfo = []
    @verboselog = []
    @all_vars = {}

    # todo: find out where/how one declares fields of a class!
    # seems to be no point in doing so, since it's all so runtimy
    # but I want to document them somewhere. Maybe just via attr_accessor?

    # @select_args
     @from_args=""
    # @using_args
    # @where_args
    # state used in WHILE productions
    #  @lastpred
    #  @lastsub
    #  @lastobj
end


  def loginfo (production, content, nextdata)
    logmsg = "LOGINFO: ['#{production}'] got content: '#{content}'\n"
#    @loginfo.push(logmsg)
#    @verboselog.push  logmsg + "    remains: #{nextdata}\n\n"
  end


  def logwarning (msg)
    @warnings.push(msg)
    puts "PARSER WARNING: #{msg}\n"
  end 


  def parseFromText (text)
    text.gsub!(/\n/," ")
    text.gsub!(/\r/," ")
   

    parse = select_keyword(text)
    extractAllVars
    expandAllNamespaces
    return self
  end 

  # select_keyword:
  # entry point
  #
  # -> select_arglist
  #
  def select_keyword (text)
    if text =~ /^\s*SELECT\s+(.*)/is
      nextdata = $1
      # puts "Found select keyword. Next is: #{nextdata}\n"
      loginfo ( 'select_keyword', 'SELECT', nextdata)
      return select_arglist (nextdata)
    end
    puts "Error: Expected first chars to be 'SELECT ' GOT: '#{text}'"
  end  



  ################
  # select_arglist:
  #
  def select_arglist (text)
    ###
    #
    # The SELECT arg list can be terminated with an (optional) FROM clause
    if text =~ /^\s*(.*?)\s*(FROM\s+.*)/si
      content = $1
#     content.gsub!(/\s*$/, "") ## todo: shouldn't have to strip ws here

      nextdata = $2
      @select_args = content 

      loginfo ('select_arglist (found FROM) ', content, nextdata)

      rawsel = content
      rawsel.gsub!("\\?","")
      rawsel.gsub!(/,\s*$/,"") 
      vars = rawsel.split(/,\s*/) # ws mandatory?
#      puts ("SELS-with-from: #{vars}\n")
      @select_arglist = vars;

      return from_keyword(nextdata) # move to from_keyword production

    ### 
    #
    # Else it should be terminated with a WHERE clause
    elsif text =~ /^\s*(.*)\s+WHERE\s+(.*)/is 
      # puts "select_arglist: no from, got WHERE!\n"
      content = $1
      nextdata = $2
      @select_args = content


      loginfo ('select_arglist (omitted FROM) ', content, nextdata)
      
      rawsel = content			# Should be separate function
      rawsel.gsub!("\\?","")
      vars = rawsel.split(/,\s*/)
      #puts ("SELS-without-from: #{vars.inspect}\n")
      @select_arglist = vars;

      if where_lpar (nextdata)
      #puts "Processed all where clauses OK, nothing left to do."# todo
      end
      # this is a *mess*! 
      #      if @remainder =~ /\S/
      #        return using_keyword ( nextdata )
      #      end
      return true # Done!
    end
    # neither ended in FROM or WHERE
    puts "Error: Expected arg list for SELECT to end with FROM or WHERE. GOT: #{text}"
  end  



  # the optional FROM keyword
  #  -> where_lpar (todo: to where_keyword)
  #
  def from_keyword (text)
    if text =~ /\s*FROM\s+(.*?)(\s+WHERE\s+.*)/i
      content = $1
      nextdata = $2
      loginfo ( 'from_keyword, next is where_lpar', content, nextdata)
      @from_args = content
      nextdata.gsub!(/WHERE\s+/i,"") ## TODO: make a node for WHERE_KEYWORD
      return where_lpar(nextdata)
    end
    puts "Expecting (optional) FROM; didn't find it."
    return false
  end


  # using_keyword:
  # final clause (optional), deals with namespace expansions
  #
  def using_keyword (text)
    # puts "Using-keyword got: #{text} \n"
    if text =~ /\s*USING+(.*)/i
      nextdata = $1
      loginfo ( 'using_keyword', 'USING', nextdata)

      # puts "using_keyword: #{nextdata} \n"
      return using_arglist(nextdata)
    end
    puts "Expecting 'USING' keyword, found: #{text} "
    return false
  end 
  

  def using_arglist(text)
    if text =~ /\s*(.*)$/
      content = $1

      loginfo ( 'using_arglist', content, '[end]' )

      @using_args = content
      usedPrefixes(content) # extract
      #puts "using_args: #{content} \n"
      return true
    end
    puts "Expected arguments to USING, found nothing.\n"
    return false
  end

  #### the 'WHERE' clause

  # where_lpar: '('
  # where clause, left paren
  # 
  # -> deal with each pred_expr or drop out of WHERE via using_keyword
  #
  def where_lpar (text)
    if text =~ /^\s*\(\s*(.*)/  
      nextdata = $1
      loginfo ( 'where_lpar', ' ( ', nextdata )
      return pred_expr( nextdata )
    end
    if text =~ /^\s*USING/i
      return using_keyword(text)
    end
    raise " squish parser error. Expected: '(' or 'USING' Got: #{text}" 
    # note: test1-bogusclause.squish tries 'ABUSING' clause 
    @remainder=text
    return false;
  end

  # pred_expr
  # where clause, predicate expression
  # -> sub_expr
  #
  def pred_expr (text) 
    if (text =~ /\s*(\S+)\s+(.*)/)
      content = $1
      @lastpred = content
      nextdata = $2
      loginfo ( 'pred_lpar', content, nextdata )

      return sub_expr(nextdata)
    end
    puts "Error: pred_expr didn't find expected content \n"
    return false
  end


  # sub_expr
  # where clause, subject expression
  # -> obj_expr
  #
  def sub_expr (text)
    if (text =~ /\s*(\S+)\s+(.*)\s*/)
      content = $1
      @lastsub = content
      nextdata = $2
      loginfo ( 'sub_expr', content, nextdata )
      return obj_expr(nextdata)
    end
    puts "Error: sub_expr didn't find expected content in '#{text}'\n"
  end


 # obj_expr
  # where clause, object expression
  # -> where_rpar
  #
  def obj_expr (text)
    if (text =~ /(\S+)\s*(\).*)/)
      content = $1
      # puts("object: "+content+"\n")
      @lastobj=content 
      nextdata = $2
      loginfo ( 'obj_expr', content, nextdata )
      return where_rpar(nextdata)
    end
    puts "Error: obj_expr didn't find expected content in '#{text} \n"
  end






  # where_rpar: 
  # where clause, right paren
  # -> where_lpar
  #
  def where_rpar (text)
    # puts "Does text '#{text} match ')'... \n"
    if text =~ /^\)\s*(.*)\s*/  
      nextdata = $1
  #    puts "rpar: next prod: where_lpar #{$1} \n"
      loginfo ( 'where_rpar', ' ) ', nextdata )

      if (nextdata =~ /\S/ ) 

        clause = "#{@lastsub} -- #{@lastpred} --> #{@lastobj}\n"
        qt = [ @lastpred, @lastsub, @lastobj ]
        self.clauses.push( qt ) 
 
        ## puts ("DEBUG: #{clauses.inspect} \n")
	## TODO: Store this in SquishQuery
        return where_lpar( nextdata )
      end 
      puts "[finished with the entire WHERE clause]\n\n"
         return true # we're done with this where
	          # should flush temporary state, store goodstuff etc
		  # Note: qnames need expanding later
    end
    puts "Error: Expected ')'"
    return false;
  end


# output requested variables as contents of a Squish SELECT clause
#
# eg: "?x, ?y, ?z"
# 
# todo: refactor to hide the * case from apps that want a clean list
# of vars, ie. sharecode w/ algae function. self.sels is bad data.
#
def toVarQList
  s=[]
  if self.sels[0] =~ /^\*/ 
    return "*" # special case for 'SELECT *' (ie. select all named vars)
  end
  self.sels.each {|q| s.push("?#{q}") } 
  return s.join(", ")
end

def toSquish 
  sq = "SELECT #{toVarQList} \n"
  if from_args =~ /\S/
    sq += "FROM #{from_args} \n"
  end
  sq += "WHERE \n"
  cl= clauses.each { |qt|
    sq += " ( #{qt[0]} #{qt[1]} #{qt[2]} ) \n" #todo: commas? whitespace?
  } 
  sq += "USING #{using_args} \n"
  return sq
end


# expand namespaces and store in expanded_clauses
#
def expandAllNamespaces
   sq=""
   full_clauses=[] # reset state. wise?
   cl= clauses.each { |qt|
     p = expns(qt[0])
     s = expns(qt[1])
     o = expns(qt[2])
    sq += " ( #{p} #{s} #{o} ) "
    # puts ("Epanding: #{sq} \n")
    self.full_clauses.push([p,s,o] ) 
   }
   return ("Expanded: "+sq)
end

def expns (text)
  # possibly qualified expression, eg dc::foo
  if text =~ /(\w+)::(.*)/
    if ($1 != nil) 
      ns = xmlns[$1]
      if (ns == nil) 
        raise "squish parser error: undeclared namespace #{$1}\n"
      else 
      text = xmlns[$1] + $2
      end
    end
#   puts "Expns: Got #{$1} and #{$2} -> #{text} \n"  
  end
  return text
end


# update self.all_vars based on variables named in self.clauses
#
def extractAllVars
  ## todo: reset state first? same issue re xmlns / USING...
  clauses.each { |qt|
    parts = qt[0..3]
    parts.each { |term|
     if term =~ /^\?(.*)/
       l = self.all_vars[$1] # look for list of clauses using this variable
       if l == nil
         self.all_vars[$1] = [qt]
       else
        self.all_vars[$1].push(qt)
       end
     end
    }
  }
end
  
  

# Output in Algae syntax (URI for spec?)
# todo: look at commas, throw exception for '*' or workaround as below
#       - for this, need an query.allvarnames() method 
#	- warn if there's a FROM clause, or figure out Algae syntax
#       - find out Algae notation for using ns prefixes
def toAlgae 
  a = "(ask '(\n"
  cl= full_clauses.each { |qt|
    a += " ( #{qt[0]} #{qt[1]} #{qt[2]} ) \n"
  }
  vars = toVarQList
  vout=[]
  if vars =~ /\*/
     #    warnings.push "Algae doesn't support * selector. (todo) Default is  collect all vars"
     all_vars.keys.each {|q| vout.push("?#{q}") }  
  else
    vars.each {|q| vout.push("#{q}") } 
  end
  a += " ) collect '( #{vout.join(', ')} )\n)\n" ## lose the commas?
  return a
end


def usedPrefixes (text)
  nslist = text.split(/\s+/)
  state='prefix' #Todo: investigate ruby constants mechanism
  content=""
  nslist.each { |item| 
    if state =~ /prefix/
      content=item
      state='for'
    elsif state =~ /for/
      state = 'uri'
    elsif state =~ /uri/
      self.xmlns[content]=item
      content=""
      state = 'prefix'
    elsif 
      puts "TODO: parse USING clause properly!\n"
    end
  }


  return xmlns
end





################################################################################
##
## Squish2SQL facilities
#

def toSQLQuery (opts={})

  sql=""
  sqlVariableNamesA = []
  sqlVariableNamesB = []
  sqlVariableMatchAB = {}
  realToSqlVarname_A={}
  id_a_clause=1 # counter

  # RDBMS table and field names
  p_field='predicate'
  s_field='subject'
  o_field='object'
  main_table = 'triples'
  lookup_table = 'resources'

  # for storing generated WHERE Clause fragments, two categories:
  where_triples=[]   # triples, ie. "a" (assertions)
  where_lookup=[]	   # libby's "b" or 'resources' table

  clauses.each { |clause|
    p,s,o = clause[0..2]
    p=expns(p)
    s=expns(s)
    o=expns(o)
    p_bound = false
    s_bound = false
    o_bound = false
    p.gsub!(/^\?/,"") # this by reference, changes the contents of clauses 
                      # TODO: this will trip us up. Fix! decide on whether has ? or not
    s.gsub!(/^\?/,"")
    o.gsub!(/^\?/, "")
    #puts("CLAUSE:  #{p} ; #{s} ; #{o} \n")
    all_vars.keys.each { |varname|
      # puts "Scanning for varname: #{varname.inspect} sub=#{s.inspect} id: #{id_a_clause}\n"
      if s.eql? varname
        realToSqlVarname_A[varname] =  "a#{id_a_clause}.#{s_field}"
        s_bound = true # hmm, back to front?
      end
      if p.eql? varname
        realToSqlVarname_A[varname]=  "a#{id_a_clause}.#{p_field}"
        p_bound= true
      end
      if o.eql? varname
        realToSqlVarname_A[varname]= "a#{id_a_clause}.#{o_field}"
        o_bound = true
      end
    }
    sh1_sub = hashcodeIntFromString(s) # wasteful, not always needed (see below)
    sh1_pred = hashcodeIntFromString(p)
    sh1_obj = hashcodeIntFromString(o)

    # puts "\n\nSHA-Triple: s=#{s} (#{sh1_sub}) p=#{p} (#{sh1_pred}) o=#{o} (#{sh1_obj})  \n"
    # puts "Var bindings: #{s_bound} #{p_bound} #{o_bound} \n"

    if !s_bound
      where_triples.push "a#{id_a_clause}.#{s_field} = '#{sh1_sub}'"
      #puts "DEBUG: s = #{sh1_sub} \n"
    end 
    if !p_bound
      where_triples.push "a#{id_a_clause}.#{p_field} = '#{sh1_pred}'"
      #puts "DEBUG: p = #{sh1_pred} \n"
    end 
    if !o_bound
      where_triples.push "a#{id_a_clause}.#{o_field} = '#{sh1_obj}'"
      #puts "DEBUG: o = #{sh1_obj} \n"
    end 

    id_a_clause += 1

  } # end big loop thru clauses

  #puts "VARNAMES: #{realToSqlVarname_A.inspect}\n"
  #puts "Got constraints: \n\n #{where_triples.inspect} \n"

  sqlVarnames=[] # todo: describe this 

  all_vars.keys.each do |variableNameToMatch|
    sqlVariableNamesA = []
    #puts "\nScanning for clauses that use variable: #{variableNameToMatch} \n\n"
    cl_idx=1
    clauses.each do |clause|   
      # puts "Clause: #{clause.inspect} \n"
      p,s,o = clause[0..2]
      p_bound = false
      s_bound = false
      o_bound = false
      if s.eql? variableNameToMatch
        sqlVariableNamesA.push("a#{cl_idx}.#{s_field}")
        #puts "Matched! (subject)\n"
      end
      if p.eql? variableNameToMatch
        sqlVariableNamesA.push("a#{cl_idx}.#{p_field}")
        #puts "Matched! (predicate)\n"
      end
      if o.eql? variableNameToMatch
        sqlVariableNamesA.push("a#{cl_idx}.#{o_field}")
        #puts "Matched! (object) \n"
      end
  
      cl_idx= cl_idx+1
      #++ didn't.
    end

    if sqlVariableNamesA.size > 1 
      sqlVarnames.push(sqlVariableNamesA)
    end
  end

  #puts "Current varname equalities for where_triples constraints: #{sqlVarnames.inspect}\n\n"
  #
  # this writes "a1.subject = a2.predicate" constraints.
  # note that there is (I think) some redundancy in this method. (@@check java code)
  sqlVarnames.each { |bindings|
    j = 0
    bindings.each { |part| 
      if (j+1<bindings.size) 
        where_triples.push " #{part} = #{bindings[j+1]} " 
      end
      j=j+1 # j++ not work
    }
  }

  # selectvars is the list of b variables and the actual variables that match them:
  # e.g b1.value as ?x
  selectvars=[]
  
  clause_lookup_id=1
  realToSqlVarname_A.keys.each { |realkey|
    val = realToSqlVarname_A[realkey]
    # drop '?' prefix (needed? seems not)
    realkey.sub!(/!\?/,"") 
    #puts "building where_lookup constraints: key=#{realkey} val=#{val} \n\n\n"
   if self.sels.include?(realkey)
 
    if opts['quotevars'] 
      selectvars.push "b#{clause_lookup_id}.value AS '#{realkey}'"
    else
      selectvars.push "b#{clause_lookup_id}.value AS #{realkey}"
    end

    sqlVariableNamesB.push("b#{clause_lookup_id}");
    where_lookup.push("b#{clause_lookup_id}.keyhash="+val );
  end
  
  if (sqlVariableMatchAB[val] == nil) 
    tmp=[]
    tmp.push "b#{clause_lookup_id}.value}"
    sqlVariableMatchAB[val]=tmp
  else
    sqlVariableMatchAB[val].push "b#{clause_lookup_id}.value}"
  end  
  
  clause_lookup_id += 1
  }

  sql = "SELECT DISTINCT "+ selectvars.join(", ")+" "
  sql += "FROM "

(id_a_clause-1).times do |i|
  sql += " #{main_table} a#{i+1}, "
end

lookup_tmp=[]
sqlVariableNamesB.each do |v|
  lookup_tmp.push "#{lookup_table} #{v}"
end 
sql += lookup_tmp.join(", ")

sql += "\nWHERE\n\t" + where_lookup.join(" AND ") + " AND "+ where_triples.join(" AND ")

return sql

# notes on how it works:
#
#    SELECT DISTINCT    b2.value AS mbox, b5.value AS thumb, b7.value AS name
# 
##### ^ 'b' is the lookups-prefix (a* was for triples, b* for resource id lookups 
#####  ^ '1' us a where-clause-counter (var may have several numbers) 
#####    ^ 'value' is the field name from lookups that contains content (not sha1'd ints
#####             ^ from query.sels ([1]), the variable name
#####               ....repeated for query.sels.each, picking a
#####               counter number from the clause numbers they appear in
#     FROM
#        triples a1,  triples a2,  triples a3,  triples a4,
#        triples a5,  triples a6,  triples a7,
#        resources b2,  resources b5,  resources b7
#    WHERE 
#         a1.predicate = '116868652'
#   AND   a2.predicate = '116868652'
#     AND a3.predicate = '1547507681'
#     AND    a3.object = '1145937192'
#     AND a4.predicate = '1547507681'
#   AND   a5.predicate = '1577895888'
#   AND a6.predicate = '-1848367484'
#   AND a7.predicate = '-221079518'
#   AND a1.subject=a3.subject
#   AND a1.object=a2.object

end



## Other functions (should move to basicrdf.rb someday)


def hashcodeIntFromString (data)
  require 'sha1'
  sh = SHA1::new(data)
  dig = sh.digest()
  r = (dig[0])|((dig[1]) << 8)|((dig[2]) << 16)|((dig[3]) << 24)
  # restrict to signed 32 bit int (didn't need this in java)
  if  ( r > ( ( 1 << 31 ) -1 ) )
    return ( r - ( 1 << 32 ) )
  end
  return r
end




end # end of SquishQuery class definition


################################################################################
##
## Basic Squish implementation (over RDF graph)

# query a graph using squish (returns ResultSet)
#
def SquishQuery.ask(query,db)
  tables=[]

  query.full_clauses.each do |clause|
    vars={}
    p,s,o=clause[0..2]
    if p.gsub!(/^\?/,"")
      vars['p']=p
      p=nil
    end
    if s.gsub!(/^\?/,"")
      vars['s']=s
      s=nil
    end
    if o.gsub!(/^\?/,"")
      vars['o']=o
      o=nil
    end

    bindings = ResultSet.new  
    a = db.ask(Statement.new(s,p,o)).statements.each do |s| 
      # puts "Vars are: #{vars.values.join ' ' } "
      # todo: store vars
      vb={}
      vb[vars['s']] = s.subject.to_s   if (vars['s'] != nil)  
      vb[vars['p']] = s.predicate.to_s  if (vars['p'] != nil)  
      vb[vars['o']] = s.object.to_s if (vars['o'] != nil)  
      bindings.push (ResultRow.new (vb))
      # puts "vars.values: #{vars.values}\n" #hmm: bindings.vars=vars.values
    end
    tables.push bindings
  end
 
  htmllog = ""
  current = tables.shift # todo: if no matches, this would be returned.
  begin 
    while (!tables.empty?)  
    # puts "Calling chooseTable with current vars: #{current.vars}"
    nx = chooseTable (tables, current.vars) 
      joinvars = current.vars & nx.vars
      htmllog += "<h2>Matching next constraint: vars: #{joinvars.inspect} </h2>"
      htmllog += "<h3>Current matches:</h3>" + current.html_report
      old=current
      new = current.match nx, joinvars
      current = new
      htmllog += "<h3>New matches:</h3>"+current.html_report
    end
  rescue
    if (!tables.empty?) 
      # puts "query failed - unbound tables"
      tables.each do |t| 
        # puts " todo: #{t} tables still to do: #{t.vars.inspect} "
      end
      current = ResultSet.new
    else 
      # puts "Checked all tables."
    end
    # todo: should we check that tables is now empty?
  end
  current.debug_report += htmllog 
  # todo: how to make sure all constraints met? todo...

  answers=ResultSet.new
  answers.debug_report=current.debug_report
  dup={}
  current.each do |row|
    r=''
    row.values.each_key do |col|
      r += col.to_s + row.values[col]
    end
    if (!dup[r.hash])
      dup[r.hash] =1
      answers.push row
     end
  end
  return answers
end

def SquishQuery.chooseTable (tables, vars)
  # puts "Choosing amongst tables to match vars: #{vars} \n"
  maybe=[]
  if (vars!=nil)
    vars.each do |v|
      # puts "trying for a table that knows about '#{v}' "
      tables.each do |t|
        if t.vars.member? v     # puts "Got a match: #{v}"
          maybe.push t 
        end
      end
    end
    else
    # raise "*** warning: chooseTable called with nil vars. (do we care?)"
   end

  # puts "Possibilities: #{maybe.size} \n"
  if maybe.size > 0 
    tables.delete (maybe[0])
    ## puts "Chose table w/ #{maybe[0].vars} "
    return maybe[0]
  else 
    raise "No plausible table"
  end
end

################################################################################
##
## Sample Web service plugins: Google/backlinks
#
# todo: a general API for such things would be nice. This is rather arbitrary.

def SquishQuery.googleBacklinks (uri = ARGV[0], varname='page', nohost=nil) 

  require 'webfetcher'

  r = ResultSet.new
  nohost=nil # TODO: it doesn't work yet, so ignore...
  uri = 'http://www.w3.org/2001/sw/' if (uri == nil)
  uri.gsub!(/http:\/\//,"")
  maxhits=100
  #todo: 
  nohost = "+-site:#{nohost}" if (nohost != nil)
  uri = "#{uri}#{nohost}&num=#{maxhits}"
  pc = WebFetcher::Page.url('http://www.google.com/search?q=link:'+uri).links
  pc.each do |link|
     doc="#{link.to_s}"
     unless (doc =~ /google/i)
	# puts "Storing: #{doc} "
       r.push ResultRow.new( {varname=> doc} )
     end
  end
  return r
end





################################################################################
##
## DBI storage / query facilities
#

# haven't modeled this particularly well.
# 

#require 'dbi'

class ResultSet < Array
attr_accessor :query , :debug_report#, :vars
  def initialize (query=nil)
    @query=query
    @debug_report='<h3>Debugging info...</h3>'
  end

def vars= (v)
  puts "***WARNING: can't set vars() on ResultSet. Ignoring data: #{v} Real vars: #{self.vars} \n"
end


def html_report (report='')
  report += "<table border='1'>\n"
    report += "<tr>\n\n"
    self.vars.each do |v|  
	report += " <th>#{v}</th> " 
    end
    report  += "</tr>\n"
    self.each do |row|
      report += "<tr>\n"
      row.values.each_key do |col|
        report += "<td>#{row.values[col]}</td>\n"  
      end
      report += "</tr>\n"
    end 
    report += "</table>\n\n"
  return report
end


def vars
  if self.size>0
    ## puts "!!! rs.vars: #{self[0].values.keys} "
    return self[0].values.keys
  else 
    ## puts "!!! rs.vars: empty list. "
    return []
  end

end



# match this result set against another, returning a 3rd
# examine their associated queries to determine column to join on
#
# - tests should make sure that calling result2.match(result1) gets same results
  def match(results2=nil, joinvars=nil)
    
    results1=self
    results3 = ResultSet.new()
    
    if (joinvars==nil) 
      joinvars = results2.query.sels & results1.query.sels
    end
    self.debug_report += "\n<p>Graph matcher called with tables:\n</p>"
    results3.debug_report += "<hr />"
    #joinvars.each do |j| puts "matching on common field(s) jv: #{j} " end      

    results2.each do |r2|
      results1.each do |r1| 
        reject=false
        r1.values.each do |col,val|
          next unless joinvars.member? col
          if r2.values[col] == val 
	  #puts "Considering: #{col} -> #{val}"
          else
            # puts "Rejecting!"
            reject=true # a required column doesn't match
            break # we could be more efficient...?
		  # also, if results from graph sorted 
		  # predictably, this might all be much faster    
   	  end
        end

        if (!reject)
          # merge and store
	  # todo: add .toHTML() to resultset and code
	  #       to make sure ResultSets are grids not wierd trees

	  # puts "matched row! Storing #{r1.inspect} \n"
          r3 = r2.clone;
          r1.values.each do |col,val| r3.values[col]=val end
          results3.push r3
	  ## TODO: we currently allow duplicates. This is bad.
	  ## Not clear how to avoid this problem. 
          ## results3.vars = results1.vars & results2.vars 
        end
      end
    end
    return results3
  end # end match
end



class ResultRow
  attr_accessor :values
  def method_missing (methid)
    str = methid.id2name
    return values[str] 
  end
  def initialize (row)
    @values=row # a hash
    #puts "NEW ROW: #{row.inspect} \n"
    #row.each do |k|
    #  puts "K: #{k} V:#{row['loc']}"
    #end
  end
end


# An SQL-based RDF data service
#
# todo: various things hardcoded

class DBIDataService

def initialize (driver,user,pass)
  @DBI_DRIVER = 'DBI:Pg:test1'
  @DBI_USER = 'danbri'
  @DBI_PASS=''
end

#not used yet
def connect (d,u,p)
  c= DBI.connect( @DBI_DRIVER, @DBI_USER, @DBI_PASS) 
  puts "Connected: #{c} "
  return true
end


# return pairs of node content and node id that match some substring 
#
def substrings (q=nil)
  service = DBIDataService.new(@DBI_DRIVER,@DBI_USER,@DBI_PASS)
  matches={}
  DBI.connect( DBI_DRIVER, DBI_USER, DBI_PASS) do | dbh |
    sql  = "select keyhash, value from resources where lower(value) like '%#{q}%';"
    dbh.select_all( sql ) do | row |  
       matches[ row['keyhash']] = row['value'] 
     end
  end
  return matches
end



# TODO:  - extend for RDF Core WG's datatyping idioms
#        - integrate more fully with query processor
#
def findPropertySubstring (q=nil, propuri=nil)
  service = DBIDataService.new(@DBI_DRIVER,@DBI_USER,@DBI_PASS)
  matches={}
  DBI.connect( DBI_DRIVER, DBI_USER, DBI_PASS) do | dbh | 
    sql  = "SELECT r1.keyhash  AS keyhash, r3.value AS res, r1.value AS val 
		FROM resources r1, resources r2, resources r3, triples 
		WHERE lower(r1.value) 
		LIKE '%#{q}%' AND triples.object = r1.keyhash  
		AND triples.subject = r3.keyhash
		AND triples.predicate = r2.keyhash AND r2.value='#{propuri}';"

     #puts "FIND: #{sql}"
  
    dbh.select_all( sql ) do | row |  
       matches[ row['res']] = row['val'] 
     end
  end
  return matches
  ## todo: we should be able to return a ResultRow 
end



def defrag (idprops=nil)

  # Smushing: defragment the stored graph
  egns = 'http://example.com/xmlns/aggregation-demo#' #demo only

  if (idprops == nil)
    idprops = [ egns+'personalMailbox', egns+'homepage', egns+'corporateHomepage' ]
  end
  sameIndividual={}

  DBI.connect(DBI_DRIVER, DBI_USER, DBI_PASS) do | dbh |

  idprops.each do |idprop| 
    q1 = "select ?resource, ?value, WHERE (#{idprop} ?resource ?value) USING foo for bar"
    query = SquishQuery.new()
    sqlq = query.parseFromText(q1).toSQLQuery
    dbh.select_all(sqlq) do | row |  
      res = row['resource']
      val = row['value']
      eq = sameIndividual[idprop + val ];
      if(eq != nil) 
        eq[res] = 1
      else
        sameIndividual[ idprop + val ] = { res => '1' } 
      end
     end

   end

   sameIndividual.each_key do |nameset| 
     # puts "Resource: #{nameset} Values: #{sameIndividual[nameset].keys} \n\n"
     idset = sameIndividual[nameset].keys;
     new = idset.shift; # randomly picked; should take care to value URIs over genids, common terms over obscure ones
			# if we have two URIs, should add URIs in as  properties
			# rather than lose them.
     idset.each do |name|
      dbh.do "update triples SET subject='#{hashcodeIntFromString new}' WHERE subject='#{hashcodeIntFromString name}';"
      dbh.do "update triples SET predicate='#{hashcodeIntFromString new}' WHERE predicate='#{hashcodeIntFromString name}';"
      dbh.do "update triples SET object='#{hashcodeIntFromString new}' WHERE object='#{hashcodeIntFromString name}';"
      end
   end
end
return self
end


# status: not working yet.
#
def addAllSuperProperties 

  subprop='http://www.w3.org/2000/01/rdf-schema#subPropertyOf'## @@todo: define elsewhere
  superProperty={}
  q1 = "select ?resource, ?value, WHERE (#{subprop} ?resource ?value) USING foo for bar"
  query = SquishQuery.new()
  newtriples=[]
  reshash={}

  # todo: fix to respect transitive hierachy. currently works on layer only. 

  DBI.connect(@DBI_DRIVER, @DBI_USER, @DBI_PASS) do | dbh |

    dbh.select_all ( query.parseFromText(q1).toSQLQuery  ) do | row |  
      superProperty[row['resource']] = row['value']
    end
   
    # todo: should add the implied superProperty relations to superProperty 

    superProperty.each_key do |prop| 
      #puts " #{prop} is a sub of the superProperty: #{superProperty[prop]} \n\n"

      # DesignIssue: should we persevere with doing this over the RDF interface?
      #
      q1 = "select ?resource, ?value, WHERE (#{prop} ?resource ?value) USING foo for bar"
      query = SquishQuery.new()
      sq= query.parseFromText(q1).toSQLQuery  
      # puts "Asking db: #{q1} \n #{sq}\n\n "
      dbh.select_all ( sq  ) do | row |  
        #puts "ADDING: #{row['resource']} -- #{superProperty[prop]} -> #{row['value']}"

        val = row['value']
        #TODO: we need to know type, ie. whether a literal or res, genid...
	# this isn't right...!
        if (val =~ /\s/)
          val = "\""+val+"\""
        end
        #puts "VALUE: #{val} "
        newtriples.push ([ hashcodeIntFromString(row['resource']), 
		hashcodeIntFromString( superProperty[prop]  ),
		hashcodeIntFromString( val ) ])
        reshash[row['resource']]= hashcodeIntFromString(row['resource'])
        reshash[row['value']]= hashcodeIntFromString(row['value'])
        reshash[superProperty[prop]]= hashcodeIntFromString(superProperty[prop])

      end
    end  


    # delete old cache'd triples (WARNING: be careful with DELETE WHEREs!)
    dbh.do "DELETE FROM triples WHERE assertid='SUPERPROP_CACHE'; "
 
    cache_id='SUPERPROP_CACHE'

    # todo: store these up as one batch for faster loading
    newtriples.each do |t| 
      dbh.do "insert into triples values ('#{t[0]}','#{t[1]}','#{t[2]}', '#{cache_id}' ); "
      #puts "insert into triples values ('#{t[0]}','#{t[1]}','#{t[2]}');"
      # todo: isresource column shouldn't be empty
    end
    reshash.each_key do |r|
      
   #debug: make sure resources table is populated. 
   #todo: this shouldn't be needed. We're hashing literals incorrectly :( 
      begin
      #  dbh.do "insert into resources values ('#{reshash[r]}', '#{r}' ); "
      #   puts "insert into resources values ('#{reshash[r]}', '#{r}' ); "
       rescue
         # puts "Caught error (duplicate resource info)"
      end

   end

  end
  return self
end


end # DBIDataService class


# usage: service = DBIDataService.new(DBI_DRIVER,DBI_USER,DBI_PASS).defrag
 





############################################################################


#squish = ''
#if (ARGV[0] != nil) 
#  squish =  `cat #{ ARGV[0]}` 
#  query = SquishQuery.new()
#  query.parseFromText(squish) 
#  puts "#{query.toSQLQuery} \n\n"
#end 




#puts "Query parser output was :\n #{query.toSquish}  \n\n"
#query.expandAllNamespaces # move internal
#puts "Allvars: #{query.all_vars.keys} \n"
#puts "Algae: #{query.toAlgae} \n\n"
#puts query.inspect
# we can round-trip our output. This is good...
#query2 = SquishQuery.new()
#query2.clauses=[]
#q3 = query2.parseFromText(squish)
#puts "Final Output:\n #{query2.toSquish}\n\n"







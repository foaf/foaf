<!--

This schema should have the following constraints:

* single root foaf:Person
* any foaf:Person, foaf:Agent properties, and those with domain rdf:Resource
* no other FOAF elements, except knows
* allow foaf:knows
* no elements from other namespaces
* check literals for no children
* check resources for no children
* confirm no foaf attributes on foaf:Person (non-repeating properties shortcut)
* ...others?

-->
<schema xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#" xmlns:foaf="http://xmlns.com/foaf/0.1/" xmlns:dc="http://purl.org/dc/elements/1.1/" fpi="">

   <title>FOAF "Basic" Validator</title>

   <ns prefix="rdf" uri="http://www.w3.org/1999/02/22-rdf-syntax-ns#"/>
   <ns prefix="rdfs" uri="http://www.w3.org/2000/01/rdf-schema#"/>
   <ns prefix="foaf" uri="http://xmlns.com/foaf/0.1/"/>

   <!-- =============== Namespace Checking ================ -->
   <pattern name="Namespace Checking">

      <!-- Report on elements from namespaces unknown to the validator -->
      <rule context="*">
         <assert test="namespace-uri()='http://www.w3.org/1999/02/22-rdf-syntax-ns#' or                     namespace-uri()='http://www.w3.org/2000/01/rdf-schema#' or                     namespace-uri()='http://xmlns.com/foaf/0.1/'" diagnostics="ns">
         Error: Element <name/> is from an unknown namespace.
         </assert>

      </rule>

   </pattern>

   <!-- =============== Basic Validation ================ -->
   <pattern name="Basic Validation">
      <rule context="/">
         <assert test="rdf:RDF">Root element must be rdf:RDF</assert>
      </rule>
      
      <rule context="rdf:RDF">
         <assert test="foaf:Person" diagnostics="single-person">Error: The root rdf:RDF element must contain a foaf:Person element</assert>
         <assert test="count(foaf:Person) = 1" diagnostics="single-person">Error: There should only be a single root foaf:Person</assert>
         <assert test="count(foaf:Person) = count(*)">Error: There must be a single element as a child of rdf:RDF and that must be a foaf:Person element.</assert>
         
      </rule>

      <!-- main person -->      
      <rule context="rdf:RDF/foaf:Person">      
         <!-- we need a name -->
         <assert test="foaf:name or foaf:firstName or foaf:surname or foaf:nick" diagnostics="naming">Error: You should include a name</assert>

         <report test="@*[namespace-uri()='http://xmlns.com/foaf/0.1/']" diagnostics="property-as-attribute">Error: Illegal use of atrtibute in FOAF namespace</report>

      </rule>

      <!-- friend -->
      <rule context="foaf:knows/foaf:Person">
         <assert test="foaf:name or foaf:firstName or foaf:surname or foaf:nick" diagnostics="naming">Error: You should include a name</assert>         
      </rule>

      <!-- people elsewhere? -->
      <rule context="foaf:Person">
         <report test="true()">Error: People should only appear in a foaf:knows element, or within the root rdf:RDF element</report>
      </rule>

      <rule context="foaf:mbox">
         <!-- not sure if this is legal, but seems to work... -->
         <extends rule="rdf_resource"/>
         <assert test="starts-with(@rdf:resource, 'mailto:')" diagnostics="uris">Mail-boxes must be specified as URIs</assert>
      </rule>
      
   <!--Begin Auto-Generated Block: Legal Elements--><rule xmlns:owl="http://www.w3.org/2002/07/owl#" context="foaf:mbox | foaf:mbox_sha1sum | foaf:geekcode | foaf:jabberID | foaf:aimChatID | foaf:icqChatID | foaf:yahooChatID | foaf:msnChatID | foaf:firstName | foaf:surname | foaf:family_name | foaf:homepage | foaf:weblog | foaf:plan | foaf:made | foaf:maker | foaf:img | foaf:depiction | foaf:myersBriggs | foaf:workplaceHomepage | foaf:workInfoHomepage | foaf:schoolHomepage | foaf:knows | foaf:interest | foaf:topic_interest | foaf:publications | foaf:currentProject | foaf:pastProject | foaf:fundedBy | foaf:logo | foaf:page | foaf:theme | foaf:holdsAccount"/>
<rule xmlns:owl="http://www.w3.org/2002/07/owl#" context="foaf:sha1 | foaf:based_near | foaf:depicts | foaf:thumbnail | foaf:topic | foaf:accountServiceHomepage | foaf:accountName | foaf:member | foaf:membershipClass">
<report test="true()">This element is not allowed in FOAF Minimal</report>
</rule>
<!--End Auto-Generated Block: Legal Elements-->
</pattern>

   <!-- =============== FOAF Best Practices ================ -->
   <pattern name="Best Practices">
      <rule context="rdf:RDF/foaf:Person">
         <report test="foaf:mbox" diagnostics="plain-text-email">Warning: Plain text email found</report>
      </rule>

      <!-- friend -->
      <rule context="foaf:knows/foaf:Person">
         <report test="foaf:mbox" diagnostics="plain-text-email">Warning: Friends should never have a plain-text specified in a FOAF Basic document</report>
         <!-- warn about extra data in friends -->         
      </rule>
   
   </pattern>

   <!-- =============== RDF/XML Validation ================ -->
   <pattern name="RDF Validation">
      <rule abstract="true" id="rdf_resource">
         <assert test="@rdf:resource">This element should have an rdf:resource attribute</assert>
         <report test="resource">The resource attribute should be namespace qualified</report>
         <report test="child::*">Error: Resource elements should not have child elements</report>         
      </rule>      
      <rule context="foaf:mbox">
         <extends rule="rdf_resource"/>
         <assert test="starts-with(@rdf:resource, 'mailto:')" diagnostics="uris">Error: Mail-boxes must be specified as mailto: URIs</assert>
      </rule>      
      
      <rule context="foaf:phone">
         <extends rule="rdf_resource"/>
         <assert test="starts-with(@rdf:resource, 'tel:')" diagnostics="uris">Error: Telephone numbers must be specified as tel: URIs</assert>

      </rule>
      
      <rule context="rdfs:seeAlso|foaf:homepage|foaf:weblog|foaf:depiction|foaf:workplaceHomepage|foaf:schoolHomepage">
         <extends rule="rdf_resource"/>
      </rule>
   </pattern>

   <!-- ============= DIAGNOSTIC ELEMENTS =============== -->   
   <diagnostics>
      <diagnostic id="ns">
      FOAF Basic does not allow the use of elements from anything other than the RDF, RDFS or FOAF namespaces, 
      and even those are strictly limited.
      </diagnostic>

      <diagnostic id="limited-rdf">
      The only RDF element supported in FOAF Basic is rdf:RDF. No others are allowed.
      </diagnostic>

      <diagnostic id="property-as-attribute">
      RDF supports a shortcut syntax that allows non-repeating properties to be specified as attributes 
      of the resource element. This is NOT supported in FOAF Basic.
      </diagnostic>

      <diagnostic id="single-person">
      FOAF Basic restricts a document to containing a single "root" foaf:Person element. 
      This is the main or primary person in the document.
      </diagnostic>
      
      <diagnostic id="plain-text-email">
      Including plain-text emails in a FOAF document opens the possibility that the email 
      can be used for spamming and other nefarious uses.
      </diagnostic>

      <diagnostic id="uris">
      RDF Resources are always URIs. E.g. http://, mailto:, tel:, etc.
      </diagnostic>

      <diagnostic id="naming">
      You should include the name of a foaf:Person. Include simply a foaf:nick or a complete name (foaf:title, foaf:firstName, foaf:surname)
      </diagnostic>
   <diagnostic xmlns:owl="http://www.w3.org/2002/07/owl#" id="identifying-properties">
         "Inverse Functional Properties" uniquely identify a foaf:Person and are used to collate 
         statements made about that person from multiple sources.
         </diagnostic>
</diagnostics>
         
<!--Begin Auto-Generated Block: Literals--><pattern xmlns:owl="http://www.w3.org/2002/07/owl#" name="Content Model (Literals)">
<rule context="foaf:mbox_sha1sum">
<report test="child::*">Literals should not have child elements</report>
</rule>
<rule context="foaf:geekcode">
<report test="child::*">Literals should not have child elements</report>
</rule>
<rule context="foaf:dnaChecksum">
<report test="child::*">Literals should not have child elements</report>
</rule>
<rule context="foaf:jabberID">
<report test="child::*">Literals should not have child elements</report>
</rule>
<rule context="foaf:aimChatID">
<report test="child::*">Literals should not have child elements</report>
</rule>
<rule context="foaf:icqChatID">
<report test="child::*">Literals should not have child elements</report>
</rule>
<rule context="foaf:yahooChatID">
<report test="child::*">Literals should not have child elements</report>
</rule>
<rule context="foaf:msnChatID">
<report test="child::*">Literals should not have child elements</report>
</rule>
<rule context="foaf:name">
<report test="child::*">Literals should not have child elements</report>
</rule>
<rule context="foaf:firstName">
<report test="child::*">Literals should not have child elements</report>
</rule>
<rule context="foaf:surname">
<report test="child::*">Literals should not have child elements</report>
</rule>
<rule context="foaf:family_name">
<report test="child::*">Literals should not have child elements</report>
</rule>
<rule context="foaf:plan">
<report test="child::*">Literals should not have child elements</report>
</rule>
<rule context="foaf:myersBriggs">
<report test="child::*">Literals should not have child elements</report>
</rule>
<rule context="foaf:accountName">
<report test="child::*">Literals should not have child elements</report>
</rule>
</pattern>
<!--End Auto-Generated Block: Literals-->
<!--Begin Auto-Generated Block: Inverse Functional Properties--><pattern xmlns:owl="http://www.w3.org/2002/07/owl#" name="Identifying Properties">
<rule context="rdf:RDF/foaf:Person">
<assert diagnostics="identifying-properties" test="foaf:mbox or foaf:mbox_sha1sum or foaf:jabberID or foaf:aimChatID or foaf:icqChatID or foaf:yahooChatID or foaf:msnChatID or foaf:homepage or foaf:weblog">You should include at least one inverse functional property</assert>
</rule>
<rule context="foaf:knows/foaf:Person">
<assert diagnostics="identifying-properties" test="foaf:mbox or foaf:mbox_sha1sum or foaf:jabberID or foaf:aimChatID or foaf:icqChatID or foaf:yahooChatID or foaf:msnChatID or foaf:homepage or foaf:weblog">You should include at least one inverse functional property</assert>
</rule>
</pattern>
<!--End Auto-Generated Block: Inverse Functional Properties-->
</schema>

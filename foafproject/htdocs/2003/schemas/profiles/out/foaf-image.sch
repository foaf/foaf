<!--

FOAF-Image

This schema should have the following constraints:

* foaf:Image elements
* only foaf:depicts (with foaf:Person child element) and foaf:thumbnail as children
* foaf:Person can only have rdfs:seeAlso and mbox_sha1sum
* no other FOAF elements, e.g. knows, Image
* required dc:title, and dc:creator for image
* confirm no foaf attributes on foaf:Person (non-repeating properties shortcut)
* ...others?

This profile used for creating lists of images.

-->
<schema xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#" xmlns:foaf="http://xmlns.com/foaf/0.1/" xmlns:dc="http://purl.org/dc/elements/1.1/">

   <title>FOAF "Image" Validator</title>

   <ns prefix="rdf" uri="http://www.w3.org/1999/02/22-rdf-syntax-ns#"/>
   <ns prefix="rdfs" uri="http://www.w3.org/2000/01/rdf-schema#"/>
   <ns prefix="foaf" uri="http://xmlns.com/foaf/0.1/"/>
   <ns prefix="dc" uri="http://purl.org/dc/elements/1.1/"/>

   <pattern name="Namespace Checks">
      <rule context="*">
         <assert test="namespace-uri()='http://www.w3.org/1999/02/22-rdf-syntax-ns#' or                     namespace-uri()='http://www.w3.org/2000/01/rdf-schema#'                     or namespace-uri()='http://xmlns.com/foaf/0.1/'                     or namespace-uri()='http://purl.org/dc/elements/1.1/'" diagnostics="ns">
         Error: Element <name/> is from an unknown namespace.
         </assert>
      </rule>   
   </pattern>
   
   <pattern name="Basic Validation">
      <rule context="/">
         <assert test="rdf:RDF">Error: Root element must be rdf:RDF</assert>         
      </rule>
      
      <rule context="rdf:RDF">
         <assert test="foaf:Image">Error: The root rdf:RDF element must contain at least one foaf:Image element</assert>
         <assert test="count(foaf:Image) = count(*)">Error: The only legal children of rdf:RDF are foaf:Image elements</assert>
      </rule>

      <rule context="foaf:Image">
         <assert test="dc:title">Error: Images must have a title</assert>
         <assert test="dc:creator">Error: Images must have a creator</assert>
         <assert test="foaf:depicts">Error: Images must indicate the people they depict</assert>
         <assert test="@rdf:about">Error: An <name/> must have an rdf:about attribute, indicating the images location</assert>
         <assert test="starts-with(@rdf:about, 'http://')">rdf:about attributes must contain non-relative HTTP URIs</assert>
      </rule>

      <rule context="foaf:depicts">      
         <assert test="foaf:Person">Error: The person depicted is not specified</assert>
         <assert test="count(foaf:Person) = 1">Error: Only a single foaf:Person can be included in a foaf:depicts element</assert>
         <assert test="count(foaf:Person) = count(*)">Error: Only a foaf:Person element can be included in foaf:depicts</assert>         
      </rule>
      
      <rule context="dc:creator|dc:title">
         <report test="child::*">Error: A <name/> element must may only contain text.</report>   
      </rule>
      
      <rule context="foaf:Person">
         <assert test="count(rdfs:seeAlso) + count(foaf:mbox_sha1sum) = 2">Error: a <name/> element must contain only an rdfs:seeAlso element and a foaf:mbox_sha1sum element</assert>
      </rule>
      
   </pattern>

   <!-- =============== RDF/XML Validation ================ -->
   <pattern name="RDF Validation">
      <rule abstract="true" id="rdf_resource">
         <assert test="@rdf:resource">Error: This element should have an rdf:resource attribute</assert>
         <report test="resource">Error: The resource attribute should be namespace qualified</report>
         <report test="child::*">Error: Resource elements should not have child elements</report>
      </rule>      
      <rule context="foaf:thumbnail">
         <extends rule="rdf_resource"/>
         <assert test="starts-with(@rdf:resource, 'http:')" diagnostics="uris">Error: Mail-boxes must be specified as http: URIs</assert>
      </rule>      
      
      <rule context="rdfs:seeAlso">
         <extends rule="rdf_resource"/>
      </rule>

   </pattern>

   <!-- ============= DIAGNOSTIC ELEMENTS =============== -->   
   <diagnostics>
      <diagnostic id="ns">
      FOAF Images does not allow the use of elements from anything other than the RDF, FOAF or DC namespaces, 
      and even those are strictly limited.
      </diagnostic>

      <diagnostic id="limited-rdf">
      The only RDF element supported in FOAF Image is rdf:RDF. No others are allowed.
      </diagnostic>
            
      <diagnostic id="uris">
      RDF Resources are always URIs. E.g. http://, mailto:, tel:, etc.
      </diagnostic>
   <diagnostic xmlns:owl="http://www.w3.org/2002/07/owl#" id="identifying-properties">
         "Inverse Functional Properties" uniquely identify a foaf:Person and are used to collate 
         statements made about that person from multiple sources.
         </diagnostic>
</diagnostics>
         
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

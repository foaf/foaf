<!--

Minimally useful FOAF. Basically just a series of 
properties about a person. 

This schema should have the following constraints:

* single root foaf:Person
* no other foaf:Person elements
* any foaf:Person, foaf:Agent properties, and those with domain rdf:Resource
* no other FOAF elements, e.g. knows, Image
* no elements from other namespaces
* check literals for no children
* check resources for no children
* confirm no foaf attributes on foaf:Person (non-repeating properties shortcut)
* ...others?

Sort of like vCard but using FOAF vocabulary. 

-->
<schema
      xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
      xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
      xmlns:foaf="http://xmlns.com/foaf/0.1/"
      xmlns:dc="http://purl.org/dc/elements/1.1/">

   <title>FOAF "Minimal" Validator</title>

   <ns prefix="rdf" uri="http://www.w3.org/1999/02/22-rdf-syntax-ns#"/>
   <ns prefix="rdfs" uri="http://www.w3.org/2000/01/rdf-schema#"/>
   <ns prefix="foaf" uri="http://xmlns.com/foaf/0.1/"/>

   <pattern name="Namespace Checks">
      <rule context="*">
         <assert test="namespace-uri()='http://www.w3.org/1999/02/22-rdf-syntax-ns#' or
                    namespace-uri()='http://www.w3.org/2000/01/rdf-schema#'
                    or namespace-uri()='http://xmlns.com/foaf/0.1/'" diagnostics="ns">
         Error: Element <name/> is from an unknown namespace.
         </assert>

      </rule>   
   </pattern>
   
   <pattern name="Basic Validation">
      <rule context="/">
         <assert test="rdf:RDF">Root element must be rdf:RDF</assert>         
      </rule>
      
      <rule context="rdf:RDF">
         <assert test="foaf:Person" diagnostics="single-person">Error: The root rdf:RDF element must contain a foaf:Person element</assert>
         <assert test="count(foaf:Person) = 1" diagnostics="single-person">Error: There should only be a single root foaf:Person</assert>
         <assert test="count(foaf:Person) = count(*)">Error: There must be a single element as a child of rdf:RDF and that must be a foaf:Person element.</assert>
         <!-- <assert test="rdf:*" diagnostics="limited-rdf">Error: illegal use of element from rdf namespace</assert> -->
      </rule>

      <!-- main person -->      
      <rule context="rdf:RDF/foaf:Person">
         <report test="rdf:*" diagnostics="limited-rdf">Error: illegal use of element from rdf namespace</report>
         <report test="rdfs:*" diagnostics="limited-rdf">Error: illegal use of element from rdfs namespace</report>

         <!-- we need a name -->
         <assert test="foaf:firstName or foaf:surname or foaf:nick or foaf:name" diagnostics="naming">
         Error: You should include a name
         </assert>

         <report test="foaf:knows">Error: foaf:knows is not supported in FOAF Minimal</report>
         
         <report test="@*[namespace-uri()='http://xmlns.com/foaf/0.1/']" diagnostics="property-as-attribute">Error: Illegal use of atrtibute in FOAF namespace</report>
         
      </rule>

      <!-- people elsewhere -->
      <rule context="foaf:Person">
         <report test="true()">Error: Only a single person can be specified in a FOAF Minimal document</report>
      </rule>

   </pattern>

   <!-- =============== FOAF Best Practices ================ -->
   <pattern name="Best Practices">
      <rule context="rdf:RDF/foaf:Person">
         <report test="foaf:mbox" diagnostics="plain-text-email">Warning: Plain text email found</report>
      </rule>   
   </pattern>

   <!-- =============== RDF/XML Validation ================ -->
   <pattern name="RDF Validation">
      <rule abstract="true" id="rdf_resource">
         <assert test="@rdf:resource">Error: This element should have an rdf:resource attribute</assert>
         <report test="resource">Error: The resource attribute should be namespace qualified</report>
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
      FOAF Minimal does not allow the use of elements from anything other than the RDF or FOAF namespaces, 
      and even those are strictly limited.
      </diagnostic>

      <diagnostic id="limited-rdf">
      The only RDF element supported in FOAF Minimal is rdf:RDF. No others are allowed.
      </diagnostic>

      <diagnostic id="property-as-attribute">
      RDF supports a shortcut syntax that allows non-repeating properties to be specified as attributes 
      of the resource element. This is NOT supported in FOAF Minimal.
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
   </diagnostics>
         
</schema>
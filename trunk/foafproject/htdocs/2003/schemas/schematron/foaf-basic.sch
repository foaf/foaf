
<schema fpi="">

   <title>FOAF "Classic" Validator</title>

   <ns prefix="rdf" uri="http://www.w3.org/1999/02/22-rdf-syntax-ns#"/>
   <ns prefix="rdfs" uri="http://www.w3.org/2000/01/rdf-schema#"/>
   <ns prefix="foaf" uri="http://xmlns.com/foaf/0.1/"/>
   <ns prefix="dc"  uri="http://purl.org/dc/elements/1.1/"/>

   <pattern name="Namespace Checking">

      <!-- Report on elements from namespaces unknown to the validator -->
      <rule context="*">
         <assert test="namespace-uri()='http://www.w3.org/1999/02/22-rdf-syntax-ns#' or
                    namespace-uri()='http://www.w3.org/2000/01/rdf-schema#' or
                    namespace-uri()='http://xmlns.com/foaf/0.1/' or
                    namespace-uri()='http://purl.org/dc/elements/1.1/'" diagnostics="ns">
         Element <name/> is from an unknown namespace.
         </assert>

      </rule>

   </pattern>


   <pattern name="Basic Checks">
      <rule context="/">
         <assert test="rdf:RDF">Root element must be rdf:RDF</assert>
      </rule>
      
      <rule context="rdf:RDF">
         <assert test="foaf:Person" diagnostics="single-person">The root rdf:RDF element must contain a foaf:Person element</assert>
         <assert test="count(foaf:Person) = 1" diagnostics="single-person">There should only be a single root foaf:Person</assert>
      </rule>

      <!-- main person -->      
      <rule context="rdf:RDF/foaf:Person">
         <assert test="foaf:mbox or foaf:mbox_sha1sum">You should include an email address, preferably a encrypted one using foaf:mbox_sha1sum</assert>
         <report test="foaf:mbox" diagnostics="plain-text-email">Warning plain text email found</report>
         <!-- we need a name -->

         <!-- other identifying properties? -->

         <!-- check content models for literal elements -->
      </rule>

      <!-- friend -->
      <rule context="foaf:knows/foaf:Person">
         <report test="foaf:mbox" diagnostics="plain-text-email">Friends should never have a plain-text specified in a FOAF Classic document</report>
         
         <!-- warn about extra data in friends -->
      </rule>
         
   </pattern>
   
   <pattern name="RDF Validation">
      <rule abstract="true" id="rdf_resource">
         <assert test="@rdf:resource">This element should have an rdf:resource attribute</assert>
         <report test="resource">The resource attribute should be namespace qualified</report>
      </rule>

      <rule context="rdfs:seeAlso|foaf:homepage|foaf:weblog|foaf:depiction|foaf:workplaceHomepage|foaf:schoolHomepage">
         <extends rule="rdf_resource"/>
      </rule>

      <rule context="foaf:mbox">
         <extends rule="rdf_resource"/>
         <assert test="starts-with(@rdf:resource, 'mailto:')" diagnostics="uris">Mail-boxes must be specified as URIs</assert>
      </rule>
      
   </pattern>

   
   <!-- ============= DIAGNOSTIC ELEMENTS =============== --> 
   <diagnostics>
      <diagnostic id="ns">
      This validator only checks elements from the RDF, RDFS, FOAF, and DC namespaces. 
      Either the FOAF file includes elements from another namespace (which will not be 
      validated by this schema) or the namespace declarations are incorrect.
      </diagnostic>

      <diagnostic id="single-person">
      FOAF Classic restricts a document to containing a single "root" foaf:Person element. 
      This is the main or primary person in the document.
      </diagnostic>
      
      <diagnostic id="plain-text-email">
      Including plain-text emails in a FOAF document opens the possibility that the email 
      can be used for spamming and other nefarious uses.
      </diagnostic>

      <diagnostic id="uris">
      RDF Resources are always URIs. E.g. http://, mailto:, tel:, etc.
      </diagnostic>
      
   </diagnostics>
         
</schema>

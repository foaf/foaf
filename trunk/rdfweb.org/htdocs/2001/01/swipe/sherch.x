
&lt;rdf:RDF xml:lang="en"
        xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
        xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
        xmlns:dc="http://purl.org/dc/elements/1.1/"
        xmlns:rss="http://purl.org/rss/1.0/"
        xmlns="http://rdfweb.org/2000/01/swipe#"&gt;

        &lt;rdf:Description rdf:about="http://search03.apple.com/search97cgi/s97_cgi"&gt;
&lt;dc:Title&gt;Apple.com&lt;/dc:Title&gt;
&lt;dc:Description&gt;Searches the Apple web site.&lt;/dc:Description&gt;
&lt;rss:image rdf:resource="http://snowball.ilrt.bris.ac.uk/~pldms/plugins/Apple.src.gif"/&gt;

&lt;searchInterface&gt;
 &lt;ScreenScrapeSpec&gt;
  &lt;macPlugin rdf:resource="http://si.info.apple.com/updates/Apple.src.hqx"/&gt;
  &lt;bannerImage rdf:resource="http://www.apple.com/main/elements/sherlockbar.gif"/&gt;
  &lt;bannerALT&gt;Apple Computer&lt;/bannerALT&gt;
  &lt;bannerLink rdf:resource="http://www.apple.com"/&gt;
  &lt;httpMethod&gt;GET&lt;/httpMethod&gt;
  &lt;input&gt;
   &lt;rdf:Description&gt;
    &lt;defaults&gt;
     &lt;rdf:Bag&gt;

      &lt;rdf:li&gt;
       &lt;rdf:Description rdfs:label="Action" rdf:value="Search"/&gt;
      &lt;/rdf:li&gt;


      &lt;rdf:li&gt;
       &lt;rdf:Description rdfs:label="ServerKey" rdf:value="Primary" /&gt;
      &lt;/rdf:li&gt;

     &lt;/rdf:Bag&gt;
    &lt;/defaults&gt;

    &lt;browser&gt;
     &lt;rdf:Bag&gt;
      &lt;rdf:li&gt;
       &lt;rdf:Description rdfs:label="ResultTemplate" rdf:value="webx2.hts"/&gt;
      &lt;/rdf:li&gt;
     &lt;/rdf:Bag&gt;
    &lt;/browser&gt;


    &lt;user&gt;
     &lt;rdf:Bag&gt;
      &lt;rdf:li&gt;
       &lt;rdf:Description rdfs:label="queryText" rdf:value="" /&gt;
      &lt;/rdf:li&gt;
     &lt;/rdf:Bag&gt;
    &lt;/user&gt;

    &lt;results&gt;
     &lt;rdf:Bag&gt;
      &lt;rdf:li&gt;
       &lt;rdf:Description rdfs:label="ResultTemplate" rdf:value="sherlock.hts" /&gt;
      &lt;/rdf:li&gt;


      &lt;rdf:li&gt;
       &lt;rdf:Description rdfs:label="Collection" rdf:value="web" /&gt;
      &lt;/rdf:li&gt;

      &lt;rdf:li&gt;
       &lt;rdf:Description rdfs:label="ResultMaxDocs" rdf:value="9" /&gt;
      &lt;/rdf:li&gt;

     &lt;/rdf:Bag&gt;
    &lt;/results&gt;

   &lt;/rdf:Description&gt;
  &lt;/input&gt;


 &lt;parserules&gt;
    &lt;rdf:Description
        resultListStart = "&amp;lt;!--IL--&amp;gt;"
        resultListEnd = "&amp;lt;!--/IL--&amp;gt;"
        resultItemStart = "&amp;lt;!--IS--&amp;gt;"
        resultItemEnd = "&amp;lt;!--/IS--&amp;gt;"
        relevanceStart = "&amp;lt;!--REL--&amp;gt;"
        relevanceEnd = "&amp;lt;!--/REL--&amp;gt;"
	/&gt;
 &lt;/parserules&gt;

&lt;/ScreenScrapeSpec&gt;

&lt;/searchInterface&gt;

&lt;/rdf:Description&gt;
&lt;/rdf:RDF&gt;


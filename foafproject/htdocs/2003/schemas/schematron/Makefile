XSLT=java org.apache.xalan.xslt.Process

all:	foafc foaftest1 foaftest2

clean:
	rm -f _* foafc.xsl wai.xsl

foafc:
	$(XSLT) -in foaf-basic.sch -xsl schematron-report.xsl  -out  foafc.xsl

foaftest1:
	$(XSLT) -in foaf1.rdf -xsl foafc.xsl  -out _foaf1-errors.html

foaftest2:
	$(XSLT) -in foaf2.rdf -xsl foafc.xsl  -out _foaf2-errors.html
	
waitest: wai waireport

wai:
	$(XSLT) -in wai.xml -xsl schematron-report.xsl  -out wai.xsl

waireport:
	$(XSLT) -in waitest.xml -xsl wai.xsl -out _wai-schematron-errors.html
 



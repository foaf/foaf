PARSER=/home/pldab/working/w3c/perl/modules/W3C/Rdf/bin/xrdfparser

sign:
	@echo "Sign your Schema!"
	gpg -sab index.html 
	mv index.html.asc ../wotsig

check:
	gpg --quiet --verify ../wotsig index.html      



PARSER=/home/pldab/working/w3c/perl/modules/W3C/Rdf/bin/xrdfparser

test:
	$(PARSER) index.html


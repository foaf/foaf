all:	clean rss-family.png

clean:
	rm -f rss-family.png

rss-family.png: rss-family.dot
	dot -Tpng -o rss-family.png rss-family.dot

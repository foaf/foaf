all:	clean rss-family.png rss-family.svg

clean:
	rm -f rss-family.png rss-family.svg

rss-family.png: rss-family.dot
	dot -Tpng -o rss-family.png rss-family.dot

rss-family.svg: rss-family.dot
	dot -Tsvg -o rss-family.svg rss-family.dot


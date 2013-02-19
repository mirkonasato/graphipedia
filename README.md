Graphipedia
===========

A set of tools for creating a graph database of Wikipedia pages and the links
between them.

Importing Data
--------------

The graphipedia-dataimport module allows to create a [Neo4j](http://neo4j.org)
database from a Wikipedia database dump.

See [Wikipedia:Database_download](http://en.wikipedia.org/wiki/Wikipedia:Database_download)
for instructions on getting a Wikipedia database dump.

Assuming you downloaded `pages-articles.xml.bz2`, follow these steps:

1.  Run ExtractLinks to create a smaller intermediate XML file containing page titles
    and links only. The best way to do this is decompress the bzip2 file and pipe the output directly to ExtractLinks:

    bzip2 -dc pages-articles.xml.bz2 | java -classpath graphipedia-dataimport.jar org.graphipedia.dataimport.ExtractLinks - enwiki-links.xml

2.  Run ImportGraph to create a Neo4j database with nodes and relationships into
    a `graphdb` directory

    java -Xmx3G -classpath graphipedia-dataimport.jar org.graphipedia.dataimport.neo4j.ImportGraph enwiki-links.xml graphdb

Just to give an idea, enwiki-20130204-pages-articles.xml.bz2 is 9.1G and
contains almost 10M pages, resulting in over 92M links to be extracted.

The import took 31m 42s to decompress/ExtractLinks (pretty much the same time
as decompressing only) and 10m 42s to ImportGraph on a T420 laptop running
Linux _with an SSD drive_.

(Note that disk I/O is the critical factor here: the same import will easily
take several hours with an old 5400RPM drive.)

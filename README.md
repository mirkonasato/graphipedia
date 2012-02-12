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

Assuming you downloaded `pages-articles.xml.bz2` and uncompressed it, follow these steps:

1.  ExtractLinks: create a smaller intermediate XML file containing page titles
    and links only

    java -classpath graphipedia-dataimport.jar org.graphipedia.dataimport.ExtractLinks enwiki-latest-pages-articles.xml enwiki-links.xml

2.  ImportGraph: create a Neo4j database with nodes and relationships into
    a `graphdb` directory

    java -Xmx3G -classpath graphipedia-dataimport.jar org.graphipedia.dataimport.neo4j.ImportGraph enwiki-links.xml graphdb

The English wiki downloaded end Dec 2011 was 34G uncompressed and resulted in
over 9M pages and 82M links being created, taking about 13 and 25 minutes on
my laptop.

-- Mirko Nasato

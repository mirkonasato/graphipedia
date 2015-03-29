Graphipedia
===========

A tool for creating a [Neo4j](http://neo4j.org) graph database of Wikipedia pages and the links between them.

Building
--------

This is a Java project built with [Maven](http://maven.apache.org).

Check the `neo4j.version` property in the top-level `pom.xml` file and make sure it matches the Neo4j version
you intend to use to open the database. Then build with

    mvn package

This will generate a package including all dependencies in `graphipedia-dataimport/target/graphipedia-dataimport.jar`.

Importing Data
--------------

The graphipedia-dataimport module allows to create a Neo4j database from a Wikipedia database dump.

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

On my laptop _with an SSD drive_ the import takes about 30 minutes to decompress/ExtractLinks (pretty much the same time
as decompressing only) and an additional 10 minutes to ImportGraph.

(Note that disk I/O is the critical factor here: the same import will easily take several hours with an old 5400RPM drive.)

Querying
--------

The [Neo4j browser](http://blog.neo4j.org/2013/10/neo4j-200-m06-introducing-neo4js-browser.html) can be used to query and visualise
the imported graph. Here are some sample Cypher queries.

Show all pages linked to a given starting page - e.g. "Neo4j":

    MATCH (p0:Page {title:'Neo4j'}) -[Link]- (p:Page)
    RETURN p0, p

Find how two pages - e.g. "Neo4j" and "Kevin Bacon" - are connected:

    MATCH (p0:Page {title:'Neo4j'}), (p1:Page {title:'Kevin Bacon'}),
      p = shortestPath((p0)-[*..6]-(p1))
    RETURN p

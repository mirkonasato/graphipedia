//
// Copyright (c) 2012 Mirko Nasato
//
// Permission is hereby granted, free of charge, to any person obtaining a
// copy of this software and associated documentation files (the "Software"),
// to deal in the Software without restriction, including without limitation
// the rights to use, copy, modify, merge, publish, distribute, sublicense,
// and/or sell copies of the Software, and to permit persons to whom the
// Software is furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included
// in all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
// THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR
// OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
// ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
// OTHER DEALINGS IN THE SOFTWARE.
//
package org.graphipedia.query;

import java.util.ArrayList;
import java.util.List;

import org.neo4j.graphalgo.GraphAlgoFactory;
import org.neo4j.graphalgo.PathFinder;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Expander;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.index.Index;
import org.neo4j.kernel.Traversal;

public class GraphipediaService {

    private static final Expander OUTGOING_LINKS = Traversal.expanderForTypes(WikiRelationshipType.Link, Direction.OUTGOING);

    private final GraphDatabaseService db;
    private final Index<Node> index;

    public GraphipediaService(String storeDir) {
        db = new GraphDatabaseFactory().newEmbeddedDatabase(storeDir);
        registerShutdownHook(db);
        index = db.index().forNodes("pages");
    }

    public List<String> findPath(String startPage, String endPage, int maxDepth) {
        Node startNode = findPage(startPage);
        Node endNode = findPage(endPage);
        PathFinder<Path> finder = GraphAlgoFactory.shortestPath(OUTGOING_LINKS, maxDepth);
        Path path = finder.findSinglePath(startNode, endNode);
        return extractTitles(path);
    }

    public List<List<String>> findShortestPaths(String startPage, String endPage, int maxDepth) {
        Node startNode = findPage(startPage);
        Node endNode = findPage(endPage);
        PathFinder<Path> finder = GraphAlgoFactory.shortestPath(OUTGOING_LINKS, maxDepth);
        Iterable<Path> paths = finder.findAllPaths(startNode, endNode);
        List<List<String>> pagePaths = new ArrayList<List<String>>();
        for (Path path : paths) {
            pagePaths.add(extractTitles(path));
        }
        return pagePaths;
    }

    private Node findPage(String title) {
        Node node = index.get("title", title).getSingle();
        if (node == null) {
            throw new IllegalArgumentException("no such page: " + title);
        }
        return node;
    }

    private List<String> extractTitles(Path path) {
        if (path == null) {
            return null;
        }
        List<String> pages = new ArrayList<String>();
        for (Node node : path.nodes()) {
            pages.add((String) node.getProperty("title"));
        }
        return pages;
    }

    private void registerShutdownHook(final GraphDatabaseService db) {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override public void run() {
                db.shutdown();
            }
        });
    }

}

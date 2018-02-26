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
package org.graphipedia.dataimport.neo4j;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.neo4j.unsafe.batchinsert.BatchInserter;
import org.neo4j.unsafe.batchinsert.BatchInserters;

public class ImportGraph {

    private final BatchInserter inserter;
    private final Map<String, Long> inMemoryIndex;

    public ImportGraph(String dataDir) throws IOException {
        inserter = BatchInserters.inserter(new File(dataDir));
        inMemoryIndex = new HashMap<String, Long>();
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("USAGE: ImportGraph <input-file> <data-dir>");
            System.exit(255);
        }
        String inputFile = args[0];
        String dataDir = args[1];
        ImportGraph importer = new ImportGraph(dataDir);
        importer.createNodes(inputFile);
        importer.createRelationships(inputFile);
        importer.finish();
    }

    public void createNodes(String fileName) throws Exception {
        System.out.println("Importing pages...");
        NodeCreator nodeCreator = new NodeCreator(inserter, inMemoryIndex);
        long startTime = System.currentTimeMillis();
        nodeCreator.parse(fileName);
        long elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000;
        System.out.printf("\n%d pages imported in %d seconds.\n", nodeCreator.getPageCount(), elapsedSeconds);
    }

    public void createRelationships(String fileName) throws Exception {
        System.out.println("Importing links...");
        RelationshipCreator relationshipCreator = new RelationshipCreator(inserter, inMemoryIndex);
        long startTime = System.currentTimeMillis();
        relationshipCreator.parse(fileName);
        long elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000;
        System.out.printf("\n%d links imported in %d seconds; %d broken links ignored\n",
                relationshipCreator.getLinkCount(), elapsedSeconds, relationshipCreator.getBadLinkCount());
    }

    public void finish() {
        inserter.createDeferredSchemaIndex(WikiLabel.Page).on("title").create();
        inserter.shutdown();
    }

}

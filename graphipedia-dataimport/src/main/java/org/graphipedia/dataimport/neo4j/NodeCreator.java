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

import java.util.Arrays;
import java.util.Map;

import org.graphipedia.dataimport.ProgressCounter;
import org.graphipedia.dataimport.SimpleStaxParser;
import org.neo4j.helpers.collection.MapUtil;
import org.neo4j.unsafe.batchinsert.BatchInserter;
import org.neo4j.unsafe.batchinsert.BatchInserterIndex;

public class NodeCreator extends SimpleStaxParser {

    private final BatchInserter inserter;
    private final BatchInserterIndex index;
    private final Map<String, Long> inMemoryIndex;

    private final ProgressCounter pageCounter = new ProgressCounter();

    public NodeCreator(BatchInserter inserter, BatchInserterIndex index, Map<String, Long> inMemoryIndex) {
        super(Arrays.asList("t"));
        this.inserter = inserter;
        this.index = index;
        this.inMemoryIndex = inMemoryIndex;
    }

    public int getPageCount() {
        return pageCounter.getCount();
    }

    @Override
    protected void handleElement(String element, String value) {
        if ("t".equals(element)) {
            createNode(value);
        }
    }

    private void createNode(String title) {
        Map<String, Object> properties = MapUtil.map("title", title);
        long nodeId = inserter.createNode(properties);
        index.add(nodeId, properties);
        inMemoryIndex.put(title, nodeId);
        pageCounter.increment();
        if (pageCounter.getCount() % 50000 == 0) {
            index.flush();
        }
    }

}

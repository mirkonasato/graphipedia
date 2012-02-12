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
package org.graphipedia.dataimport;

import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.codehaus.stax2.XMLOutputFactory2;

public class ExtractLinks {

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("USAGE: ExtractLinks <input-file> <output-file>");
            System.exit(255);
        }
        ExtractLinks self = new ExtractLinks();
        self.extract(args[0], args[1]);
    }

    private void extract(String inputFile, String outputFile) throws IOException, XMLStreamException {
        System.out.println("Parsing pages and extracting links...");
        
        long startTime = System.currentTimeMillis();
        XMLOutputFactory outputFactory = XMLOutputFactory2.newInstance();
        
        XMLStreamWriter writer = outputFactory.createXMLStreamWriter(new FileOutputStream(outputFile), "UTF-8");
        writer.writeStartDocument();
        writer.writeStartElement("d");
        
        LinkExtractor linkExtractor = new LinkExtractor(writer);
        linkExtractor.parse(inputFile);

        writer.writeEndElement();
        writer.writeEndDocument();
        writer.close();
        
        long elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000;
        System.out.printf("\n%d pages parsed in %d seconds.\n", linkExtractor.getPageCount(), elapsedSeconds);
    }

}

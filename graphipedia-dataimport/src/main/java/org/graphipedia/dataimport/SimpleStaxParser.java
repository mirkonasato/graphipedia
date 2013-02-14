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

import org.codehaus.stax2.XMLInputFactory2;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.LinkedList;

public abstract class SimpleStaxParser {
	private static final XMLInputFactory XML_INPUT_FACTORY = XMLInputFactory2.newInstance();

    private final Collection<String> interestingElements;

    public SimpleStaxParser(Collection<String> interestingElements) {
	    this.interestingElements = interestingElements;
    }

    protected abstract void handleElement(String element, String value);

	public void parse(String fileName) throws IOException, XMLStreamException {
		final InputStream inputStream = new FileInputStream(fileName);

		parse(inputStream);
	}

	/**
	 * Parses the XML from the provided inputStream, closing it when finished
	 *
	 * @param inputStream an InputStream containing UTF-8 encoded XML
	 * @throws java.io.IOException if an error occurs closing the file
	 * @throws XMLStreamException if an error occurs reading XML from the stream
	 */
	public void parse(InputStream inputStream) throws IOException, XMLStreamException {
		final XMLStreamReader reader = XML_INPUT_FACTORY.createXMLStreamReader(inputStream, "UTF-8");

		try {
			parse(reader);
		}
		finally {
			inputStream.close();
		}
	}

	public void parse(XMLStreamReader reader) throws XMLStreamException {
		try {
			parseElements(reader);
		}
		finally {
			reader.close();
		}
	}

    private void parseElements(XMLStreamReader reader) throws XMLStreamException {
        LinkedList<String> elementStack = new LinkedList<String>();
        StringBuilder textBuffer = new StringBuilder();
        
        while (reader.hasNext()) {
            switch (reader.next()) {
            case XMLEvent.START_ELEMENT:
                elementStack.push(reader.getName().getLocalPart());
                textBuffer.setLength(0);
                break;
            case XMLEvent.END_ELEMENT:
                String element = elementStack.pop();
                if (isInteresting(element)) {
                    handleElement(element, textBuffer.toString().trim());
                }
                break;
            case XMLEvent.CHARACTERS:
                if (isInteresting(elementStack.peek())) {
                    textBuffer.append(reader.getText());
                }
                break;
            }
        }
    }

    private boolean isInteresting(String element) {
        return interestingElements.contains(element);
    }

}

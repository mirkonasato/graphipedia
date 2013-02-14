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

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.codehaus.stax2.XMLOutputFactory2;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ExtractLinks
{
	public static void main(String[] args) throws Exception
	{
		if (args.length < 2)
		{
			System.out.println("USAGE: ExtractLinks <input-file> <output-file>");
			System.exit(255);
		}

		final String inputFile = args[0];
		final String outputFile = args[1];

		ExtractLinks self = new ExtractLinks();

		self.extract(inputFile, outputFile);
	}

	private void extract(String inputFile, String outputFile) throws IOException, XMLStreamException {
		final InputStream inputStream = openInput(inputFile);
		final OutputStream outputStream  = openOutput(outputFile);

		try {
			extract(inputStream, outputStream);
		}
		finally {
			closeQuietly(inputStream);
			closeQuietly(outputStream);
		}
	}

	private void extract(InputStream inputStream, OutputStream outputStream) throws IOException, XMLStreamException
	{
		System.out.println("Parsing pages and extracting links...");

		long startTime = System.currentTimeMillis();
		XMLOutputFactory outputFactory = XMLOutputFactory2.newInstance();

		XMLStreamWriter writer = outputFactory.createXMLStreamWriter(outputStream, "UTF-8");
		writer.writeStartDocument();
		writer.writeStartElement("d");

		LinkExtractor linkExtractor = new LinkExtractor(writer);
		linkExtractor.parse(inputStream);

		writer.writeEndElement();
		writer.writeEndDocument();
		writer.close();

		long elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000;
		System.out.printf("\n%d pages parsed in %d seconds.\n", linkExtractor.getPageCount(), elapsedSeconds);
	}

	/**
	 * Acquires an InputStream for the nominated input source: either "-" for stdin or a filename. If the filename ends in .bz2 then it will be decompressed
	 *
	 * @param inputFile
	 * @return
	 * @throws IOException
	 */
	private static InputStream openInput(String inputFile) throws IOException {
		if ("-".equals(inputFile)) {
			System.out.println("Reading from stdin");
			// Reading from standard input

			return System.in;
		}
		else {
			final InputStream fileStream = new FileInputStream(inputFile);

			// Try to decompress bzip2 inputs on the fly
			if (inputFile.toLowerCase().endsWith(".bz2")) {
				// We need to buffer the file because BZip2 doesn't do that natively
				final InputStream bufferedStream = new BufferedInputStream(fileStream);

				// Need to use the Bzip2CompressorInputStream directly rather than CompressorStreamFactory because
				// it doesn't support reading concatenated bzip2 streams
				return new BZip2CompressorInputStream(bufferedStream, true);
			}
			else {
				// Assume not compressed
				return fileStream;
			}
		}
	}

	/**
	 * Acquires an OutputStream for
	 *
	 * @param outputFile
	 * @return
	 * @throws IOException
	 */
	private static OutputStream openOutput(final String outputFile) throws IOException {
		return new FileOutputStream(outputFile);
	}


	/**
	 * Closes a Closeable & ignores any exception thrown on close
	 * @param stream
	 */
	private static void closeQuietly(Closeable stream) {
		try {
			if (stream != null)
				stream.close();
		}
		catch (IOException e) {
			// silently ignore
		}
	}
}

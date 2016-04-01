package org.lasalledebain.libris.xmlUtils;

import java.io.Reader;

import javax.xml.stream.Location;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import org.lasalledebain.libris.exception.InputException;

public class ElementReader implements XMLEventReader {
	XMLEventReader xmlReader;
	private XMLEvent currentEvent;
	private Location currentLocation;
	private String sourceFilePath;

	public ElementReader(XMLInputFactory inputFactory, Reader xmlSourceReader, String sourcePath) throws InputException {	
		try {
			xmlReader = inputFactory.createXMLEventReader(xmlSourceReader);
			sourceFilePath = sourcePath;
		} catch (XMLStreamException e) {
			throw new InputException(e.getMessage());
		}	
	}

	public String getSourceFilePath() {
		return sourceFilePath;
	}

	public int getSourceLine() {
		if (null == currentLocation) {
			return -1;
		} else {
			return currentLocation.getLineNumber();
		}
	}
	@Override
	public void close() throws XMLStreamException {
		xmlReader.close();
	}

	@Override
	public String getElementText() throws XMLStreamException {
		return xmlReader.getElementText();
	}

	@Override
	public Object getProperty(String propertyName) throws IllegalArgumentException {
		return xmlReader.getProperty(propertyName);
	}

	@Override
	public boolean hasNext() {
		return xmlReader.hasNext();
	}

	@Override
	public XMLEvent nextTag() throws XMLStreamException {
		/* not supported */
		return null;
	}

	@Override
	public XMLEvent peek() throws XMLStreamException {
		return xmlReader.peek();
	}

	@Override
	public Object next() {
		try {
			return nextEvent();
		} catch (XMLStreamException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public void remove() {
		/* not implemented */
	}

	@Override
	public XMLEvent nextEvent() throws XMLStreamException {
		XMLEvent temp = xmlReader.nextEvent();
		currentLocation = temp.getLocation();
		currentEvent = temp;
		return currentEvent;
	}

}

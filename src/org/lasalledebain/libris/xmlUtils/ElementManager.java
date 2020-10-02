package org.lasalledebain.libris.xmlUtils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.lasalledebain.libris.LibrisDatabase;
import org.lasalledebain.libris.exception.Assertion;
import org.lasalledebain.libris.exception.DatabaseError;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.exception.XmlException;

public class ElementManager implements Iterable<ElementManager>, Iterator<ElementManager> {
	ElementReader xmlReader;
	XMLEvent startEvent;
	final QName tagQname;
	boolean atEndOfElement;
	String nextId;
	private ElementShape xmlShape;
	private LibrisAttributes elementAttributes;
	private LibrisException lastException;
	private XmlShapes shapes;

	public ElementManager(ElementReader rdr, QName elementName, XmlShapes shapes) throws XmlException {
		this.tagQname = elementName;
		this.xmlReader = rdr;
		this.shapes = shapes;
		ElementShape shape = shapes.getShape(elementName.toString());
		if (null != shape) {
			this.xmlShape = shape;
		} else {
			throw new XmlException("No shape defined for "+elementName);
		}
		elementAttributes = new LibrisAttributes();
	}

	public String getSourceFilePath() {
		return xmlReader.getSourceFilePath();
	}

	public String getSourceFilePathAndLine() {
		return xmlReader.getSourceFilePath()+" line "+Integer.toString(xmlReader.getSourceLine());
	}

	public String getSourcePath() {
		String srcFile = getSourceFilePath();
		if (null == srcFile) {
			return "unknown source";
		} else {
			return srcFile;
		}
	}
	public HashMap<String, String> parseOpenTag(String expectedTag) throws InputException {
		Assertion.assertEqualsInputException("Wrong tag", expectedTag, tagQname.toString());
		return parseOpenTag();
	}

	public HashMap<String, String> parseOpenTag() throws InputException {
		XMLEvent nextEvt = null;
		try {
			do {
				if ((null == nextEvt) || nextEvt.isStartDocument() || nextEvt.isCharacters()) {
					nextEvt = xmlReader.nextEvent();
				} else {
					throw new XmlException(nextEvt, "expected opening "+tagQname.toString());
				}
			} while (!nextEvt.isStartElement());
		} catch (XMLStreamException e) {
			throw new XmlException(nextEvt, "XML exception", e);
		}
		startEvent = nextEvt;
		StartElement openEvent = nextEvt.asStartElement();
		if (!openEvent.getName().equals(tagQname)) {
			throw new XmlException(nextEvt, "expected opening "+tagQname.toString());
		}
		atEndOfElement = checkEndElement(nextEvt);

		HashMap<String, String> values = parseAttributes(openEvent, xmlShape);
		values.forEach((String k, String v) -> elementAttributes.setAttribute(k, v));
		if (!hasSubElements() && hasNext()) {
			throw new InputException(nextEvt.toString()+" is not an empty element");
		}
		return values;
	}

	public static HashMap<String, String> parseAttributes(StartElement openEvent, ElementShape myShape) throws XmlException {
		HashMap<String, String> values = new HashMap<String, String>();
		for (QName attrQname: myShape.getRequiredAttributes()) {
			Attribute attr = openEvent.getAttributeByName(attrQname);
			final String attrName = attrQname.toString();
			if ((null == attr)) {
				throw new XmlException(openEvent, openEvent.getName().toString()+" missing attribute: "+attrName);
			}
			String v = attr.getValue();
			values.put(attrName, v);
		}
		for (QName attrQname: myShape.getOptionalAttributes()) {
			Attribute attr = openEvent.getAttributeByName(attrQname);
			String v = (null == attr) ?
					myShape.getDefaultValue(attrQname) : attr.getValue();
					values.put(attrQname.toString(), v);
		}
		return values;
	}

	public LibrisAttributes getElementAttributes() {
		return elementAttributes;
	}

	public boolean checkEndElement(XMLEvent nextEvt) {
		if (nextEvt.isEndElement()) {
			return true;
		} else {
			return false;
		}
	}

	public boolean hasSubElements() {
		return (xmlShape.hasSubElements());
	}

	public boolean hasContent() throws XmlException {
		if (!xmlShape.hasContent() || atEndOfElement || !xmlReader.hasNext()) {
			return false;
		}
		try {
			return peek().isCharacters();
		} catch (XMLStreamException e) {
			throw new XmlException(this, e);
		}
	}

	public XMLEvent peek() throws XMLStreamException {
		XMLEvent peek = xmlReader.peek();
		return peek;
	}

	public String getContent() throws XmlException {
		if (xmlShape.hasContent()) {
			StringBuffer content = new StringBuffer();
			XMLEvent nextEvt;
			try {
				while ((nextEvt = xmlReader.peek()).isCharacters()) {
					content.append(nextEvt.asCharacters().getData());
					xmlReader.nextEvent();
				}
			} catch (XMLStreamException e) {
				throw new XmlException(startEvent, "error reading record element");
			}
			return content.toString();
		} else {
			return "";
		}
	}

	public boolean hasNext() {
		if (atEndOfElement || !xmlReader.hasNext()) {
			return false;
		} else try {
			XMLEvent nextEvt;
			while ((nextEvt = xmlReader.peek()).isCharacters()) {
				if (!xmlShape.hasContent()) {
					xmlReader.nextEvent();
				}
			}
			if (checkEndElement(nextEvt)) {
				EndElement e = nextEvt.asEndElement();
				if (e.getName().equals(tagQname)) {
					xmlReader.nextEvent();
					atEndOfElement = true;
					return false;
				} else {
					throw new DatabaseError("Illegal closing tag: "+e.getName());
				}
			} else if (nextEvt.isStartElement()) {
				StartElement startElem = nextEvt.asStartElement();
				if (isValidSubelement(startElem, null)) {
					nextId = startElem.getName().toString().intern();
					return true;
				} else {
					throw new DatabaseError("Illegal nested element tag: "+startElem.getName());
				}
			} else {
				return true;
			}
		} catch (XMLStreamException e) {
			throw new DatabaseError("Error parsing XML", new DatabaseException(this, e));
		}
	}

	/**
	 * 
	 * @return null if end of current element
	 * @throws InputDataException if element contains an illegal sub-element
	 * @throws XMLStreamException ill-formed XML
	 */
	public ElementManager nextElement(String tagName) throws XmlException {
		XMLEvent evt = null;
		boolean done = false;
		QName subelementName = null;

		try {
			while (!done) {
				evt = xmlReader.peek();
				if (evt.isCharacters() || evt.isStartDocument()) {
					evt = xmlReader.nextEvent();
					continue;				
				} else if (checkEndElement(evt)) {
					EndElement endElem = evt.asEndElement();
					if (endElem.getName().equals(tagQname)) {
						evt = null;
						done = true;
					} else {
						throw new XmlException(endElem.getName().toString()+" end tag not expected in "+tagQname);
					}
					xmlReader.nextEvent();
					/* dump the event */
				} else if (evt.isStartElement()) {
					StartElement startElem = evt.asStartElement();
					if (!isValidSubelement(startElem, tagName)) {
						throw new XmlException(startElem, " not expected in "+tagQname);
					} else {
						done = true;
						subelementName =startElem.getName();
						ElementManager nextMgr = new ElementManager(xmlReader, subelementName, shapes);
						return nextMgr;
					}
				} else if (evt.isEndDocument()) {
					throw new XmlException(evt, ": unexpected end of XML document");
				} 
			}
		} catch (XMLStreamException e) {
			throw new XmlException(this, e);
		}

		throw new XmlException(": unexpected end of XML element"+tagQname.toString()+", last element parsed on "+xmlReader.getSourceLine());
	}

	/**
	 * Read and discard the current element
	 * @return null if end of current element
	 * @throws InputDataException if element contains an illegal sub-element
	 * @throws XMLStreamException ill-formed XML
	 */
	public void flushElement() throws XmlException {
		XMLEvent evt = null;

		while (xmlReader.hasNext()) {
			try {
				evt = xmlReader.nextEvent();
			} catch (XMLStreamException e) {
				throw new XmlException(this, "XML error");
			}
			if (checkEndElement(evt)) {
				EndElement endElem = evt.asEndElement();
				if (endElem.getName().equals(tagQname)) {
					evt = null;
					return;
				}
			}
		}
		throw new XmlException(evt, ": unexpected end of XML document");
	}

	private boolean isValidSubelement(StartElement startElem, String tagName) {
		boolean found = false;
		QName elemName = startElem.getName();
		if (null != tagName) {
			if (!elemName.toString().equals(tagName)) {
				return false;
			}
		}
		for (QName q: xmlShape.getSubElements()) {
			if (elemName.equals(q)) {
				found = true;
				break;
			}
		}
		return found;
	}
	public String getElementTag() {
		return tagQname.toString();
	}

	public String getNextId() throws DatabaseException {
		if (hasNext()) {
			return nextId;
		} else {
			return null;
		}
	}

	public XMLEvent getStartEvent() {
		return startEvent;
	}

	public void parseClosingTag() throws XmlException {
		if (atEndOfElement) {
			return;
		}
		try {
			XMLEvent evt = xmlReader.nextEvent();
			while (evt.isCharacters()) {
				evt = xmlReader.nextEvent();
				if (!checkEndElement(evt)) {
					throw new XmlException(this, "Missing end tag");
				}
			}
		} catch (XMLStreamException e) {
			throw new XmlException(this, e);
		}
	}

	public void closeFile() throws XmlException {
		try {
			xmlReader.close();
		} catch (XMLStreamException e) {
			throw new XmlException(this, e);
		}
	}

	@Override
	public Iterator<ElementManager> iterator() {
		lastException = null;
		return this;
	}

	@Override
	public ElementManager next() {
		try {
			return nextElement();
		} catch (XmlException e) {
			lastException = e;
			return null;
		}
	}

	public LibrisException getLastException() {
		final LibrisException exc = lastException;
		lastException = null;
		return exc;
	}

	@Override
	public void remove() {
		/* empty */
	}

	public ElementManager nextElement() throws XmlException {
		return nextElement(null);
	}
	public static ElementManager makeElementManager(String filePath,
			String initialElementName) throws InputException {
		FileInputStream fileStream;
		try {
			fileStream = new FileInputStream(filePath);
			return makeElementManager(fileStream, filePath, initialElementName);
		} catch (FileNotFoundException e) {
			throw new InputException(filePath, e);
		}
	}

	public static ElementManager makeElementManager(FileInputStream fileStream, String filePath,
			String initialElementName) throws InputException {
		try {
			InputStreamReader xmlFileReader = new InputStreamReader(fileStream);
			fileStream.getChannel().position(0);
			Reader rdr = xmlFileReader;
			ElementManager mgr = LibrisDatabase.makeElementManager(rdr, initialElementName, filePath);
			return mgr;
		} catch (IOException e) {
			String msg = "error opening "+filePath; //$NON-NLS-1$
			LibrisDatabase.librisLogger.log(Level.SEVERE, msg, e); //$NON-NLS-1$
			throw new InputException("error opening "+msg, e); //$NON-NLS-1$
		}
	}
}

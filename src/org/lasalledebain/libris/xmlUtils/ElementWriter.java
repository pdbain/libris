package org.lasalledebain.libris.xmlUtils;

import java.io.OutputStream;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.lasalledebain.libris.exception.XmlException;

public class ElementWriter implements LibrisXMLConstants {
	XMLStreamWriter elemWriter;
	int indent;
	boolean autoIndent;
	public static ElementWriter eventWriterFactory(OutputStream stream, int indent) throws XMLStreamException {
		XMLStreamWriter writer = null;
		XMLOutputFactory fact =  XMLOutputFactory.newInstance();
		writer = fact.createXMLStreamWriter(stream);
		ElementWriter w = new ElementWriter(writer);
		w.indent = indent;
		return w;
	}
	public static ElementWriter eventWriterFactory(OutputStream stream) throws XMLStreamException {
		return eventWriterFactory(stream, 0);
	}

	public ElementWriter(XMLStreamWriter writer) {
		this.elemWriter = writer;
	}
	
	public void writeStartElement(String elementName) throws XmlException {
		writeStartElement(elementName, null, false);
	}
	
	public void writeStartElement(String elementName, LibrisAttributes attributes, boolean empty) 
	throws XmlException {
		try {
			elemWriter.writeCharacters(tabs, 0, indent);
			if (empty) {	
				elemWriter.writeEmptyElement(elementName);
			} else {
				elemWriter.writeStartElement(elementName);
				increaseIndent();
			}
			if (null != attributes) {
				for (String[] pair: attributes) {
					if (!attributes.isDefault(pair[0])) {
						elemWriter.writeAttribute(pair[0], pair[1]);
					}
				}
			}
			elemWriter.writeCharacters("\n");
		} catch (XMLStreamException e) {
			throw new XmlException(e);
		}
	}
	
	public void writeEndElement() throws XmlException {
		try {
		decreaseIndent();
		elemWriter.writeCharacters(tabs, 0, indent);
		elemWriter.writeEndElement();
		elemWriter.writeCharacters("\n");
		} catch (XMLStreamException e) {
			throw new XmlException(e);
		}
	}
	
	public void writeContent(String value) throws XMLStreamException {
		elemWriter.writeCData(value);
	}
	
	public void flush() throws XmlException {
		try {
			elemWriter.flush();
		} catch (XMLStreamException e) {
			throw new XmlException(e);
		}
	}

	public void addLineBreak() throws XmlException {
		try {
			elemWriter.writeCharacters("\n");
		} catch (XMLStreamException e) {
			throw new XmlException(e);
		}
	}
	public void increaseIndent() throws XmlException {
		++indent;
		if (indent > tabs.length) {
			throw new XmlException("overflow indent");
		}
	}
	public void decreaseIndent() throws XmlException {
		--indent;
		if (indent < 0) {
			XmlException exc = new XmlException("underflow indent");
			throw exc;
		}
	}
	public String getTitle() {
		return null;
	}
	public String getId() {
		return null;
	}
	public int getFieldNum() {
		return 0;
	}
	public static String makeOpeningTag(String tagName) {
		String buff = "<"+tagName+">";
		return buff;
	}

	public static String makeClosingTag(String tagName) {
		String buff = "</"+tagName+">";
		return buff;
	}
}

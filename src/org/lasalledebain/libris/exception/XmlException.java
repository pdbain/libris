package org.lasalledebain.libris.exception;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import org.lasalledebain.libris.xmlUtils.ElementManager;

public class XmlException extends InputException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected String message;

	public XmlException(ElementManager mgr, Exception e) {
		this(getElementInfo(mgr.getStartEvent()), e);
	}

	public XmlException(XMLEvent evt, XMLStreamException e) {
		this(getElementInfo(evt), e);
	}

	public XmlException(XMLEvent evt, String msg) {
		super(getElementInfo(evt)+": "+msg);
	}

	public XmlException(XMLEvent evt, String msg,
			XMLStreamException e) {
		super(getElementInfo(evt)+": "+msg, e);
	}
	
	protected static String getElementInfo(XMLEvent evt) {
		int lineNumber = evt.getLocation().getLineNumber();
		String info;
		
		QName name = new QName("");
		if (evt.isStartElement()) {
			name = evt.asStartElement().getName();
		}
		if (evt.isEndElement()) {
			name = evt.asEndElement().getName();
		}
		info = "line "+Integer.toString(lineNumber)+" element \""+name+"\" ";
		return info;
	}

	public XmlException(ElementManager mgr, String msg) {
		super(mgr.getSourcePath()+": "+mgr.getStartEvent()+msg);
	}

	public XmlException() {
		super();
	}

	public XmlException(Exception e) {
		super(e);
	}

	public XmlException(String msg) {
		super(msg);
	}

	public XmlException(String msg, Exception cause) {
		super(msg, cause);
	}

}

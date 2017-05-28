package org.lasalledebain.libris.exception;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import org.lasalledebain.libris.xmlUtils.ElementManager;

public class DatabaseException extends LibrisException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected String message;

	public DatabaseException(ElementManager mgr, Exception e) {
		this(getElementInfo(mgr.getStartEvent()), e);
	}

	public DatabaseException(ElementManager mgr, String msg) {
		super(mgr.getStartEvent()+msg);
	}

	public DatabaseException() {
		super();
	}

	public DatabaseException(Exception e) {
		super(e);
	}

	public DatabaseException(String msg) {
		super(msg);
	}

	public DatabaseException(String msg, Exception cause) {
		super(msg, cause);
	}

	public DatabaseException(XMLEvent evt, String msg,
			XMLStreamException e) {
		super(getElementInfo(evt)+": "+msg, e);
	}

	public DatabaseException(XMLEvent evt, Exception e) {
		this(getElementInfo(evt), e);
	}

	public DatabaseException(XMLEvent evt, String msg) {
		super(getElementInfo(evt)+": "+msg);
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

}

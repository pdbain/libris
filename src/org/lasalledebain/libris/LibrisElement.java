package org.lasalledebain.libris;

import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.xmlUtils.ElementWriter;

public abstract class LibrisElement  implements XMLElement {

	public LibrisElement() {
		super();
	}

	@Override
	public void toXml(ElementWriter xmlWriter) throws LibrisException {
		xmlWriter.writeStartElement(getElementTag(), getAttributes(), true);	
	}

	public abstract String getElementTag();

}
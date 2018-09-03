package org.lasalledebain.libris;

import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.xmlUtils.ElementWriter;
import org.lasalledebain.libris.xmlUtils.LibrisAttributes;

public abstract class EmptyXmlElement implements XMLElement {

	@Override
	public void toXml(ElementWriter xmlWriter) throws LibrisException {
		xmlWriter.writeStartElement(getElementTag(), getAttributes(), true);	
	}

	@Override
	public LibrisAttributes getAttributes() {
		return LibrisAttributes.getLibrisEmptyAttributes();
	}
}

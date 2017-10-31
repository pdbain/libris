package org.lasalledebain.libris.index;

import java.util.logging.Level;

import org.lasalledebain.libris.LibrisDatabase;
import org.lasalledebain.libris.XMLElement;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.xmlUtils.ElementManager;
import org.lasalledebain.libris.xmlUtils.ElementWriter;
import org.lasalledebain.libris.xmlUtils.LibrisAttributes;

public class IndexDef implements XMLElement {

	@Override
	public LibrisAttributes getAttributes() {
		return LibrisAttributes.getLibrisEmptyAttributes();
	}

	public static String getXmltag() {
		return XML_INDEXDEFS_TAG;
	}
	@Override
	public String getElementTag() {
		return getXmltag();
	}

	@Override
	public void toXml(ElementWriter xmlWriter) throws LibrisException {
		xmlWriter.writeStartElement(XML_INDEXDEFS_TAG);
		xmlWriter.writeEndElement();
	}
	public void fromXml(ElementManager mgr) throws InputException, DatabaseException {
		mgr.parseOpenTag();
		mgr.parseClosingTag();
	}
	@Override
	public boolean equals(Object comparand) {
		try {
			IndexDef otherDef = (IndexDef) comparand;
			return otherDef.getAttributes().equals(getAttributes());
		} catch (ClassCastException e) {
			LibrisDatabase.librisLogger.log(Level.WARNING, "Incompatible comparand for "+getClass().getName()+".equals()", e);
			return false;
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

}

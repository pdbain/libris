package org.lasalledebain.libris.index;

import java.util.logging.Level;

import org.lasalledebain.libris.LibrisDatabase;
import org.lasalledebain.libris.XmlExportable;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.xmlUtils.ElementWriter;
import org.lasalledebain.libris.xmlUtils.LibrisAttributes;
import org.lasalledebain.libris.xmlUtils.LibrisXMLConstants;

public class IndexDef implements LibrisXMLConstants, XmlExportable {

	@Override
	public LibrisAttributes getAttributes() {
		return LibrisAttributes.getLibrisEmptyAttributes();
	}

	@Override
	public void toXml(ElementWriter xmlWriter) throws LibrisException {
		xmlWriter.writeStartElement(XML_INDEXDEFS_TAG);
		xmlWriter.writeEndElement();
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

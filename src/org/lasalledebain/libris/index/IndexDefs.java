package org.lasalledebain.libris.index;

import java.util.ArrayList;
import java.util.logging.Level;

import org.lasalledebain.libris.LibrisDatabase;
import org.lasalledebain.libris.Schema;
import org.lasalledebain.libris.XmlExportable;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.exception.XmlException;
import org.lasalledebain.libris.xmlUtils.ElementManager;
import org.lasalledebain.libris.xmlUtils.ElementWriter;
import org.lasalledebain.libris.xmlUtils.LibrisAttributes;

public class IndexDefs implements XmlExportable {


	private LibrisDatabase database;
	private ArrayList<IndexDef> indexList;
	
	public IndexDefs(LibrisDatabase db) {
		this.database = db;
		indexList = new ArrayList<IndexDef>();
	}

	public void fromXml(Schema schem, ElementManager mgr) throws InputException {
		mgr.parseOpenTag();
		while (mgr.hasNext()) {
			ElementManager indexMgr = mgr.nextElement();
		}
		mgr.parseClosingTag();
	}

	public static String getXmlTag() {
		return XML_INDEXDEFS_TAG;
	}
	@Override
	public String getElementTag() {
		return getXmlTag();
	}

	@Override
	public void fromXml(ElementManager mgr) throws LibrisException {
		mgr.parseOpenTag();
		mgr.parseClosingTag();
	}

	@Override
	public LibrisAttributes getAttributes() {
		return LibrisAttributes.getLibrisEmptyAttributes();
	}

	@Override
	public void toXml(ElementWriter xmlWriter) throws XmlException {
		xmlWriter.writeStartElement(XML_INDEXDEFS_TAG, getAttributes(), false);
		xmlWriter.writeEndElement();
	}
	@Override
	public boolean equals(Object comparand) {
		try {
			IndexDefs otherIndexDefs = (IndexDefs) comparand;
			return indexList.equals(otherIndexDefs.indexList);
		} catch (ClassCastException e) {
			LibrisDatabase.librisLogger.log(Level.WARNING, "Incompatible comparand for "+getClass().getName()+".equals()", e);
			return false;
		
		}
	}

}

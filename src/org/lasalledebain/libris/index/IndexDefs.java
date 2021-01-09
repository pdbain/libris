package org.lasalledebain.libris.index;

import static org.lasalledebain.libris.LibrisDatabase.log;

import java.util.LinkedHashMap;
import java.util.logging.Level;

import org.lasalledebain.libris.Schema;
import org.lasalledebain.libris.XMLElement;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.xmlUtils.ElementManager;
import org.lasalledebain.libris.xmlUtils.ElementWriter;
import org.lasalledebain.libris.xmlUtils.LibrisAttributes;

public class IndexDefs implements XMLElement {
	private Schema databaseSchema;
	LinkedHashMap<String, IndexDef> indexList;
	
	public IndexDefs(Schema schem) {
		databaseSchema = schem;
		indexList = new LinkedHashMap<>();
	}

	public IndexDefs(Schema schem, ElementManager mgr) throws InputException {
		this(schem);
		fromXml(mgr);
	}

	public static String getXmlTag() {
		return XML_INDEXDEFS_TAG;
	}
	@Override
	public String getElementTag() {
		return getXmlTag();
	}

	@Override
	public void fromXml(ElementManager mgr) throws InputException  {
		mgr.checkAndParseOpenTag(getXmlTag());
		while (mgr.hasNext()) {
			ElementManager indexMgr = mgr.nextElement();
			IndexDef def = new IndexDef(databaseSchema);
			IndexDef theDef = def;
			theDef.fromXml(indexMgr);
			addIndexDef(theDef);
		}
		mgr.parseClosingTag();
	}

	public void addIndexDef(IndexDef theDef) {
		indexList.put(theDef.getId(), theDef);
	}

	@Override
	public LibrisAttributes getAttributes() {
		return LibrisAttributes.getLibrisEmptyAttributes();
	}

	@Override
	public void toXml(ElementWriter xmlWriter) throws LibrisException {
		xmlWriter.writeStartElement(XML_INDEXDEFS_TAG, getAttributes(), false);
		for (IndexDef id: indexList.values()) {
			id.toXml(xmlWriter);
		}
		xmlWriter.writeEndElement();
	}
	@Override
	public boolean equals(Object comparand) {
		try {
			IndexDefs otherIndexDefs = (IndexDefs) comparand;
			 LinkedHashMap<String, IndexDef> otherIndexList = otherIndexDefs.indexList;
			if (otherIndexList.size() != indexList.size()) {
				return false;
			}
			for (String id: indexList.keySet()) {
				if (!indexList.get(id).equals(otherIndexList.get(id))) {
					return false;
				}
			}
			return true;
		} catch (ClassCastException e) {
			log(Level.WARNING, "Incompatible comparand for "+getClass().getName()+".equals()", e);
			return false;
		
		}
	}

	public IndexDef getIndex(String id) {
		return indexList.get(id);
	}

}

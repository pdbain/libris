package org.lasalledebain.libris.index;

import java.util.ArrayList;
import java.util.logging.Level;

import org.lasalledebain.libris.LibrisDatabase;
import org.lasalledebain.libris.Schema;
import org.lasalledebain.libris.XMLElement;
import org.lasalledebain.libris.exception.Assertion;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.xmlUtils.ElementManager;
import org.lasalledebain.libris.xmlUtils.ElementWriter;
import org.lasalledebain.libris.xmlUtils.LibrisAttributes;

public class IndexDefs implements XMLElement {
	private Schema databaseSchema;
	IndexDef[] indexList;
	
	public IndexDefs(Schema schem) {
		databaseSchema = schem;
	}

	public void fromXml(Schema schem, ElementManager mgr) throws InputException {
		mgr.parseOpenTag();
		ArrayList<IndexDef> indexListArray = new ArrayList<IndexDef>();
		while (mgr.hasNext()) {
			final ElementManager nextElement = mgr.nextElement();
			ElementManager indexMgr = nextElement;
			Assertion.assertEqualsInputException("Wrong opening tag for indexDef", 
					XML_INDEXDEF_TAG, nextElement.getElementTag());
			IndexDef def = new IndexDef(databaseSchema);
			def.fromXml(indexMgr);
			indexListArray.add(def);
		}
		mgr.parseClosingTag();
		indexList = indexListArray.toArray(new IndexDef[indexListArray.size()]);
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
	public void toXml(ElementWriter xmlWriter) throws LibrisException {
		xmlWriter.writeStartElement(XML_INDEXDEFS_TAG, getAttributes(), false);
		for (IndexDef id: indexList) {
			id.toXml(xmlWriter);
		}
		xmlWriter.writeEndElement();
	}
	@Override
	public boolean equals(Object comparand) {
		try {
			IndexDefs otherIndexDefs = (IndexDefs) comparand;
			IndexDef[] otherIndexList = otherIndexDefs.indexList;
			if (otherIndexList.length != indexList.length) {
				return false;
			}
			for (int i = 0; i < indexList.length; ++i) {
				if (!indexList[i].equals(otherIndexList[i])) {
					return false;
				}
			}
			return true;
		} catch (ClassCastException e) {
			LibrisDatabase.librisLogger.log(Level.WARNING, "Incompatible comparand for "+getClass().getName()+".equals()", e);
			return false;
		
		}
	}

	public IndexDef[] getIndexList() {
		return indexList;
	}

}

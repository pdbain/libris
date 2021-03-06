package org.lasalledebain.libris.index;

import static org.lasalledebain.libris.LibrisDatabase.log;

import java.util.ArrayList;
import java.util.logging.Level;

import org.lasalledebain.libris.Schema;
import org.lasalledebain.libris.XMLElement;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.xmlUtils.ElementManager;
import org.lasalledebain.libris.xmlUtils.ElementWriter;
import org.lasalledebain.libris.xmlUtils.LibrisAttributes;

public class IndexDef implements XMLElement {

	private Schema databaseSchema;
	private String indexId;
	private IndexField[] fieldList;

	public IndexDef(Schema schem) {
		databaseSchema = schem;
	}

	public IndexDef(Schema schem, String theId, int[] theFields) {
		databaseSchema = schem;
		indexId = theId;
		fieldList = new IndexField[theFields.length];
		for (int i = 0; i < theFields.length; ++i) {
			fieldList[i] = new IndexField(databaseSchema,theFields[i]);
		}
	}

	@Override
	public LibrisAttributes getAttributes() {
		return new LibrisAttributes(new String[][]{{XML_INDEXDEF_ID_ATTR, indexId}});
	}

	public static String getXmlTag() {
		return XML_INDEXDEF_TAG;
	}
	@Override
	public String getElementTag() {
		return getXmlTag();
	}

	@Override
	public void toXml(ElementWriter xmlWriter) throws LibrisException {
		xmlWriter.writeStartElement(XML_INDEXDEF_TAG, getAttributes(), false);	
		for (IndexField f: fieldList) {
			f.toXml(xmlWriter);
		}
		xmlWriter.writeEndElement();
	}
	public void fromXml(ElementManager mgr) throws InputException  {
		LibrisAttributes attrs = mgr.checkAndParseOpenTag(getElementTag());
		indexId = attrs.get(XML_INDEXDEF_ID_ATTR);
		ArrayList<IndexField> fieldArray = new ArrayList<>();
		while (mgr.hasNext()) {
			ElementManager fieldMgr = mgr.nextElement();
			IndexField fld = new IndexField(databaseSchema);
			fld.fromXml(fieldMgr);
			fieldArray.add(fld);
		}
		fieldList = fieldArray.toArray(new IndexField[fieldArray.size()]);
	}
	/**
	 * @return the fieldList
	 */
	public IndexField[] getFieldList() {
		return fieldList;
	}

	@Override
	public boolean equals(Object comparand) {
		try {
			IndexDef otherDef = (IndexDef) comparand;
			return otherDef.getAttributes().equals(getAttributes());
		} catch (ClassCastException e) {
			log(Level.WARNING, "Incompatible comparand for "+getClass().getName()+".equals()", e);
			return false;
		}
	}

	public String getId() {
		return indexId;
	}
}

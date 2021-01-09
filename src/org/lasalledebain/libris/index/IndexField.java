package org.lasalledebain.libris.index;

import java.util.HashMap;
import java.util.logging.Level;

import org.lasalledebain.libris.EmptyXmlElement;
import org.lasalledebain.libris.LibrisDatabase;
import org.lasalledebain.libris.Schema;
import org.lasalledebain.libris.exception.Assertion;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.xmlUtils.ElementManager;
import org.lasalledebain.libris.xmlUtils.LibrisAttributes;

public class IndexField extends EmptyXmlElement {

	private Schema databaseSchema;
	private int fieldNum;

	public IndexField(Schema schem) {
		databaseSchema = schem;
	}

	public IndexField(Schema theSchema, int theFieldNum) {
		super();
		databaseSchema = theSchema;
		fieldNum = theFieldNum;
	}

	public static String getXmlTag() {
		return XML_INDEXFIELD_TAG;
	}

	@Override
	public String getElementTag() {
		return getXmlTag();
	}

	public void fromXml(ElementManager mgr) throws InputException  {
		HashMap<String, String> attrs = mgr.parseOpenTag();
		String fieldIdString = attrs.get(XML_INDEXFIELD_ID_ATTR);
		Assertion.assertNotNullInputException("indexDef."+XML_INDEXFIELD_ID_ATTR, fieldIdString);
		fieldNum = databaseSchema.getFieldNum(fieldIdString);
		Assertion.assertTrueInputException("Unknown field: " + fieldIdString, fieldNum >= 0);
	}
	@Override
	public boolean equals(Object comparand) {
		try {
			IndexField otherDef = (IndexField) comparand;
			return otherDef.getAttributes().equals(getAttributes());
		} catch (ClassCastException e) {
			LibrisDatabase.log(Level.WARNING, "Incompatible comparand for "+getClass().getName()+".equals()", e);
			return false;
		}
	}

	public int getFieldNum() {
		return fieldNum;
	}
	
	@Override
	public LibrisAttributes getAttributes() {
		return new LibrisAttributes(new String[][]{{XML_INDEXFIELD_ID_ATTR, databaseSchema.getFieldId(fieldNum)}});
	}


}

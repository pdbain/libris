package org.lasalledebain.libris;

import org.lasalledebain.libris.Field.FieldType;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.FieldDataException;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.exception.RecordDataException;
import org.lasalledebain.libris.xmlUtils.ElementManager;
import org.lasalledebain.libris.xmlUtils.ElementShape;


public abstract class Record implements Comparable<Record>, XmlExportable, XmlImportable {
	static final String elementTag = XML_RECORD_TAG;
	public static String getElementTag() {
		return elementTag;
	}
	public static ElementShape getShape() {
		ElementShape shape = new ElementShape(elementTag);
		shape.setRequiredAttributeNames(new String[] {XML_RECORD_ID_ATTR});
		shape.setHasContent(false);
		shape.setSubElementNames(new String[] {XML_MEMBER_TAG, XML_FIELD_TAG});
		return shape;
	}
	public abstract void setRecordId(RecordId id) throws RecordDataException;
	public abstract RecordId getRecordId();
	public abstract void setEditable(boolean newValue);
	public abstract boolean isEditable();
	public abstract void setRecordId(int recId) throws DatabaseException;
	public abstract Record duplicate() throws DatabaseException, FieldDataException;
	
	/* field management */
	public abstract void setAllFields(String[] fieldData) throws InputException;
	public abstract void setField(int fieldNum, Field values) throws DatabaseException;
	public abstract void setField(String fid, Field values) throws InputException, DatabaseException;
	public abstract Field addFieldValue(String fieldName, String fieldData) throws InputException;
	public abstract Field addFieldValue(String fid, int data) throws InputException;
	public abstract Field addFieldValue(int fieldNum, String fieldData) throws InputException;
	public abstract Field addFieldValue(int fieldNum, int fieldData) throws InputException;
	public abstract Field getField(String fieldId) throws InputException;
	public abstract Field getField(int fieldNum) throws InputException;
	public abstract Field getFieldWithInheritance(int fieldNum, RecordList records) throws LibrisException;
	public abstract void removeField(int fieldNum) throws LibrisException;
	public abstract Iterable<Field> getFields();
	public abstract String[] getFieldIds();
	public abstract FieldType getFieldType(String fid);
	public abstract boolean valuesEqual(Record comparand);
	
	/* format conversion */
	public abstract String toString();
	public abstract String generateTitle(String[] titleFieldIds);
	public abstract long getFilePosition();
	public abstract long getDataLength();
	public abstract void fromXml(ElementManager recMgr) throws LibrisException;
	
	/* Group management */
	public abstract RecordId getParent(int groupNum);
	public abstract Iterable<RecordId> getAffiliates(int groupNum);
	public abstract void setParentId(int groupNum, RecordId parent);
	public abstract void addAffiliate(int groupNum, RecordId affiliate);
}

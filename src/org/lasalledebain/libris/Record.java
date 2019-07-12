package org.lasalledebain.libris;

import java.util.Arrays;

import org.lasalledebain.libris.Field.FieldType;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.FieldDataException;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.field.FieldValue;
import org.lasalledebain.libris.index.GroupMember;
import org.lasalledebain.libris.index.IndexField;
import org.lasalledebain.libris.indexes.RecordKeywords;
import org.lasalledebain.libris.xmlUtils.ElementManager;
import org.lasalledebain.libris.xmlUtils.ElementShape;
import org.lasalledebain.libris.xmlUtils.ElementWriter;
import org.lasalledebain.libris.xmlUtils.LibrisAttributes;


public abstract class Record implements Comparable<Record>, XMLElement {
	static final String elementTag = XML_RECORD_TAG;
	protected int id;
	protected String name;
	protected LibrisAttributes attributes;
	Field[] recordFields;
	protected static final GroupMember[] dummyAffiliations = new GroupMember[0];
	boolean editable = false;


	public static String getXmlTag() {
		return elementTag;
	}
	
	public static ElementShape getShape() {
		ElementShape shape = new ElementShape(elementTag);
		shape.setRequiredAttributeNames(new String[] {XML_RECORD_ID_ATTR});
		shape.setOptionalAttributeNames(new String[][] {{"name", ""}});
		shape.setHasContent(false);
		shape.setSubElementNames(new String[] {XML_MEMBER_TAG, XML_FIELD_TAG});
		return shape;
	}
	public static boolean validateRecordName(String name) {
		return name.matches("^\\p{Alpha}[\\p{Alpha}\\p{Digit}_]*");
	}

	public void setRecordId(int recId) {
		id = recId;
	}

	public int getRecordId() {
		return id;
	}
	
	@Override
	public String getElementTag() {
		return getXmlTag();
	}
	
	/**
	 * @return name of record, or null if no name
	 */
	public String getName() {
		return name;
	}

	public abstract void setName(String name) throws InputException;
	/**
	 * TODO Add idAdjustment to the recordId.  Also add idAdjustment to all references to other records
	 * greater than baseId.
	 * @param baseId ID of the last record at the time of the fork
	 * @param idAdjustment amount to adjust the IDs.
	 * @throws InputException if an error occurs during the change
	 */
	public abstract void offsetIds(int baseId, int idAdjustment) throws InputException;
	public abstract Record duplicate() throws DatabaseException, FieldDataException;
	
	/* field management */
	public abstract void setAllFields(String[] fieldData) throws InputException;
	public abstract void setField(int fieldNum, Field values) throws DatabaseException;
	public Field addFieldValue(String fieldName, String fieldData) throws InputException {
		return addFieldValue(getFieldNum(fieldName), fieldData);
	}
	
	public abstract Field addFieldValue(int fieldNum, String fieldData) throws InputException;
	public abstract Field addFieldValue(int fieldNum, int mainValue, String extraValue) throws InputException;
	public abstract Field addFieldValue(int fieldNum, String mainValue, String extraValue) throws InputException;
	public abstract Field addFieldValue(int fieldNum, int fieldData) throws InputException;
	public abstract Field addFieldValuePair(int fieldNum, String value, String extraValue) throws InputException;
	public Field getField(String fieldId) throws InputException {
		return getField(getFieldNum(fieldId));
	}
	
	public abstract Field getDefaultField(int fieldNum) throws InputException;
	public abstract Field getField(int fieldNum) throws InputException;
	public abstract FieldValue getFieldValue(int fieldNum) throws InputException;
	public abstract void removeField(int fieldNum) throws LibrisException;
	public abstract Iterable<Field> getFields();
	public abstract String[] getFieldIds();
	public abstract FieldType getFieldType(String fieldId);
	public abstract boolean valuesEqual(Record comparand);
	
	/* format conversion */
	@Override
	public String toString() {
		StringBuffer buff = new StringBuffer();
		buff.append("Record ID: ");
		buff.append(RecordId.toString(getRecordId())); 
		buff.append('\n');
		affiliationInfoToString(buff);
		for (Field f:recordFields) {
			if (null == f) {
				continue;
			}
			String fieldString = f.toString();
			buff.append(fieldString); 
			buff.append('\n');
		}
		return buff.toString();
	}
	protected abstract void affiliationInfoToString(StringBuffer buff);

	public abstract String generateTitle(String[] titleFieldIds);
	public abstract void fromXml(ElementManager recMgr) throws LibrisException;
	public abstract void toXml(ElementWriter output) throws LibrisException;
	
	/* Group management */
	public abstract boolean hasAffiliations();
	public abstract int getParent(int groupNum);
	public abstract int getParent(String groupId);
	/**
	 * @param groupNum Group number to query
	 * @return array, possibly empty, of record IDs
	 * @throws InputException
	 */
	public abstract int[] getAffiliates(int groupNum) throws InputException;
	public abstract void setAffiliates(int groupNum, int[] affiliates) throws InputException;
	public abstract GroupMember[] getMembers();
	public abstract GroupMember getMember(int groupNum);
	public abstract void setMember(int groupNum, GroupMember newMember);
	/**
	 * @param groupNum Group to query
	 * @return 0 if no parent, 1 if parent only, N if parent and N-1 affiliates
	 */
	public abstract int getNumAffiliatesAndParent(int groupNum);
	public abstract void addAffiliate(int groupNum, int affiliate) throws FieldDataException;
	public abstract void addAffiliate(String groupId, Record affiliate) throws FieldDataException;
	public abstract void setParent(String groupId, Record parent) throws FieldDataException;
	public abstract void setParent(int groupNum, int parent) throws FieldDataException;
	public boolean hasAffiliations(int groupNum) {
		return false;
	}
	public abstract String generateTitle();

	public LibrisAttributes getAttributes() {
		if (null == attributes) {
			attributes = new LibrisAttributes();
		}
		return attributes;
	}

	public void setEditable(boolean newValue) {
		editable = newValue;
	}
	
	public boolean isEditable() {
		return editable;
	}
	public abstract Integer getFieldNum(String fieldId) throws FieldDataException;
	public void setField(String fid, Field values) throws FieldDataException, DatabaseException {
		int position = getFieldNum(fid);
		setField(position, values);
	}
	public Field addFieldValue(String fieldName, int fieldData) throws InputException { 
		return addFieldValue(getFieldNum(fieldName), fieldData);
	}

	public Field addFieldValuePair(String fieldName, String value, String extraValue) throws InputException {
		int fieldNum = getFieldNum(fieldName);
		return addFieldValuePair(fieldNum, value, extraValue);
	}

	public Field getDefaultField(String fieldId) throws InputException {
		int i = getFieldNum(fieldId);
		return getDefaultField(i);
	}

	public FieldValue getFieldValue(String fieldId) throws InputException {
		return getFieldValue(getFieldNum(fieldId));
	}

	public void getKeywords(int[] fieldList, RecordKeywords keywordList) throws InputException {
		for (int fieldNum: fieldList) {
			getFieldKeywords(keywordList, fieldNum);
		}
	}

	public void getKeywords(IndexField[] indexFields, RecordKeywords keywordList) throws InputException {
		for (IndexField f: indexFields) {
			int fieldNum = f.getFieldNum();
			getFieldKeywords(keywordList, fieldNum);
		}
	}

	protected void getFieldKeywords(RecordKeywords keywordList, int fieldNum) throws InputException {
		Field fld = getField(fieldNum);
		if (null != fld) {
			String values = fld.getValuesAsString();
			if ((null != values) && !values.isEmpty()) {
				keywordList.addKeywords(Arrays.asList(values.split("\\W+")));
			}
		}
	}
	// TODO abbreviate title
	
}

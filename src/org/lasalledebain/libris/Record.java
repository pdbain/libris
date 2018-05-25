package org.lasalledebain.libris;

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


public abstract class Record implements Comparable<Record>, XMLElement {
	static final String elementTag = XML_RECORD_TAG;
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
	public abstract int getRecordId();
	/**
	 * @return name of record, or null if no name
	 */
	public abstract String getName();
	public abstract void setName(String name) throws InputException;
	public abstract void setEditable(boolean newValue);
	public abstract boolean isEditable();
	public abstract void setRecordId(int recId);
	/**
	 * Add idAdjustment to the recordId.  Also add idAdjustment to all references to other records
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
	public abstract void setField(String fid, Field values) throws InputException, DatabaseException;
	public abstract Field addFieldValue(String fieldName, String fieldData) throws InputException;
	public abstract Field addFieldValue(String fid, int data) throws InputException;
	public abstract Field addFieldValue(int fieldNum, String fieldData) throws InputException;
	public abstract Field addFieldValue(int fieldNum, int fieldData) throws InputException;
	public abstract Field addFieldValuePair(int fieldNum, String value, String extraValue) throws InputException;
	public abstract Field addFieldValuePair(String fid, String value, String extraValue) throws InputException;
	public abstract Field getField(String fieldId) throws InputException;
	public abstract Field getDefaultField(int fieldNum) throws InputException;
	public abstract Field getDefaultField(String fieldId) throws InputException;
	public abstract Field getField(int fieldNum) throws InputException;
	public abstract FieldValue getFieldValue(String fieldId) throws InputException;
	public abstract FieldValue getFieldValue(int fieldNum) throws InputException;
	public abstract void removeField(int fieldNum) throws LibrisException;
	public abstract Iterable<Field> getFields();
	public abstract String[] getFieldIds();
	public abstract FieldType getFieldType(String fid);
	public abstract boolean valuesEqual(Record comparand);
	public abstract void getKeywords(int[] fieldList, RecordKeywords keywordList) throws InputException;
	public abstract void getKeywords(IndexField[] indexFields, RecordKeywords keywordList) throws InputException;
	
	/* format conversion */
	public abstract String toString();
	public abstract String generateTitle(String[] titleFieldIds);
	public abstract long getDataLength();
	public abstract void fromXml(ElementManager recMgr) throws LibrisException;
	
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
	public abstract short getNumAffiliatesAndParent(int groupNum);
	public abstract void addAffiliate(int groupNum, int affiliate) throws FieldDataException;
	public abstract void addAffiliate(String groupId, Record affiliate) throws FieldDataException;
	public abstract void setParent(String groupId, Record parent) throws FieldDataException;
	public abstract void setParent(int groupNum, int parent) throws FieldDataException;
	public boolean hasAffiliations(int groupNum) {
		return false;
	}
	public abstract String generateTitle();
	
	// TODO abbreviate title
	
}

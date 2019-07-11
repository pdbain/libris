package org.lasalledebain.libris;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;

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
import org.lasalledebain.libris.xmlUtils.ElementWriter;

public class ArtifactRecord extends Record {

	public ArtifactRecord(Schema theSchema) {
		fieldList = new Field[NUM_FIELDS];
		mySchema = theSchema;
	}

	private Field[] fieldList;
	private static final int NUM_FIELDS = 7;
	final Schema mySchema;
	

	@Override
	public int compareTo(Record o) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setName(String name) throws InputException {
		// TODO Auto-generated method stub

	}

	@Override
	public void offsetIds(int baseId, int idAdjustment) throws InputException {
		// TODO Auto-generated method stub

	}

	@Override
	public Record duplicate() throws DatabaseException, FieldDataException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setAllFields(String[] fieldData) throws InputException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setField(int fieldNum, Field values) throws DatabaseException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setField(String fid, Field values) throws DatabaseException {
		// TODO Auto-generated method stub

	}

	@Override
	public Field addFieldValue(String fieldName, String fieldData) throws InputException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Field addFieldValue(String fid, int data) throws InputException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Field addFieldValue(int fieldNum, String fieldData) throws InputException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Field addFieldValue(int fieldNum, int fieldData) throws InputException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Field addFieldValuePair(int fieldNum, String value, String extraValue) throws InputException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Field addFieldValuePair(String fid, String value, String extraValue) throws InputException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Field getField(String fieldId) throws InputException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Field getDefaultField(int fieldNum) throws InputException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Field getDefaultField(String fieldId) throws InputException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Field getField(int fieldNum) throws InputException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FieldValue getFieldValue(String fieldId) throws InputException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FieldValue getFieldValue(int fieldNum) throws InputException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void removeField(int fieldNum) throws LibrisException {
		// TODO Auto-generated method stub

	}

	@Override
	public Iterable<Field> getFields() {
		return new Iterable<Field>() {
			
			@Override
			public Iterator<Field> iterator() {
				return Arrays.stream(fieldList).filter(f -> !Objects.isNull(f)).iterator();
			}
		};
	}

	@Override
	public String[] getFieldIds() {
		return Repository.fieldIds;
	}

	@Override
	public FieldType getFieldType(String fid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean valuesEqual(Record comparand) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void getKeywords(int[] fieldList, RecordKeywords keywordList) throws InputException {
		// TODO Auto-generated method stub

	}

	@Override
	public void getKeywords(IndexField[] indexFields, RecordKeywords keywordList) throws InputException {
		// TODO Auto-generated method stub

	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String generateTitle(String[] titleFieldIds) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getDataLength() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void fromXml(ElementManager recMgr) throws LibrisException {
		// TODO Auto-generated method stub

	}

	@Override
	public void toXml(ElementWriter output) throws LibrisException {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean hasAffiliations() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getParent(int groupNum) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getParent(String groupId) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int[] getAffiliates(int groupNum) throws InputException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setAffiliates(int groupNum, int[] affiliates) throws InputException {
		// TODO Auto-generated method stub

	}

	@Override
	public GroupMember[] getMembers() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GroupMember getMember(int groupNum) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setMember(int groupNum, GroupMember newMember) {
		// TODO Auto-generated method stub

	}

	@Override
	public short getNumAffiliatesAndParent(int groupNum) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void addAffiliate(int groupNum, int affiliate) throws FieldDataException {
		// TODO Auto-generated method stub

	}

	@Override
	public void addAffiliate(String groupId, Record affiliate) throws FieldDataException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setParent(String groupId, Record parent) throws FieldDataException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setParent(int groupNum, int parent) throws FieldDataException {
		// TODO Auto-generated method stub

	}

	@Override
	public String generateTitle() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Field addFieldValue(int fieldNum, int mainValue, String extraValue) throws InputException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Field addFieldValue(int fieldNum, String mainValue, String extraValue) throws InputException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer getFieldNum(String fieldId) throws FieldDataException {
		return mySchema.getFieldNum(fieldId);
	}

}

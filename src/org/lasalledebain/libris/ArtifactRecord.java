package org.lasalledebain.libris;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;

import org.lasalledebain.libris.Field.FieldType;

import static org.lasalledebain.libris.RecordId.NULL_RECORD_ID;
import static org.lasalledebain.libris.exception.Assertion.assertTrueError;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.FieldDataException;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.field.FieldValue;
import org.lasalledebain.libris.field.GenericField;
import org.lasalledebain.libris.index.GroupDefs;
import org.lasalledebain.libris.index.GroupMember;
import org.lasalledebain.libris.index.IndexField;
import org.lasalledebain.libris.indexes.RecordKeywords;
import org.lasalledebain.libris.xmlUtils.ElementManager;
import org.lasalledebain.libris.xmlUtils.ElementWriter;
import org.lasalledebain.libris.xmlUtils.LibrisAttributes;

public class ArtifactRecord extends Record {
	private static final int NUM_FIELDS = 7;
	final Schema mySchema;
	GroupMember affiliations;

	public ArtifactRecord(Schema theSchema) {
		recordFields = new Field[NUM_FIELDS];
		mySchema = theSchema;
		affiliations = null;
	}

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
		id += idAdjustment;

		if (hasAffiliations()) {
			int[] affiliates = affiliations.getAffiliations();
			for (int a = 0; a < affiliates.length; ++a) {
				if (affiliates[a] > baseId) {
					affiliates[a] += idAdjustment;
				}
			}
		}
	}

	@Override
	public Record duplicate() throws DatabaseException, FieldDataException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setAllFields(String[] fieldData) throws InputException {
		for (int i = 0; i < fieldData.length; ++i) {
			if ((null == fieldData) || fieldData[i].isEmpty()) {
				recordFields[i] = null;
			} else {
				recordFields[i] = Repository.newField(i, fieldData[i]);
			}
		}
	}

	@Override
	public void setField(int fieldNum, Field value) throws DatabaseException {
		if (null != value) {
			if (1 == value.getNumberOfValues()) {
				recordFields[fieldNum] = value;
			} else {
				throw new DatabaseException("Multiple field values not permitted in field "+fieldNum);
			}
		}
	}

	@Override
	public Field addFieldValue(int fieldNum, String fieldData) throws InputException {
		if (null == recordFields[fieldNum]) {
			recordFields[fieldNum] = Repository.newField(fieldNum, fieldData);
			return recordFields[fieldNum];
		} else {
			throw new InputException("Multiple field values not permitted in this field");
		}
	}

	@Override
	public Field addFieldValue(int fieldNum, int fieldData) throws InputException {
		if (null == recordFields[fieldNum]) {
			recordFields[fieldNum] = Repository.newField(fieldNum, fieldData);
			return recordFields[fieldNum];
		} else {
			throw new InputException("Multiple field values not permitted in this field");
		}
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
	public Field addFieldValuePair(int fieldNum, String value, String extraValue) throws InputException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Field getDefaultField(int fieldNum) throws InputException {
		return null;
	}

	@Override
	public Field getDefaultField(String fieldId) throws InputException {
		return null;
	}

	@Override
	public Field getField(int fieldNum) throws InputException {
		return recordFields[fieldNum];
	}

	@Override
	public FieldValue getFieldValue(int fieldNum) throws InputException {
		FieldValue result = null;
		Field resultField = recordFields[fieldNum];
		if (null != resultField) {
			result = resultField.getFirstFieldValue();
		}
		return result;
	}

	@Override
	public void removeField(int fieldNum) throws LibrisException {
		if (isEditable()) {
			recordFields[fieldNum] = null;
		} else {
			throw new DatabaseException("Cannot remove field "+fieldNum);
		}
	}

	@Override
	public Iterable<Field> getFields() {
		return new Iterable<Field>() {
			
			@Override
			public Iterator<Field> iterator() {
				return Arrays.stream(recordFields).filter(f -> !Objects.isNull(f)).iterator();
			}
		};
	}

	@Override
	public String[] getFieldIds() {
		return mySchema.getFieldIds();
	}

	@Override
	public FieldType getFieldType(String fieldId) {
		return mySchema.getFieldType(fieldId);
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
	protected void affiliationInfoToString(StringBuffer buff) {
		if (hasAffiliations()) {
			buff.append(affiliations.getTitle());
			buff.append(": ");
			String separator = "";
			for ( int affiliate: affiliations.getAffiliations()) {
				buff.append(separator);
				buff.append(String.valueOf(affiliate));
				separator = ", ";
			}
			buff.append('\n');
		}
	}

	@Override
	public String generateTitle(String[] titleFieldIds) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void fromXml(ElementManager recMgr) throws LibrisException {
		this.attributes = new LibrisAttributes(recMgr.parseOpenTag());
		setRecordId(RecordId.toId(attributes.get(XML_RECORD_ID_ATTR)));
		setName(attributes.get(XML_RECORD_NAME_ATTR));
		while (recMgr.hasNext()) {
			String nextId = recMgr.getNextId();
			if (XML_MEMBER_TAG != nextId) {
				break;
			}
			ElementManager memberMgr = recMgr.nextElement();
			GroupMember mem = new GroupMember(mySchema.getGroupDefs(), null);
			mem.fromXml(memberMgr);
			affiliations = mem;		
		}
		while (recMgr.hasNext()) {
			ElementManager fieldManager = recMgr.nextElement();
			GenericField.fromXml(fieldManager, this);		
		}
		recMgr.parseClosingTag();
	}

	@Override
	public void toXml(ElementWriter output) throws LibrisException {
		String recordNumberString = RecordId.toString(getRecordId());
		LibrisAttributes attrs = new LibrisAttributes();
		attrs.setAttribute(XML_RECORD_ID_ATTR, recordNumberString);
		if (null != name) {
			attrs.setAttribute(XML_RECORD_NAME_ATTR, name);
		}
		output.writeStartElement(XML_RECORD_TAG, attrs, false);
		if (hasAffiliations()) {
			affiliations.toXml(output);
		}
		for (Field f: recordFields) {
			if ((null != f) && !f.isEmpty()) {
				f.toXml(output);
			}
		}
		output.writeEndElement();
		output.flush();
	}

	@Override
	public boolean hasAffiliations() {
		return (null != affiliations) && (!affiliations.isEmpty());
	}

	@Override
	public int getParent(int groupNum) {
		checkGroupNum(groupNum);
		if (groupNum < 0) {
			return NULL_RECORD_ID;
		} else if (null == affiliations) {
			return NULL_RECORD_ID;
		} else {
			return affiliations.getParent();
		}
	}

	@Override
	public int getParent(String groupId) {
		checkGroupId(groupId);
		return affiliations.getParent();
	}

	@Override
	public int[] getAffiliates(int groupNum) throws InputException {
		checkGroupNum(groupNum);
		if (null == affiliations) {
			return null;
		} else {
			return affiliations.getAffiliations();
		}
	}

	@Override
	public void setAffiliates(int groupNum, int[] affiliates) throws InputException {
		checkGroupNum(groupNum);
		ensureAffiliation();
		affiliations.setAffiliates(affiliates);
	}

	@Override
	public GroupMember[] getMembers() {
		return (null == affiliations) ? dummyAffiliations: new GroupMember[] {affiliations};
	}

	@Override
	public GroupMember getMember(int groupNum) {
		checkGroupNum(groupNum);
		return affiliations;
	}

	@Override
	public void setMember(int groupNum, GroupMember newMember) {
		checkGroupNum(groupNum);
		affiliations = newMember;
	}

	@Override
	public int getNumAffiliatesAndParent(int groupNum) {
		int numAffiliates = 0;
		if (hasAffiliations()) {
			numAffiliates = affiliations.getNumberOfValues();
		}
		return numAffiliates;

	}

	@Override
	public void addAffiliate(int groupNum, int affiliate) throws FieldDataException {
		checkGroupNum(groupNum);
		addAffiliateImpl(affiliate);
	}

	private void addAffiliateImpl(int affiliate) throws FieldDataException {
		ensureAffiliation();
		affiliations.addIntegerValue(affiliate);
	}

	@Override
	public void addAffiliate(String groupId, Record affiliate) throws FieldDataException {
		checkGroupId(groupId);
		addAffiliateImpl(affiliate.getRecordId());
	}

	@Override
	public void setParent(String groupId, Record parent) throws FieldDataException {
		checkGroupId(groupId);
		setParentImpl(parent.getRecordId());
	}

	@Override
	public void setParent(int groupNum, int parent) throws FieldDataException {
		setParentImpl(parent);
	}

	private void setParentImpl(int parent) throws FieldDataException {
		if (null == affiliations) {
			addAffiliateImpl(parent);
		} else {
			affiliations.setParent(parent);
		}
	}

	@Override
	public String generateTitle() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer getFieldNum(String fieldId) throws FieldDataException {
		return mySchema.getFieldNum(fieldId);
	}
	private void ensureAffiliation() {
		if (null == affiliations) {
			GroupDefs grpDefs = mySchema.getGroupDefs();
			affiliations = new GroupMember(grpDefs, grpDefs.getGroupDef(0));
		}
	}
	private static void checkGroupId(String groupId) {
		assertTrueError("Invalid group "+groupId+" for artifact database", Repository.ID_GROUPS.equals(groupId));
	}

	private static void checkGroupNum(int groupNum) {
		assertTrueError("Invalid group number "+groupNum+" for artifact database", 0 == groupNum);
	}


}

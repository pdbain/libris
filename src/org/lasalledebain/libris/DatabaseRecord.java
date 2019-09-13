package org.lasalledebain.libris;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;
import java.util.logging.Level;

import org.lasalledebain.libris.Field.FieldType;
import org.lasalledebain.libris.exception.Assertion;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.FieldDataException;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.field.FieldValue;
import org.lasalledebain.libris.field.GenericField;
import org.lasalledebain.libris.index.GroupDefs;
import org.lasalledebain.libris.index.GroupMember;
import org.lasalledebain.libris.xmlUtils.ElementManager;
import org.lasalledebain.libris.xmlUtils.ElementWriter;
import org.lasalledebain.libris.xmlUtils.LibrisAttributes;
import org.lasalledebain.libris.xmlUtils.LibrisXMLConstants;

import static org.lasalledebain.libris.RecordId.NULL_RECORD_ID;
public class DatabaseRecord extends Record implements  LibrisXMLConstants {

	GroupMember affiliations[];
	private RecordTemplate template;
	private int artifactId;
	// TODO 1 save artifactId in native file

	public DatabaseRecord(RecordTemplate recordTemplate) {
		super();
		template = recordTemplate;
		name = null;
		affiliations = null;
		recordFields = new Field[template.getNumFields()];
		for (int i = 0; i < recordFields.length; ++i) {
			recordFields[i] = null;
		}
		artifactId = RecordId.NULL_RECORD_ID;
	}
	
	/**
	 * @return id of the related file in the artifact database
	 */
	@Override
	public int getArtifactId() {
		return artifactId;
	}
	/**
	 * @param artifactId id of the related file in the artifact database
	 */
	@Override
	public void setArtifactId(int artifactId) {
		this.artifactId = artifactId;
	}

	@Override
	public void setAllFields(String[] fieldData) throws InputException {
		int i = 0;
		for (String d: fieldData) {
			Field f = template.newField(i, d);
			recordFields[i] = f;
			++i;
		}
	}
	
	@Override
	public void setField(int fieldNum, Field values) throws DatabaseException {
		if (isEditable() && (fieldNum < recordFields.length)) {
			recordFields[fieldNum] = values;
		} else {
			throw new DatabaseException("Cannot set field "+fieldNum);
		}
	}
	@Override
	public Field addFieldValue(int position, String fieldData) throws InputException {
		if (Objects.isNull(fieldData) || fieldData.isEmpty()){
			return null;
		}
		if (null == recordFields[position]) {
			recordFields[position] = template.newField(position, fieldData);
		} else {
			recordFields[position].addValue(fieldData);
		}
		return recordFields[position];
	}
	/**
	 * @param fieldNum Position of the field in the field list
	 * @param mainValue First value of the pair
	 * @param extraValue Second value of the pair.  May be null or zero length
	 * @return new field
	 * @throws InputException
	 */
	public Field addFieldValue(int fieldNum, int mainValue, String extraValue) throws InputException {
		if (null == recordFields[fieldNum]) {
		recordFields[fieldNum] = template.newField(fieldNum, mainValue, extraValue);
		} else {
			recordFields[fieldNum].addValuePair(mainValue, extraValue);
		}
		return recordFields[fieldNum];
	}

	@Override
	public Field addFieldValue(int position, int fieldData) throws InputException {
		if (null == recordFields[position]) {
		recordFields[position] = template.newField(position, fieldData);
		} else {
			recordFields[position].addIntegerValue(fieldData);
		}
		return recordFields[position];
	}
	
	/**
	 * Create a new value pair field
	 * @param fieldNum Position of the field in the field list
	 * @param mainValue First value of the pair
	 * @param extraValue Second value of the pair.  May be null or zero length
	 * @return new field
	 * @throws DatabaseException
	 * @throws InputException 
	 */
	public Field addFieldValue(int fieldNum, String mainValue, String extraValue) throws InputException {
		if (null == recordFields[fieldNum]) {
			recordFields[fieldNum] = template.newField(fieldNum, mainValue, extraValue);
		} else {
			recordFields[fieldNum].addValuePair(mainValue, extraValue);
		}
		return recordFields[fieldNum];
	}

	@Override
	public Field getField(int fieldNum) throws InputException  {
		Field fld = recordFields[fieldNum];
		if (isEditable()) {
			if (null == fld) {
				try {
					fld = template.newField(fieldNum);
					recordFields[fieldNum] = fld;
				} catch (InputException e) {
					throw new FieldDataException("cannot create new "+id, e);
				}
			}
		} else if ((null != fld) && fld.isEmpty()) {
			fld = null;
		}
		return fld;
	}

	@Override
	public  FieldValue getFieldValue(int fieldNum) throws InputException {
		Field fld = getField(fieldNum);
		if ((null == fld) || fld.isEmpty()) {
			fld = getDefaultField(fieldNum);
		}
		FieldValue firstFieldValue = (null == fld) ? null: fld.getFirstFieldValue();
		return firstFieldValue;
	}

	@Override
	public Field getDefaultField(int fieldNum) throws InputException {
		Field fld;
		if (null == template.records) {
			fld = template.getDefaultField(fieldNum);					
		} else {
			int parentGroup = template.getInheritanceGroup(fieldNum);
			if (LibrisConstants.NULL_GROUP == parentGroup) {
				fld = template.getDefaultField(fieldNum);
			} else {
				int parentRecordId = getParent(parentGroup);
				if (!RecordId.isNull(parentRecordId)) {
					Record parentRecord = template.records.getRecord(parentRecordId);
					if (null == parentRecord) {
						throw new InputException("Cannot locate parent record "+parentRecordId);
					}
					fld = parentRecord.getField(fieldNum);
					if ((null == fld) || fld.isEmpty()) {
						fld = parentRecord.getDefaultField(fieldNum);
					}
				} else {
					fld = template.getDefaultField(fieldNum);					
				}
			}
		}
		return fld;
	}

	@Override
	public Integer getFieldNum(String fieldId) throws FieldDataException {
		return template.getFieldIndex(fieldId);
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
		return  new FieldIterator();
	}
	@Override
	public String[] getFieldIds() {
		ArrayList<String> idList = new ArrayList<String>();
		for (Field f: recordFields) {
			if (null != f) {
				idList.add(f.getFieldId());
			}
		}
		String[] ids = new String[idList.size()];
		return idList.toArray(ids);
	}
	
	public FieldType getFieldType(String fieldId) {
		return template.getFieldType(fieldId);
	}
	
	private class FieldIterator implements Iterator<Field>, Iterable<Field> {
	
		int fieldIndex;
		public FieldIterator() {
			fieldIndex = 0;
		}
		@Override
		public boolean hasNext() {
			while ((fieldIndex < recordFields.length) && (null == recordFields[fieldIndex])) {
				++fieldIndex;
			}
			return (fieldIndex < recordFields.length);
		}
	
		@Override
		public Field next() {
			return recordFields[fieldIndex++];
		}
	
		@Override
		public void remove() {
			return;
		}
	
		@Override
		public Iterator<Field> iterator() {
			return this;
		}
		
	}
	public void fromXml(ElementManager mgr) throws LibrisException  {
		this.attributes = new LibrisAttributes(mgr.parseOpenTag());
		setRecordId(RecordId.toId(attributes.get(XML_RECORD_ID_ATTR)));
		setName(attributes.get(XML_RECORD_NAME_ATTR));
		if (attributes.contains(XML_RECORD_ARTIFACT_ATTR)) {
			setArtifactId(attributes.getInt(XML_RECORD_ARTIFACT_ATTR));
		}
		while (mgr.hasNext()) {
			String nextId = mgr.getNextId();
			if (XML_MEMBER_TAG != nextId) {
				break;
			}
			ElementManager memberMgr = mgr.nextElement();
			GroupMember mem = new GroupMember(template.getGroupDefs(), null);
			mem.fromXml(memberMgr);
			if (null == affiliations) {
				affiliations = new GroupMember[template.getNumGroups()];
			}
			affiliations[mem.getGroupNum()] = mem;		
		}
		while (mgr.hasNext()) {
			ElementManager fieldManager = mgr.nextElement();
			GenericField.fromXml(fieldManager, this);		
		}
		mgr.parseClosingTag();
	}
	
	@Override
	public void toXml(ElementWriter output) throws LibrisException {
		String recordNumberString = RecordId.toString(getRecordId());
		LibrisAttributes attrs = new LibrisAttributes();
		attrs.setAttribute(XML_RECORD_ID_ATTR, recordNumberString);
		if (null != name) {
			attrs.setAttribute(XML_RECORD_NAME_ATTR, name);
		}
		final int myArtifactId = getArtifactId();
		if (!RecordId.isNull(myArtifactId)) {
			attrs.setAttribute(XML_RECORD_ARTIFACT_ATTR, myArtifactId);
		}
		output.writeStartElement(XML_RECORD_TAG, attrs, false);
		if (null != affiliations) {
			for (GroupMember m: affiliations) {
				if (null != m) {
					m.toXml(output);
				}
			}
		}
		for (Field f: recordFields) {
			if ((null != f) && !f.isEmpty()) {
				f.toXml(output);
			}
		}
		output.writeEndElement();
		output.flush();
	}


	protected void affiliationInfoToString(StringBuffer buff) {
		for (int groupNum = 0; groupNum < template.getNumGroups(); ++groupNum) {
			if (hasAffiliations(groupNum)) {
				GroupMember member = affiliations[groupNum];
				buff.append(member.getTitle());
				buff.append(": ");
				String separator = "";
				for ( int affiliate: member.getAffiliations()) {
					buff.append(separator);
					buff.append(String.valueOf(affiliate));
					separator = ", ";
				}
				buff.append('\n');
			}
		}
	}
	
	@Override
	public boolean valuesEqual(Record comparand) {
		if (!getAttributes().equals(comparand.getAttributes())) {
			return false;
		}
		for (Field f: recordFields) {
			try {
				Field otherField = comparand.getField(f.getFieldId());
				if (!f.equals(otherField)) {
					return false;
				}
			} catch (InputException e) {
				return false;
			}
		}
		return true;
	}
	
	@Override
	public int[] getAffiliates(int groupNum) throws InputException {
		int result[];
		if ((null != affiliations) && (null != affiliations[groupNum])) {
			result = affiliations[groupNum].getAffiliations();
		} else {
			result = GroupMember.getDummyAffiliates();
		}
		return result;
	}

	@Override
	public void setAffiliates(int groupNum, int[] newAffiliates) throws InputException {
		ensureAffiliation(groupNum);
		affiliations[groupNum].setAffiliates(newAffiliates);
	}

	@Override
	public boolean equals(Object comparand) {
		boolean result = true;
		if (!Record.class.isInstance(comparand)) {
			result = false;
		} else {
			Record otherRec = (Record) comparand;
			for (int i = 0; i < template.getNumGroups(); ++i) {
				try {
					
					int[] myAffiliations = getAffiliates(i);
					int [] otherAffiliations = otherRec.getAffiliates(i);
					if (!Arrays.equals(myAffiliations, otherAffiliations)) {
						return false;						
					}
				} catch (InputException e) {
					LibrisDatabase.log(Level.WARNING, "Error getting affilaitions", e);
					return false;
				}
			}
			if (otherRec.getFieldIds().length != getFieldIds().length) {
				result = false;
			} else {
				for (Field fld: recordFields) {
					try {
						if (null == fld) {
							continue;
						}
						String nextId = fld.getFieldId();
						Field otherField = otherRec.getField(nextId);
						result = isFieldEqual(fld, otherField);
					} catch (InputException e) {
						result = false;
					}
					if (!result) {
						break;
					}
				}
			}
			return result;
		}
		return result;
	}
	
	private boolean isFieldEqual(Field f1, Field f2) {
		boolean result = true;
		if (f1.isEmpty()) {
			if ((null != f2) && !f2.isEmpty()) {
				result = false;
			}
		} else {
			result = f1.equals(f2);
		}
		return result;
	}
	
	@Override
	public DatabaseRecord duplicate() throws DatabaseException, FieldDataException  {
		DatabaseRecord otherRec = new DatabaseRecord(template);
		otherRec.setEditable(true);
		for (int i = 0; i < recordFields.length; ++i) {
			Field fld = recordFields[i];
			if (null == fld) {
				continue;
			}
			Field otherField = fld.duplicate();
			otherRec.setField(i, otherField);
		}
		return otherRec;
	}

	/* Group management */
	
	@Override
	public void setParent(int groupNum, int parent) throws FieldDataException {
		if ((null == affiliations) || (null == affiliations[groupNum])) {
			ensureAffiliation(groupNum);
			affiliations[groupNum].addIntegerValue(parent);
		} else {
			affiliations[groupNum].setParent(parent);
		}
	}
	
	@Override
	public void setParent(String groupId, Record parent) throws FieldDataException {
		int grpNum = template.getGroupNum(groupId);
		if (-1 == grpNum) {
			throw new FieldDataException("group "+groupId+" not recognized");
		}
		setParent(grpNum, parent.getRecordId());
	}

	@Override
	public boolean hasAffiliations() {
		if (null != affiliations) {
			for (int i = 0; i < affiliations.length; ++i) {
				if (hasAffiliations(i)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean hasAffiliations(int groupNum) {
		return (null != affiliations) && (null != affiliations[groupNum]) && (affiliations[groupNum].getNumberOfValues() > 0);
	}

	@Override
	public int getNumAffiliatesAndParent(int groupNum) {
		int numAffiliates = 0;
		if (hasAffiliations() && (null != affiliations[groupNum])) {
			numAffiliates = affiliations[groupNum].getNumberOfValues();
		}
		return numAffiliates;
	}

	@Override
	public void addAffiliate(int groupNum, int affiliate) throws FieldDataException {
		if (NULL_RECORD_ID == getParent(groupNum)) {
			setParent(groupNum, NULL_RECORD_ID);
		}
		addGroupMember(groupNum, affiliate, false);
	}

	@Override
	public void addAffiliate(String groupId, Record affiliate) throws FieldDataException {
		int groupNum = template.getGroupNum(groupId);
		if (-1 == groupNum) {
			throw new FieldDataException("group "+groupId+" not recognized");
		}
		addGroupMember(groupNum, affiliate.getRecordId(), false);
	}

	@Override
	public int getParent(int groupNum) {
		if (groupNum < 0) {
			return NULL_RECORD_ID;
		} else if ((null == affiliations) || (null == affiliations[groupNum])) {
			return NULL_RECORD_ID;
		} else {
			return affiliations[groupNum].getParent();
		}
	}
	
	
	@Override
	public int getParent(String groupId) {
		int groupNum = template.getGroupNum(groupId);
		return getParent(groupNum);
	}

	private void addGroupMember(int groupNum, int parent,
			boolean addAtFront) throws FieldDataException {
		ensureAffiliation(groupNum);
		affiliations[groupNum].addIntegerValue(parent);
	}

	private void ensureAffiliation(int groupNum) {
		if (null == affiliations) {
			affiliations = new GroupMember[template.getNumGroups()];
		}
		Assertion.assertTrueError("wrong group index", groupNum < affiliations.length);
		if (null == affiliations[groupNum]) {
			GroupDefs grpDefs = template.getGroupDefs();
			affiliations[groupNum] = new GroupMember(grpDefs, grpDefs.getGroupDef(groupNum));
		}
	}

	@Override
	public void offsetIds(int baseId, int idAdjustment) throws InputException {
		id += idAdjustment;
		if (null != affiliations) {
			int numGroups = template.getNumGroups();
			for (int groupNum = 0; groupNum < numGroups; ++ groupNum) {
				if (hasAffiliations(groupNum)) {
					int[] affiliates = getAffiliates(groupNum);
					for (int a = 0; a < affiliates.length; ++a) {
						if (affiliates[a] > baseId) {
							affiliates[a] += idAdjustment;
						}
					}
				}
			}
		}
	}

	@Override
	public GroupMember[] getMembers() {
		return (null == affiliations) ? dummyAffiliations: affiliations;
	}
	@Override
	public GroupMember getMember(int groupNum) {
		return (null == affiliations) ? null: affiliations[groupNum];
	}
	
	@Override
	public void setMember(int groupNum, GroupMember newMember) {
		if (null == affiliations) {
			ensureAffiliation(groupNum);
		}
		affiliations[groupNum] = newMember;
	}

}

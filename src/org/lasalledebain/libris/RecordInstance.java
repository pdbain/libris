package org.lasalledebain.libris;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;

import org.lasalledebain.libris.Field.FieldType;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.FieldDataException;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.field.FieldValue;
import org.lasalledebain.libris.field.GenericField;
import org.lasalledebain.libris.index.Group;
import org.lasalledebain.libris.index.GroupDefs;
import org.lasalledebain.libris.index.GroupMember;
import org.lasalledebain.libris.indexes.RecordKeywords;
import org.lasalledebain.libris.xmlUtils.ElementManager;
import org.lasalledebain.libris.xmlUtils.ElementWriter;
import org.lasalledebain.libris.xmlUtils.LibrisAttributes;
import org.lasalledebain.libris.xmlUtils.LibrisXMLConstants;

public class RecordInstance extends Record implements  LibrisXMLConstants {
	private int id;
	private String name;
	private static final int NULL_ID = RecordId.getNullId();

	Field[] recordData;
	GroupMember affiliations[];
	boolean editable = false;
	private RecordTemplate template;
	private long filePosition = -1;
	private long dataLength = -1;
	private LibrisAttributes attributes;
	
	private static final GroupMember[] dummyAffiliations = new GroupMember[0];

	public RecordInstance(RecordTemplate recordTemplate) {
		template = recordTemplate;
		name = null;
		affiliations = null;
		recordData = new Field[template.getNumFields()];
		for (int i = 0; i < recordData.length; ++i) {
			recordData[i] = null;
		}
	}
	
	@Override
	public void setAllFields(String[] fieldData) throws InputException {
		int i = 0;
		for (String d: fieldData) {
			Field f = template.newField(i, d);
			recordData[i] = f;
			++i;
		}
	}
	
	@Override
	public void setField(int fieldNum, Field values) throws DatabaseException {
		if (isEditable() && (fieldNum < recordData.length)) {
			recordData[fieldNum] = values;
		} else {
			throw new DatabaseException("Cannot set field "+fieldNum);
		}
	}
	@Override
	public void setField(String fid, Field values) throws FieldDataException, DatabaseException {
		int position = template.getFieldIndex(fid);
		setField(position, values);
	}
	
	@Override
	public Field addFieldValue(int position, String fieldData) throws InputException {
		if (null == recordData[position]) {
			recordData[position] = template.newField(position, fieldData);
		} else {
			recordData[position].addValue(fieldData);
		}
		return recordData[position];
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
		if (null == recordData[fieldNum]) {
		recordData[fieldNum] = template.newField(fieldNum, mainValue, extraValue);
		} else {
			recordData[fieldNum].addValuePair(mainValue, extraValue);
		}
		return recordData[fieldNum];
	}

	/**
	 * @param fieldNum Position of the field in the field list
	 * @param mainValue First value of the pair
	 * @param extraValue Second value of the pair.  May be null or zero length
	 * @return new field
	 * @throws InputException
	 */
	public Field addFieldValue(int fieldNum, int mainValue, String extraValue) throws InputException {
		if (null == recordData[fieldNum]) {
		recordData[fieldNum] = template.newField(fieldNum, mainValue, extraValue);
		} else {
			recordData[fieldNum].addValuePair(mainValue, extraValue);
		}
		return recordData[fieldNum];
	}

	@Override
	public Field addFieldValue(String fieldName, String fieldData) throws InputException {
		int position = template.getFieldIndex(fieldName);
		return addFieldValue(position, fieldData);
	}
	
	@Override
	public Field addFieldValue(int position, int fieldData) throws InputException {
		if (null == recordData[position]) {
		recordData[position] = template.newField(position, fieldData);
		} else {
			recordData[position].addIntegerValue(fieldData);
		}
		return recordData[position];
	}
	
	@Override
	public Field addFieldValue(String fieldName, int fieldData) throws InputException { 
		int position = template.getFieldIndex(fieldName);
		return addFieldValue(position, fieldData);
	}
	
	@Override
	public Field addFieldValuePair(int fieldNum, String fieldValue, String extraValue)
	throws InputException {
		if (null == recordData[fieldNum]) {
			recordData[fieldNum] = template.newField(fieldNum);
		} 
		recordData[fieldNum].addValuePair(fieldValue, extraValue);
		return recordData[fieldNum];
	}

	@Override
	public Field addFieldValuePair(String fieldName, String value, String extraValue)
			throws InputException {
		int fieldNum = template.getFieldIndex(fieldName);
		return addFieldValuePair(fieldNum, value, extraValue);
	}

	@Override
	public Field getField(String fieldId) throws InputException {
		int i = template.getFieldIndex(fieldId);
		return getField(i);
	}
	
	@Override
	public Field getField(int fieldNum) throws InputException  {
		Field fld = recordData[fieldNum];
		if (isEditable()) {
			if (null == fld) {
				try {
					fld = template.newField(fieldNum);
					recordData[fieldNum] = fld;
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
			if (Group.NULL_GROUP == parentGroup) {
				fld = template.getDefaultField(fieldNum);
			} else {
				int parentRecordId = getParent(parentGroup);
				if (!RecordId.isNull(parentRecordId)) {
					Record parentRecord = template.records.getRecord(parentRecordId);
					if (null == parentRecord) {
						throw new InputException("Cannot locate parent record "+parentRecordId);
					}
					fld = parentRecord.getField(fieldNum);
					if (null == fld) {
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
	public Field getDefaultField(String fieldId) throws InputException {
		int i = template.getFieldIndex(fieldId);
		return getDefaultField(i);
	}

	@Override
	public FieldValue getFieldValue(String fieldId) throws InputException {
		int i = template.getFieldIndex(fieldId);
		return getFieldValue(i);
	}

	@Override
	public void removeField(int fieldNum) throws LibrisException {
		if (isEditable()) {
			recordData[fieldNum] = null;
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
		for (Field f: recordData) {
			if (null != f) {
				idList.add(f.getFieldId());
			}
		}
		String[] ids = new String[idList.size()];
		return idList.toArray(ids);
	}
	public FieldType getFieldType(String fid) {
		return template.getFieldType(fid);
	}
	@Override
	public void getKeywords(int[] fieldList, RecordKeywords keywordList) throws InputException {
		for (int fieldNum: fieldList) {
			Field fld = getField(fieldNum);
			if (null != fld) {
				String values = fld.getValuesAsString();
				if ((null != values) && !values.isEmpty()) {
					keywordList.addKeywords(Arrays.asList(values.split("\\W+")));
				}
			}
		}
	}
	private class FieldIterator implements Iterator<Field>, Iterable<Field> {
	
		int fieldIndex;
		public FieldIterator() {
			fieldIndex = 0;
		}
		@Override
		public boolean hasNext() {
			while ((fieldIndex < recordData.length) && (null == recordData[fieldIndex])) {
				++fieldIndex;
			}
			return (fieldIndex < recordData.length);
		}
	
		@Override
		public Field next() {
			return recordData[fieldIndex++];
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
		HashMap<String, String> attrs = mgr.parseOpenTag();
		this.attributes = new LibrisAttributes(attrs);
		setRecordId(RecordId.toId(attributes.get(XML_RECORD_ID_ATTR)));
		setName(attributes.get(XML_RECORD_NAME_ATTR));
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
		output.writeStartElement(XML_RECORD_TAG, attrs, false);
		if (null != affiliations) {
			for (GroupMember m: affiliations) {
				if (null != m) {
					m.toXml(output);
				}
			}
		}
		for (Field f: recordData) {
			if ((null != f) && !f.isEmpty()) {
				f.toXml(output);
			}
		}
		output.writeEndElement();
		output.flush();
	}

	@Override
	public String toString() {
		StringBuffer buff = new StringBuffer();
		buff.append(RecordId.toString(getRecordId())); 
		buff.append('\n');
		for (Field f:recordData) {
			if (null == f) {
				continue;
			}
			String fieldString = f.toString();
			buff.append(fieldString); 
			buff.append('\n');
		}
		return buff.toString();
	}
	
	public int getRecordId() {
		return id;
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String newName) throws InputException {
		if ((null != newName) && newName.isEmpty()) {
			this.name = null;
		} else {
			if (!validateRecordName(newName)) {
				throw new InputException("Record name "+newName+" is illegal");
			}
			this.name = newName;
		}
	}

	@Override
	public void setEditable(boolean newValue) {
		editable = newValue;
	}
	public boolean isEditable() {
		return editable;
	}
	@Override
	public boolean valuesEqual(Record comparand) {
		try {
			if (!getAttributes().equals(comparand.getAttributes())) {
				return false;
			}
		} catch (LibrisException e) {
			return false;
		}
		for (Field f: recordData) {
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
	public String generateTitle(String[] fieldIds) {
		StringBuffer buff = new StringBuffer("[");
		String idString = (NULL_ID == id)? "<unknown>": RecordId.toString(id);
		buff.append(idString);
		buff.append("]: ");
		if (null != name) {
			buff.append(name);
		} else {
			boolean firstField = true;
			for (String fieldId: fieldIds) {
				Field fld;
				try {
					fld = getField(fieldId);
				} catch (InputException e) {
					continue;
				}
				if (null == fld) {
					continue;
				}
				String value = fld.getValuesAsString();
				if (!firstField) {
					buff.append("; ");
				}
				firstField = false;
				buff.append(value);
			}
			if (0 == buff.length()) {
				buff.append("Untitled");
			}
		}
		return buff.toString();
	}
	
	@Override
	public int[] getAffiliates(int groupNum) throws InputException {
		int result[];
		if ((null != affiliations) && (null != affiliations[groupNum])) {
			result = affiliations[groupNum].getAffiliations();
		} else {
			result = GroupMember.getDummyAffiliations();
		}
		return result;
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
				for (Field fld: recordData) {
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
	public Record duplicate() throws DatabaseException, FieldDataException  {
		Record otherRec = new RecordInstance(template);
		otherRec.setEditable(true);
		for (int i = 0; i < recordData.length; ++i) {
			Field fld = recordData[i];
			if (null == fld) {
				continue;
			}
			Field otherField = fld.duplicate();
			otherRec.setField(i, otherField);
		}
		return otherRec;
	}

	public long getFilePosition() {
		return filePosition;
	}
	public void setFilePosition(long filePosition) {
		this.filePosition = filePosition;
	}
	public long getDataLength() {
		return dataLength;
	}
	public void setDataLength(long dataLength) {
		this.dataLength = dataLength;
	}
	@Override
	public int compareTo(Record comparand) {
		int result = Integer.signum(id - comparand.getRecordId());
		return result;
	}
	@Override
	public LibrisAttributes getAttributes() {
		if (null == attributes) {
			attributes = new LibrisAttributes();
		}
		return attributes;
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
	public short getNumAffiliatesAndParent(int groupNum) {
		short numAffiliates = 0;
		if (hasAffiliations() && (null != affiliations[groupNum])) {
			numAffiliates = (byte) affiliations[groupNum].getNumberOfValues();
		}
		return numAffiliates;
	}

	@Override
	public void addAffiliate(int groupNum, int affiliate) throws FieldDataException {
		if (NULL_ID == getParent(groupNum)) {
			setParent(groupNum, NULL_ID);
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
			return NULL_ID;
		} else if ((null == affiliations) || (null == affiliations[groupNum])) {
			return NULL_ID;
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
		if (null == affiliations[groupNum]) {
			GroupDefs grpDefs = template.getGroupDefs();
			affiliations[groupNum] = new GroupMember(grpDefs, grpDefs.getGroupDef(groupNum));
		}
	}

	@Override
	public void setRecordId(int recId) {
		id = recId;
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

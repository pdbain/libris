package org.lasalledebain.libris;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

import org.lasalledebain.libris.Field.FieldType;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.FieldDataException;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.exception.RecordDataException;
import org.lasalledebain.libris.field.GenericField;
import org.lasalledebain.libris.index.Group;
import org.lasalledebain.libris.index.GroupMember;
import org.lasalledebain.libris.xmlUtils.ElementManager;
import org.lasalledebain.libris.xmlUtils.ElementWriter;
import org.lasalledebain.libris.xmlUtils.LibrisAttributes;
import org.lasalledebain.libris.xmlUtils.LibrisXMLConstants;

public class RecordInstance extends Record implements  LibrisXMLConstants {
	private RecordId id;

	Field[] recordData;
	RecordId groups[][];
	boolean editable = false;
	private RecordTemplate template;
	private long filePosition = -1;
	private long dataLength = -1;
	private LibrisAttributes attributes;

	public RecordInstance(RecordTemplate recordTemplate) {
		template = recordTemplate;
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
	
	public Field getField(String id) throws InputException {
		int i = template.getFieldIndex(id);
		Field f = recordData[i];
		if (isEditable() && (null == f)) {
			try {
				f = template.newField(i);
				recordData[i] = f;
			} catch (InputException e) {
				throw new FieldDataException("cannot create new "+id, e);
			}
		}
		return f;
	}
	


	@Override
	public Field getField(int fieldNum) throws InputException {
		Field f = recordData[fieldNum];
		if (null == f) {
			if (isEditable()) {
				try {
					f = template.newField(fieldNum);
					recordData[fieldNum] = f;
				} catch (InputException e) {
					throw new FieldDataException("cannot create new "+id, e);
				}
			}
		}
		return f;
	}
	
	@Override
	public Field getFieldWithInheritance(int fieldNum, RecordList records)
			throws LibrisException {
		Field fld = getField(fieldNum);
		if (null == fld) {
			int parentGroup = template.getInheritanceGroup(fieldNum);
			if (Group.NULL_GROUP == parentGroup) {
				fld = template.getDefaultField(fieldNum);
			} else {
				RecordId parentRecordId = getParent(parentGroup);
				if (!parentRecordId.isNull()) {
					Record parentRecord = records.getRecord(parentRecordId);
					fld = parentRecord.getField(fieldNum);
				} else {
					fld = template.getDefaultField(fieldNum);					
				}
			}
		}
		return fld;
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
		setRecordId(new RecordId(attributes.get(XML_RECORD_ID_ATTR)));
		while (mgr.hasNext()) {
			String nextId = mgr.getNextId();
			if (XML_MEMBER_TAG != nextId) {
				break;
			}
			ElementManager memberMgr = mgr.nextElement();
			GroupMember mem = new GroupMember(template.getGroupDefs());
			mem.fromXml(memberMgr);
			int groupNum = mem.getGroupNum();
			setParentId(groupNum, mem.getParent());
			for (RecordId id: mem.getAffiliations()) {
				addAffiliate(groupNum, id);
			}
		}
		while (mgr.hasNext()) {
			ElementManager fieldManager = mgr.nextElement();
			GenericField.fromXml(fieldManager, this);		
		}
		mgr.parseClosingTag();
	}
	
	@Override
	public void toXml(ElementWriter output) throws LibrisException {
		String recordNumberString = getRecordId().toString();
		LibrisAttributes attrs = new LibrisAttributes();
		attrs.setAttribute(XML_RECORD_ID_ATTR, recordNumberString);
		output.writeStartElement(XML_RECORD_TAG, attrs, false);
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
		buff.append(getRecordId()); buff.append('\n');
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
	
	public void setRecordId(RecordId newId) throws RecordDataException {
		this.id = newId;
	}
	@Override
	public void setRecordId(int recId) throws DatabaseException {
		try {
			setRecordId(new RecordId(recId));
		} catch (DatabaseException e) {
			throw new DatabaseException("could not create record ID "+recId, e);
		}
	}
	public RecordId getRecordId() {
		return id;
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
		StringBuffer buff = new StringBuffer();
		if (null != id) {
			buff.append(id.toString());
		}
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
			if (buff.length() != 0) {
				buff.append("; ");
			}
			buff.append(value);
		}
		if (0 == buff.length()) {
			buff.append("Untitled");
		}
		return buff.toString();
	}
	@Override
	public boolean equals(Object comparand) {
		if (!Record.class.isInstance(comparand)) {
			return false;
		} else {
			Record otherRec = (Record) comparand;
			if (otherRec.getFieldIds().length != getFieldIds().length) {
				return false;
			}
			for (Field fld: recordData) {
				try {
					if (null == fld) {
						continue;
					}
					String nextId = fld.getFieldId();
					Field otherField = otherRec.getField(nextId);
					if (null == otherField) {
						return false;
					}
					if (!fld.equals(otherField)) {
						return false;
					}
				} catch (InputException e) {
					return false;
				}
			}
			return true;
		}
	}
	
	
	@Override
	public Record duplicate() throws DatabaseException, FieldDataException  {
		RecordInstance otherRec = new RecordInstance(template);
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
		int result = id.compareTo(comparand.getRecordId());
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
	public void setParentId(int groupNum, RecordId parent) {
		addGroupMember(groupNum, parent, true);
	}
	@Override
	public void addAffiliate(int groupNum, RecordId affiliate) {
		addGroupMember(groupNum, affiliate, false);
	}
	@Override
	public Iterable<RecordId> getAffiliates(int groupNum) {	
		final Iterator<RecordId> affiliatesIterator = Arrays.asList(groups[groupNum]).iterator();
		return new Iterable<RecordId>() {
			@Override
			public Iterator<RecordId> iterator() {
				affiliatesIterator.next(); /* flush the parent */
				return affiliatesIterator;
			}		
		};
	}
	@Override
	public RecordId getParent(int groupNum) {
		if (groupNum < 0) {
			return RecordId.getNullId();
		} else if ((null == groups) || (null == groups[groupNum])) {
			return null;
		} else {
			return groups[groupNum][0];
		}
	}
	
	private void addGroupMember(int groupNum, RecordId parent,
			boolean addAtFront) {
		RecordId[] groupVec = ensureGroupCapacity(groupNum);
		if (null == groupVec[0]) {
			groupVec[0] = parent;
		} else {
			boolean found = false;
			for (int i = 0; i < groupVec.length; ++i) {
				if (groupVec[i].equals(parent)) {
					RecordId temp = groupVec[0];
					groupVec[0] = groupVec[i];
					groupVec[i] = temp;
					found = true;
					break;
				}
			}
			if (!found) {
				RecordId temp[] = new RecordId[groupVec.length+1];
				if (addAtFront) {
					System.arraycopy(groupVec, 0, temp, 1, groupVec.length);
					temp[0] = parent;
				} else {
					System.arraycopy(groupVec, 0, temp, 0, groupVec.length);
					temp[groupVec.length] = parent;
				}
				groups[groupNum] = temp;
			}
		}
	}
	private RecordId[] ensureGroupCapacity(int groupNum) {
		if (null == groups) {
			groups = new RecordId[template.getNumGroups()][];
		}
		if (null == groups[groupNum]) {
			groups[groupNum] = new RecordId[1];
		};
		return groups[groupNum];
	}

}

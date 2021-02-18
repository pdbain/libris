package org.lasalledebain.libris;

import java.util.HashSet;
import java.util.List;

import org.lasalledebain.libris.Field.FieldType;
import org.lasalledebain.libris.exception.FieldDataException;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.field.FieldValue;
import org.lasalledebain.libris.index.GroupDefs;


public class RecordTemplate implements RecordFactory<DatabaseRecord> {
	/**
	 * Array of the field templates in the order listed in the schema
	 */
	// TODO change fieldMasters and defaultFields to arrayList
	FieldTemplate[] fieldMasters;

	/**
	 * read-only fields with default values
	 */
	Field[] defaultFields;
	/**
	 * lists the fields already added
	 */
	HashSet <String> existingFieldIds;
	private int numFields = 0;
	private int numGroups = 0;
	// TODO test setting, getting fields by index, ID
	// TODO test duplicate IDs
	private Schema dbSchema;
	private GroupDefs grpDefs;
	public RecordList<DatabaseRecord> records;

	public GroupDefs getGroupDefs() {
		return grpDefs;
	}

	public int getInheritanceGroup(int fieldNum) {
		return fieldMasters[fieldNum].getInheritanceGroup();
	}

	public int getGroupNum(String groupName) {
		int groupNum = grpDefs.groupIdToNum(groupName);
		return groupNum;
	}
	private RecordTemplate(Schema s, int initialNumFields) {
		this.numFields = 0;
		dbSchema = s;
		fieldMasters = new FieldTemplate[initialNumFields];
		defaultFields = new Field[initialNumFields];
		existingFieldIds = new HashSet<String>(initialNumFields);
		grpDefs = dbSchema.getGroupDefs();
		numGroups = (null == grpDefs) ? 0 : grpDefs.getNumGroups();
	}

	public static RecordTemplate templateFactory(Schema s) throws InputException {
		return templateFactory(s, null);
	}
	public static RecordTemplate templateFactory(Schema s, RecordList<DatabaseRecord> recs) throws InputException {
		String[] fieldIds = s.getFieldIds();
		RecordTemplate recTemplate = new RecordTemplate(s, fieldIds.length);
		for (String id: fieldIds) {
			recTemplate.addField(id);
		}
		recTemplate.records = recs;
		return recTemplate;
	}
	private void addField(String fieldId) throws InputException {
		if (numFields >= fieldMasters.length) {
			FieldTemplate[] newFtList = new FieldTemplate[numFields+1];
			Field[] newDefaultFields = new Field[numFields+1];
			System.arraycopy(fieldMasters, 0, newFtList, 0, fieldMasters.length);
			System.arraycopy(defaultFields, 0, newDefaultFields, 0, defaultFields.length);
			fieldMasters = newFtList;
		}
		if (existingFieldIds.contains(fieldId)) {
			throw new InputException("duplicate field "+fieldId);
		} else {
			FieldTemplate f = dbSchema.getFieldTemplate(fieldId);
			if (null == f) {
				throw new InputException("field "+fieldId+" not defined in schema");
			}
			fieldMasters[numFields] = f;
			Integer fieldIndex = getFieldIndex(fieldId);
			String defaultData = fieldMasters[fieldIndex].getDefaultData();
			if ((null != defaultData) && !defaultData.isEmpty()) {
				defaultFields[fieldIndex] = fieldMasters[fieldIndex].newField(defaultData);
			}
			existingFieldIds.add(fieldId);
		}
		++numFields;
	}

	public DatabaseRecord makeRecord() {
		return new DatabaseRecord(this);
	}

	@Override
	public DatabaseRecord makeRecord(boolean editable) {
		DatabaseRecord rec = makeRecord();
		rec.setEditable(editable);
		return rec;
	}

	@Override
	public DatabaseRecord makeRecordUnchecked(boolean editable) {
		DatabaseRecord rec = makeRecord();
		rec.setEditable(editable);
		return rec;
	}

	public Field newField(int fieldNum) throws InputException {
		return fieldMasters[fieldNum].newField();
	}

	public Field newField(int fieldNum, FieldValue fieldData) throws FieldDataException {
		return fieldMasters[fieldNum].newField(fieldData);
	}

	public Field newField(int i, String fieldData) throws InputException {
		Field f = fieldMasters[i].newField(fieldData);
		return f;
	}

	public Field newField(int i, String mainData, String extraData) throws InputException {
		Field f = fieldMasters[i].newField(mainData, extraData);
		return f;
	}

	public Field newField(int i, int mainData, String extraData) throws InputException {
		Field f = fieldMasters[i].newField(mainData, extraData);
		return f;
	}

	public Field newField(int position, int fieldData) throws InputException {
		Field f = fieldMasters[position].newField(fieldData);
		return f;
	}

	public Integer getFieldIndex(String id) throws FieldDataException {
		int i = dbSchema.getFieldNum(id);
		if (i < 0) {
			throw new FieldDataException("field "+id+" not defined");
		}
		return i;
	}

	public int getNumFields() {
		return numFields;
	}
	public int getNumGroups() {
		return numGroups;
	}

	public FieldType getFieldType(String fid) {
		return dbSchema.getFieldType(fid);
	}

	public String[] getFieldIds() {
		return dbSchema.getFieldIds();
	}

	public String getDefaultValue(int i) {
		return fieldMasters[i].getDefaultData();
	}

	public List<FieldValue> getFieldLegalValues(int i) {
		 return fieldMasters[i].getLegalValues();
	}

	public Field getDefaultField(int fieldId) {
		return defaultFields[fieldId];
	}
}

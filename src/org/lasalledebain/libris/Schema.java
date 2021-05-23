package org.lasalledebain.libris;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;

import org.lasalledebain.libris.Field.FieldType;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.index.GroupDef;
import org.lasalledebain.libris.index.GroupDefs;
import org.lasalledebain.libris.index.IndexDef;
import org.lasalledebain.libris.index.IndexDefs;
import org.lasalledebain.libris.index.IndexField;
import org.lasalledebain.libris.xmlUtils.ElementWriter;
import org.lasalledebain.libris.xmlUtils.LibrisAttributes;
import org.lasalledebain.libris.xmlUtils.LibrisXMLConstants;

public abstract class Schema implements LibrisXMLConstants, XMLElement {
	protected static final IndexField[] emptyIndexFieldList = new IndexField[0];
	public static String currentVersion = "1.0";
	protected TreeMap<String, EnumFieldChoices> enumSets;
	protected ArrayList<FieldTemplate> fieldList;
	HashMap<String, Integer> fieldNumById;
	ArrayList<String> fieldIds;
	ArrayList<String> fieldTitles;
	String fieldIdArray[];
	String fieldTitleArray[];
	protected GroupDefs myGroupDefs;
	protected IndexDefs myIndexDefs;
	protected LibrisAttributes xmlAttributes;

	public Schema() {
		enumSets = new TreeMap<String, EnumFieldChoices>();
		fieldList = new ArrayList<FieldTemplate>();
		fieldNumById = new HashMap<String, Integer>();
		fieldIds = new ArrayList<String>();
		fieldTitles = new ArrayList<String>();
	}
	
	public GroupDefs getGroupDefs() {
		return myGroupDefs;
	}

	public void toXml(ElementWriter xmlWriter) throws LibrisException {
		xmlWriter.writeStartElement(XML_SCHEMA_TAG);
		myGroupDefs.toXml(xmlWriter);
		xmlWriter.writeStartElement(XML_FIELDDEFS_TAG);
		for (String enumSetName: enumSets.navigableKeySet()) {
			EnumFieldChoices es = enumSets.get(enumSetName);
			es.toXml(xmlWriter);
		}

		for (FieldTemplate recField: fieldList) {
			if (FieldType.T_FIELD_AFFILIATES != recField.getFtype()) {
				recField.toXml(xmlWriter);
			}
		}
		xmlWriter.writeEndElement(); /* fielddefs */
		myIndexDefs.toXml(xmlWriter);
		xmlWriter.writeEndElement(); /* schema */
	}

	public LibrisAttributes getAttributes() {
		return xmlAttributes;
	}


	protected EnumFieldChoices addEnumSet(String enumSetId,
			EnumFieldChoices enumSet) {
		return enumSets.put(enumSetId, enumSet);
	}

	public int addField(FieldTemplate theField) {
		String fieldId = theField.getFieldId();
		fieldIds.add(fieldId);
		fieldTitles.add(theField.getFieldTitle());
		fieldNumById.put(fieldId, (fieldIds.size()-1));
		final int index = fieldIds.size()-1;
		theField.setFieldNum(index);
		fieldList.add(index, theField);
		return index;
	}

	public EnumFieldChoices getEnumSet(String id) {
		return enumSets.get(id);
	}

	public int getNumFields() {
		return fieldIds.size();
	}

	public int getNumGroups() {
		return myGroupDefs.getNumGroups();
	}

	public String getFieldId(int fieldNum) {
		return fieldList.get(fieldNum).fieldId;
	}
	public String[] getFieldIds() {
		if (null == fieldIdArray) {
			fieldIdArray = new String[getNumFields()];
			fieldIds.toArray(fieldIdArray);
		}
		return fieldIdArray;
	}
	
	public IndexField[] getIndexFields(String indexId) {
		final IndexDef index = myIndexDefs.getIndex(indexId);
		return Objects.isNull(index)? emptyIndexFieldList: index.getFieldList();
	}

	public void setIndexFields(String indexId, int[] theFields) {
		final IndexDef index = new IndexDef(this, indexId, theFields);
		myIndexDefs.addIndexDef(index);
	}

	public String[] getFieldTitles() {
		if (null == fieldTitleArray) {
			fieldTitleArray = new String[fieldTitles.size()];
			fieldTitles.toArray(fieldTitleArray);
		}
		return fieldTitleArray;
	}

	public String getFieldTitle(int fieldNum) {
		return fieldTitles.get(fieldNum);
	}

	/**
	 * @param fieldId string name of the field
	 * @return index of field in the list of fields, or -1 if the field is not found.
	 */
	public int getFieldNum(String fieldId) {
		Integer fieldIndex = fieldNumById.get(fieldId);
		if (null == fieldIndex) {
			return LibrisConstants.NULL_FIELD_NUM;
		} else {
			return fieldIndex.intValue();
		}
	}

	public Iterable <FieldTemplate> getFields() {
		return fieldList;
	}
	
	public int[] getFieldsOfTypes(Set<FieldType> fieldTypes) {
		ArrayList<Integer> fieldIds = new ArrayList<Integer>();
		for (FieldTemplate t: fieldList) {
			if (fieldTypes.contains(t.getFtype())) {
				fieldIds.add(t.getFieldNum());
			}
		}
		return fieldIds.stream().mapToInt(Integer::intValue).toArray();
	}

	public FieldTemplate getFieldTemplate(int fieldNum) {
		return fieldList.get(fieldNum);
	}

	public FieldTemplate getFieldTemplate(String id) {
		return fieldList.get(fieldNumById.get(id));
	}

	public FieldType getFieldType(String i) {
		Integer fieldNum = fieldNumById.get(i);
		FieldTemplate f = fieldList.get(fieldNum);
		FieldType t = f.getFtype();
		return t;
	}

	public FieldType getFieldType(int fieldNum) {
		FieldTemplate f = fieldList.get(fieldNum);
		return f.getFtype();
	}

	public GroupDef getGroupDef(String groupName) {
		return myGroupDefs.getGroupDef(groupName);
	}

	public String getGroupId(int groupNum) {
		return getFieldIds()[groupNum];
	}

	public Iterable<String> getGroupIds() {
		return myGroupDefs.getGroupIds();
	}

	@Override
	public boolean equals(Object comparand) {
		try {
			Schema otherSchema = (Schema) comparand;
			if (!enumSets.equals(otherSchema.enumSets)) {
				return false;
			} else if (!myGroupDefs.equals(otherSchema.myGroupDefs)) {
				return false;				
			} else if (!myIndexDefs.equals(otherSchema.myIndexDefs)) {
				return false;				
			} else {
				return fieldList.equals(otherSchema.fieldList);
			}
		} catch (ClassCastException e) {
			LibrisDatabase.log(Level.WARNING, "Incompatible comparand in "+getClass().getName()+".equals()", e);
			return false;
		}
	}

	public static String getXmlTag() {
		return XML_SCHEMA_TAG;
	}

	public String getElementTag() {
		return getXmlTag();
	}
}

package org.lasalledebain.libris;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.TreeMap;
import java.util.logging.Level;

import javax.xml.namespace.QName;

import org.lasalledebain.libris.Field.FieldType;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.exception.XmlException;
import org.lasalledebain.libris.index.GroupDef;
import org.lasalledebain.libris.index.GroupDefs;
import org.lasalledebain.libris.index.IndexDef;
import org.lasalledebain.libris.index.IndexDefs;
import org.lasalledebain.libris.index.IndexField;
import org.lasalledebain.libris.xmlUtils.ElementManager;
import org.lasalledebain.libris.xmlUtils.ElementReader;
import org.lasalledebain.libris.xmlUtils.ElementWriter;
import org.lasalledebain.libris.xmlUtils.LibrisXMLConstants;
import org.lasalledebain.libris.xmlUtils.XmlShapes;
import org.lasalledebain.libris.xmlUtils.XmlShapes.SHAPE_LIST;

public class Schema implements LibrisXMLConstants {
	protected static final IndexField[] emptyIndexFieldList = new IndexField[0];
	public Schema() {
		enumSets = new TreeMap<String, EnumFieldChoices>();
		fieldList = new ArrayList<FieldTemplate>();
		fieldNumById = new HashMap<String, Short>();
		fieldIds = new ArrayList<String>();
		fieldTitles = new ArrayList<String>();
	}

	public GroupDefs getGroupDefs() {
		return myGroupDefs;
	}

	TreeMap<String, EnumFieldChoices> enumSets;
	ArrayList<FieldTemplate> fieldList;
	HashMap <String, Short> fieldNumById;
	ArrayList<String> fieldIds;
	ArrayList<String> fieldTitles;
	String fieldIdArray[];
	String fieldTitleArray[];
	private LibrisDatabase database;
	private GroupDefs myGroupDefs;
	private IndexDefs myIndexDefs;

	public void fromXml(ElementReader xmlReader) throws DatabaseException, InputException {
		ElementManager schemaManager = new ElementManager(xmlReader, new QName(LibrisXMLConstants.XML_SCHEMA_TAG),
				new XmlShapes(SHAPE_LIST.DATABASE_SHAPES));
		fromXml(schemaManager);
	}

	public void fromXml(ElementManager schemaManager) throws DatabaseException, InputException
	{
		schemaManager.parseOpenTag(getXmlTag());
		if (!schemaManager.hasNext()) {
			throw new XmlException(schemaManager, "<schema> cannot be empty");
		}
		ElementManager groupDefsManager = schemaManager.nextElement(XML_GROUPDEFS_TAG);
		myGroupDefs = new GroupDefs();
		myGroupDefs.fromXml(groupDefsManager);
		for (GroupDef gd: myGroupDefs) {
			addField(gd);
		}

		ElementManager fieldDefsManager = schemaManager.nextElement(XML_FIELDDEFS_TAG);
		parseFieldDefs(fieldDefsManager);

		ElementManager indexDefsManager = schemaManager.nextElement(XML_INDEXDEFS_TAG);
		myIndexDefs = new IndexDefs(this);
		myIndexDefs.fromXml(indexDefsManager);

		schemaManager.parseClosingTag();
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

	private void parseFieldDefs(ElementManager fieldDefsManager)
	throws DatabaseException, InputException {
		fieldDefsManager.parseOpenTag();
		while (fieldDefsManager.hasNext()) {
			String nextId;
			nextId = fieldDefsManager.getNextId();
			if (nextId.equals(EnumFieldChoices.getXmlTag())) {
				ElementManager enumSetManager = fieldDefsManager.nextElement();
				EnumFieldChoices c = EnumFieldChoices.fieldChoicesFactory(enumSetManager);
				String cId = c.getId();
				if (enumSets.containsKey(cId)) {
					throw new XmlException(enumSetManager, "duplicate enumset "+cId);
				}
				addEnumSet(cId, c);					
			} else {
				break;
			}
		}
		while (fieldDefsManager.hasNext()) {
			String nextId;
			nextId = fieldDefsManager.getNextId();
			if (nextId.equals(LibrisXMLConstants.XML_FIELDDEF_TAG)) {
				ElementManager fieldDefManager = fieldDefsManager.nextElement();
				FieldTemplate f = new FieldTemplate(this);
				f.fromXml(fieldDefManager);
				String fId = f.getFieldId();
				if (fieldNumById.containsKey(fId)) {
					throw new XmlException(fieldDefManager, "duplicate field "+fId);
				}
				addField(f);
			} else {
				throw new XmlException("Unexpected tag "+nextId+" in "+XML_FIELDDEFS_TAG+" section");
			}
		}
		fieldDefsManager.parseClosingTag();
	}

	private EnumFieldChoices addEnumSet(String enumSetId,
			EnumFieldChoices enumSet) {
		return enumSets.put(enumSetId, enumSet);
	}

	public void addField(FieldTemplate field) {
		String fieldId = field.getFieldId();
		fieldIds.add(fieldId);
		fieldTitles.add(field.getFieldTitle());
		fieldNumById.put(fieldId, (short) (fieldIds.size()-1));
		fieldList.add((fieldIds.size()-1), field);
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

	public String[] getFieldTitles() {
		if (null == fieldTitleArray) {
			fieldTitleArray = new String[fieldTitles.size()];
			fieldTitles.toArray(fieldTitleArray);
		}
		return fieldTitleArray;
	}


	/**
	 * @param fieldId string name of the field
	 * @return index of field in the list of fields, or -1 if the field is not found.
	 */
	public short getFieldNum(String fieldId) {
		Short fieldIndex = fieldNumById.get(fieldId);
		if (null == fieldIndex) {
			return LibrisConstants.NULL_FIELD_NUM;
		} else {
			return fieldIndex.shortValue();
		}
	}

	public Iterable <FieldTemplate> getFields() {
		return fieldList;
	}

	public FieldTemplate getFieldTemplate(int fieldNum) {
		return fieldList.get(fieldNum);
	}

	public FieldTemplate getFieldTemplate(String id) {
		return fieldList.get(fieldNumById.get(id));
	}

	public FieldType getFieldType(String i) {
		Short fieldNum = fieldNumById.get(i);
		FieldTemplate f = fieldList.get(fieldNum);
		FieldType t = f.getFtype();
		return t;
	}

	public FieldType getFieldType(short fieldNum) {
		FieldTemplate f = fieldList.get(fieldNum);
		return f.getFtype();
	}

	public GroupDef getGroupDef(String groupName) {
		return myGroupDefs.getGroupDef(groupName);
	}

	public String getGroupId(int groupNum) {
		return fieldIdArray[groupNum];
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
			database.log(Level.WARNING, "Incompatible comparand in "+getClass().getName()+".equals()", e);
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

package org.lasalledebain.libris;

import java.util.HashMap;

import org.lasalledebain.libris.Field.FieldType;
import org.lasalledebain.libris.exception.FieldDataException;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.XmlException;
import org.lasalledebain.libris.field.BooleanField;
import org.lasalledebain.libris.field.EnumField;
import org.lasalledebain.libris.field.GenericField;
import org.lasalledebain.libris.field.IntegerField;
import org.lasalledebain.libris.field.PairField;
import org.lasalledebain.libris.field.StringField;
import org.lasalledebain.libris.index.Group;
import org.lasalledebain.libris.index.GroupDef;
import org.lasalledebain.libris.xmlUtils.ElementManager;
import org.lasalledebain.libris.xmlUtils.ElementWriter;
import org.lasalledebain.libris.xmlUtils.LibrisAttributes;
import org.lasalledebain.libris.xmlUtils.LibrisXMLConstants;


/* Create one instance for each of the field types.  Instances of the inner classes represents field instances in 
 * record instances.
 */

public class FieldMasterCopy implements XmlExportable {
	
	public int getInheritanceGroup() {
		return inheritanceGroup;
	}

	private static String typeName;
	private LibrisAttributes fieldAttributes;
	FieldFactory factory;
	private Field.FieldType ftype;
	private String fieldId;
	private String fieldTitle;
	private String defaultData = null;
	EnumFieldChoices legalValues; /* for enum fields */
	private String valueSeparator; /* separate value pairs */
	private boolean singleValue;
	private boolean restricted;
	private boolean required;
	private int inheritanceGroup = Group.NULL_GROUP;
	
	public FieldMasterCopy () {
		fieldAttributes = new LibrisAttributes();		
	}

	public FieldMasterCopy(String id, String title, FieldType ft) {
		this();
		initialize(id, title, ft);
		fieldAttributes.setAttribute(XML_FIELDDEF_ID_ATTR, id);
	}

	private void initialize(String id, String title, FieldType ft) {
		this.ftype = ft;
		this.factory = fieldClasses.get(ft);
		this.fieldId = id.intern();
		this.fieldTitle = title;
	}
	
	public void fromXml(Schema s, ElementManager fieldDefManager) throws InputException {
			HashMap<String, String> attributes = fieldDefManager.parseOpenTag();
			fieldAttributes = fieldDefManager.getElementAttributes();
	
			String id = attributes.get(XML_FIELDDEF_ID_ATTR);
	
			String title = attributes.get(XML_FIELDDEF_TITLE_ATTR);
			valueSeparator = attributes.get(XML_VALUESEPARATOR_ATTR);
			valueSeparator = attributes.get(XML_VALUESEPARATOR_ATTR);
			String groupName = attributes.get(XML_FIELDDEF_INHERIT_ATTR);
			inheritanceGroup = Group.NULL_GROUP;
			if (null != groupName) {
				GroupDef grp = s.getGroupDef(groupName);
				if (null != grp) {
					inheritanceGroup = grp.getGroupNum();
				}
			}
	
			typeName = attributes.get(LibrisXMLConstants.XML_FIELDDEF_TYPE_TAG);
			FieldType ft = GenericField.getFieldType(typeName);
			if (null == ft) {
				throw new XmlException(fieldDefManager, typeName+" is not a recognized field type");
			}
	
			initialize(id, title, ft);
			if (ft.equals(FieldType.T_FIELD_ENUM)) {
				String tempEnum;
				EnumFieldChoices c = s.getEnumSet(tempEnum = attributes.get(LibrisXMLConstants.XML_FIELDDEF_ENUMSET_TAG));
				if (null == c) {
					throw new XmlException(fieldDefManager, tempEnum+" is not a recognized enumset");
				}
				this.legalValues = c;
			}
	
			String tempDefault = attributes.get(LibrisXMLConstants.XML_FIELDDEF_DEFAULT_VALUE_ATTR);
			if (!tempDefault.isEmpty()) {
				this.defaultData = tempDefault;
			}
			restricted = Boolean.parseBoolean(attributes.get(LibrisXMLConstants.XML_FIELDDEF_RESTRICTED_ATTR));
			singleValue = Boolean.parseBoolean(attributes.get(LibrisXMLConstants.XML_FIELDDEF_SINGLEVALUE_ATTR));
	}

	@Override
	public void toXml(ElementWriter xmlWriter) throws XmlException {
		xmlWriter.writeStartElement(XML_FIELDDEF_TAG, getAttributes(), true);
	}

	@Override
	public LibrisAttributes getAttributes() {
		return fieldAttributes;
	}

	public Field newField() throws InputException {
		Field temp = factory.newField(this);
		return temp;
	}

	public Field newField(String fieldData) throws InputException {
		Field f = null;
		if ((null != valueSeparator) && (valueSeparator.length() > 0) && fieldData.contains(valueSeparator)) {
			String[] valueStrings = fieldData.split(valueSeparator);
			if (valueStrings.length > 2) {
				throw new FieldDataException("more than two field values for "+fieldId+" found in \""
						+fieldData+"\" using separator "+valueSeparator);
			} else if (2 == valueStrings.length){
				f = newField(valueStrings[0], valueStrings[1]);
			}
		} else {
			f = newField();
			try {
				f.addValue(fieldData);
			} catch (FieldDataException e) {
				throw new InputException("Field "+getFieldId(), e);
			}
		}
		return f;
	}

	public Field newField(String mainData, String extraData) throws InputException {
		Field f = newField();
		f.addValuePair(mainData, extraData);
		return f;
	}

	public Field newField(int mainData, String extraData) throws InputException {
		Field f = newField();
		f.addValuePair(mainData, extraData);
		return f;
	}

	public Field newField(int fieldData) throws InputException {
		Field f = newField();
		f.addIntegerValue(fieldData);
		return f;
	}
	
	public EnumFieldChoices getEnumChoices() {
		return legalValues;
	}

	public void setEnumChoices(EnumFieldChoices enumChoices) {
		this.legalValues = enumChoices;
		fieldAttributes.setAttribute(XML_FIELDDEF_ENUMSET_TAG, enumChoices.getId());
	}

	public Field.FieldType getFtype() {
		return ftype;
	}

	public String getFieldId() {
		return fieldId;
	}

	public String getFieldTitle() {
		return fieldTitle.isEmpty()? getFieldId(): fieldTitle;
	}

	public String getDefaultData() {
		return defaultData;
	}

	public void setDefaultData(String defaultData) {
		this.defaultData = defaultData;
		fieldAttributes.setAttribute(XML_FIELDDEF_DEFAULT_VALUE_ATTR, defaultData);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FieldMasterCopy other = (FieldMasterCopy) obj;
		if (fieldId == null) {
			if (other.fieldId != null)
				return false;
		} else if (!fieldId.equals(other.fieldId))
			return false;
		return true;
	}
	public boolean isSingleValue() {
		return singleValue;
	}
	
	public boolean isRestricted() {
		return restricted;
	}
	
	public boolean isRequired() {
		return required;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fieldId == null) ? 0 : fieldId.hashCode());
		return result;
	}

	static private HashMap<Field.FieldType, FieldFactory> fieldClasses = initializeFieldClasses();
	private static HashMap<FieldType, FieldFactory> initializeFieldClasses() {
		HashMap<FieldType, FieldFactory> temp = new HashMap<Field.FieldType, FieldFactory>(7); /* 7 defined types */
		temp.put(Field.FieldType.T_FIELD_UNKNOWN, null);
		temp.put(Field.FieldType.T_FIELD_BOOLEAN, new BooleanFieldFactory());
		temp.put(Field.FieldType.T_FIELD_STRING, new StringFieldFactory());
		temp.put(Field.FieldType.T_FIELD_TEXT, new StringFieldFactory());
		temp.put(Field.FieldType.T_FIELD_INTEGER, new IntegerFieldFactory());
		temp.put(Field.FieldType.T_FIELD_PAIR, new PairFieldFactory());
		temp.put(Field.FieldType.T_FIELD_INDEXENTRY, new IndexEntryFieldFactory());
		temp.put(Field.FieldType.T_FIELD_ENUM, new EnumFieldFactory());
		return temp;
	}

	static public class StringFieldFactory implements FieldFactory {
		
		public GenericField newField(FieldMasterCopy template) {
			return new StringField(template);
		}
	
	}
	static class BooleanFieldFactory implements FieldFactory {
		public GenericField newField(FieldMasterCopy template) {
			return new BooleanField(template);
		}
	}
	static public class EnumFieldFactory implements FieldFactory {
	
		public GenericField newField(FieldMasterCopy template) {
			return new EnumField(template);
		}
	
	}

	static public class IndexEntryFieldFactory implements FieldFactory {
	
		@Override
		public
		GenericField newField(FieldMasterCopy template) {
			return new IntegerField(template);
		}
	
	}

	static public class PairFieldFactory implements FieldFactory {
	
		@Override
		public
		GenericField newField(FieldMasterCopy template) {
			return new PairField(template);
		}
	
	}

	static public class IntegerFieldFactory implements FieldFactory {
	
		@Override
		public
		GenericField newField(FieldMasterCopy template) {
			return new IntegerField(template);
		}
	
	}
}


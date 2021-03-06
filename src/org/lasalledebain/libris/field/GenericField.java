package org.lasalledebain.libris.field;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import javax.xml.stream.XMLStreamException;

import org.lasalledebain.libris.EnumFieldChoices;
import org.lasalledebain.libris.Field;
import org.lasalledebain.libris.FieldTemplate;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.exception.DatabaseError;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.FieldDataException;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.exception.XmlException;
import org.lasalledebain.libris.xmlUtils.ElementManager;
import org.lasalledebain.libris.xmlUtils.ElementShape;
import org.lasalledebain.libris.xmlUtils.ElementWriter;
import org.lasalledebain.libris.xmlUtils.LibrisAttributes;
import org.lasalledebain.libris.xmlUtils.LibrisXMLConstants;

public abstract class GenericField<FieldValueType extends FieldValue> implements Field {

	protected FieldTemplate template;
	/**
	 * Allows a field to have multiple values;
	 */
	private final ArrayList<FieldValueType> valueList;
	private static HashMap<String, FieldType> fieldTypeMap = GenericField.initializeTypeMap();
	
	public GenericField(FieldTemplate template) {
		this();
		this.template = template;
	}

	protected GenericField() {
		valueList = new ArrayList<FieldValueType>(0);
	}

	@Override
	public void addURLValue(URL value) throws FieldDataException {
		throw new FieldDataException("addURLValue(URL value) not defined for "+getFieldId());
	}

	@Override
	public void addValuePair(Integer value, String extraValue)
	throws FieldDataException {
		throw new FieldDataException("addValuePair(int, String) not defined for "+getFieldId());
	}

	@Override
	public boolean isText() {
		return false;
	}

	@Override
	public boolean isEmpty() {
		return valueList.isEmpty();
	}

	@Override
	public boolean isReadOnly() {
		return false;
	}

	@Override
	public boolean isRestricted() {
		return template.isRestricted();
	}

	@Override
	public String getValuesAsString() {
		return valuesToString(valueList);
	}

	public static String valuesToString(Iterable<? extends FieldValue> fieldValues) {
		String result = "";
		if (null != fieldValues) {
			StringBuffer buff = new StringBuffer();
			String separator = null;
			for (FieldValue v: fieldValues) {
				if (null != separator) {
					buff.append(separator);
				}
				separator = ", ";
				buff.append(v.getValueAsString());
			}
			result = buff.toString();
		}
		return result;
	}
	
	@Override
	public FieldValue removeValue() {
		FieldValue deletedValue = null;
		if (!valueList.isEmpty()) {
			deletedValue = valueList.get(0);
			valueList.remove(0);
		}
		return deletedValue;
	}

	public void deleteAllvalues() {
		valueList.clear();
	}

	public void setValues(Iterable<FieldValue> valueList) throws FieldDataException {
		deleteAllvalues();
		for (FieldValue v: valueList) {
			addFieldValue(v);
		}
	}

	public boolean isTrue() throws FieldDataException {
		throw new FieldDataException(getFieldId()+" is not a boolean field");
	}
	
	@Override
	public Optional<EnumField> asEnumField() {
		return EnumField.class.isInstance(this)? Optional.of(EnumField.class.cast(this)): Optional.empty();
	}

	@Override
	public FieldType getType() {
		return template.getFtype();
	}
	public static FieldType getFieldType(String typeName) {
		return fieldTypeMap.get(typeName);
	}
	public String getFieldId() {
		return template.getFieldId();
	}

	@Override
	public void addValue(FieldValue fieldData) throws FieldDataException {
		FieldValueType v = valueOf(fieldData);
		addFieldValueImpl(v);
	}

	public abstract FieldValueType valueOf(FieldValue original) throws FieldDataException;
	
	public void addValue(String fieldData) throws FieldDataException {

		final FieldValueType v = valueOf(fieldData);
		addFieldValueImpl(v);
			
	}

	@Override
	public void addValuePair(String value, String extraValue)
	throws FieldDataException {
		throw new FieldDataException("Multiple values not supported in "+getFieldId());
	}
	
	protected void addFieldValue(FieldValue v) throws FieldDataException {
		addFieldValueImpl(valueOf(v));
	}
	
	protected void addFieldValueImpl(FieldValueType v) throws FieldDataException {
		if (isSingleValue() && !isEmpty())  {
			throw new FieldDataException("cannot add "+v+" to "+getFieldId()+": that field does not allow multiple values");
		}
		
		valueList.add(v);
	}
	
	public void addNativeFieldValue(FieldValueType v) {
		valueList.add(v);
	}
	@Override
	public void changeValue(FieldValue newValue) throws FieldDataException {
		FieldValueType v = valueOf(newValue);
		if (valueList.isEmpty()) addFieldValueImpl(v);
		else valueList.set(0, v);
	}
	@Override
	/**
	 * changes removes the first value and replaces it with new data
	 */
	public void changeValue(String valueString) throws FieldDataException {
		changeValue(valueOf(valueString));
	}

	@Override
	public void changeValue(int value, String extraValue) throws FieldDataException {
		changeValue(valueOf(value, extraValue));
	}

	public Iterable<? extends FieldValueType> getFieldValues() {
		return getValueList();
	}
	
	public List<FieldValueType> getValueList() {
		return valueList;
	}

	@Override
	public Optional<? extends FieldValue> getFirstFieldValue() {
		return (isEmpty())? Optional.empty(): Optional.of(valueList.get(0));
	}

	public EnumFieldChoices getLegalValues() {
		return null;
	}

	public int getNumberOfValues() {
		return valueList.size();
	}
	@Override
	public boolean isSingleValue() {
		return template.isSingleValue();
	}
	private static HashMap<String, FieldType> initializeTypeMap() {
		HashMap<String, FieldType> tMap = new HashMap<String, FieldType>();
		tMap.put(XML_FIELDDEF_DEFAULT_VALUE_ATTR, FieldType.T_FIELD_STRING);
		tMap.put("boolean", FieldType.T_FIELD_BOOLEAN);
		tMap.put(XML_FIELDDEF_TYPE_STRING_VALUE, FieldType.T_FIELD_STRING);
		tMap.put("integer", FieldType.T_FIELD_INTEGER);
		tMap.put("valuepair", FieldType.T_FIELD_PAIR);
		tMap.put("indexentry", FieldType.T_FIELD_INDEXENTRY);
		tMap.put("enum", FieldType.T_FIELD_ENUM);
		tMap.put("location", FieldType.T_FIELD_PAIR);

		return tMap;
	}
	public boolean equals(Field comparand) {
		if (this == comparand)
			return true;
		if (comparand == null)
			return false;
		if (getClass() != comparand.getClass())
			return false;
		GenericField<?> other = (GenericField<?>) comparand;
		if (template == null) {
			if (other.template != null) {
				return false;
			}
		} else if (!template.equals(other.template))
			return false;
		if (isEmpty()) {
			if (!other.isEmpty()) {
				return false;
			}
		} else if (!valueList.equals(other.valueList)) {
			return false;
		}
		return true;
	}
	@Override
	public String toString() {
		return getFieldId()+": "+getValuesAsString();
	}
	@Override
	public void fromXml(ElementManager mgr) throws LibrisException {
		throw new DatabaseError("Use static method "+getClass().getName()+".fromXml()");
	}

	public static Field fromXml(ElementManager fieldManager, Record rec) throws XmlException, DatabaseException, InputException {
		LibrisAttributes attrs = fieldManager.parseOpenTag();
		String elementContents;
		if (fieldManager.hasContent()) {
			elementContents = fieldManager.getContent();
		} else {
			elementContents = "";
		}
		String valueAttr = attrs.get(LibrisXMLConstants.XML_ENUMCHOICE_VALUE_ATTR);
		String extraValueAttr = attrs.get(LibrisXMLConstants.XML_EXTRA_VALUE_ATTR);
		String fieldId = attrs.get(LibrisXMLConstants.XML_FIELD_ID_ATTR);
		Field f = null;
		if (!elementContents.isEmpty()) {
			if (!valueAttr.isEmpty()) {
				throw new XmlException(fieldManager.getStartEvent(), "field element must be empty if value attribute is used");
			}
			rec.addFieldValue(fieldId, elementContents);
		} else {
			if ((null != extraValueAttr) && !extraValueAttr.isEmpty()) {
				f = rec.addFieldValuePair(fieldId, valueAttr, extraValueAttr);
			} else if ((null != valueAttr) && !valueAttr.isEmpty()) {
				f = rec.addFieldValue(fieldId, valueAttr);
			} else {
				throw new InputException("value and extraValue empty: "+rec.getRecordId());
			}
		}
		fieldManager.parseClosingTag();
		return f;
	}


	@Override
	public void toXml(ElementWriter output) throws LibrisException {
		boolean storeValueInAttributes = ElementShape.storeValueInAttributes(getMyFieldType());

		if (isEmpty()) {
			return;
		}
		String fieldId = getFieldId();
		for (FieldValue v: valueList) {
			if (!v.isEmpty()) {
				try {
					if (storeValueInAttributes) {
						LibrisAttributes attributes = v.getValueAsAttributes();
						attributes.setAttribute(LibrisXMLConstants.XML_FIELD_ID_ATTR, fieldId);
						output.writeStartElement(LibrisXMLConstants.XML_FIELD_TAG, attributes, true);
					} else {
						LibrisAttributes attributes = new LibrisAttributes();
						attributes.setAttribute(LibrisXMLConstants.XML_FIELD_ID_ATTR, fieldId);
						output.writeStartElement(LibrisXMLConstants.XML_FIELD_TAG, attributes, false);
						output.writeContent(v.getValueAsString());
						output.writeEndElement();
					}
				} catch (XMLStreamException e) {
					throw new XmlException(e);
				}
			}
		}
	}
	@Override
	public LibrisAttributes getAttributes() throws XmlException {
		if (!ElementShape.storeValueInAttributes(getMyFieldType()) || isEmpty()) {
			return new LibrisAttributes();
		}

		String fieldId = getFieldId();
		LibrisAttributes firstAttr = null;
		LibrisAttributes lastAttr = null;
		for (FieldValue v: getFieldValues()) {
			LibrisAttributes attributes;
			try {
				attributes = v.getValueAsAttributes();
				attributes.setAttribute(LibrisXMLConstants.XML_FIELD_ID_ATTR, fieldId);
				if (null == firstAttr) {
					firstAttr = attributes;
				} else {
					lastAttr.setNext(attributes);
				}
				lastAttr = attributes;
			} catch (FieldDataException e) {
				throw new XmlException("error getting attributes for "+fieldId, e);
			}
		}
		return firstAttr;
	}

	@Override
	public String getElementTag() {
		return getXmlTag();
	}

	public static String getXmlTag() {
		return LibrisXMLConstants.XML_FIELD_TAG;
	}

	private FieldType getMyFieldType() {
		return template.getFtype();
	}
	
	protected abstract FieldValueType valueOf(String valueString) throws FieldDataException;

	protected abstract FieldValue valueOf(int value, String extraValue) throws FieldDataException;

	public void copyValues(GenericField<FieldValueType> fieldCopy) throws FieldDataException {
		fieldCopy.deleteAllvalues();
		for (FieldValue v: getFieldValues()) {
			fieldCopy.addFieldValue(v);
		}
	}
	@Override
	public Field getReadOnlyView() {
		return new ReadOnlyField<FieldValueType>(this);
	}
}
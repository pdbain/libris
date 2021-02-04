package org.lasalledebain.libris.field;

import java.net.URL;
import java.util.HashMap;
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
import org.lasalledebain.libris.ui.EmptyField;
import org.lasalledebain.libris.xmlUtils.ElementManager;
import org.lasalledebain.libris.xmlUtils.ElementShape;
import org.lasalledebain.libris.xmlUtils.ElementWriter;
import org.lasalledebain.libris.xmlUtils.LibrisAttributes;
import org.lasalledebain.libris.xmlUtils.LibrisXMLConstants;

public abstract class GenericField implements Field {

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
		if (null != values) {
			for (FieldValue v: values) {
				if (!v.isEmpty()) {
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public boolean isReadOnly() {
		return false;
	}

	@Override
	public boolean isRestricted() {
		return template.isRestricted();
	}

	protected FieldTemplate template;
	/**
	 * Allows a field to have multiple values;
	 */
	private FieldValue values = null;
	private static HashMap<String, FieldType> fieldTypeMap = GenericField.initializeTypeMap();
	@Override
	public String getValuesAsString() {
		FieldValue fieldValues = values;
		String result = valuesToString(fieldValues);
		return result;
	}

	public static String valuesToString(FieldValue fieldValues) {
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
	public FieldValue removeValue() {
		FieldValue deletedValue = values;
		if (null != values) {
			values = values.nextValue;
		}
		return deletedValue;
	}

	public void deleteAllvalues() {
		while (null != removeValue());
	}

	public void setValues(Iterable<FieldValue> valueList) throws FieldDataException {
		deleteAllvalues();
		for (FieldValue v: valueList) {
			addFieldValue(v);
		}
	}

	@Override
	public void setValues(FieldValue[] valueArray) throws FieldDataException {
		setValues(valueArray);
	}

	public GenericField(FieldTemplate template) {
		super();
		this.template = template;
	}

	protected GenericField() {
		super();
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
	public abstract void addValue(String data) throws FieldDataException;

	@Override
	public void addValuePair(String value, String extraValue)
	throws FieldDataException {
		throw new FieldDataException("Multiple values not supported in "+getFieldId());
	}
	protected void addFieldValue(FieldValue v) throws FieldDataException {
		if (null != v) {
			v.nextValue = null;
		}
		if (null == values) {
			values = v;
		} else if (isSingleValue()) {
			throw new FieldDataException("cannot add "+v+" to "+getFieldId()+": that field does not allow multiple values");
		} else {
			FieldValue current = values;
			while (null != current.nextValue) {
				current = current.nextValue;
			}
			current.nextValue = v;
		}
	}
	@Override
	public void changeValue(FieldValue newValue) throws FieldDataException {
		removeValue();
		addFieldValue(newValue);
	}
	@Override
	/**
	 * changes removes the first value and replaces it with new data
	 */
	public void changeValue(String newValue) throws FieldDataException {
		removeValue();
		addValue(newValue);
	}

	public Iterable<FieldValue> getFieldValues() {
		if (null == values) {
			return new EmptyField();
		} else {
			return values;
		}
	}

	public FieldValue getFirstFieldValue() {
		if (null == values) {
			return new FieldNullValue();
		} else {
			return values;
		}
	}

	public EnumFieldChoices getLegalValues() {
		return null;
	}

	public int getNumberOfValues() {
		if ((null == values) || (values.isEmpty())) {
			return 0;
		} else {
			return values.getNumberOfValues();
		}
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
		GenericField other = (GenericField) comparand;
		if (template == null) {
			if (other.template != null) {
				return false;
			}
		} else if (!template.equals(other.template))
			return false;
		if (values == null) {
			if (other.values != null) {
				return false;
			}
		} else if (!values.equals(other.values)) {
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
		throw new DatabaseError("Use staic method "+getClass().getName()+".fromXml()");
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

		if (null == values) {
			return;
		}
		String fieldId = getFieldId();
		for (FieldValue v: values) {
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
		if (!ElementShape.storeValueInAttributes(getMyFieldType()) || (null == values)) {
			return new LibrisAttributes();
		}

		String fieldId = getFieldId();
		LibrisAttributes firstAttr = null;
		LibrisAttributes lastAttr = null;
		for (FieldValue v: values) {
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

	public void copyValues(GenericField otherField) throws FieldDataException {
		if (null != values) {
			for (FieldValue v: values) {
				FieldValue otherValue = v.duplicate();
				otherField.addFieldValue(otherValue);
			}
		}
	}
	@Override
	public Field getReadOnlyView() {
		return new ReadOnlyField(this);
	}
}
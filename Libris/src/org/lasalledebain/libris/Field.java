package org.lasalledebain.libris;

import org.lasalledebain.libris.exception.FieldDataException;
import org.lasalledebain.libris.field.FieldValue;


public interface Field extends XmlExportable {
	public enum FieldType {T_FIELD_UNKNOWN, 
		T_FIELD_STRING, // short string
		T_FIELD_TEXT, // long string
		T_FIELD_BOOLEAN, T_FIELD_INTEGER, T_FIELD_INDEXENTRY, T_FIELD_ENUM, T_FIELD_PAIR // value pair
		;
	}
	public void addValue(String data) throws FieldDataException;
	public void addIntegerValue(int value) throws FieldDataException;
	public void addValuePair(String value, String extraValue) throws FieldDataException;
	public void addValuePair(Integer value, String extraValue) throws FieldDataException;
	public boolean isTrue()   throws FieldDataException;
	public String getFieldId();
	public FieldType getType();
	public int getNumberOfValues();
	public Iterable<FieldValue> getFieldValues();
	public String getValuesAsString();
	public FieldValue getFirstFieldValue();
	public FieldValue removeValue() throws FieldDataException;
	public void changeValue(String string) throws FieldDataException;
	public void changeValue(FieldValue fieldValue) throws FieldDataException;
	public boolean equals(Field comparand);
	public EnumFieldChoices getLegalValues();
	public boolean isRestricted();
	public boolean isSingleValue();
	public boolean isEmpty();
	public void setValues(FieldValue[] valueArray) throws FieldDataException;
	public void setValues(Iterable<FieldValue> values) throws FieldDataException;
	public Field duplicate() throws FieldDataException;
	public Field getReadOnlyView();
	public boolean isReadOnly();
}


package org.lasalledebain.libris;

import java.net.URL;
import java.util.Optional;

import org.lasalledebain.libris.exception.FieldDataException;
import org.lasalledebain.libris.field.EnumField;
import org.lasalledebain.libris.field.FieldValue;

public interface Field extends XMLElement {
	// TODO add T_FIELD_DATE
	public enum FieldType {T_FIELD_UNKNOWN, 
		T_FIELD_STRING, // short string
		T_FIELD_TEXT, // long string
		T_FIELD_BOOLEAN, T_FIELD_INTEGER, T_FIELD_INDEXENTRY, T_FIELD_ENUM, T_FIELD_PAIR, // value pair
		T_FIELD_AFFILIATES, T_FIELD_LOCATION;
	}
	public void addValue(FieldValue fieldData) throws FieldDataException;
	public void addValue(String data) throws FieldDataException;
	public void addIntegerValue(int value) throws FieldDataException;
	public void addValuePair(String value, String extraValue) throws FieldDataException;
	public void addValuePair(Integer value, String extraValue) throws FieldDataException;
	public void addURLValue(URL value) throws FieldDataException;
	public boolean isTrue()   throws FieldDataException;
	public String getFieldId();
	public FieldType getType();
	public boolean isText();
	public default boolean isEnum() {
		return false;		
	}
	public Optional<EnumField> asEnumField();
	public boolean isRestricted();
	public boolean isSingleValue();
	public boolean isEmpty();
	public int getNumberOfValues();
	public Iterable<FieldValue> getFieldValues();
	public String getValuesAsString();
	public Optional<FieldValue> getFirstFieldValue();
	public FieldValue removeValue() throws FieldDataException;
	public void changeValue(String string) throws FieldDataException;
	public void changeValue(FieldValue fieldValue) throws FieldDataException;
	public void changeValue(int value, String extraValue) throws FieldDataException;
	public boolean equals(Field comparand);
	public EnumFieldChoices getLegalValues();
	public void setValues(Iterable<FieldValue> values) throws FieldDataException;
	public Field duplicate() throws FieldDataException;
	public Field getReadOnlyView();
	public boolean isReadOnly();
}


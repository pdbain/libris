package org.lasalledebain.libris.field;

import org.lasalledebain.libris.LibrisConstants;
import org.lasalledebain.libris.exception.FieldDataException;

public class FieldBooleanValue extends FieldValue {

	boolean value;
	public FieldBooleanValue(String data) throws FieldDataException {
		value = Boolean.parseBoolean(data);
		if (!value && !data.equalsIgnoreCase(LibrisConstants.BOOLEAN_FALSE_STRING)) {
			throw new FieldDataException("\""+data+"\" is not a legal boolean value");
		}
	}

	public FieldBooleanValue(int data) throws FieldDataException {
		if (0 == data) {
			value = false;
		} else if (1 == data) {
			value = true;
		} else {
			throw new FieldDataException(data+" is not a legal boolean value");
		}
	}

	public FieldBooleanValue(boolean v) {
		value = v;
	}

	public String getValueAsString() {
		return Boolean.toString(value);
	}

	@Override
	public boolean isTrue() {
		return value;
	}

	@Override
	protected boolean singleValueEquals(FieldValue comparand) {
		FieldBooleanValue other = (FieldBooleanValue) comparand;
		boolean valueEquals = (value == other.value);
		return valueEquals;
	}

	@Override
	public int getValueAsInt() {
		return value? 1: 0; /* 1 == true, 0 == false */
	}

	@Override
	public FieldValue duplicate() {
		FieldBooleanValue v = new FieldBooleanValue(value);
		return v;
	}

}

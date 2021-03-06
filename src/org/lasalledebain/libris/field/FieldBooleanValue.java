package org.lasalledebain.libris.field;

import static java.util.Objects.isNull;

import java.util.List;

import org.lasalledebain.libris.LibrisConstants;
import org.lasalledebain.libris.exception.FieldDataException;

public class FieldBooleanValue extends FieldValue {

	private final boolean value;
	private static FieldBooleanValue trueValue, falseValue;
	
	public static FieldBooleanValue getTrueValue() {
		if (isNull(trueValue)) trueValue = new FieldBooleanValue(true);
		return trueValue;
	}

	public static FieldBooleanValue getFalseValue() {
		if (isNull(falseValue)) falseValue = new FieldBooleanValue(false);
		return falseValue;
	}

	public FieldBooleanValue(boolean v) {
		value = v;
	}

	public static FieldBooleanValue of(boolean data) {
		return data? getTrueValue(): getFalseValue();
	}

	public static FieldBooleanValue of(int data) throws FieldDataException {
		FieldBooleanValue result = null;
		if (0 == data) {
			result = getFalseValue();
		} else if (1 == data) {
			result = getTrueValue();
		} else {
			throw new FieldDataException(data+" is not a legal boolean value");
		}
		return result;
	}

	public static FieldBooleanValue of(String data) throws FieldDataException {
		boolean booleanValue = Boolean.parseBoolean(data);
		if (!booleanValue && !data.equalsIgnoreCase(LibrisConstants.BOOLEAN_FALSE_STRING)) {
			throw new FieldDataException("\""+data+"\" is not a legal boolean value");
		}
		FieldBooleanValue result = null;
		if (booleanValue) {
			result = getTrueValue();
		} else {
			result = getFalseValue();
		}
		return result;
	}

	public String getValueAsString() {
		return Boolean.toString(value);
	}

	@Override
	public boolean isTrue() {
		return value;
	}
	
	@Override
	protected boolean equals(FieldValue comparand) {
		FieldBooleanValue other = (FieldBooleanValue) comparand;
		boolean valueEquals = (value == other.value);
		return valueEquals;
	}

	@Override
	public int getValueAsInt() {
		return value? 1: 0; /* 1 == true, 0 == false */
	}

	public static List<FieldValue> getLegalValues() {
		return List.of(getFalseValue(), getTrueValue());
	}
}

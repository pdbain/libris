package org.lasalledebain.libris.field;

import java.util.ArrayList;
import java.util.List;

import org.lasalledebain.libris.LibrisConstants;
import org.lasalledebain.libris.exception.FieldDataException;
import static java.util.Objects.isNull;

public class FieldBooleanValue extends FieldValue {

	private final boolean value;
	private final static List<FieldValue> legalValues = new ArrayList<>(2);
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

	public static List<FieldValue> getLegalValues() {
		if (legalValues.size() == 0) {
			legalValues.add(getFalseValue());
			legalValues.add(getTrueValue());
		}
		return legalValues;
	}

}

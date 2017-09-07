package org.lasalledebain.libris.field;

import org.lasalledebain.libris.exception.FieldDataException;

public class FieldStringPairValue extends FieldValue {
	@Override
	public boolean isEmpty() {
		return (null == values[0]) || values[0].isEmpty();
	}
	String[] values;
	private String concatenator = "-";
	public FieldStringPairValue(String val0, String val1) {
		values = new String[2];
		values[0] = val0;
		values[1] = val1;
	}
	public FieldStringPairValue(String[] newValues) {
		values = newValues;
	}
	@Override
	public String getValueAsString() {
		return values[0]+concatenator +values[1];
	}
	@Override
	public String getExtraValueAsKey() {
		return values[1];
	}
	@Override
	public String getExtraValueAsString() {
		return values[1];
	}
	@Override
	public String getMainValueAsKey() throws FieldDataException {
		return values[0];
}
	@Override
	public String getMainValueAsString() throws FieldDataException {
		return values[0];
}
	@Override
	protected boolean singleValueEquals(FieldValue comparand) {
		FieldStringPairValue other = (FieldStringPairValue) comparand;
		boolean valueEquals = true;
		if (null != values[0]) {
			valueEquals &= values[0].equals(other.values[0]);
		} else {
			valueEquals &= (null == other.values[0]);
		}
		if (null != values[1]) {
			valueEquals &= values[1].equals(other.values[1]);
		} else {
			valueEquals &= (null == other.values[1]);
		}
		return valueEquals;
	}
	@Override
	public FieldValue duplicate() {
		String[] newValues = new String[values.length];
		int i = 0;
		for (String s: values) {
			newValues[i] = s;
			++i;
		}
		FieldStringPairValue v = new FieldStringPairValue(newValues);
		return v;
	}
}

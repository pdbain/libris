package org.lasalledebain.libris.field;

import org.lasalledebain.libris.exception.FieldDataException;
import static java.util.Objects.isNull;

public class FieldStringPairValue extends FieldValue {
	@Override
	public boolean isEmpty() {
		return (null == values[0]) || values[0].isEmpty();
	}
	private final String[] values;
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
	public String getMainValueAsString() {
		return values[0];
}
	@Override
	protected boolean equals(FieldValue comparand) {
		boolean result = false;
		if (comparand instanceof FieldStringPairValue) {
			if (isEmpty() && comparand.isEmpty()) {
				result = true;
			} else
				try {
					if (getMainValueAsString().equals(comparand.getMainValueAsString())) {
						String myExtra = getExtraValueAsString();
						String otherExtra = comparand.getExtraValueAsString();
						result = (isNull(myExtra) && isNull(otherExtra)) || myExtra.equals(otherExtra);
					}
				} catch (FieldDataException e) {
					return false;
				}
		}
		return result;
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

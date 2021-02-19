package org.lasalledebain.libris.field;

import org.lasalledebain.libris.exception.Assertion;
import org.lasalledebain.libris.exception.FieldDataException;
import org.lasalledebain.libris.exception.InputException;

import static java.util.Objects.isNull;

public class FieldStringPairValue extends FieldValue {
	@Override
	public boolean isEmpty() {
		return mainValue.isEmpty() && !hasExtraValue();
	}
	@Override
	public boolean hasExtraValue() {
		return extraPresent;
	}
	private final String mainValue, extraValue;
	private final boolean extraPresent;
	private String concatenator = "-";
	public FieldStringPairValue(String val0, String val1) throws InputException {
		Assertion.assertNotNullInputException("FieldStringPairValue main value is null", val0);
		mainValue = val0;
		Assertion.assertNotNullInputException("FieldStringPairValue extra value is null", val1);
		extraValue = val1;
		extraPresent = true;
	}
	
	public FieldStringPairValue(String val0) throws InputException {
		Assertion.assertNotNullInputException("FieldStringPairValue main value is null", val0);
		mainValue = val0;
		extraValue = null;
		extraPresent = false;
	}

	@Override
	public String getValueAsString() {
		return (extraPresent)? (mainValue + concatenator + extraValue): mainValue;
	}
	@Override
	public String getExtraValueAsKey() {
		return getExtraValueAsString();
	}
	@Override
	public String getExtraValueAsString() {
		return (extraPresent)? extraValue: "";
	}
	@Override
	public String getMainValueAsKey() throws FieldDataException {
		return mainValue;
}
	@Override
	public String getMainValueAsString() {
		return mainValue;
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
}

package org.lasalledebain.libris.field;

import org.lasalledebain.libris.exception.FieldDataException;

public class FieldSingleStringValue extends FieldValue {
	private final String value;
	@Override
	public boolean isEmpty() {
		return (null == value) || value.isEmpty();
	}
	public FieldSingleStringValue(String value) {
		this.value = value;
	}
	@Override
	public String getValueAsString() {
		return value;
	}
	@Override
	protected boolean equals(FieldValue comparand) {
		try {
			return (comparand instanceof FieldSingleStringValue) 
					&& getMainValueAsString().equals(comparand.getMainValueAsString());
		} catch (FieldDataException e) {
			return false;
		}
	}
}

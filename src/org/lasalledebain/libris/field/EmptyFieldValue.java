package org.lasalledebain.libris.field;
@Deprecated
public class EmptyFieldValue extends FieldValue {

	@Override
	public String getValueAsString() {
		return "";
	}

	@Override
	public boolean equals(FieldValue comparand) {
		return false;
	}

	@Override
	public boolean isTrue() {
		return false;
	}

	@Override
	public boolean isEmpty() {
		return true;
	}

}

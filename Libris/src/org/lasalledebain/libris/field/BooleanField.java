package org.lasalledebain.libris.field;

import org.lasalledebain.libris.Field;
import org.lasalledebain.libris.FieldMasterCopy;
import org.lasalledebain.libris.exception.FieldDataException;

public class BooleanField extends GenericField implements Field {
	public BooleanField(FieldMasterCopy template) {
		super(template);
	}
	@Override
	public void addValue(String data) throws FieldDataException {
		addFieldValue(new FieldBooleanValue(data));
	}
	@Override
	public boolean isTrue() {
		return getFirstFieldValue().isTrue();
	}
	@Override
	public boolean isSingleValue() {
		return true;
	}
	@Override
	public void addIntegerValue(int value) throws FieldDataException {
		addFieldValue(new FieldBooleanValue(value));
	}

	public Field duplicate() throws FieldDataException {
		BooleanField otherField = new BooleanField(template);
		copyValues(otherField);
		return otherField;
	}
}

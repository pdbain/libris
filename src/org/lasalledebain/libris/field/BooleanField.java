package org.lasalledebain.libris.field;

import org.lasalledebain.libris.Field;
import org.lasalledebain.libris.FieldTemplate;
import org.lasalledebain.libris.exception.FieldDataException;

public class BooleanField extends GenericField implements Field {
	public BooleanField(FieldTemplate template) {
		super(template);
	}
	@Override
	public void addValue(String data) throws FieldDataException {
		addFieldValue(FieldBooleanValue.of(data));
	}
	
	@Override
	public void addValueGeneral(FieldValue fieldData) throws FieldDataException {
		addValue(fieldData.toString());
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
		addFieldValue(FieldBooleanValue.of(value));
	}

	public Field duplicate() throws FieldDataException {
		BooleanField otherField = new BooleanField(template);
		copyValues(otherField);
		return otherField;
	}
	@Override
	protected boolean isValueCompatible(FieldValue fv) {
		return fv instanceof FieldBooleanValue;
	}
}

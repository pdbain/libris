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
		addFieldValue(valueOf(data));
	}
	protected FieldBooleanValue valueOf(String data) throws FieldDataException {
		return FieldBooleanValue.of(data);
	}
	
	@Override
	public void addValueGeneral(FieldValue fieldData) throws FieldDataException {
		addValue(fieldData.toString());
	}
	
	@Override
	public boolean isTrue() {
		var optV = getFirstFieldValue();
		return optV.isPresent() && optV.get().isTrue();
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
		BooleanField fieldCopy = new BooleanField(template);
		copyValues(fieldCopy);
		return fieldCopy;
	}
	@Override
	protected boolean isValueCompatible(FieldValue fv) {
		return fv instanceof FieldBooleanValue;
	}
	@Override
	protected FieldValue valueOf(int value, String extraValue) throws FieldDataException {
		throw new FieldDataException("addIntegerValue not defined for BooleanField");
	}
}

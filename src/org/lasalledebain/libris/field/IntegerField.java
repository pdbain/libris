package org.lasalledebain.libris.field;

import org.lasalledebain.libris.Field;
import org.lasalledebain.libris.FieldTemplate;
import org.lasalledebain.libris.exception.FieldDataException;

public class IntegerField extends GenericField<FieldIntValue> {
	public IntegerField(FieldTemplate template) {
		super(template);
	}

	@Override
	public void addValue(String data) throws FieldDataException {

		try {
			addFieldValue(valueOf(data));
		} catch (NumberFormatException e) {
			throw new FieldDataException("invalid data: "+data+" for integer field "+getFieldId());
		}
	}

	@Override
	public FieldIntValue valueOf(FieldValue original) throws FieldDataException {
		FieldIntValue result = (original instanceof FieldIntValue)? (FieldIntValue) original: valueOf(original.getMainValueAsKey());
		return result;
	}

	protected FieldIntValue valueOf(String data) throws FieldDataException {
		return new FieldIntValue(data);
	}

	@Override
	protected FieldValue valueOf(int value, String extraValue) throws FieldDataException {
		throw new FieldDataException("valueOf(int, String) not defined for IntegerField");
	}

	@Override
	public void addValueGeneral(FieldValue fieldData) throws FieldDataException {
		addValue(fieldData.getMainValueAsKey());		
	}

	public void addValue(FieldIntValue fieldData) throws FieldDataException {
		// TODO Auto-generated method stub
		super.addValue(fieldData);
	}

	@Override
	public void addIntegerValue(int data) throws FieldDataException {
		addFieldValue(new FieldIntValue(data));
	}

	@Override
	public Field duplicate() throws FieldDataException {
		IntegerField f = new IntegerField(template);
		copyValues(f);
		return f;
	}

	@Override
	protected boolean isValueCompatible(FieldValue fv) {
		return fv instanceof FieldIntValue;
	}

}


package org.lasalledebain.libris.field;

import org.lasalledebain.libris.Field;
import org.lasalledebain.libris.FieldTemplate;
import org.lasalledebain.libris.exception.FieldDataException;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.util.StringUtils;

public class PairField extends GenericField<FieldStringPairValue> implements Field {
	@Override
	public FieldStringPairValue valueOf(FieldValue original) throws FieldDataException {
		FieldStringPairValue result;
		try {
			result = (original instanceof FieldStringPairValue)? 
					(FieldStringPairValue) original: new FieldStringPairValue(original.getMainValueAsString(), original.getExtraValueAsString());
		} catch (InputException e) {
			throw new FieldDataException("error in field "+getFieldId(), e);
		}
		return result;
	}

	public PairField(FieldTemplate template) {
		super(template);
	}

	public void addValue(String value) throws FieldDataException {		
		addFieldValue(valueOf(value));
	}

	protected FieldStringPairValue valueOf(String value) throws FieldDataException {
		try {
			return new FieldStringPairValue(value);
		} catch (InputException e) {

			throw new FieldDataException("error in field "+getFieldId(), e);
		}
	}

	@Override
	protected FieldValue valueOf(int value, String extraValue) throws FieldDataException {
		throw new FieldDataException("valueOf(int, String) not defined for pairField");
	}

	@Override
	// TODO check type and create new object if necessary
	public void addValueGeneral(FieldValue fieldData) throws FieldDataException {
		addValuePair(fieldData.getMainValueAsString(), fieldData.getExtraValueAsString());		
	}

	@Override
	public void addValuePair(String mainValue, String extraValue) throws FieldDataException {
		FieldValue v;
		try {
			v = (StringUtils.isStringEmpty(extraValue))? valueOf(mainValue): new FieldStringPairValue(mainValue, extraValue);
		} catch (InputException e) {

			throw new FieldDataException("error in field "+getFieldId(), e);
		}
		addFieldValue(v);
	}

	@Override
	public void addIntegerValue(int value) throws FieldDataException {
		throw new FieldDataException("addIntegerValue not defined for PairField");
	}

	@Override
	public Field duplicate() throws FieldDataException {
		PairField f = new PairField(template);
		copyValues(f);
		return f;
	}

	@Override
	protected boolean isValueCompatible(FieldValue fv) {
		return (fv instanceof FieldSingleStringValue) || (fv instanceof FieldStringPairValue);
	}

}	

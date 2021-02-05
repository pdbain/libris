package org.lasalledebain.libris.field;

import org.lasalledebain.libris.Field;
import org.lasalledebain.libris.FieldTemplate;
import org.lasalledebain.libris.exception.FieldDataException;
import org.lasalledebain.libris.util.StringUtils;

public class PairField extends GenericField implements Field {
	public PairField(FieldTemplate template) {
		super(template);
	}

	public void addValue(String value) throws FieldDataException {		
		FieldValue v = new FieldSingleStringValue(value);
		addFieldValue(v);
	}

	@Override
	// TODO check type and create new object if necessary
	public void addValueGeneral(FieldValue fieldData) throws FieldDataException {
		addValuePair(fieldData.getMainValueAsString(), fieldData.getExtraValueAsString());		
	}

	@Override
	public void addValuePair(String mainValue, String extraValue)
	throws FieldDataException {
		FieldValue v = (StringUtils.isStringEmpty(extraValue))? new FieldSingleStringValue(mainValue): new FieldStringPairValue(mainValue, extraValue);
		addFieldValue(v);
	}

	@Override
	public void addIntegerValue(int value) throws FieldDataException {
		throw new FieldDataException("addIntegerValue not defined for pairField");
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

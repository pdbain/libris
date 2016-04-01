package org.lasalledebain.libris.field;

import org.lasalledebain.libris.Field;
import org.lasalledebain.libris.FieldMasterCopy;
import org.lasalledebain.libris.exception.FieldDataException;

public class PairField extends GenericField implements Field {
	public PairField(FieldMasterCopy template) {
		super(template);
	}

	public void addValue(String value) throws FieldDataException {		
		FieldValue v = new FieldSingleStringValue(value);
		addFieldValue(v);
	}

	@Override
	public void addValuePair(String mainValue, String extraValue)
	throws FieldDataException {
		FieldValue v;
		if ((null != extraValue) && (extraValue.length() > 0)) {
			v = new FieldStringPairValue(mainValue, extraValue);
		} else {
			v = new FieldSingleStringValue(mainValue);

		}
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

}	

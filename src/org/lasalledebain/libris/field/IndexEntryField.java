package org.lasalledebain.libris.field;

import org.lasalledebain.libris.Field;
import org.lasalledebain.libris.FieldTemplate;
import org.lasalledebain.libris.exception.FieldDataException;

public class IndexEntryField extends GenericField implements Field {
	private int value;

	public IndexEntryField(FieldTemplate template) {
		super(template);
	}

	@Override
	public void addValue(String data) throws FieldDataException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addIntegerValue(int data) throws FieldDataException {
		throw new FieldDataException("not implemented");
	}

	@Override
	public Field duplicate() throws FieldDataException {
		return new IndexEntryField(template);
	}

}


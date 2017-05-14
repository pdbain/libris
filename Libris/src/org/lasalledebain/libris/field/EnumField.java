package org.lasalledebain.libris.field;

import org.lasalledebain.libris.EnumFieldChoices;
import org.lasalledebain.libris.Field;
import org.lasalledebain.libris.FieldTemplate;
import org.lasalledebain.libris.exception.FieldDataException;

public class EnumField extends GenericField implements Field {
	public EnumField(FieldTemplate template) {
		super(template);
	}

	@Override
	public EnumFieldChoices getLegalValues() {
		EnumFieldChoices choices = template.getEnumChoices();
		return choices;
	}

	@Override
	public Field duplicate() throws FieldDataException {
		EnumField f = new EnumField(template);
		copyValues(f);
		return f;
	}

	@Override
	public void addIntegerValue(int data) throws FieldDataException {
		addFieldValue(new FieldEnumValue(template.getEnumChoices(), data));
	}

	@Override
	public void addValue(String data) throws FieldDataException {
		String iData = data.intern();
		addFieldValue(new FieldEnumValue(template.getEnumChoices(), iData));
	}

	@Override
	public void addValuePair(Integer value, String extraValue)
			throws FieldDataException {
		addFieldValue(new FieldEnumValue(template.getEnumChoices(), value, extraValue));
	}

	@Override
	public void addValuePair(String value, String extraValue)
			throws FieldDataException {
		if (value.isEmpty()) {
			addFieldValue(new FieldEnumValue(template.getEnumChoices(), -1, extraValue));
		} else if ((null == extraValue) || extraValue.isEmpty()) {
			addFieldValue(new FieldEnumValue(template.getEnumChoices(), value));
		} else {
			throw new FieldDataException("either value or extravalue field must be empty.\nfound "+"\""+value+"\",\""+value+"\"");
		}
	}
}


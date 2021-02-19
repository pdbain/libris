package org.lasalledebain.libris.field;

import org.lasalledebain.libris.EnumFieldChoices;
import org.lasalledebain.libris.Field;
import org.lasalledebain.libris.FieldTemplate;
import org.lasalledebain.libris.LibrisConstants;
import org.lasalledebain.libris.exception.DatabaseError;
import org.lasalledebain.libris.exception.FieldDataException;
import org.lasalledebain.libris.util.StringUtils;

public class EnumField extends GenericField<FieldEnumValue> implements Field {
	public EnumField(FieldTemplate template) {
		super(template);
	}

	@Override
	public EnumFieldChoices getLegalValues() {
		EnumFieldChoices choices = template.getEnumChoices();
		return choices;
	}

	@Override
	public boolean isEnum() {
		return true;
	}

	@Override
	public Field duplicate() throws FieldDataException {
		EnumField f = new EnumField(template);
		copyValues(f);
		return f;
	}

	@Override
	public void addIntegerValue(int choiceNum) throws FieldDataException {
		addFieldValue(template.getEnumChoices().of(choiceNum));
	}

	@Override
	public void addValue(String choiceName) throws FieldDataException {
		addFieldValue(valueOf(choiceName));
	}

	protected FieldEnumValue valueOf(String choiceName) throws FieldDataException {
		return template.getEnumChoices().of(choiceName);
	}

	protected FieldEnumValue valueOf(int value, String extraValue) {
		return new FieldEnumValue(template.getEnumChoices(), value, extraValue);
	}

	@Override
	public FieldEnumValue valueOf(FieldValue original) throws FieldDataException {
		return (original instanceof FieldEnumValue)? (FieldEnumValue) original: valueOf(original.getValueAsInt(), original.getExtraValueAsString());
	}

	@Override
	public void addValuePair(Integer value, String extraValue)
			throws FieldDataException {
		addFieldValue(valueOf(value, extraValue));
	}

	@Override
	public void addValuePair(String value, String extraValue)
			throws FieldDataException {
		if (StringUtils.isStringEmpty(value)) {
			addFieldValue(new FieldEnumValue(template.getEnumChoices(), LibrisConstants.ENUM_VALUE_OUT_OF_RANGE, extraValue));
		} else if (StringUtils.isStringEmpty(extraValue)) {
			addFieldValue(new FieldEnumValue(template.getEnumChoices(), value));
		} else {
			throw new FieldDataException("either value or extravalue field must be empty.\nfound "+"\""+value+"\",\""+value+"\"");
		}
	}
}


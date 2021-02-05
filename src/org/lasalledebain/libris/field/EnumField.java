package org.lasalledebain.libris.field;

import org.lasalledebain.libris.EnumFieldChoices;
import org.lasalledebain.libris.Field;
import org.lasalledebain.libris.FieldTemplate;
import org.lasalledebain.libris.exception.DatabaseError;
import org.lasalledebain.libris.exception.FieldDataException;
import org.lasalledebain.libris.util.StringUtils;

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
	public void addIntegerValue(int data) throws FieldDataException {
		addFieldValue(new FieldEnumValue(template.getEnumChoices(), data));
	}

	@Override
	public void addValue(String data) throws FieldDataException {
		String iData = data.intern();
		addFieldValue(new FieldEnumValue(template.getEnumChoices(), iData));
	}

	@Override
	public void addValueGeneral(FieldValue fieldData) throws FieldDataException {
		addValuePair(fieldData.getMainValueAsKey(), fieldData.getExtraValueAsString());
	}

	@Override
	public void addValuePair(Integer value, String extraValue)
			throws FieldDataException {
		addFieldValue(new FieldEnumValue(template.getEnumChoices(), value, extraValue));
	}

	@Override
	public void addValuePair(String value, String extraValue)
			throws FieldDataException {
		if (StringUtils.isStringEmpty(value)) {
			addFieldValue(new FieldEnumValue(template.getEnumChoices(), -1, extraValue));
		} else if (StringUtils.isStringEmpty(extraValue)) {
			addFieldValue(new FieldEnumValue(template.getEnumChoices(), value));
		} else {
			throw new FieldDataException("either value or extravalue field must be empty.\nfound "+"\""+value+"\",\""+value+"\"");
		}
	}

	public static EnumField of(Field f) {
		if (f instanceof EnumField) return (EnumField) f;
		else throw new DatabaseError("Field of type "+f.getClass().getName()+" is not compatible with EnumField");
	}

	@Override
	public FieldValue getFirstFieldValue() {
		// TODO Auto-generated method stub
		return super.getFirstFieldValue();
	}

	@Override
	protected boolean isValueCompatible(FieldValue fv) {
		return fv instanceof FieldEnumValue;
	}
}


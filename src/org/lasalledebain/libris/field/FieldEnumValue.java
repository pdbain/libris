package org.lasalledebain.libris.field;

import org.lasalledebain.libris.EnumFieldChoices;
import org.lasalledebain.libris.LibrisConstants;
import org.lasalledebain.libris.exception.FieldDataException;

public class FieldEnumValue extends FieldValue implements LibrisConstants {

	private EnumFieldChoices enumChoices;
	private final int choice;
	private final String extraValue;
	private final boolean extraPresent;

	public FieldEnumValue(EnumFieldChoices enumChoices, String data) throws FieldDataException {
		this.enumChoices = enumChoices;
		this.choice = enumChoices.indexFromId(data);
		extraValue = null;
		extraPresent = false;
	}

	public FieldEnumValue(EnumFieldChoices enumChoices, int data) throws FieldDataException {
		this.enumChoices = enumChoices;
		if ((data < 0) || (data > enumChoices.maxChoice())) {
			throw new FieldDataException(data+" is not a valid value for this field");
		} else {
			choice = data;
			extraValue = null;
			extraPresent = false;
		}
	}
	public FieldEnumValue(EnumFieldChoices legalValues, int currentValue,
			String selectedItem) {
		this.enumChoices = legalValues;
		choice = currentValue;
		extraValue = selectedItem;
		extraPresent = true;
	}

	@Override
	public String getValueAsString() {
		if (null == extraValue) {
			try {
				return enumChoices.getChoiceValue(choice);
			} catch (FieldDataException e) {
				return null;
			}
		} else {
			return extraValue;
		}
	}

	@Override
	public String toString() {
		return getValueAsString();
	}

	@Override
	public String getMainValueAsString() throws FieldDataException {
		if ((null == enumChoices) || (ENUM_VALUE_OUT_OF_RANGE == choice)) {
			return extraValue;
		} else {
			return enumChoices.getChoiceValue(choice);
		}
	}

	@Override
	public int getValueAsInt() {	
		return choice;
	}

	@Override
	public String getMainValueAsKey() throws FieldDataException {
		if (null == enumChoices) {
			return null;
		} else if (
				(EnumFieldChoices.INVALID_CHOICE == choice) 
				|| (enumChoices.maxChoice() < choice)
		) {

			return null;
		} else {
			return enumChoices.getChoiceId(choice);
		}
	}

	@Override
	public String getExtraValueAsString() {
		return extraValue;
	}

	@Override
	public String getExtraValueAsKey() {
		return extraValue;
	}

	@Override
	protected boolean equals(FieldValue comparand) {
		FieldEnumValue other = (FieldEnumValue) comparand;
		boolean valueEquals = (choice == other.choice);
		if (null != extraValue) {
			valueEquals &= extraValue.equals(other.extraValue);
		}
		return valueEquals;
	}

	@Override
	public boolean isEmpty() {
		return (EnumFieldChoices.INVALID_CHOICE == choice) &&((null == extraValue) || extraValue.isEmpty());
	}

	@Override
	public boolean hasExtraValue() {
		return extraPresent;
	}
}

package org.lasalledebain.libris.ui;

import java.util.HashMap;

import org.lasalledebain.libris.EnumFieldChoices;
import org.lasalledebain.libris.Field;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.FieldDataException;
import org.lasalledebain.libris.field.FieldValue;
public class GuiControlFactory<RecordType extends Record> {
	private final HashMap<String, GuiControlConstructor<RecordType>> controls;
	GuiControlFactory() {
		controls = initializeControlList();
	}

	HashMap<String, GuiControlConstructor<RecordType>> initializeControlList() {
		HashMap<String, GuiControlConstructor<RecordType>> map = new HashMap<String, GuiControlConstructor<RecordType>>(12);

		try {
			String cName = GuiConstants.GUI_TEXTBOX;
			map.put(cName, new TextboxConstructor<RecordType>());

			cName = GuiConstants.GUI_TEXTFIELD;
			map.put(cName, new TextfieldConstructor<RecordType>());

			cName = GuiConstants.GUI_PAIRFIELD;
			map.put(cName, new RangefieldConstructor<RecordType>());

			cName = GuiConstants.GUI_CHECKBOX;
			map.put(cName, new CheckboxConstructor<RecordType>());

			cName = GuiConstants.GUI_ENUMFIELD;
			map.put(cName, new GuiControlConstructor<RecordType>() {
				public GuiControl<RecordType> makeControl(String title, int height, int width, boolean editable) {	
					return new EnumField<RecordType>(height, width, editable);
				}		
			});

			cName = GuiConstants.GUI_LOCATIONFIELD;
			map.put(cName, new GuiControlConstructor<RecordType>() {
				public GuiControl<RecordType> makeControl(String title, int height, int width, boolean editable) {	
					return new LocationField<RecordType>(height, width, editable);
				}
			});

		} catch (SecurityException e) {
			e.printStackTrace();
		}
		return map;
	}
	
	MultipleValueUiField<RecordType> makeMultiControlField(LayoutField<RecordType> fieldPosn, Field recordField, ModificationTracker modTrk) throws DatabaseException {
		try {
			int numValues = recordField.getNumberOfValues();
			final GuiControlConstructor<RecordType> ctrlConst = fieldPosn.getControlContructor();
			MultipleValueUiField<RecordType> guiFld = new MultipleValueUiField<RecordType>(this, fieldPosn, ctrlConst.labelField(), 
					recordField, numValues, modTrk);
			if (0 == numValues) {
				guiFld.addControl(modTrk.isModifiable());
			} else {
				for (FieldValue v: recordField.getFieldValues()) {
					GuiControl<RecordType> control = guiFld.addControl(modTrk.isModifiable());
					control.setFieldValue(v);
				}
			}
			return guiFld;
		} catch (Exception e) {
			throw new DatabaseException(e);
		}
	}

	GuiControl<RecordType> newControl(LayoutField<RecordType> fieldPosn,
			Field recordField, ModificationTracker modTrk, boolean editable) throws FieldDataException {
		GuiControl<RecordType> control = fieldPosn.getControlContructor().makeControl(fieldPosn, modTrk, editable);
		EnumFieldChoices legalValues = recordField.getLegalValues();
		control.setLegalValues(legalValues);
		control.setRestricted(recordField.isRestricted());
		control.setSingleValue(recordField.isSingleValue());
		return control;
	}

	public GuiControlConstructor<RecordType> getControlConstructor(
			String controlType) {
		return controls.get(controlType);
	}

}

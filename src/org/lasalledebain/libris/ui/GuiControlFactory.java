package org.lasalledebain.libris.ui;

import java.util.HashMap;

import org.lasalledebain.libris.EnumFieldChoices;
import org.lasalledebain.libris.Field;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.FieldDataException;
import org.lasalledebain.libris.field.FieldValue;
public class GuiControlFactory {
	private final HashMap<String, GuiControlConstructor> controls;
	GuiControlFactory() {
		controls = initializeControlList();
	}

	HashMap<String, GuiControlConstructor> initializeControlList() {
		HashMap<String, GuiControlConstructor> map = new HashMap<String, GuiControlConstructor>(12);

		try {
			String cName = GuiConstants.GUI_TEXTBOX;
			map.put(cName, new TextboxConstructor());

			cName = GuiConstants.GUI_TEXTFIELD;
			map.put(cName, new TextfieldConstructor());

			cName = GuiConstants.GUI_PAIRFIELD;
			map.put(cName, new RangefieldConstructor());

			cName = GuiConstants.GUI_CHECKBOX;
			map.put(cName, new CheckboxConstructor());

			cName = GuiConstants.GUI_ENUMFIELD;
			map.put(cName, new GuiControlConstructor() {
				public GuiControl makeControl(String title, int height, int width, boolean editable) {	
					return new EnumField(height, width, editable);
				}		
			});

			cName = GuiConstants.GUI_LOCATIONFIELD;
			map.put(cName, new GuiControlConstructor() {
				public GuiControl makeControl(String title, int height, int width, boolean editable) {	
					return new LocationField(height, width, editable);
				}
			});

		} catch (SecurityException e) {
			e.printStackTrace();
		}
		return map;
	}
	
	MultipleValueUiField makeMultiControlField(LayoutField fieldPosn, Field recordField, ModificationTracker modTrk) throws DatabaseException {
		try {
			int numValues = recordField.getNumberOfValues();
			final GuiControlConstructor ctrlConst = fieldPosn.getControlContructor();
			MultipleValueUiField guiFld = new MultipleValueUiField(this, fieldPosn, ctrlConst.labelField(), 
					recordField, numValues, modTrk);
			if (0 == numValues) {
				guiFld.addControl(modTrk.isModifiable());
			} else {
				for (FieldValue v: recordField.getFieldValues()) {
					GuiControl control = guiFld.addControl(modTrk.isModifiable());
					control.setFieldValue(v);
				}
			}
			return guiFld;
		} catch (Exception e) {
			throw new DatabaseException(e);
		}
	}

	GuiControl newControl(LayoutField fieldPosn,
			Field recordField, ModificationTracker modTrk, boolean editable) throws FieldDataException {
		GuiControl control = fieldPosn.getControlContructor().makeControl(fieldPosn, modTrk, editable);
		EnumFieldChoices legalValues = recordField.getLegalValues();
		control.setLegalValues(legalValues);
		control.setRestricted(recordField.isRestricted());
		control.setSingleValue(recordField.isSingleValue());
		return control;
	}

	public GuiControlConstructor getControlConstructor(
			String controlType) {
		return controls.get(controlType);
	}

}

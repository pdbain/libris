package org.lasalledebain.libris.ui;

import java.util.HashMap;

import org.lasalledebain.libris.EnumFieldChoices;
import org.lasalledebain.libris.Field;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.FieldDataException;
import org.lasalledebain.libris.field.FieldValue;

public abstract class GuiControlFactory<RecordType extends Record> {
	static HashMap<String, ControlConstructor> controls = initializeControlList();
	public GuiControlFactory() {
	}
	
	synchronized static HashMap<String, ControlConstructor> initializeControlList() {
		HashMap<String, ControlConstructor> map = new HashMap<String, ControlConstructor>(12);

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
			map.put(cName, new ControlConstructor() {
				public GuiControl makeControl(String title, int height, int width, boolean editable) {	
					return new EnumField(height, width, editable);
				}		
			});

			cName = GuiConstants.GUI_LOCATIONFIELD;
			map.put(cName, new ControlConstructor() {
				public GuiControl makeControl(String title, int height, int width, boolean editable) {	
					return new LocationField(height, width, editable);
				}
			});

		} catch (SecurityException e) {
			e.printStackTrace();
		}
		return map;
	}
	
	static MultipleValueUiField makeMultiControlField(LayoutField fieldPosn, Field recordField, ModificationTracker modTrk) throws DatabaseException {
		try {
			int numValues = recordField.getNumberOfValues();
			final ControlConstructor ctrlConst = fieldPosn.getControlContructor();
			MultipleValueUiField guiFld = new MultipleValueUiField(fieldPosn, ctrlConst.labelField(), 
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

	static GuiControl newControl(LayoutField fieldPosn,
			Field recordField, ModificationTracker modTrk, boolean editable) throws FieldDataException {
		GuiControl control = fieldPosn.getControlContructor().makeControl(fieldPosn, modTrk, editable);
		EnumFieldChoices legalValues = recordField.getLegalValues();
		control.setLegalValues(legalValues);
		control.setRestricted(recordField.isRestricted());
		control.setSingleValue(recordField.isSingleValue());
		return control;
	}

	public static ControlConstructor getControlConstructor(
			String controlType) {
		return controls.get(controlType);
	}
	
	
	public static abstract class ControlConstructor {
		public abstract GuiControl makeControl(String title, int height, int width, boolean editable);

		public GuiControl makeControl(LayoutField fieldPosn, ModificationTracker modTrk, boolean editable) {
			GuiControl ctrl = makeControl(fieldPosn.getTitle(), fieldPosn.getHeight(), 
					fieldPosn.getWidth(), editable);
			ctrl.setFieldInfo(fieldPosn);
			ctrl.setModificationTracker(modTrk);
			return ctrl;
		}
		public boolean labelField() {
			return true;
		}
	}
	
	static class TextboxConstructor extends ControlConstructor {
		public GuiControl makeControl(String title, int height, int width, boolean editable) {		
			return new TextBox(height, width, editable);
		}		
	}
	
	static class TextfieldConstructor extends ControlConstructor {
		public GuiControl makeControl(String title, int height, int width, boolean editable) {		
			return new RecordTextField(height, width, editable);
		}		
	}
	
	static class RangefieldConstructor extends ControlConstructor {
		public GuiControl makeControl(String title, int height, int width, boolean editable) {		
			return new RangeField(height, width, editable);
		}		
	}
	
	static class CheckboxConstructor extends ControlConstructor {
		public GuiControl makeControl(String title, int height, int width, boolean editable) {	
			return new CheckBoxControl(title, height, width, editable);
		}		
		public boolean labelField() {
			return false;
		}
	}

	}

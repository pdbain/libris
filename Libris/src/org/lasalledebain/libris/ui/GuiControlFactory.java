package org.lasalledebain.libris.ui;

import java.util.HashMap;

import org.lasalledebain.libris.EnumFieldChoices;
import org.lasalledebain.libris.Field;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.FieldDataException;
import org.lasalledebain.libris.field.FieldValue;

public abstract class GuiControlFactory {
	static HashMap<String, ControlConstructor> controls = initializeControlList();
	public GuiControlFactory() {
	}
	
	synchronized static HashMap<String, ControlConstructor> initializeControlList() {
		HashMap<String, ControlConstructor> map = new HashMap<String, ControlConstructor>(12);

		try {
			String cName = GuiConstants.GUI_TEXTBOX.intern();
			map.put(cName, new TextboxConstructor());
			
			cName = GuiConstants.GUI_TEXTFIELD.intern();
			map.put(cName, new TextfieldConstructor());
			
			cName = GuiConstants.GUI_PAIRFIELD.intern();
			map.put(cName, new RangefieldConstructor());
			
			cName = GuiConstants.GUI_CHECKBOX.intern();
			map.put(cName, new CheckboxConstructor());
			cName = GuiConstants.GUI_ENUMFIELD.intern();
			map.put(cName, new EnumfieldConstructor());
		} catch (SecurityException e) {
			e.printStackTrace();
		}
		return map;
	}
	
	static GuiControl makeControl(ControlConstructor controlConstructor, 
			FieldPosition fieldInfo, String initialValue) throws DatabaseException {
		try {
			GuiControl control = controlConstructor.makeControl(fieldInfo.getTitle(), fieldInfo.getHeight(), fieldInfo.getWidth());
			control.setFieldPosition(fieldInfo);
			control.setFieldValue(initialValue);
			return control;
		} catch (Exception e) {
			throw new DatabaseException(e);
		}
	}

	static UiField makeControl(FieldPosition fieldInfo, Field recordField, ModificationTracker modTrk) throws DatabaseException {
		try {
			int numValues = recordField.getNumberOfValues();
			final ControlConstructor ctrlConst = fieldInfo.getControlContructor();
			UiField guiFld = new UiField(fieldInfo, ctrlConst.labelField(), 
					recordField, numValues, modTrk);
			if (0 == numValues) {
				GuiControl control = guiFld.addControl(modTrk.isModifiable());
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

	static GuiControl newControl(FieldPosition fieldInfo,
			Field recordField, ModificationTracker modTrk, boolean editable) throws FieldDataException {
		GuiControl control = fieldInfo.getControlContructor().makeControl(fieldInfo, modTrk);
		EnumFieldChoices legalValues = recordField.getLegalValues();
		control.setLegalValues(legalValues);
		control.setEditable(editable);
		control.setRestricted(recordField.isRestricted());
		control.setSingleValue(recordField.isSingleValue());
		return control;
	}

	public static ControlConstructor getControlConstructor(
			String controlType) {
		return controls.get(controlType);
	}
	
	
	public static abstract class ControlConstructor {
		public abstract GuiControl makeControl(String title, int height, int width);
		public GuiControl makeControl(FieldPosition fieldInfo, ModificationTracker modTrk)
		{
			GuiControl ctrl = makeControl(fieldInfo.getTitle(), fieldInfo.getHeight(), 
					fieldInfo.getWidth());
			ctrl.setFieldPosition(fieldInfo);
			ctrl.setModificationTracker(modTrk);
			return ctrl;
		}
		public boolean labelField() {
			return true;
		}
	}
	
	static class TextboxConstructor extends ControlConstructor {
		public GuiControl makeControl(String title, int height, int width) {		
			return new TextBox(height, width);
		}		
	}
	
	static class TextfieldConstructor extends ControlConstructor {
		public GuiControl makeControl(String title, int height, int width) {		
			return new RecordTextField(height, width);
		}		
	}
	
	static class RangefieldConstructor extends ControlConstructor {
		public GuiControl makeControl(String title, int height, int width) {		
			return new RangeField(height, width);
		}		
	}
	
	static class CheckboxConstructor extends ControlConstructor {
		public GuiControl makeControl(String title, int height, int width) {	
			return new CheckBox(title, height, width);
		}		
		public boolean labelField() {
			return false;
		}
	}

	static class EnumfieldConstructor extends ControlConstructor {
		public GuiControl makeControl(String title, int height, int width) {	
			return new EnumField(height, width);
		}		
	}
}

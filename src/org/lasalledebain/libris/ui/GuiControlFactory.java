package org.lasalledebain.libris.ui;

import java.util.HashMap;

import org.lasalledebain.libris.EnumFieldChoices;
import org.lasalledebain.libris.Field;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.FieldDataException;
import org.lasalledebain.libris.field.FieldValue;
// TODO make this a singleton
public class GuiControlFactory<RecordType extends Record> {
	static HashMap<String, StaticControlConstructor> static_controls = staticinitializeControlList();
	private final HashMap<String, ControlConstructor> controls;
	GuiControlFactory() {
		controls = initializeControlList();
	}

	HashMap<String, ControlConstructor> initializeControlList() {
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
				public GuiControl<RecordType> makeControl(String title, int height, int width, boolean editable) {	
					return new EnumField(height, width, editable);
				}		
			});

			cName = GuiConstants.GUI_LOCATIONFIELD;
			map.put(cName, new ControlConstructor() {
				public GuiControl<RecordType> makeControl(String title, int height, int width, boolean editable) {	
					return new LocationField(height, width, editable);
				}
			});

		} catch (SecurityException e) {
			e.printStackTrace();
		}
		return map;
	}
	
	@Deprecated
	 static HashMap<String, StaticControlConstructor> staticinitializeControlList() {
		HashMap<String, StaticControlConstructor> map = new HashMap<String, StaticControlConstructor>(12);

		try {
			String cName = GuiConstants.GUI_TEXTBOX;
			map.put(cName, new StaticTextboxConstructor());

			cName = GuiConstants.GUI_TEXTFIELD;
			map.put(cName, new StaticTextfieldConstructor());

			cName = GuiConstants.GUI_PAIRFIELD;
			map.put(cName, new StaticRangefieldConstructor());

			cName = GuiConstants.GUI_CHECKBOX;
			map.put(cName, new StaticCheckboxConstructor());

			cName = GuiConstants.GUI_ENUMFIELD;
			map.put(cName, new StaticControlConstructor() {
				public GuiControl makeControl(String title, int height, int width, boolean editable) {	
					return new EnumField(height, width, editable);
				}		
			});

			cName = GuiConstants.GUI_LOCATIONFIELD;
			map.put(cName, new StaticControlConstructor() {
				public GuiControl makeControl(String title, int height, int width, boolean editable) {	
					return new LocationField(height, width, editable);
				}
			});

		} catch (SecurityException e) {
			e.printStackTrace();
		}
		return map;
	}
	
	@Deprecated
	static MultipleValueUiField staticmakeMultiControlField(LayoutField fieldPosn, Field recordField, ModificationTracker modTrk) throws DatabaseException {
		try {
			int numValues = recordField.getNumberOfValues();
			final StaticControlConstructor ctrlConst = fieldPosn.getStaticControlContructor();
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

	MultipleValueUiField makeMultiControlField(LayoutField<RecordType> fieldPosn, Field recordField, ModificationTracker modTrk) throws DatabaseException {
		try {
			int numValues = recordField.getNumberOfValues();
			final ControlConstructor ctrlConst = fieldPosn.getControlContructor();
			MultipleValueUiField guiFld = new MultipleValueUiField(fieldPosn, ctrlConst.labelField(), 
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

	@Deprecated
	static GuiControl staticnewControl(LayoutField fieldPosn,
			Field recordField, ModificationTracker modTrk, boolean editable) throws FieldDataException {
		GuiControl control = fieldPosn.getStaticControlContructor().makeControl(fieldPosn, modTrk, editable);
		EnumFieldChoices legalValues = recordField.getLegalValues();
		control.setLegalValues(legalValues);
		control.setRestricted(recordField.isRestricted());
		control.setSingleValue(recordField.isSingleValue());
		return control;
	}

	public static StaticControlConstructor getStaticControlConstructor(
			String controlType) {
		return static_controls.get(controlType);
	}
	
	@Deprecated
	public static abstract class StaticControlConstructor {
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
	
	@Deprecated
	static class StaticTextboxConstructor extends StaticControlConstructor {
		public GuiControl makeControl(String title, int height, int width, boolean editable) {		
			return new TextBox(height, width, editable);
		}		
	}
	
	@Deprecated
	static class StaticTextfieldConstructor extends StaticControlConstructor {
		public GuiControl makeControl(String title, int height, int width, boolean editable) {		
			return new RecordTextField(height, width, editable);
		}		
	}
	
	@Deprecated
	static class StaticRangefieldConstructor extends StaticControlConstructor {
		public GuiControl makeControl(String title, int height, int width, boolean editable) {		
			return new RangeField(height, width, editable);
		}		
	}
	
	@Deprecated
	static class StaticCheckboxConstructor extends StaticControlConstructor {
		public GuiControl makeControl(String title, int height, int width, boolean editable) {	
			return new CheckBoxControl(title, height, width, editable);
		}		
		public boolean labelField() {
			return false;
		}
	}

	GuiControl<RecordType> newControl(LayoutField<RecordType> fieldPosn,
			Field recordField, ModificationTracker modTrk, boolean editable) throws FieldDataException {
		GuiControl<RecordType> control = fieldPosn.getStaticControlContructor().makeControl(fieldPosn, modTrk, editable);
		EnumFieldChoices legalValues = recordField.getLegalValues();
		control.setLegalValues(legalValues);
		control.setRestricted(recordField.isRestricted());
		control.setSingleValue(recordField.isSingleValue());
		return control;
	}

	public static StaticControlConstructor getControlConstructor(
			String controlType) {
		return static_controls.get(controlType);
	}
	
	
	public abstract class ControlConstructor {
		public abstract GuiControl<RecordType> makeControl(String title, int height, int width, boolean editable);

		public GuiControl<RecordType> makeControl(LayoutField<RecordType> fieldPosn, ModificationTracker modTrk, boolean editable) {
			GuiControl<RecordType> ctrl = makeControl(fieldPosn.getTitle(), fieldPosn.getHeight(), 
					fieldPosn.getWidth(), editable);
			ctrl.setFieldInfo(fieldPosn);
			ctrl.setModificationTracker(modTrk);
			return ctrl;
		}
		public boolean labelField() {
			return true;
		}
	}
	
	class TextboxConstructor extends ControlConstructor {
		public GuiControl<RecordType> makeControl(String title, int height, int width, boolean editable) {		
			return new TextBox<RecordType>(height, width, editable);
		}		
	}
	
	 class TextfieldConstructor extends ControlConstructor {
		public GuiControl<RecordType> makeControl(String title, int height, int width, boolean editable) {		
			return new RecordTextField(height, width, editable);
		}		
	}
	
	 class RangefieldConstructor extends ControlConstructor {
		public GuiControl<RecordType> makeControl(String title, int height, int width, boolean editable) {		
			return new RangeField(height, width, editable);
		}		
	}
	
	 class CheckboxConstructor extends ControlConstructor {
		public GuiControl<RecordType> makeControl(String title, int height, int width, boolean editable) {	
			return new CheckBoxControl(title, height, width, editable);
		}		
		public boolean labelField() {
			return false;
		}
	}

	}

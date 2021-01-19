package org.lasalledebain.libris.ui;

import org.lasalledebain.libris.Record;

public abstract class GuiControlConstructor<RecordType extends Record> {
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

class TextboxConstructor<RecordType extends Record> extends GuiControlConstructor<RecordType> {
	public GuiControl<RecordType> makeControl(String title, int height, int width, boolean editable) {		
		return new TextBox<RecordType>(height, width, editable);
	}		
}

class TextfieldConstructor<RecordType extends Record> extends GuiControlConstructor<RecordType> {
	public GuiControl<RecordType> makeControl(String title, int height, int width, boolean editable) {		
		return new RecordTextField<RecordType>(height, width, editable);
	}		
}

class RangefieldConstructor<RecordType extends Record> extends GuiControlConstructor<RecordType> {
	public GuiControl<RecordType> makeControl(String title, int height, int width, boolean editable) {		
		return new RangeField<RecordType>(height, width, editable);
	}		
}

class CheckboxConstructor<RecordType extends Record> extends GuiControlConstructor<RecordType> {
	public GuiControl<RecordType> makeControl(String title, int height, int width, boolean editable) {	
		return new CheckBoxControl<RecordType>(title, height, width, editable);
	}		
	public boolean labelField() {
		return false;
	}
}


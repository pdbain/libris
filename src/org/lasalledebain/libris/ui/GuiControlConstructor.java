package org.lasalledebain.libris.ui;

public abstract class GuiControlConstructor {
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

class TextboxConstructor extends GuiControlConstructor {
	public GuiControl makeControl(String title, int height, int width, boolean editable) {		
		return new TextBox(height, width, editable);
	}		
}

class TextfieldConstructor extends GuiControlConstructor {
	public GuiControl makeControl(String title, int height, int width, boolean editable) {		
		return new RecordTextField(height, width, editable);
	}		
}

class RangefieldConstructor extends GuiControlConstructor {
	public GuiControl makeControl(String title, int height, int width, boolean editable) {		
		return new RangeField(height, width, editable);
	}		
}

class CheckboxConstructor extends GuiControlConstructor {
	public GuiControl makeControl(String title, int height, int width, boolean editable) {	
		return new CheckBoxControl(title, height, width, editable);
	}		
	public boolean labelField() {
		return false;
	}
}


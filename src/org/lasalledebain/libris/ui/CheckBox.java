package org.lasalledebain.libris.ui;

import java.awt.Checkbox;
import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import org.lasalledebain.libris.LibrisConstants;
import org.lasalledebain.libris.exception.FieldDataException;
import org.lasalledebain.libris.field.FieldBooleanValue;
import org.lasalledebain.libris.field.FieldValue;

public class CheckBox extends GuiControl {

	boolean selected;
	public class CheckboxListener implements ItemListener {

		@Override
		public void itemStateChanged(ItemEvent arg0) {
			if (!editable) {
				control.setState(!control.getState());
			} else {
				setModified(true);
			}
		}

	}

	Checkbox control;
	private boolean editable;
	public CheckBox(String title, int height, int width) {
		control = new Checkbox(title);
		control.addItemListener(new CheckboxListener());
	}

	@Override
	public void setFieldValue(String controlValue) {
		control.setState(Boolean.parseBoolean(controlValue));
		empty = false;
	}

	@Override
	public void setFieldValue(FieldValue value) throws FieldDataException {
		control.setState(value.isTrue());
		empty = false;
	}

	@Override
	public Component getGuiComponent() {
		return control;
	}

	@Override ()
	public FieldValue getFieldValue() throws FieldDataException {
		boolean buttonState = control.getState();
		return new FieldBooleanValue(buttonState? LibrisConstants.BOOLEAN_TRUE_STRING: LibrisConstants.BOOLEAN_FALSE_STRING);
	}

	@Override
	public void setEditable(boolean edtable) {
		editable = edtable;
		control.setEnabled(editable);
	}

	@Override
	public boolean isEditable() {
		return editable;
	}


}

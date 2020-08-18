package org.lasalledebain.libris.ui;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JCheckBox;
import javax.swing.JComponent;

import org.lasalledebain.libris.LibrisConstants;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.exception.FieldDataException;
import org.lasalledebain.libris.field.FieldBooleanValue;
import org.lasalledebain.libris.field.FieldValue;

public class CheckBoxControl<RecordType extends Record> extends GuiControl<RecordType> {

	boolean selected;
	protected final JCheckBox control;
	private String title;
	private boolean empty;

	public boolean isEmpty() {
		return empty;
	}
	public void setEmpty(boolean empty) {
		this.empty = empty;
	}
	public CheckBoxControl(String title, int height, int width, boolean editable) {
		super(height, width, editable);
		this.title = title;
		empty = true;
		control = displayControls();
	}
	@Override
	public void setFieldValue(String controlValue) {
		control.setSelected(Boolean.parseBoolean(controlValue));
		empty = false;
	}

	@Override
	public void setFieldValue(FieldValue value) throws FieldDataException {
		control.setSelected(value.isTrue());
		empty = false;
	}

	@Override
	public JComponent getGuiComponent() {
		return control;
	}

	@Override ()
	public FieldValue getFieldValue() throws FieldDataException {
		boolean buttonState = control.isSelected();
		return new FieldBooleanValue(buttonState? LibrisConstants.BOOLEAN_TRUE_STRING: LibrisConstants.BOOLEAN_FALSE_STRING);
	}

	@Override
	protected JCheckBox displayControls() {
		JCheckBox tempControl = new JCheckBox(title);
		tempControl.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent arg0) {
				if (!editable) {
					control.setEnabled(!control.isSelected());
				} else {
					setModified(true);
				}
			}

		}
				);
		tempControl.setEnabled(editable);
		return tempControl;
	}
	@Override
	protected void copyValuesFromControls() {
		return;
	}
}

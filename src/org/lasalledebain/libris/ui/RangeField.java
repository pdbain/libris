package org.lasalledebain.libris.ui;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.FocusListener;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.lasalledebain.libris.Record;

public class RangeField<RecordType extends Record> extends ValuePairField<RecordType> {
	protected JTextField mainControl, extraControl;
	private static final JLabel dashLabel = new JLabel("-");

	public RangeField(int height, int width, boolean editable) {
		super(height, width, editable);
		FlowLayout layout = new FlowLayout();
		control.setLayout(layout);
		layout.setHgap(0);
		addFields(editable, true);
	}

	@Override
	protected JPanel displayControls() {
		JPanel tempPanel = new JPanel();
		return tempPanel;
	}

	@Override
	public Component getFocusComponent() {
		JTextField firstValue = mainControl;
		return (null == firstValue)? control: firstValue;
	}
	
	@Override
	public void addFocusListener(FocusListener focusTracker) {
		if (null != mainControl) {
			mainControl.addFocusListener(focusTracker);
		}
		if (null != extraControl) {
			extraControl.addFocusListener(focusTracker);
		}
	}

	@Override
	protected void copyValuesFromControls() {
		mainValue = mainControl.getText();
		extraValue = extraControl.getText();
	}

	@Override
	protected void copyValuesToControls() {
		boolean bothFields = !extraValue.isEmpty();
		addFields(editable, bothFields);
		mainControl.setText(mainValue);
		if (bothFields) {
			extraControl.setText(extraValue);
		}
	}

	protected void addFields(boolean editable, boolean both) {
		control.removeAll();
		mainControl = new JTextField(mainValue, (both || editable)? width: 2 * width);
		control.add(mainControl);
		mainControl.setEditable(editable);
		mainControl.getDocument().addDocumentListener(getModificationListener());
		if (both || editable) {
			control.add(dashLabel);
			extraControl = new JTextField(extraValue, width);
			extraControl.getDocument().addDocumentListener(getModificationListener());
			control.add(extraControl);
		}
	}
}

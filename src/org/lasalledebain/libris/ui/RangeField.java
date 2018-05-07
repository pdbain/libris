package org.lasalledebain.libris.ui;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.FocusListener;

import javax.swing.JLabel;
import javax.swing.JTextField;

public class RangeField extends ValuePairField {
	protected JTextField mainControl, extraControl;
	private static final JLabel dashLabel = new JLabel("-");

	public RangeField(int height, int width, boolean editable) {
		super(height, width, editable);
		FlowLayout layout = new FlowLayout();
		control.setLayout(layout);
		layout.setHgap(0);
	}

	@Override
	protected void displayControls() {
		control.removeAll();
		mainControl = new JTextField(mainValue);
		control.add(mainControl);
		if (editable) {
			mainControl.setEditable(true);
			control.add(dashLabel);
			extraControl = new JTextField(extraValue);
			extraControl.setEditable(true);
			mainControl.getDocument().addDocumentListener(getModificationListener());
			extraControl.getDocument().addDocumentListener(getModificationListener());
			control.add(extraControl);
		} else {
			mainControl.setEditable(false);
			if (!extraValue.isEmpty()) {
				control.add(dashLabel);
				extraControl = new JTextField(extraValue);
				extraControl.setEditable(false);
				control.add(extraControl);
			}
		}
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
}

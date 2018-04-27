package org.lasalledebain.libris.ui;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.FocusListener;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.lasalledebain.libris.exception.FieldDataException;
import org.lasalledebain.libris.field.FieldSingleStringValue;
import org.lasalledebain.libris.field.FieldStringPairValue;
import org.lasalledebain.libris.field.FieldValue;

public abstract class ValuePairField extends GuiControl {

	protected JPanel control;
	JPanel pairArea;
	protected int width;
	protected ArrayList<JTextField[]> pairTextFields;
	
	public ValuePairField(int height, int width) {
		pairTextFields = new ArrayList<JTextField[]>(1);
		control = new JPanel();
		this.width = width;
		FlowLayout layout = new FlowLayout();
		control.setLayout(layout);
		layout.setHgap(0);
		empty = false;
		setFieldValue("", "", true);
		empty = true;
	}

	@Override
	public Component getGuiComponent() {
		return control;
	}
	@Override
	public FieldValue getFieldValue() throws FieldDataException {
		FieldValue head = null;
		FieldValue lastValue = null;
		for (JTextField[] f: pairTextFields) {
			if ((null == f) || (f.length == 0)) {
				continue;
			}
			String rangeStart = f[0].getText();
			boolean firstValueEmpty = rangeStart.isEmpty();
			FieldValue temp;
			if (null == f[1]) {
				if (firstValueEmpty) {
					continue;
				}
				temp = new FieldSingleStringValue(rangeStart);				
			} else {
				String rangeEnd = f[1].getText();
				if (firstValueEmpty && rangeEnd.isEmpty()) {
					continue;
				}
				temp = new FieldStringPairValue(rangeStart, rangeEnd);
			}
			if (null == lastValue) {
				head = lastValue = temp;
			} else {
				lastValue.append(temp);
			}
		}
		return head;
	}
	@Override
	public void setFieldValue(String controlValue) throws FieldDataException {
		setFieldValue(controlValue, null, false);
	}
	public void setFieldValue(FieldValue val) throws FieldDataException {
		String firstValue = val.getMainValueAsString();
		String secondValue = val.getExtraValueAsString();
		if (null == secondValue) {
			secondValue = firstValue;
		}
		setFieldValue(firstValue, secondValue, false);
	}
	protected void setFieldValue(String firstValue, String secondValue, boolean twoValues) {
		JTextField pairField[] = new JTextField[2];
		control.removeAll();
		if ((null == secondValue) || (secondValue.isEmpty() && !twoValues)) {
			pairField[0] = new JTextField(firstValue,2*width);
			control.add(pairField[0]);
		} else {
			pairField[0] = new JTextField(firstValue,width);
			pairField[1] = new JTextField(secondValue,width);
			control.add(pairField[0]);				
			control.add(new JLabel("-"));
			control.add(pairField[1]);				
			pairField[1].getDocument().addDocumentListener(getModificationListener());
		}
		pairField[0].getDocument().addDocumentListener(getModificationListener());
		if (empty) {
			pairTextFields.set(0, pairField);
		} else {
			pairTextFields.add(pairField);
		}
		empty = false;
	}
	@Override
	public Component getFocusComponent() {
		JTextField firstValue = pairTextFields.get(0)[0];
		return (null == firstValue)? control: firstValue;
	}
	@Override
	public void addFocusListener(FocusListener focusTracker) {
		JTextField[] val = pairTextFields.get(0);
		if (null != val[0]) {
			val[0].addFocusListener(focusTracker);
		}
		if (null != val[1]) {
			val[1].addFocusListener(focusTracker);
		}
	}
	@Override
	public void setEditable(boolean editable) {
		if (null == pairTextFields) {
			return;
		}
		for (JTextField[] f: pairTextFields) {
			if (null == f) {
				continue;
			}
			f[0].setEditable(editable);
			if (null != f[1]) {
				f[1].setEditable(editable);
			}
		}
	}
	@Override
	public boolean isEditable() {
		if ((null == pairTextFields) || (pairTextFields.size() == 0)){
			return false;
		} else {
			JTextField[] v = pairTextFields.get(0);
			if (v.length == 0) {
				return false;
			} else {
				return v[0].isEditable();
			}
		}
	}
	
}

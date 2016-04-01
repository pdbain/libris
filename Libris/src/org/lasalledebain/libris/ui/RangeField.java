package org.lasalledebain.libris.ui;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.FocusListener;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.lasalledebain.libris.exception.FieldDataException;
import org.lasalledebain.libris.field.FieldSingleStringValue;
import org.lasalledebain.libris.field.FieldStringPairValue;
import org.lasalledebain.libris.field.FieldValue;

public class RangeField extends GuiControl {

	JPanel control;
	JPanel rangeArea;
	private ArrayList<JTextField[]> ranges;
	private int width;
	private DocumentListener modListener;
	public RangeField(int height, int width) {
		ranges = new ArrayList<JTextField[]>(1);
		control = new JPanel();
		this.width = width;
		FlowLayout layout = new FlowLayout();
		control.setLayout(layout);
		layout.setHgap(0);
		empty = false;
		setFieldValue("", "", true);
		empty = true;
	}

	private DocumentListener getModificationListener() {
		if (null == modListener) {
			modListener = new DocumentListener() {
				@Override
				public void removeUpdate(DocumentEvent e) {
					setModified(true);
				}
				@Override
				public void insertUpdate(DocumentEvent e) {
					setModified(true);
				}
				@Override
				public void changedUpdate(DocumentEvent e) {
					setModified(true);
				}
			};
		}
		return modListener;

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

	private void setFieldValue(String rangeStart, String rangeEnd, boolean twoValues) {
		JTextField rf[] = new JTextField[2];
		control.removeAll();
		if ((null == rangeEnd) || (rangeEnd.isEmpty() && !twoValues)) {
			rf[0] = new JTextField(rangeStart,2*width);
			control.add(rf[0]);
		} else {
			rf[0] = new JTextField(rangeStart,width);
			rf[1] = new JTextField(rangeEnd,width);
			control.add(rf[0]);				
			control.add(new JLabel("-"));
			control.add(rf[1]);				
			rf[1].getDocument().addDocumentListener(getModificationListener());
		}
		rf[0].getDocument().addDocumentListener(getModificationListener());
		if (empty) {
			ranges.set(0, rf);
		} else {
			ranges.add(rf);
		}
		empty = false;
	}

	@Override
	public Component getGuiComponent() {
		return control;
	}

	@Override
	public FieldValue getFieldValue() throws FieldDataException {
		FieldValue head = null;
		for (JTextField[] f: ranges) {
			if ((null == f) || (f.length == 0)) {
				continue;
			}
			String rangeStart = f[0].getText();
			FieldValue temp;
			if ((null == f[1]) || (rangeStart.length() == 0)) {
				temp = new FieldSingleStringValue(rangeStart);				
			} else {
				String rangeEnd = f[1].getText();
				temp = new FieldStringPairValue(rangeStart, rangeEnd);
			}
			if (null == head) {
				head = temp;
			} else {
				head.append(temp);
			}
		}
		return head;
	}

	@Override
	public Component getFocusComponent() {
		JTextField firstValue = ranges.get(0)[0];
		return (null == firstValue)? control: firstValue;
	}

	@Override
	public void addFocusListener(FocusListener focusTracker) {
		JTextField[] val = ranges.get(0);
		if (null != val[0]) {
			val[0].addFocusListener(focusTracker);
		}
		if (null != val[1]) {
			val[1].addFocusListener(focusTracker);
		}
	}

	@Override
	public void setEditable(boolean editable) {
		if (null == ranges) {
			return;
		}
		for (JTextField[] f: ranges) {
			if (null == f) {
				continue;
			}
			f[0].setEditable(editable);
			if (null != f[1]) {
				f[1].setEditable(editable);
			}
		}
	}

}

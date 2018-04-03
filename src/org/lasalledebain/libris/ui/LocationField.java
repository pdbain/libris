package org.lasalledebain.libris.ui;

import java.awt.Component;
import java.awt.FlowLayout;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.lasalledebain.libris.exception.FieldDataException;
import org.lasalledebain.libris.field.FieldLocationValue;
import org.lasalledebain.libris.field.FieldValue;

public class LocationField extends GuiControl {

	private ArrayList<JTextField[]> locations;
	private JPanel control;
	private int width;
	private DocumentListener modListener;

	public LocationField(int height, int width) {
		locations = new ArrayList<JTextField[]>(1);
		control = new JPanel();
		this.width = width;
		FlowLayout layout = new FlowLayout();
		control.setLayout(layout);
		layout.setHgap(0);
		setFieldValue("", "", true);
		empty = true;
	}
	
	@Override
	public void setFieldValue(String controlValue) throws FieldDataException {
		if (!controlValue.startsWith("."))  try {
			new URL(controlValue);
		} catch (MalformedURLException e) {
			throw new FieldDataException("Invalid URL: "+controlValue, e);
		}
		setFieldValue(new FieldLocationValue(controlValue, null));
	}

	@Override
	public void setFieldValue(FieldValue newValue) throws FieldDataException {
		String firstValue = newValue.getMainValueAsString();
		String secondValue = newValue.getExtraValueAsString();
		if (null == secondValue) {
			secondValue = firstValue;
		}
		setFieldValue(firstValue, secondValue, false);
	}

	private void setFieldValue(String firstValue, String secondValue, boolean twoValues) {
		JTextField rf[] = new JTextField[2];
		control.removeAll();
		if ((null == secondValue) || (secondValue.isEmpty() && !twoValues)) {
			rf[0] = new JTextField(firstValue,2*width);
			control.add(rf[0]);
		} else {
			rf[0] = new JTextField(firstValue,width);
			rf[1] = new JTextField(secondValue,width);
			control.add(rf[0]);				
			control.add(new JLabel("-"));
			control.add(rf[1]);				
			rf[1].getDocument().addDocumentListener(getModificationListener());
		}
		rf[0].getDocument().addDocumentListener(getModificationListener());
		if (empty) {
			locations.set(0, rf);
		} else {
			locations.add(rf);
		}
		empty = false;
	}

	private final void showEditableFields(ArrayList<JTextField[]> fieldList) {
		control.removeAll();
		for (JTextField[] rf: fieldList) {
			control.add(rf[0]);
			if (rf.length == 2) {
				control.add(rf[1]);
			}
		}
	}
	
	private final void showNonEditableFields(ArrayList<JTextField[]> fieldList) {
		control.removeAll();
		for (JTextField[] rf: fieldList) {
			String fieldText;
			if (rf.length == 1) {
				control.add(rf[1]);
			}
		}
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
	public FieldValue getFieldValue() throws FieldDataException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Component getGuiComponent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setEditable(boolean editable) {
		if (null == locations) {
			return;
		}
		if (editable) {
			showEditableFields(locations);
		}
	}

	@Override
	public boolean isEditable() {
		// TODO Auto-generated method stub
		return false;
	}

	class URLField extends JLabel {
		URL href;
		String linkText;
		private URLField(URL href, String linkText) {
			super();
			this.href = href;
			this.linkText = linkText;
		}
		private URLField(URL href) {
			super();
			this.href = href;
			this.linkText = href.toString();
		}
	}
}

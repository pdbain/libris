package org.lasalledebain.libris.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.lasalledebain.libris.EnumFieldChoices;
import org.lasalledebain.libris.exception.FieldDataException;
import org.lasalledebain.libris.field.FieldEnumValue;
import org.lasalledebain.libris.field.FieldValue;

public class EnumFieldControl extends GuiControl {

	protected final JPanel control;
	private EnumFieldChoices legalValues;
	private ArrayList<String> extraValues;
	private final JComboBox<String> valueSelector;
	private int numLegalValues;
	private JPanel filler;
	private boolean empty;
	
	public EnumFieldControl(int height, int width, boolean editable) {
		super(height, width, editable);
		control = displayControls();
		setEmpty(true);
		valueSelector = new JComboBox<String>();
		valueSelector.setEnabled(editable);
		setContent(!editable);
	}

	protected JPanel displayControls() {
		JPanel tempControl = new JPanel();
		filler = new JPanel();
		tempControl.setSize(width, height);
		Dimension ctrlDim = new Dimension(Math.max(100, width), Math.max(20,height));
		filler.setPreferredSize(ctrlDim);
		filler.setMinimumSize(ctrlDim);
		tempControl.setVisible(true);
		filler.setVisible(true);
		return tempControl;
	}
	
	@Override
	public void setEmpty(boolean newEmpty) {
		empty = newEmpty;
		setContent(newEmpty);
	}

	private void setContent(boolean useFiller) {
		if (useFiller) {
			if (null != valueSelector) {
				control.remove(valueSelector);
			}
			control.add(filler);
		} else {
			control.add(valueSelector);
			valueSelector.setVisible(true);
			control.remove(filler);
		}
	}

	@Override
	public JComponent getGuiComponent() {
		return control;
	}

	@Override
	public void setFieldValue(String controlValue) {
		valueSelector.setSelectedItem(controlValue.intern());
	}
	@Override
	public void setLegalValues(EnumFieldChoices newValues) {
		this.legalValues = newValues;
		String[] comboValues = legalValues.getChoiceValues();
		 numLegalValues = comboValues.length;
		for (int i = 0; i < numLegalValues; ++i) {
			comboValues[i] = comboValues[i].intern();
		}
		valueSelector.setModel(new DefaultComboBoxModel<>(comboValues));
		valueSelector.setSelectedIndex(-1);
		if ((height > 0) && (width > 0)) {
			valueSelector.setSize(width, height);
		}
		valueSelector.addItemListener(new ItemListener() {


			@Override
			public void itemStateChanged(ItemEvent arg0) {
				setModified(true);
				empty = false;
			}
			
		});
	}
	@Override
	public FieldValue getFieldValue() throws FieldDataException {
		if (isEmpty()) {
			return null;
		}
		int currentValue = valueSelector.getSelectedIndex();
		if (currentValue >= 0) {
			if (currentValue < numLegalValues) {
				String valueId = legalValues.getChoiceId(currentValue);
				return new FieldEnumValue(legalValues, valueId);
			} else {
				return new FieldEnumValue(legalValues, EnumFieldChoices.INVALID_CHOICE, valueSelector.getSelectedItem().toString());
			}
		} else {
			return new FieldEnumValue(legalValues, EnumFieldChoices.INVALID_CHOICE, valueSelector.getSelectedItem().toString());
		}
	}
	@Override
	public void setFieldValue(FieldValue key) throws FieldDataException {
		String mainValue = key.getMainValueAsKey();
		String extraValue = key.getExtraValueAsKey();
		
		if (null == extraValue) {
			int index = legalValues.indexFromId(mainValue);
			valueSelector.setSelectedIndex(index);
		} else {
			ArrayList<String> extras = getExtraValues();
			int index = extras.indexOf(extraValue);
			if (index < 0) {
				extras.add(extraValue);
				valueSelector.addItem(extraValue);
				valueSelector.setSelectedItem(extraValue);
			}
		}
		setEmpty(false);
		valueSelector.setVisible(true);
	}
	@Override
	public Component getFocusComponent() {
		return isRestricted()? valueSelector: valueSelector.getEditor().getEditorComponent();
	}

	private synchronized ArrayList<String> getExtraValues() {
		if (null == extraValues) { 
			extraValues = new ArrayList<String>();
		}
		return extraValues;
	}

	@Override
	public void setRestricted(boolean restricted) {
		super.setRestricted(restricted);
		valueSelector.setEditable(!restricted);
	}

	@Override
	public boolean isEmpty() {
		return empty;
	}

	@Override
	protected void copyValuesFromControls() {
		return;
	}
}

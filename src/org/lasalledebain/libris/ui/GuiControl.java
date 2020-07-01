package org.lasalledebain.libris.ui;

import java.awt.Component;
import java.awt.Frame;
import java.awt.event.FocusListener;
import java.util.Arrays;

import javax.swing.JComponent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.lasalledebain.libris.EnumFieldChoices;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.FieldDataException;
import org.lasalledebain.libris.field.FieldValue;

public abstract class GuiControl<RecordType extends Record> {
	protected static final String EMPTY_TEXT_VALUE = "";
	private LayoutField<RecordType> fldInfo;
	private ModificationTracker modMon;
	protected final boolean editable;
	protected int height;
	protected int width;
	public GuiControl(int height, int width, boolean editable) {
		this.editable = editable;
		this.height = height;
		this.width = width;
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

	public boolean isSingleValue() {
		return singleValue;
	}

	public void setSingleValue(boolean singleValue) {
		this.singleValue = singleValue;
	}

	public boolean isRestricted() {
		return restricted;
	}

	public void setRestricted(boolean restricted) {
		this.restricted = restricted;
	}

	boolean singleValue;
	boolean restricted;
	protected Frame parentFrame = null;
	protected final DocumentListener modListener;

	public abstract boolean isEmpty();

	public abstract void setFieldValue(String controlValue) throws FieldDataException;

	public void setFieldValues(String controlValue, String extraValue) throws DatabaseException {
		throw new DatabaseException("cannot set multiple values for "+fldInfo.getId());
	}

	public abstract void setFieldValue(FieldValue newValue) throws FieldDataException;

	protected abstract void copyValuesFromControls();

	/**
	 * Gets the value(s) from the GUI control into the recordField
	 * @return linked list of FieldValues
	 * @throws FieldDataException if the value in the control is illegal
	 */
	public abstract FieldValue getFieldValue() throws FieldDataException;
	
	public Iterable<FieldValue> getFieldValues() throws FieldDataException {
		FieldValue firstField = getFieldValue();
		if (null != firstField) {
			return Arrays.asList();
		} else {
			return firstField;
		}
	}
	
	public abstract JComponent getGuiComponent();
	public void setLegalValues(EnumFieldChoices legalValues) {
		return;
	}
	public void setFieldInfo(LayoutField<RecordType> fieldInfo) {
		this.fldInfo = fieldInfo;
	}

	public boolean isEditable() {
		return editable;
	}

	protected abstract Component displayControls();

	protected void setModified(boolean modified) {
		modMon.setModified(modified);
	}

	public void setModificationTracker(ModificationTracker modMon) {
		this.modMon = modMon;
		// TODO track modifications in subclasses
	}

	public abstract void setEmpty(boolean empty);

	public void requestFocusInWindow() {
		getFocusComponent().requestFocusInWindow();
	}

	public Component getFocusComponent() {
		return getGuiComponent();
	}

	public void addFocusListener(FocusListener focusTracker) {
		Component focusComponent = getFocusComponent();
		focusComponent.addFocusListener(focusTracker);
	}
	
	public int getNumValues() {
		return 1;
	}

	protected DocumentListener getModificationListener() {
		return modListener;
	}
}

package org.lasalledebain.libris.ui;

import java.awt.Component;
import java.awt.Frame;
import java.awt.event.FocusListener;
import java.util.Arrays;

import org.lasalledebain.libris.EnumFieldChoices;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.FieldDataException;
import org.lasalledebain.libris.field.FieldValue;

public abstract class GuiControl {
	private FieldInfo fldInfo;
	private ModificationTracker modMon;
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
	boolean empty = true;
	protected Frame parentFrame = null;

	public boolean isEmpty() {
		return empty;
	}

	public abstract void setFieldValue(String controlValue) throws FieldDataException;

	public void setFieldValues(String controlValue, String extraValue) throws DatabaseException {
		throw new DatabaseException("cannot set multiple values for "+fldInfo.getId());
	}

	public abstract void setFieldValue(FieldValue newValue) throws FieldDataException;

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
	
	public abstract Component getGuiComponent();
	public void setLegalValues(EnumFieldChoices legalValues) {
		return;
	}
	public void setFieldInfo(FieldInfo fieldInfo) {
		this.fldInfo = fieldInfo;
	}

	public abstract void setEditable(boolean editable);
	public abstract boolean isEditable();
	
	protected void setModified(boolean modified) {
		modMon.setModified(modified);
	}

	public void setModificationTracker(ModificationTracker modMon) {
		this.modMon = modMon;
		// TODO track modifications in subclasses
	}

	public void setEmpty(boolean empty) {
		this.empty = empty;
	}

	public void requestFocusInWindow() {
		getFocusComponent().requestFocusInWindow();
	}

	public Component getFocusComponent() {
		return getGuiComponent();
	}

	public void addFocusListener(FocusListener focusTracker) {
		getFocusComponent().addFocusListener(focusTracker);
	}
	
	public int getNumValues() {
		return 1;
	}
}

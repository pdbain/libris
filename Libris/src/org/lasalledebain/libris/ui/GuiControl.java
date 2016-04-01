package org.lasalledebain.libris.ui;

import java.awt.Component;
import java.awt.event.FocusListener;

import org.lasalledebain.libris.EnumFieldChoices;
import org.lasalledebain.libris.Field;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.FieldDataException;
import org.lasalledebain.libris.field.FieldValue;

public abstract class GuiControl {
	private FieldPosition fieldInfo;
	private ModificationTracker modMon;
	protected Field recordField;
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

	public boolean isEmpty() {
		return empty;
	}

	public abstract void setFieldValue(String controlValue) throws FieldDataException;

	public void setFieldValues(String controlValue, String extraValue) throws DatabaseException {
		throw new DatabaseException("cannot set multiple values for "+fieldInfo.getId());
	}

	public abstract void setFieldValue(FieldValue newValue) throws FieldDataException;

	/**
	 * Gets the value(s) from the GUI control into the recordField
	 * @return linked list of FieldValues
	 * @throws FieldDataException if the value in the control is illegal
	 */
	public abstract FieldValue getFieldValue() throws FieldDataException;
	
	public abstract Component getGuiComponent();
	public void setLegalValues(EnumFieldChoices legalValues) {
		return;
	}
	public void setFieldPosition(FieldPosition fieldInfo) {
		this.fieldInfo = fieldInfo;
	}

	public abstract void setEditable(boolean editable);
	
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
}

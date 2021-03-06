package org.lasalledebain.libris.ui;

import java.util.Collections;
import java.util.Iterator;
import static java.util.Objects.isNull;

import org.lasalledebain.libris.Field;
import org.lasalledebain.libris.exception.DatabaseError;
import org.lasalledebain.libris.exception.FieldDataException;
import org.lasalledebain.libris.field.FieldValue;

public class SingleControlUiField extends UiField {
	public SingleControlUiField(Field fld, ModificationTracker modTrk) {
		super(fld, modTrk);
	}


	GuiControl control;
	@Override
	public GuiControl getControl() {
		return control;
	}

	public void setControl(GuiControl control) {
		this.control = control;
		control.setModificationTracker(modificationTrack);
	}

	@Override
	public void enterFieldValues() throws FieldDataException {
		recordField.setValues(this);
	}

	@Override
	public int getNumValues() {
		return control.getNumValues();
	}

	@Override
	public void setFieldValues(FieldValue[] valueArray)
			throws FieldDataException {
	}

	@Override
	public GuiControl addControl(boolean editable) {	
		return null;
	}

	@Override
	public Iterator<FieldValue> iterator() {
		try {
			final FieldValue fldValue = control.getFieldValue();
			return isNull(fldValue)? Collections.emptyIterator(): Collections.singleton(fldValue).iterator();
		} catch (FieldDataException e) {
			throw new DatabaseError("error getting field values", e);
		}
	}

	@Override
	public boolean isMultiControl() {
		return false;
	}
}

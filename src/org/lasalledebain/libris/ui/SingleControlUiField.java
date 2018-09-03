package org.lasalledebain.libris.ui;

import java.util.Iterator;

import org.lasalledebain.libris.Field;
import org.lasalledebain.libris.exception.FieldDataException;
import org.lasalledebain.libris.exception.InternalError;
import org.lasalledebain.libris.field.FieldValue;

public class SingleControlUiField extends UiField {

	GuiControl control;
	public SingleControlUiField(Field fld, ModificationTracker modTrk) {
		super(fld, modTrk);
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
	public Iterator<FieldValue> iterator() {
		try {
			return control.getFieldValues().iterator();
		} catch (FieldDataException e) {
			throw new InternalError("error getting field values", e);
		}
	}

	@Override
	public boolean isMultiControl() {
		return false;
	}
}

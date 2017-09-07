package org.lasalledebain.libris.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import org.lasalledebain.libris.Field;
import org.lasalledebain.libris.exception.FieldDataException;
import org.lasalledebain.libris.field.FieldValue;

public abstract class UiField  implements Iterable<FieldValue> {

	protected Box controlsContainer;
	protected Field recordField;
	protected boolean isSelected = false;
	protected TitledBorder titledBorder;
	protected ModificationTracker modificationTrack;
	protected static final Border LINE_BORDER = BorderFactory.createLineBorder(Color.black, 1);
	protected static final Border HIGHLIGHT_BORDER = BorderFactory.createLineBorder(Color.black, 2);
	static final Border HOVER_BORDER = BorderFactory.createLineBorder(Color.red, 2);

	public UiField(Field fld, ModificationTracker modTrk) {
		recordField = fld;
		modificationTrack = modTrk;
	}

	public abstract void setEditable(boolean edit);

	public Field getRecordField() {
		return recordField;
	}

	public Component getGuiComponent() {
		return controlsContainer;
	}

	public void setSelected(boolean selected) {
		titledBorder.setBorder(selected? HIGHLIGHT_BORDER: LINE_BORDER);
		isSelected = selected;
		controlsContainer.repaint();
	}
	
	public void doSelect() {
		if (isSelected) {
			modificationTrack.unselect();
		} else {
			modificationTrack.unselect();
			modificationTrack.select(this);
		}
	}

	public void repaint() {	
		controlsContainer.repaint();
	}

	public abstract void enterFieldValues() throws FieldDataException;

	public abstract void setFieldValues(FieldValue[] valueArray) throws FieldDataException;
	
	public abstract boolean isMultiControl(); 

	public abstract int getNumValues();

	protected class SelectFocusListener implements FocusListener {
		private UiField myUiField;
		public SelectFocusListener(UiField myUiField) {
			this.myUiField = myUiField;
		}

		@Override
		public void focusGained(FocusEvent e) {

			modificationTrack.unselect();
			modificationTrack.select(myUiField);
		}

		@Override
		public void focusLost(FocusEvent e) {
			modificationTrack.unselect();
		}
	}

}

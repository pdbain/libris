package org.lasalledebain.libris.ui;

import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;

import javax.swing.BorderFactory;
import javax.swing.Box;

import org.lasalledebain.libris.Field;
import org.lasalledebain.libris.LibrisDatabase;
import org.lasalledebain.libris.exception.FieldDataException;
import org.lasalledebain.libris.field.FieldValue;
import org.lasalledebain.libris.field.FieldValueIterator;

/**
 * Holds the individual GUI controls for each value in a field
 *
 */
public class MultipleValueUiField extends UiField  implements Iterable<FieldValue> {
	private ArrayList<GuiControl> controlList;
	private Iterator<GuiControl> controlListIterator;
	private FieldPosition fldInfo;
	public MultipleValueUiField(FieldPosition fInfo, boolean labelField, Field fld, int numValues, ModificationTracker modTrk) {
		super(fld,modTrk);
		fldInfo = fInfo;
		controlList = new ArrayList<GuiControl>(numValues);
		controlsContainer = Box.createVerticalBox();
		titledBorder = labelField? BorderFactory.createTitledBorder(LINE_BORDER, fldInfo.getTitle()):
			BorderFactory.createTitledBorder(LINE_BORDER);
		controlsContainer.setBorder(titledBorder);
		controlsContainer.addMouseListener(new FieldMouseListener());
	}

	public void add(GuiControl control) {
		controlList.add(control);
		controlsContainer.add(control.getGuiComponent());
	}

	public GuiControl addControl(boolean editable) throws FieldDataException {
		GuiControl ctrl = GuiControlFactory.newControl(fldInfo,
				recordField,  modificationTrack,  editable);
		FocusListener focusTracker = new SelectFocusListener(this);
		ctrl.addFocusListener(focusTracker);
		add(ctrl);
		return ctrl;
	}

	public void setEditable(boolean isEditable) {
		for (GuiControl c: controlList) {
			c.setEditable(isEditable);
		}
	}

	@Override
	public void enterFieldValues() throws FieldDataException {
		recordField.setValues(this);
	}

	@Override
	public void setFieldValues(FieldValue[] valueArray) throws FieldDataException {
		removeAll();
		for (FieldValue fv: valueArray) {
			GuiControl c = addControl(true);
			c.setFieldValue(fv);
		}
	}

	public void removeAll() {
		controlList.clear();
		controlsContainer.removeAll();
	}

	@Override
	public Iterator<FieldValue> iterator() {
		return new UiFieldValueIterator(controlList.iterator());
	}

	private class UiFieldValueIterator implements FieldValueIterator {
		private UiFieldValueIterator(Iterator<GuiControl> cListI) {
			controlListIterator = cListI;
		}

		@Override
		public boolean hasNext() {
			return controlListIterator.hasNext();
		}

		@Override
		public FieldValue next() {
			try {
				GuiControl nextControl = controlListIterator.next();
				return nextControl.getFieldValue();
			} catch (FieldDataException e) {
				LibrisDatabase.librisLogger.log(Level.SEVERE, e.getMessage());
				return null;
			}
		}

		@Override
		public void remove() {
			return;
		}


	}

	private class FieldMouseListener implements MouseListener {

		@Override
		public void mouseClicked(MouseEvent arg0) {
			doSelect();
		}

		@Override
		public void mouseEntered(MouseEvent arg0) {
			return;
		}

		@Override
		public void mouseExited(MouseEvent arg0) {
			return;
		}

		@Override
		public void mousePressed(MouseEvent arg0) {
			return;
		}

		@Override
		public void mouseReleased(MouseEvent arg0) {
			return;
		}

	}

	public GuiControl getCtrl(int i) {
		return controlList.get(i);
	}

	@Override
	public int getNumValues() {
		int result = 0;
		if ((controlList.size() > 0) && !controlList.get(0).empty) {
				result = controlList.size();
		}
		return result;
	}

	@Override
	public boolean isMultiControl() {
		return true;
	}
}

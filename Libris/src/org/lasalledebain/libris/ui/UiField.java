package org.lasalledebain.libris.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import org.lasalledebain.libris.Field;
import org.lasalledebain.libris.LibrisDatabase;
import org.lasalledebain.libris.exception.FieldDataException;
import org.lasalledebain.libris.field.FieldValue;
import org.lasalledebain.libris.field.FieldValueIterator;

/**
 * Holds the individual GUI controls for each value in a field
 *
 */
public class UiField implements Iterable<FieldValue> {
	private ArrayList<GuiControl> controlList;
	private Iterator<GuiControl> controlListIterator;
	Box controlsContainer;
	private Field recordField;
	public Field getRecordField() {
		return recordField;
	}

	private final FieldPosition fieldInfo;
	private final TitledBorder titledBorder;
	ModificationTracker modificationTrack;
	private static final Border LINE_BORDER = BorderFactory.createLineBorder(Color.black, 1);
	private static final Border HIGHLIGHT_BORDER = BorderFactory.createLineBorder(Color.black, 2);
	private static final Border HOVER_BORDER = BorderFactory.createLineBorder(Color.red, 2);
	boolean isSelected = false;
	public UiField(FieldPosition fieldInfo, boolean labelField, Field fld, int numValues, ModificationTracker modTrk) {
		this.fieldInfo = fieldInfo;
		recordField = fld;
		this.modificationTrack = modTrk;
		controlList = new ArrayList<GuiControl>(numValues);
		controlsContainer = Box.createVerticalBox();
		titledBorder = labelField? BorderFactory.createTitledBorder(LINE_BORDER, fieldInfo.getTitle()):
			BorderFactory.createTitledBorder(LINE_BORDER);
		controlsContainer.setBorder(titledBorder);
		controlsContainer.addMouseListener(new FieldMouseListener());
	}

	public Component getGuiComponent() {
		return controlsContainer;
	}

	public void add(GuiControl control) {
		controlList.add(control);
		controlsContainer.add(control.getGuiComponent());
	}

	public GuiControl addControl(boolean editable) throws FieldDataException {
		GuiControl ctrl = GuiControlFactory.newControl(fieldInfo,
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

	public void enterFieldValues() throws FieldDataException {
		recordField.setValues(this);
	}

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
	@Override
	public Iterator<FieldValue> iterator() {
		return new UiFieldValueIterator(controlList.iterator());
	}

	private class SelectFocusListener implements FocusListener {
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

	class FieldDropListener implements DropTargetListener {

		Border oldBorder = LINE_BORDER;
		@Override
		public void dragEnter(DropTargetDragEvent dtde) {
			oldBorder = titledBorder.getBorder();
			titledBorder.setBorder(HOVER_BORDER);
			controlsContainer.repaint();
		}

		@Override
		public void dragExit(DropTargetEvent dte) {
			titledBorder.setBorder(oldBorder);
			controlsContainer.repaint();
		}

		@Override
		public void dragOver(DropTargetDragEvent dtde) {
			return;
		}

		@Override
		public void drop(DropTargetDropEvent dtde) {
			Transferable tfrable = dtde.getTransferable();
			if (tfrable.isDataFlavorSupported(DataFlavor.stringFlavor)) {
				try {
					GuiControl newControl = addControl(true);
					dtde.acceptDrop(DnDConstants.ACTION_COPY);
					String dropData = (String) tfrable.getTransferData(DataFlavor.stringFlavor);
					newControl.setFieldValue(dropData);
					newControl.setModified(true);
					titledBorder.setBorder(oldBorder);
					newControl.requestFocusInWindow();
					modificationTrack.getUi().repaint();
					dtde.dropComplete(true);
				} catch (Exception e) {
					modificationTrack.getUi().alert("Error pasting to field", e);
				}
			}
		}

		@Override
		public void dropActionChanged(DropTargetDragEvent dtde) {
			return;
		}
		
	}

	Border oldBorder = LINE_BORDER;
	public GuiControl getCtrl(int i) {
		return controlList.get(i);
	}

	public int getNumValues() {
		int result = 0;
		if ((controlList.size() > 0) && !controlList.get(0).empty) {
				result = controlList.size();
		}
		return result;
	}
}

package org.lasalledebain.libris.ui;

import java.awt.Point;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JPanel;

import org.lasalledebain.libris.DatabaseRecord;
import org.lasalledebain.libris.Field;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.exception.LibrisException;

@SuppressWarnings("serial")
public class RecordWindow extends JPanel {

	protected LibrisWindowedUi ui;
	protected Layout recordLayout;
	protected String title;
	protected int recordId;
	protected JPanel recordPanel;
	protected ArrayList<UiField> guiFields = new ArrayList<UiField>();
	private final Record record;
	protected ModificationTracker modTracker;
	protected JPanel navPanel;

	public RecordWindow(LibrisWindowedUi ui, Layout layout, Record rec, Point position, boolean editable, 
			ActionListener modificationListener) throws LibrisException {
		this.record = rec;
		recordId = rec.getRecordId();
		title = layout.getTitle();
		this.recordLayout = layout;
		this.setLocation(position);
		this.ui = ui;
		modTracker = new ModificationTracker(ui, modificationListener, this, "This record has been modified. Do you want to enter it?");
		modTracker.setModifiable(editable);
		layOutWindow(ui, layout, rec);
		modTracker.setModified(false);
	}

	protected void layOutWindow(LibrisWindowedUi ui, Layout layout, Record rec) throws LibrisException {
		recordPanel = new JPanel();
		layOutFields(layout, rec, recordPanel, modTracker);
		recordPanel.setVisible(true);
		ui.fieldSelected(false);
		ui.setSelectedField(null);
	}

	public Record enter() throws LibrisException {
		for (UiField f: guiFields) {
			f.enterFieldValues();
		}
		return record;	
	}

	public boolean isEditable() {
		return modTracker.isModifiable();
	}

	public boolean setEditable(boolean isEditable) {
		if (isEditable && ui.isDatabaseReadOnly()) {
			return false;
		}
		modTracker.setModifiable(isEditable);
		record.setEditable(isEditable);
		if (!isEditable) {
			modTracker.unselect();
		}
		return true;
	}
	
	public void refresh() throws LibrisException {
		recordPanel.setVisible(false);
		remove(navPanel);
		layOutWindow(ui, recordLayout, record);
	}
	
	public int checkClose() {
		return modTracker.checkClose();
	}
	
	public void close() {
		ui.fieldSelected(false);
		setVisible(false);
	}

	protected void layOutFields(Layout layout, Record rec, JPanel recordPanel, ModificationTracker modTrk) throws LibrisException {
		setSize(layout.getWidth(), layout.getHeight());
		guiFields = layout.layOutFields(rec, ui, recordPanel, modTrk);
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getRecordId() {
		return recordId;
	}

	public void setRecordLayout(Layout layout) throws LibrisException {
		if (layout.getId().equals(recordLayout.getId())) {
			return;
		}
		this.recordLayout = layout;
		if (null != recordPanel) {
			recordPanel.removeAll(); 
			layOutFields(layout, record, recordPanel, modTracker);
		}
	}

	public Record getRecord() {
		return record;
	}

	public String createTitle(String[] titleFieldIds) {
		setTitle(record.generateTitle(titleFieldIds));
		return title;
	}

	public boolean isModified() {
		return modTracker.isModified() ;
	}

	public void setModified(boolean mod) {
		modTracker.setModified(mod);
	}

	public UiField getField(String fieldId) {
		UiField result = null;
		for (UiField f: guiFields) {
			Field fld = f.getRecordField();
			if (fld.getFieldId() == fieldId) {
				result = f;
				break;
			}
		}
		return result;
	}

}

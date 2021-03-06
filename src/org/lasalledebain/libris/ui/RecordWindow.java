package org.lasalledebain.libris.ui;

import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.lasalledebain.libris.Field;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.exception.LibrisException;

@SuppressWarnings("serial")
public abstract class RecordWindow<RecordType extends Record> extends JScrollPane {

	protected LibrisWindowedUi ui;
	protected LibrisLayout recordLayout;
	protected String title;
	protected int recordId;
	protected final JPanel recordDisplayArea;
	protected ArrayList<UiField> guiFields = new ArrayList<UiField>();
	private final RecordType record;
	protected ModificationTracker modTracker;
	protected JPanel navPanel;

	public RecordWindow(LibrisWindowedUi ui, LibrisLayout layout, RecordType rec, Point position, boolean editable, 
			ActionListener modificationListener) throws LibrisException {
		this.record = rec;
		recordDisplayArea = new JPanel(new GridLayout());
		recordId = rec.getRecordId();
		title = layout.getTitle();
		recordLayout = layout;
		this.setLocation(position);
		this.ui = ui;
		modTracker = new ModificationTracker(ui, modificationListener, this, "This record has been modified. Do you want to enter it?");
		modTracker.setModifiable(editable);
		layOutWindow(ui, layout, rec);
		recordDisplayArea.setVisible(true);
		recordDisplayArea.repaint();
	}

	protected void layOutWindow(LibrisWindowedUi ui, LibrisLayout layout, RecordType rec) throws LibrisException {
		recordDisplayArea.removeAll();
		layOutFields(layout, rec, recordDisplayArea, modTracker);
		recordDisplayArea.setVisible(true);
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
		return modTracker.isModifiable() && recordLayout.isEditable();
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
		recordDisplayArea.setVisible(false);
		layOutWindow(ui, recordLayout, record);
	}
	
	public int checkClose() {
		return modTracker.checkClose();
	}
	
	public void close() {
		ui.fieldSelected(false);
		setVisible(false);
	}

	protected void layOutFields(LibrisLayout layout, RecordType rec, JComponent recordPanel, ModificationTracker modTrk) throws LibrisException {
		guiFields = layout.layOutFields(rec, ui, recordPanel, modTrk);
		modTracker.setModified(false);
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

	public void setRecordLayout(LibrisLayout layout) throws LibrisException {
		if (layout.getId().equals(recordLayout.getId())) {
			return;
		}
		this.recordLayout = layout;
		if (null != recordDisplayArea) {
			recordDisplayArea.removeAll(); 
			layOutFields(layout, record, recordDisplayArea, modTracker);
		}
	}

	public RecordType getRecord() {
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

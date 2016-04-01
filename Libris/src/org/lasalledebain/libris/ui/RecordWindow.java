package org.lasalledebain.libris.ui;

import java.awt.Point;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.RecordId;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.LibrisException;

public class RecordWindow extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8603864809213149500L;
	LibrisUi ui;
	Layout recordLayout;
	private String title;
	private RecordId recordId;
	private JPanel recordPanel;
	private final Record record;
	ArrayList<UiField> guiFields = new ArrayList<UiField>();
	
	public RecordWindow(LibrisUi ui, Layout layout, Record rec, boolean editable, ActionListener modificationListener) throws DatabaseException {
		this(ui, layout, rec, new Point(0, 0), editable, modificationListener);
	}

	public RecordWindow(LibrisUi ui, Layout layout, Record rec, Point position, boolean editable, ActionListener modificationListener) throws DatabaseException {
		recordId = rec.getRecordId();
		title = layout.getTitle();
		this.record = rec;
		this.recordLayout = layout;
		this.setLocation(position);
		this.ui = ui;
		modTracker = new ModificationTracker(ui, modificationListener, this, "This record has been modified. Do you want to enter it?");
		modTracker.setModifiable(editable);
		recordPanel = new JPanel();
		layOutFields(layout, rec, recordPanel, modTracker);
		 add(recordPanel);
		recordPanel.setVisible(true);
		ui.fieldSelected(false);
		ui.setSelectedField(null);
	}

	public void refresh() throws LibrisException {
		ui.fieldSelected(false);
		ui.setSelectedField(null);
		recordPanel.setVisible(false);
		remove(recordPanel);
		recordPanel = new JPanel();
		layOutFields(recordLayout, record, recordPanel, modTracker);
		 add(recordPanel);
		recordPanel.setVisible(true);
	}
	
	public RecordWindow(LibrisUi ui, Layout myGuiLayout, Record rec, Point point, ActionListener modificationListener) throws DatabaseException {
		this(ui, myGuiLayout,  rec,  point, false, modificationListener);
	}

	ModificationTracker modTracker;
	
	public boolean isEditable() {
		return modTracker.isModifiable();
	}

	/**
	 * @param layout
	 * @param rec
	 * @param panelLayout
	 * @param c
	 * @param recordPanel
	 * @param modTrk 
	 * @throws DatabaseException
	 */
	private void layOutFields(Layout layout, Record rec, JPanel recordPanel, ModificationTracker modTrk)
			throws DatabaseException {
		setSize(layout.getWidth(), layout.getHeight());
		guiFields = layout.layOutFields(rec, recordPanel, modTrk);
	}
	
	public Record enter() throws LibrisException {
		for (UiField f: guiFields) {
			f.enterFieldValues();
		}
		return record;
		
	}

	JPanel makeFieldPanel(String title, JComponent fieldContent) {
		JPanel p = new JPanel();
		JLabel l = new JLabel(title);
		l.setLabelFor(fieldContent);
		p.add(l);
		l.setLocation(0, 0);
		p.add(fieldContent);
		return p;
	}

	public int checkClose() {
		return modTracker.checkClose();
	}
	
	public void close() {
		ui.fieldSelected(false);
		setVisible(false);
	}

	public void setEditable(boolean isEditable) {
		modTracker.setModifiable(isEditable);
		record.setEditable(isEditable);
		for (UiField f: guiFields) {
			f.setEditable(isEditable);
		}
		if (!isEditable) {
			modTracker.unselect();
		}
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public RecordId getRecordId() {
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
			if (f.getRecordField().getFieldId() == fieldId) {
				result = f;
				break;
			}
		}
		return result;
	}
}

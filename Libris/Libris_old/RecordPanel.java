/*
 * Created on Jan 20, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package Libris;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import Libris.LibrisFieldDatatype.FieldType;

public class RecordPanel extends JPanel {

	private LibrisRecord record;
	private JComponent[] textAreas;
	private FlowLayout layout;

	/**
	 *  Create the GUI components and arrange them
	 *  in the container.
	 */
	public RecordPanel(LibrisRecord record) {
		this.record = record;
		layoutFields(false);
	}

	public RecordPanel(LibrisRecord record, boolean editable) {
		this.record = record;
		layoutFields(editable);
	}

	private void layoutFields(boolean editable) {
		LibrisRecordField field;
		LibrisFieldDatatype datatype;
		this.setLayout(new BorderLayout());
		LibrisRecordField[] recordFields = getFieldList();
		textAreas = new JComponent[recordFields.length];
		setLayout(this.layout = new FlowLayout(FlowLayout.LEFT));
		setPreferredSize(new Dimension(400,400));
		this.setSize(getPreferredSize());
		for (int i = 0; i < recordFields.length; ++i) {
			field = recordFields[i];
			if (field == null) continue;
			JComponent fieldControl = null;
			fieldControl = field.createControl();
			if (null != fieldControl) {
				JPanel fieldPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
				fieldPanel.add(new JLabel(field.getSchemaField().getName()));
				fieldPanel.add(fieldControl); 
				textAreas[i] = fieldControl;
				add(fieldPanel);
			}
		}
	}

	private LibrisRecordField[] getFieldList() {
		LibrisRecordField[] recordFields = this.record.getFieldList();
		return recordFields;
	}
	
	public void setEditable(boolean editable) {
		LibrisRecordField[] recordFields = getFieldList();
		for (int i = 0; i < recordFields.length; ++i) {
			if (textAreas[i] != null) {
				recordFields[i].setEditable(editable);
			}
		}
	}
}
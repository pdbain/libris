package org.lasalledebain.libris.ui;

import javax.swing.JTable;

import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.Schema;

public class ListLayout<RecordType extends Record> extends LibrisSwingLayout<RecordType> {

	private JTable recordTable;
	private ListLayoutTableModel<RecordType> myTableModel;

	public ListLayout(Schema schem) {
		super(schem);
	}

	@Override
	protected void showRecord(int recId) {
		return;
	}

	@Override
	protected void validate() {
		// TODO Auto-generated method stub
		
	}
}

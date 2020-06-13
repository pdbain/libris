package org.lasalledebain.libris.ui;

import java.awt.Color;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.RecordList;
import org.lasalledebain.libris.Schema;
import org.lasalledebain.libris.SingleRecordList;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.xmlUtils.LibrisXMLConstants;

public class ListLayout extends Layout {

	public ListLayout(Schema schem) throws DatabaseException {
		super(schem);
	}

	@Override
	public String getLayoutType() {
		return LibrisXMLConstants.XML_LAYOUT_TYPE_LIST;
	}

	@Override
	ArrayList<UiField> layOutFields(RecordList recList, LibrisWindowedUi ui, JPanel recordPanel, ModificationTracker modTrk)
			throws DatabaseException {
		ListLayoutTableModel<Record> theTableModel = new ListLayoutTableModel<Record>(recList, this);
		JTable recordTable = new JTable(theTableModel);
		recordTable.setAutoCreateRowSorter(true);
		recordTable.setGridColor(Color.BLACK);
		JScrollPane scrollPane = new JScrollPane(recordTable);
		recordPanel.add(scrollPane);
		recordPanel.setVisible(true);
		return null;
	}

	@Override
	public boolean isSingleRecord() {
		return false;
	}

	@Override
	ArrayList<UiField> layOutFields(Record rec, LibrisWindowedUi ui, JPanel recordPanel, ModificationTracker modTrk)
			throws DatabaseException, LibrisException {		
		return layOutFields(new SingleRecordList<Record>(rec), ui, recordPanel, modTrk);
	}

}

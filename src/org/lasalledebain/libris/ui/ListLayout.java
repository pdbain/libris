package org.lasalledebain.libris.ui;

import java.awt.Color;
import java.awt.Rectangle;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableColumnModel;

import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.RecordList;
import org.lasalledebain.libris.Schema;
import org.lasalledebain.libris.SingleRecordList;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.xmlUtils.LibrisXMLConstants;

public class ListLayout extends Layout<Record> {

	private JTable recordTable;
	private ListLayoutTableModel<Record> myTableModel;

	public ListLayout(Schema schem) throws DatabaseException {
		super(schem);
	}

	@Override
	public String getLayoutType() {
		return LibrisXMLConstants.XML_LAYOUT_TYPE_LIST;
	}

	// TODO change this to a list
	@Override
	ArrayList<UiField> layOutFields(RecordList recList, LibrisWindowedUi ui, JPanel recordPanel, ModificationTracker modTrk)
			throws DatabaseException {
		myTableModel = new ListLayoutTableModel<Record>(recList, this, true);
		recordTable = new JTable(myTableModel);
		recordTable.setGridColor(Color.BLACK);
		JScrollPane scrollPane = new JScrollPane(recordTable);
		recordPanel.add(scrollPane);
		recordPanel.setVisible(true);
		return null;
	}

	@Override
	ArrayList<UiField> layOutFields(Record rec, LibrisWindowedUi ui, JPanel recordPanel, ModificationTracker modTrk)
			throws DatabaseException, LibrisException {		
		return layOutFields(new SingleRecordList<Record>(rec), ui, recordPanel, modTrk);
	}

	@Override
	protected void showRecord(int recId) {
		int row = myTableModel.getRowForRecord(recId);
		Rectangle cellRect = recordTable.getCellRect(row, 0, true);
		recordTable.scrollRectToVisible(cellRect);
	}

}

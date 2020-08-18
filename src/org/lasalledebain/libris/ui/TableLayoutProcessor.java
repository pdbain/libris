package org.lasalledebain.libris.ui;

import java.awt.Color;
import java.awt.FontMetrics;
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableColumnModel;

import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.RecordList;
import org.lasalledebain.libris.SingleRecordList;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.LibrisException;

public class TableLayoutProcessor<RecordType extends Record> extends GenericLayoutProcessor<RecordType> {

	public TableLayoutProcessor(LibrisLayout<RecordType> theLayout) {
		super(theLayout);
	}
	@Override
	public void layoutDisplayPanel(RecordList<RecordType> recList, int recId, StringBuffer buff) throws InputException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public
	ArrayList<UiField> layOutFields(RecordType rec, LibrisWindowedUi<RecordType> ui, JComponent recordPanel, ModificationTracker modTrk)
			throws DatabaseException, LibrisException {		
		return layOutFields(new SingleRecordList<RecordType>(rec), ui, recordPanel, modTrk);
	}

	@Override
	public ArrayList<UiField> layOutFields(RecordList<RecordType> recList, LibrisWindowedUi<RecordType> ui, JComponent recordPanel, ModificationTracker modTrk)
			throws DatabaseException {
		TableLayoutTableModel<RecordType> myTableModel = new TableLayoutTableModel<RecordType>(recList, myLayout);
		JTable recordTable = new JTable(myTableModel);
		FontMetrics myFontMetrics = recordPanel.getFontMetrics(recordTable.getFont());
		int columnWidth = myFontMetrics.stringWidth(TableLayoutTableModel.RECORD_ID) + 10;
		TableColumnModel columns = recordTable.getColumnModel();
		columns.getColumn(0).setPreferredWidth(columnWidth);
		 ArrayList<LayoutField<RecordType>> bodyFieldList = myLayout.getBodyFieldList();

		for (int i = 1; i < myTableModel.getColumnCount(); ++i) {
			LayoutField<RecordType> theFieldPosition = bodyFieldList.get(i-1);
			columnWidth = theFieldPosition.getWidth();
			columnWidth = Math.max(columnWidth, myFontMetrics.stringWidth(theFieldPosition.title) + 10);
			columns.getColumn(i).setPreferredWidth(columnWidth);
		}
		recordTable.setAutoCreateRowSorter(true);
		recordTable.setGridColor(Color.BLACK);
		recordTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		JScrollPane scrollPane = new JScrollPane(recordTable);
		recordPanel.add(scrollPane);
		scrollPane.setVisible(true);
		return null;
	}

}

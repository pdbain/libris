package org.lasalledebain.libris.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
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

public class TableLayout extends MultiRecordLayout<Record> {

	private ListLayoutTableModel<Record> myTableModel;
	private JTable recordTable;

	public TableLayout(Schema schem) throws DatabaseException {
		super(schem);
	}

	@Override
	public String getLayoutType() {
		return XML_LAYOUT_TYPE_TABLE;
	}

	@Override
	ArrayList<UiField> layOutFields(RecordList recList, LibrisWindowedUi ui, JPanel recordPanel, ModificationTracker modTrk)
			throws DatabaseException {
		myTableModel = new ListLayoutTableModel<Record>(recList, this, false);
		recordTable = new JTable(myTableModel);
		FontMetrics myFontMetrics = recordPanel.getFontMetrics(recordTable.getFont());
		int columnWidth = myFontMetrics.stringWidth(ListLayoutTableModel.RECORD_ID) + 10;
		TableColumnModel columns = recordTable.getColumnModel();
		columns.getColumn(0).setPreferredWidth(columnWidth);
		for (int i = 1; i < myTableModel.getColumnCount(); ++i) {
			FieldPosition theFieldPosition = bodyFieldList.get(i-1);
			columnWidth = theFieldPosition.getWidth();
			columnWidth = Math.max(columnWidth, myFontMetrics.stringWidth(theFieldPosition.title) + 10);
			columns.getColumn(i).setPreferredWidth(columnWidth);
		}
		recordTable.setAutoCreateRowSorter(true);
		recordTable.setGridColor(Color.BLACK);
		recordTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		JScrollPane scrollPane = new JScrollPane(recordTable);
		Dimension recordPanelSize = recordPanel.getSize();
		scrollPane.setPreferredSize(recordPanelSize);
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

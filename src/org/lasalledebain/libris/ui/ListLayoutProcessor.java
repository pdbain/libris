package org.lasalledebain.libris.ui;

import static java.util.Objects.nonNull;

import java.awt.Color;
import java.awt.FontMetrics;
import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.http.HttpServletResponse;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.table.TableColumnModel;

import org.lasalledebain.libris.DatabaseRecord;
import org.lasalledebain.libris.GenericDatabase;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.RecordList;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.LibrisException;

public class ListLayoutProcessor<RecordType extends Record> extends GenericLayoutProcessor<RecordType> {

	public ListLayoutProcessor(LibrisLayout<RecordType> theLayout) {
		super(theLayout);
	}

	@Override
	public void layOutPage(RecordList<RecordType> recList, int recId, LibrisLayout<RecordType> browserLayout,
			DatabaseUi ui, HttpServletResponse resp) throws InputException, IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void layoutDisplayPanel(RecordList<RecordType> recList, int recId, StringBuffer buff) throws InputException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ArrayList<UiField> layOutFields(RecordType theRecord, LibrisWindowedUi<RecordType> ui, JComponent recordPanel,
			ModificationTracker modTrk) throws DatabaseException, LibrisException {
		GenericDatabase<DatabaseRecord> db = ui.getDatabase();
		ListLayoutTableModel<RecordType> myTableModel = null; // TODO FIXnew ListLayoutTableModel(theRecord, db, this);
		JTable recordTable = new JTable(myTableModel);
		TableColumnModel columns = recordTable.getColumnModel();
		FontMetrics myFontMetrics = recordPanel.getFontMetrics(recordTable.getFont());
		for (int columnIndex = 0; columnIndex < myTableModel.getColumnCount(); ++columnIndex) {
			String columnName = myTableModel.getColumnName(columnIndex);
			int columnWidth = myFontMetrics.stringWidth(columnName);
			for (int rowIndex = 0; rowIndex < myTableModel.getRowCount(); ++rowIndex) {
				String cellValue = myTableModel.getValueAt(rowIndex, columnIndex);
				if (nonNull(cellValue)) columnWidth = Math.max(columnWidth, myFontMetrics.stringWidth(cellValue));
			}
			columns.getColumn(columnIndex).setPreferredWidth(columnWidth + 10);
		}
		recordTable.setGridColor(Color.BLACK);
		recordTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		recordPanel.add(recordTable);
		return null;
	}

}

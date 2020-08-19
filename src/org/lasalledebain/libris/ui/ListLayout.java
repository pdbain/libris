package org.lasalledebain.libris.ui;

import static java.util.Objects.nonNull;

import java.awt.Color;
import java.awt.FontMetrics;
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.table.TableColumnModel;

import org.lasalledebain.libris.DatabaseRecord;
import org.lasalledebain.libris.GenericDatabase;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.RecordList;
import org.lasalledebain.libris.Schema;
import org.lasalledebain.libris.exception.DatabaseError;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.LibrisException;

public class ListLayout<RecordType extends Record> extends LibrisSwingLayout<RecordType> {

	private JTable recordTable;
	private ListLayoutTableModel<Record> myTableModel;

	public ListLayout(Schema schem) {
		super(schem);
	}

	// TODO show groups
	@Override
	public
	ArrayList<UiField> layOutFields(Record theRecord, LibrisWindowedUi ui, JComponent recordPanel, ModificationTracker modTrk)
			throws LibrisException {
		GenericDatabase<DatabaseRecord> db = ui.getDatabase();
		myTableModel = new ListLayoutTableModel<Record>(theRecord, db, this);
		recordTable = new JTable(myTableModel);
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

	@Override
	protected void showRecord(int recId) {
		return;
	}

	@Override
	protected void validate() {
		// TODO Auto-generated method stub
		
	}
}

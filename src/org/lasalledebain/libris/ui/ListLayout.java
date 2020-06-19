package org.lasalledebain.libris.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableColumnModel;

import org.lasalledebain.libris.LibrisDatabase;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.Schema;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.xmlUtils.LibrisXMLConstants;
import static java.util.Objects.nonNull;

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

	// TODO show groups
	@Override
	ArrayList<UiField> layOutFields(Record theRecord, LibrisWindowedUi ui, JPanel recordPanel, ModificationTracker modTrk)
			throws LibrisException {
		LibrisDatabase db = ui.getDatabase();
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
		JScrollPane scrollPane = new JScrollPane(recordTable);
		recordPanel.add(scrollPane);
		recordPanel.setVisible(true);
		return null;
	}

	@Override
	protected void showRecord(int recId) {
		return;
	}

}

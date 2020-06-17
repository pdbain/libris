package org.lasalledebain.libris.ui;

import java.awt.Color;
import java.awt.FontMetrics;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableColumnModel;

import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.Schema;
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

	// TODO show groups
	@Override
	ArrayList<UiField> layOutFields(Record theRecord, LibrisWindowedUi ui, JPanel recordPanel, ModificationTracker modTrk)
			throws LibrisException {
		myTableModel = new ListLayoutTableModel<Record>(theRecord, this);
		recordTable = new JTable(myTableModel);
		TableColumnModel columns = recordTable.getColumnModel();
		FontMetrics myFontMetrics = recordPanel.getFontMetrics(recordTable.getFont());
		int columnWidth = myFontMetrics.stringWidth(myTableModel.getColumnName(0));
		for (int i = 0; i < getNumFields(); ++i) {
			columnWidth = Math.max(columnWidth, myFontMetrics.stringWidth(bodyFieldList.get(i).getTitle()));
		}
		columns.getColumn(0).setPreferredWidth(columnWidth + 10);
		columnWidth = 0;
		for (int i = 0; i < getNumFields(); ++i) {
			FieldPosition theFieldPosition = bodyFieldList.get(i);
			columnWidth = Math.max(columnWidth, theFieldPosition.getWidth());
		}
		columns.getColumn(1).setPreferredWidth(columnWidth);
		columns.getColumn(2).setPreferredWidth(columnWidth);
		recordTable.setGridColor(Color.BLACK);
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

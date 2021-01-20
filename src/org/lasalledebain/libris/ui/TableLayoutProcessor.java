package org.lasalledebain.libris.ui;

import java.awt.Color;
import java.awt.FontMetrics;
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableColumnModel;

import org.lasalledebain.libris.Field;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.RecordList;
import org.lasalledebain.libris.SingleRecordList;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.field.FieldValue;

public class TableLayoutProcessor<RecordType extends Record> extends LayoutProcessor<RecordType> {
	private static final String RECORD_TABLE_LAYOUT_CLASS = "recordTable",
			RECORD_FIELD_CELL_CLASS = "recordFieldCell";
	private static final String RECORD_TABLE_LAYOUT_STYLE = "."+ RECORD_TABLE_LAYOUT_CLASS + " {\n"
			+"border: 2px solid " + BORDER_COLOUR + ";\n"
			+ "border-collapse: collapse;\n"
			+ "vertical-align: top;\n"
		+	"}\n";
	private static final String RECORD_FIELD_CELL_STYLE = "."+ RECORD_FIELD_CELL_CLASS + " {\n"
			+"border: 2px solid " + BORDER_COLOUR + ";\n"
			+ "border-collapse: collapse;\n"
			+ "vertical-align: top;\n"
		+	"}\n";

	public TableLayoutProcessor(LibrisLayout theLayout) {
		super(theLayout);
	}

	private String makeStyleString() {
		StringBuffer buff = new StringBuffer(super.getStyleString());
		buff.append(
				RECORD_TABLE_LAYOUT_STYLE
						+ FIELDS_PANEL_STYLE
						+ FIELD_TITLE_TEXT_STYLE
						+ FIELD_TEXT_STYLE
						+ RECORD_FIELD_CELL_STYLE
				);
		return buff.toString();
	}

	@Override
	public void layoutDisplayPanel(RecordList<RecordType> recList, HttpParameters params, int recId, StringBuffer buff) throws InputException {
		RecordType rec = getRecordOrErrorMessage(recList, recId, buff);
		if (null == rec) return;
		layoutRecordTitle(buff, rec);
		buff.append("<table class = "
				+ RECORD_TABLE_LAYOUT_CLASS
				+ ">\n"); {
					for (LayoutField fp: myLayout.getFields()) {
						int fieldNum = fp.fieldNum;
						Field fld = rec.getField(fieldNum);
						if (null == fld) {
							continue;
						}
						buff.append("<tr "
								+ "class=\""+FIELDS_PANEL_CLASS+"\""
								+ ">\n"); {
							buff.append("<td "
									+ "class=\""+FIELD_TITLE_TEXT_CLASS+' '+RECORD_FIELD_CELL_CLASS+"\""
									+ "> "
									+ fp.getTitle()
									+ "</td>\n");
							String separator = "";
							buff.append("<td class=\""+FIELD_TEXT_CLASS+' '+RECORD_FIELD_CELL_CLASS+"\""
									+ ">\n");
							for (FieldValue fv: fld.getFieldValues()) {
								buff.append(separator);
								buff.append(fv.getValueAsString());
								separator = "<br/>\n";
							}
							buff.append("</td\n>");
						} buff.append("</tr>\n");
					}
				} buff.append("</table>\n");
	}

	@Override
	protected String getStyleString() {
		return super.getStyleString() + makeStyleString();
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
		ArrayList<LayoutField> bodyFieldList = myLayout.getBodyFieldList();

		for (int i = 1; i < myTableModel.getColumnCount(); ++i) {
			LayoutField theFieldPosition = bodyFieldList.get(i-1);
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
	@Override
	protected void validate() {
		// TODO Write tablelayout validate

	}

}

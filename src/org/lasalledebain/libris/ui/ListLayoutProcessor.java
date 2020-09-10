package org.lasalledebain.libris.ui;

import static java.util.Objects.nonNull;

import java.awt.Color;
import java.awt.FontMetrics;
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.table.TableColumnModel;

import org.lasalledebain.libris.DatabaseRecord;
import org.lasalledebain.libris.Field;
import org.lasalledebain.libris.GenericDatabase;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.RecordList;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.util.StringUtils;

public class ListLayoutProcessor<RecordType extends Record> extends LayoutProcessor<RecordType> {
	private static final String RECORD_LIST_LAYOUT_CLASS = "recordList";
	private static final String RECORD_LIST_ROW_CLASS = "recordListRow",
			RECORD_ID_CLASS = "recordId";
	protected static final String RECORD_ID_STYLE = "." + RECORD_ID_CLASS + " {"
			+"max-width: 200px;"
			+ "}\n";

	private final String myStyleString;

	public ListLayoutProcessor(LibrisLayout<RecordType> theLayout) {
		super(theLayout);
		myStyleString = makeStyleString();
	}

	private String makeStyleString() {
		StringBuffer buff = new StringBuffer(super.getStyleString());
		buff.append(
				"."+ RECORD_LIST_LAYOUT_CLASS + " {\n"
						+ BACKGROUND_COLOR_WHITE
						+ "}\n"
						+ FIELDS_PANEL_STYLE
						+ FIELD_TITLE_STYLE
						+ FIELD_TEXT_STYLE
				);
		return buff.toString();
	}

	@Override
	public void layoutDisplayPanel(RecordList<RecordType> recList, int recId, StringBuffer buff) throws InputException {
		buff.append("<table class = \""
				+ RECORD_LIST_LAYOUT_CLASS
				+ "\">\n"); {
					buff.append("<thead>"); {
						buff.append("<th></th>");
						for (LayoutField<RecordType> fp: myLayout.getFields()) {
							buff.append("<th class="
									+FIELD_TITLE_CLASS
									+ ">");
							buff.append(fp.getTitle());
							buff.append("</th>\n");
						}

					} buff.append("</thead>");
					buff.append("<tbody>"); {
						for (RecordType rec:recList) {
							buff.append("<tr "
									+ "class=\""+RECORD_LIST_ROW_CLASS+"\""
									+ ">\n"); {
										buff.append("<td class=\""
												+ RECORD_ID_CLASS
												+ "\">"); {
											buff.append(rec.getRecordId());
											String recName = rec.getName();
											if (!StringUtils.isStringEmpty(recName)) {
												buff.append(" ");
												buff.append(recName);
											}
										} buff.append("</td>\n");
										for (LayoutField<RecordType> fp: myLayout.getFields()) {
											int fieldNum = fp.fieldNum;
											Field fld = rec.getField(fieldNum);
											buff.append("<td class=\"" + FIELD_TEXT_CLASS
													+ "\">"); {
														if (null != fld) {
															buff.append(fld.getValuesAsString());
														}
													} buff.append("</td>\n");
										}
									} buff.append("</tr>\n");

						}
					} buff.append("</tbody>");

				} buff.append("</table>\n");
	}

	@Override
	protected int layoutBrowserPanel(RecordList<RecordType> recList, int start, int currentRecord,
			LibrisLayout<RecordType> browserLayout, StringBuffer buff) {
		/* no browser in this layout */
		return currentRecord;
	}

	@Override
	protected String getStyleString() {
		return super.getStyleString() + makeStyleString(); // TODO DEBUG myStyleString;
	}

	@Override
	public ArrayList<UiField> layOutFields(RecordType theRecord, LibrisWindowedUi<RecordType> ui, JComponent recordPanel,
			ModificationTracker modTrk) throws DatabaseException, LibrisException {
		GenericDatabase<DatabaseRecord> db = ui.getDatabase();
		ListLayoutTableModel<RecordType> myTableModel = new ListLayoutTableModel(theRecord, db, myLayout);
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

	@Override
	protected void validate() {
		// TODO Write listlayout validate	
	}
}

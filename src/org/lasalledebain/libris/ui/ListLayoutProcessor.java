package org.lasalledebain.libris.ui;

import static java.util.Objects.nonNull;
import static org.lasalledebain.libris.ui.BrowserWindow.LIST_LIMIT;

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
import org.lasalledebain.libris.RecordId;
import org.lasalledebain.libris.RecordList;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.util.StringUtils;

public class ListLayoutProcessor<RecordType extends Record> extends LayoutProcessor<RecordType> {
	private static final String RECORD_LIST_TABLE_CLASS = "recordList";
	private static final String RECORD_LIST_ROW_CLASS = "recordListRow",
			RECORD_ID_CLASS = "recordId",
			RECORD_LIST_CELL_CLASS = "recordListCell";
	protected static final String RECORD_ID_STYLE = "." + RECORD_ID_CLASS + " {\n"
			+"max-width: 120px;\n"
			+ "word-wrap: break-word;\n"
			+ "font-style: italic;\n"
			+ "}\n";

	protected static final String RECORD_LIST_CELL_STYLE = "." + RECORD_LIST_CELL_CLASS + " {\n"
			+"border: 2px solid " + BORDER_COLOUR + ";\n"
			+ "border-collapse: collapse;\n"
			+ BACKGROUND_COLOR_WHITE
			+ "vertical-align: top;\n"
			+ "\n}\n";

	protected static final String RECORD_LIST_TABLE_STYLE = "." + RECORD_LIST_TABLE_CLASS + " {\n"
			+ "border-collapse: collapse;\n"
			+ "\n}\n";

	protected static final String NAVIGATION_PANEL_STYLE = "." + NAVIGATION_PANEL_CLASS + " {\n"
			+ "display: inline;\n"
			+ "\n}\n";

	public ListLayoutProcessor(LibrisLayout<RecordType> theLayout) {
		super(theLayout);
	}

	private String makeStyleString() {
		StringBuffer buff = new StringBuffer(super.getStyleString());
		buff.append(FIELDS_PANEL_STYLE
				+ FIELD_TITLE_TEXT_STYLE
				+ FIELD_TEXT_STYLE
				+ RECORD_ID_STYLE
				+ RECORD_LIST_CELL_STYLE
				+ RECORD_LIST_TABLE_STYLE
				+ NAVIGATION_PANEL_STYLE
				);
		return buff.toString();
	}

	@Override
	public void layoutDisplayPanel(RecordList<RecordType> recList, HttpParameters params, int recId, StringBuffer buff) throws InputException {
		buff.append("<table class = \""
				+ RECORD_LIST_TABLE_CLASS
				+ "\">\n"); {
					buff.append("<thead>"); {
						buff.append("<th class=\""
								+RECORD_LIST_CELL_CLASS
								+ "\">"
								+ "Record</th>");
						for (LayoutField<RecordType> fp: myLayout.getFields()) {
							buff.append("<th class=\"\n"
									+ RECORD_LIST_CELL_CLASS
									+ "\">"
									);
							buff.append(fp.getTitle());
							buff.append("</th>\n");
						}

					} buff.append("</thead>");
					buff.append("<tbody>"); {
						int recCount = 0;
						for (RecordType rec:recList) {
							if (rec.getRecordId() < params.browserFirstId) continue;
							buff.append("<tr "
									+ "class=\""+RECORD_LIST_ROW_CLASS+"\""
									+ ">\n"); {
										buff.append("<td class=\""
												+ " "+RECORD_ID_CLASS
												+ " " +RECORD_LIST_CELL_CLASS
												+ "\">"); {
													buff.append(rec.getRecordId());
													String recName = rec.getName();
													if (!StringUtils.isStringEmpty(recName)) {
														buff.append("<br/>");
														buff.append(recName);
													}
												} buff.append("</td>\n");
												for (LayoutField<RecordType> fp: myLayout.getFields()) {
													int fieldNum = fp.fieldNum;
													Field fld = rec.getField(fieldNum);
													buff.append("<td class=\"" 
															+ FIELD_TEXT_CLASS
															+" "+ RECORD_LIST_CELL_CLASS
															+ "\">"); {
																if (null != fld) {
																	buff.append(fld.getValuesAsString());
																}
															} buff.append("</td>\n");
												}
									} buff.append("</tr>\n");
									++recCount;
									if (recCount >= LIST_LIMIT) break;

						}
					} buff.append("</tbody>");

				} buff.append("</table>\n");
	}

	@Override
	protected int layoutBrowserPanel(RecordList<RecordType> recList, int start, int currentRecord,
			LibrisLayout<RecordType> browserLayout, StringBuffer buff) {
		startDiv(buff);
		addLayoutSelector(buff);

		startDiv(buff, NAVIGATION_PANEL_CLASS);
		buff.append("<button onclick=\"document.getElementById('"
				+ BROWSER_STARTING_RECORD_CONTROL
				+ "').value='"
				+ (Math.max(RecordId.NULL_RECORD_ID, start-LIST_LIMIT))
				+ "'\""
				+ ((start > RecordId.NULL_RECORD_ID)? "": " disabled")
				+ ">&#x23EE</button>\n");	

		int recListSize = recList.size();
		buff.append("<button onclick=\"document.getElementById('"
				+ BROWSER_STARTING_RECORD_CONTROL
				+ "').value='"
				+ (start+LIST_LIMIT)
				+ "'\""
				+ ((start + LIST_LIMIT < recListSize)? "": " disabled")
				+ ">&#x23ED;</button>\n");
		buff.append("<input type=\"hidden\" "
				+ ONCHANGE_THIS_FORM_SUBMIT
				+ " id=\""
				+ BROWSER_STARTING_RECORD_CONTROL
				+ "\" name=\""
				+ LibrisHTMLConstants.HTTP_BROWSER_STARTING_RECORD
				+ "\""
				+ "\" value=\""
				+ start
				+ "\""
				+ ">\n");
		endDiv(buff);
		endDiv(buff);
		return currentRecord;
	}

	@Override
	protected String getStyleString() {
		return super.getStyleString() + makeStyleString();
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

	@Override
	public String getLayoutSelectStyle() {
		return '.'+LAYOUT_SELECT_CLASS +
				" {\n"
				+ "}\n";
	}
	public String getContentPanelStyle() {
		return '.'+CONTENT_PANEL_CLASS + " {\n"
				+ "}\n";
	}
}

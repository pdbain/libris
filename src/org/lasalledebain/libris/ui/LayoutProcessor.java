package org.lasalledebain.libris.ui;

import static org.lasalledebain.libris.ui.BrowserWindow.LIST_LIMIT;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JComponent;

import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.RecordId;
import org.lasalledebain.libris.RecordList;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.LibrisException;
public abstract class LayoutProcessor <RecordType extends Record>
implements LayoutHtmlProcessor<RecordType>, LayoutSwingProcessor<RecordType>, LibrisHTMLConstants {

	private final String DATABASE_TITLE_CLASS = "databaseTitle";
	private final String DATABASE_TITLE_STYLE = '.'+DATABASE_TITLE_CLASS
			+ " {\n"
			+ "text-align: center;\n"
			+ "font-weight: bold;\n"
			+ "  font-size: 150%;\n" 
			+ "}\n";
	
	private final String RECORD_SELECT_STYLE = '.'+RECORD_SELECT_CLASS +
			" {\n"
			+ "width: 95%;\n"
			+ "}\n";

	private final String BROWSER_PANEL_STYLE = 
			'.'+BROWSER_PANEL_CLASS +
			" {\n"
			+ "width: 25%;"
			+ "min-width: 200px;"
			+ "display: inline;"
			+ "float: left;\n" +
			"margin: 10px;\n" + 
			GREY_BORDER + 
			"border-radius: " + CORNER_RADIUS
			+ "\n"
			+ "}\n"
			+ RECORD_SELECT_STYLE;

	private final String DISPLAY_PANEL_STYLE = '.'+DISPLAY_PANEL_CLASS +
			" {\n" + 
			"float: left;\n"
			+ "display: inline;"
			+ "margin: 10px;\n"
			+ "width: 70%;"
			+ GREY_BORDER + 
			"border-radius: " + CORNER_RADIUS
			+ "\n" +
			BACKGROUND_COLOR_WHITE +
			"}\n";
	private final String  NAVIGATION_BUTTONS_STYLE =
			'.'+NAVIGATION_BUTTONS_CLASS + " {\n"
					+ " font-family: \"Apple Symbols\", \"Arial Unicode MS\", Symbola, \"Everson Mono\";\n"
					+ "}\n";

	private final String MASTER_STYLE = "html {background-color: "
			+ HTML_BACKGROUND_COLOUR + ";"
			+ " font-family: Arial, Helvetica, sans-serif;\n"
			+ "}\n";

	protected final String RECORT_TITLE_STYLE =
			"."+ RECORT_TITLE_CLASS + " {\n"
					+ "vertical-align: top;"
					+ "display:block;\n"
					+ "text-align: center;"
					+ "font-size: 120%;\n"
					+ "font-weight: bold;\n"
					+ "}\n";

	private final String GENERIC_STYLE = MASTER_STYLE
			+ "\n" +
			"h1 {color: blue}\n" + 
			'#'+MAIN_FRAME +
			" {\n"+
			"}\n"
			+ NAVIGATION_BUTTONS_STYLE+ "\n"
			+ BROWSER_PANEL_STYLE+
			DISPLAY_PANEL_STYLE
			+'.'+BROWSER_ITEM_CLASS
			+ " {\n" + 
			"font-size: 100%;\n" +
			"}\n";
	protected final String FIELD_TITLE_TEXT_STYLE = "."+FIELD_TITLE_TEXT_CLASS + " {\n"
			+ "font-size: 100%;\n"
			+ "font-weight: bold;\n"
			+ "}\n";
	protected final String FIELD_TITLE_BLOCK_STYLE = "."+FIELD_TITLE_BLOCK_CLASS + " {\n"
			+ "vertical-align: top;"
			+ "float:left;\n" 
			+ "display:inline;\n"
			+ "padding-right: 15px;\n"
			+ "}\n";
	protected final String FIELDS_PANEL_STYLE = "."+ FIELDS_PANEL_CLASS + " {\n" + 
			BACKGROUND_COLOR_WHITE
			+"}\n";
	protected  final String FIELD_TEXT_STYLE = "."+ FIELD_TEXT_CLASS + " {\n"
			+ "font-weight:normal\n"
			+ "}\n";

	protected String getStyleString() { 
		return GENERIC_STYLE+ "\n"
			+ DATABASE_TITLE_STYLE+ "\n"
				+ getLayoutSelectStyle();
	}

	protected final LibrisLayout myLayout;
	protected DatabaseUi<RecordType> myUi;

	public LayoutProcessor(LibrisLayout theLayout) {
		myLayout = theLayout;
	}

	abstract void validate() throws InputException;
	@Override
	public
	ArrayList<UiField> layOutFields(RecordList<RecordType> recList, LibrisWindowedUi ui, JComponent recordPanel,
			ModificationTracker modTrk) throws DatabaseException, LibrisException {
		RecordType rec = recList.getFirstRecord();
		return layOutFields(rec, ui, recordPanel, modTrk);
	}

	/* HTML utilities */
	protected void generateHeaderAndStylesheet(DatabaseUi<RecordType> ui, StringBuffer buffer) {
		buffer.append("<!DOCTYPE html>\n" + 
				"<meta charset=utf-8>\n" + 
				"<head>\n" + 
				"<title>");
		buffer.append(ui.getUiTitle());
		buffer.append("</title>\n" + 
				"<style>\n"
				+ getStyleString()
				+ getContentPanelStyle()
				+ "</style>\n"
				+ "</head>\n"
				);
	}

	protected void startBody(StringBuffer buff) {
		buff.append("<body>\n"
				+ "<div id="
				+ MAIN_FRAME
				+ ">\n");
	}

	void layoutRecordTitle(StringBuffer buff, Record rec) {
		startDiv(buff, RECORT_TITLE_CLASS); {
			String recName = rec.getName();
			buff.append("<b>Record:</b> ");
			buff.append(rec.getRecordId());
			if (null != recName) {
				buff.append(" ");
				buff.append(recName);
			}

		} endDiv(buff);
	}

	protected int layoutBrowserPanel(RecordList<RecordType> recList, int start, int currentRecord, LibrisLayout browserLayout, StringBuffer buff) {
		String[] browserFields = browserLayout.getFieldIds();
		startDiv(buff, BROWSER_PANEL_CLASS);
		startDiv(buff);
		startDiv(buff);
		endDiv(buff);
		addLayoutSelector(buff);
		startDiv(buff);
		Iterator<RecordType> recIter = recList.iterator();
		int recCount = 0;
		int firstRecord = 0;
		int lastRecord = 0;
		buff.append("<select id="
				+ RECORD_BROWSER_ID
				+ " "
				+ "name="
				+ HTTP_PARAM_RECORD_ID
				+ " class="+RECORD_SELECT_CLASS
				+ " size=\"20\""
				+ ONCHANGE_THIS_FORM_SUBMIT
				+ ">\n");
		while (recIter.hasNext() && (recCount < LIST_LIMIT)) {
			Record rec = recIter.next();
			int recordId = rec.getRecordId();
			if (recordId < start) {
				continue;
			}
			if (0 == firstRecord) {
				firstRecord = recordId;
			}
			lastRecord = recordId;
			buff.append("<option "+(recordId == currentRecord? " selected": "")+" value="
					+ recordId
					+ ">");
			buff.append(rec.generateTitle(browserFields));
			buff.append("</option>\n");
			++recCount;
		}
		buff.append("</select>\n");
		endDiv(buff);
		endDiv(buff);
		currentRecord = Math.max(Math.min(currentRecord, lastRecord), firstRecord);
		addNextLastButtons(currentRecord, buff, start, firstRecord, lastRecord);	

		endDiv(buff);
		return currentRecord;
	}

	protected void addNextLastButtons(int currentRecord, StringBuffer buff, int startRecord, int firstRecord, int lastRecord) {
		startDiv(buff);
		buff.append("<button onclick=\"document.getElementById('"
				+ BROWSER_STARTING_RECORD_CONTROL
				+ "').value='"
				+ (Math.max(RecordId.NULL_RECORD_ID, startRecord-LIST_LIMIT))
				+ "'\""
				+ ((startRecord > RecordId.NULL_RECORD_ID)? "": " disabled")
				+ ">&#x23EE</button>\n");	

		buff.append("<button onclick=\"document.getElementById('"
				+ RECORD_BROWSER_ID
				+ "').value='"
				+ (currentRecord-1)
				+ "'\""
				+ (currentRecord > firstRecord? "": " disabled")
				+ ">&#x23EA;</button>\n");	

		buff.append("<button onclick=\"document.getElementById('"
				+ RECORD_BROWSER_ID
				+ "').value='"
				+ (currentRecord+1)
				+ "'\""
				+ (currentRecord < lastRecord? "": " disabled")
				+ ">&#x23E9;</button>\n");
		buff.append("<button onclick=\"document.getElementById('"
				+ BROWSER_STARTING_RECORD_CONTROL
				+ "').value='"
				+ (startRecord+LIST_LIMIT)
				+ "'\""
				+ (lastRecord >= (startRecord + LIST_LIMIT - 1)? "": " disabled")
				+ ">&#x23ED;</button>\n");
		buff.append("<input type=\"hidden\" "
				+ ONCHANGE_THIS_FORM_SUBMIT
				+ " id=\""
				+ BROWSER_STARTING_RECORD_CONTROL
				+ "\" name=\""
				+ LibrisHTMLConstants.HTTP_BROWSER_STARTING_RECORD
				+ "\""
				+ "\" value=\""
				+ startRecord
				+ "\""
				+ ">\n");
		endDiv(buff);
	}

	protected void addLayoutSelector(StringBuffer buff) {
		buff.append("<select name=\""
				+ HTTP_PARAM_LAYOUT_ID
				+ ONCHANGE_THIS_FORM_SUBMIT
				+ " class="+LAYOUT_SELECT_CLASS
				+ ">");
		{
			myLayout.getLayouts().forEach(l -> buff.append("<option value=\""+l.id+"\" "
					+ (l.id == myLayout.id? " selected=\"selected\" ":"")
					+ ">"+l.title+"</option>"));
		}
		buff.append("</select>");
	}

	protected void startDiv(StringBuffer buff, String className) {
		buff.append("<div class="+className+">\n");
	}

	protected void startDiv(StringBuffer buff) {
		buff.append("<div>\n");
	}

	protected void startDiv(StringBuffer buff, String[] classes) {
		if (0 == classes.length) {
			buff.append("<div>\n");
		} else {
			String sep = "";
			buff.append("<div class=\"");
			for (String c: classes) {
				buff.append(sep);
				buff.append(c);
				sep = " ";
			}
			buff.append("\">");
		}
	}

	protected final void endDiv(StringBuffer buff) {
		buff.append("</div>\n");
	}

	protected void endBody(StringBuffer buff) {
		endDiv(buff);
		buff.append("</body>\n");
	}

	public void layOutPage(RecordList<RecordType> recList, HttpParameters params,
			LibrisLayout browserLayout, DatabaseUi ui) throws InputException, IOException {
		myUi = ui;
		StringBuffer buff = new StringBuffer(1000);
		generateHeaderAndStylesheet(ui, buff);
		startBody(buff);
		{
			startDiv(buff, DATABASE_TITLE_CLASS); {
			buff.append(myUi.getUiTitle());
			} endDiv(buff);
			startDiv(buff, CONTENT_PANEL_CLASS);
			buff.append("<form action=\".\" method=\"get\">");
			{
				int displayableRecId = layoutBrowserPanel(recList, params.browserFirstId, params.recId, browserLayout, buff);
				startDiv(buff, DISPLAY_PANEL_CLASS);
				{
					layoutDisplayPanel(recList, params, displayableRecId, buff);
				}
				endDiv(buff);
			}
			buff.append("</form>\n");
			endDiv(buff);
		}
		endBody(buff);
		PrintWriter myWriter = params.resp.getWriter();
		String htmlString = buff.toString();
		myWriter.append(htmlString);

	}

	protected RecordType getRecordOrErrorMessage(RecordList<RecordType> recList, int recId, StringBuffer buff) throws InputException {
		RecordType rec = recList.getRecord(recId);
		if (null == rec) {
			buff.append("<p>Record "+recId+" not found</p>");
		}
		return rec;
	}

	public String getLayoutSelectStyle() {
		return '.'+LAYOUT_SELECT_CLASS +
				" {\n"
				+ "width: 95%;"
				+ "}\n";
	}

	public String getContentPanelStyle() {
		return '.'+CONTENT_PANEL_CLASS + " {\n"
				+ "float: left;\n"
				+ "width: 80%;\n"
				+ "}\n";
	}
}
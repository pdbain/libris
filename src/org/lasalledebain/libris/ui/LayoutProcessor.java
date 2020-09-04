package org.lasalledebain.libris.ui;

import static org.lasalledebain.libris.ui.BrowserWindow.LIST_LIMIT;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JComponent;

import org.lasalledebain.libris.Field;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.RecordId;
import org.lasalledebain.libris.RecordList;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.LibrisException;
public abstract class LayoutProcessor<RecordType extends Record>
implements LayoutHtmlProcessor<RecordType>, LayoutSwingProcessor<RecordType>, LibrisHTMLConstants {

	protected static final String BROWSER_STARTING_RECORD_CONTROL = "browserStartingRecordControl";
	private static final String HTML_BACKGROUND_COLOUR = "LightCyan";
	private static final String RECORD_BROWSER = "recordBrowser";
	private static final String RECORD_SELECT_CLASS = "recordSelect";
	protected static final String ONCHANGE_THIS_FORM_SUBMIT = "\" onchange=\"this.form.submit()\"";
	protected static final String BACKGROUND_COLOR_LIGHTCYAN = "background-color: " + HTML_BACKGROUND_COLOUR + ";\n";
	protected static final String BACKGROUND_COLOR_WHITE = "background-color: white;\n";
	protected static final String GREY_BORDER = "border: 1px solid LightGrey;\n";
	protected static final String CORNER_RADIUS = "5px;";
	private static final String MAIN_FRAME = "mainFrame";
	private static final String CONTENT_PANEL_NAME = "contentPanel";
	protected static final String BROWSER_PANEL_CLASS = "browserPanel";
	protected static final String BROWSER_ITEM_CLASS = "browserItem";
	protected static final String DISPLAY_PANEL_CLASS = "displayPanel,\n",
			RECORT_TITLE_CLASS="recordTitle",
			NAVIGATION_BUTTONS_CLASS="navigationButtons";

	private static final String BROWSER_PANEL_STYLE = 
			'.'+BROWSER_PANEL_CLASS +
			" {\n"
			+ "width: 25%;"
			+ "display: inline;"
			+ "float: left;\n" +
			"margin: 10px;\n" + 
			GREY_BORDER + 
			"border-radius: " + CORNER_RADIUS
			+ "\n"
			+ "}\n"
			+ '.'+RECORD_SELECT_CLASS +
			" {\n"
			+ "width: 95%;"
			+ "}\n";

	private static final String DISPLAY_PANEL_STYLE = '.'+DISPLAY_PANEL_CLASS +
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
	private static final String CONTENT_PANEL_STYLE = 
			'.'+CONTENT_PANEL_NAME + " {\n"
					+ "float: left;\n"
					+ "}\n";

	private static final String  NAVIGATION_BUTTONS_STYLE =
			'.'+NAVIGATION_BUTTONS_CLASS + " {\n"
					+ " font-family: \"Apple Symbols\", \"Arial Unicode MS\", Symbola, \"Everson Mono\";\n"
					+ "}\n";

	private static final String MASTER_STYLE = "html {background-color: "
			+ HTML_BACKGROUND_COLOUR + ";"
			+ " font-family: Arial, Helvetica, sans-serif;\n"
			+ "}\n";

	protected static final String RECORT_TITLE_STYLE =
			"."+ RECORT_TITLE_CLASS + " {\n"
					+ "vertical-align: top;"
					+ "display:block;\n"
					+ "text-align: center;"
					+ "font-size: 120%;\n"
					+ "font-weight: bold;\n"
					+ "}\n";

	private static final String GENERIC_STYLE = MASTER_STYLE
			+ "\n" +
			"h1 {color: blue}\n" + 
			'#'+MAIN_FRAME +
			" {\n"+
			"}\n"
			+ CONTENT_PANEL_STYLE
			+ NAVIGATION_BUTTONS_STYLE
			+ BROWSER_PANEL_STYLE+
			DISPLAY_PANEL_STYLE
			+'.'+BROWSER_ITEM_CLASS
			+ " {\n" + 
			"font-size: 100%;\n" +
			"}\n";

	protected String getStyleString() { 
		return GENERIC_STYLE;
	}

	protected final LibrisLayout<RecordType> myLayout;
	protected DatabaseUi<RecordType> myUi;

	public LayoutProcessor(LibrisLayout<RecordType> theLayout) {
		myLayout = theLayout;
	}

	abstract void validate() throws InputException;
	@Override
	public
	ArrayList<UiField> layOutFields(RecordList<RecordType> recList, LibrisWindowedUi<RecordType> ui, JComponent recordPanel,
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

	void layoutRecordTitle(StringBuffer buff, RecordType rec) {
		startDiv(buff, RECORT_TITLE_CLASS); {
			String recName = rec.getName();
			buff.append("Record ");
			buff.append(rec.getRecordId());
			if (null != recName) {
				buff.append(" ");
				buff.append(recName);
			}

		} endDiv(buff);
	}

	protected int layoutBrowserPanel(RecordList<RecordType> recList, int start, int currentRecord, LibrisLayout<RecordType> browserLayout, StringBuffer buff) {
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
				+ RECORD_BROWSER
				+ " "
				+ "name="
				+ HTTP_PARAM_RECORD_ID
				+ " class="+RECORD_SELECT_CLASS
				+ " size=\"20\""
				+ ONCHANGE_THIS_FORM_SUBMIT
				+ ">\n");
		while (recIter.hasNext() && (recCount < LIST_LIMIT)) {
			RecordType rec = recIter.next();
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

	private void addNextLastButtons(int currentRecord, StringBuffer buff, int startRecord, int firstRecord, int lastRecord) {
		startDiv(buff);
		buff.append("<button onclick=\"document.getElementById('"
				+ BROWSER_STARTING_RECORD_CONTROL
				+ "').value='"
				+ (Math.max(RecordId.NULL_RECORD_ID, startRecord-LIST_LIMIT))
				+ "'\""
				+ ((startRecord > RecordId.NULL_RECORD_ID)? "": " disabled")
				+ ">&#x23EE</button>\n");	

		buff.append("<button onclick=\"document.getElementById('"
				+ RECORD_BROWSER
				+ "').value='"
				+ (currentRecord-1)
				+ "'\""
				+ (currentRecord > firstRecord? "": " disabled")
				+ ">&#x23EA;</button>\n");	

		buff.append("<button onclick=\"document.getElementById('"
				+ RECORD_BROWSER
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

	private void addLayoutSelector(StringBuffer buff) {
		buff.append("<select name=\""
				+ HTTP_PARAM_LAYOUT_ID
				+ ONCHANGE_THIS_FORM_SUBMIT
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

	protected void makeHtmlControl(LayoutField<RecordType> fieldPosn, Field recordField) {

	}

	public void layOutPage(RecordList<RecordType> recList, HttpParameters params,
			LibrisLayout<RecordType> browserLayout, DatabaseUi<RecordType> ui) throws InputException, IOException {
		myUi = ui;
		StringBuffer buff = new StringBuffer(1000);
		generateHeaderAndStylesheet(ui, buff);
		startBody(buff);
		{
			startDiv(buff, CONTENT_PANEL_NAME);
			buff.append("<form action=\".\" method=\"get\">");
			{
				int displayableRecId = layoutBrowserPanel(recList, params.browserFirstId, params.recId, browserLayout, buff);
				startDiv(buff, DISPLAY_PANEL_CLASS);
				{
					layoutDisplayPanel(recList, displayableRecId, buff);
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
}
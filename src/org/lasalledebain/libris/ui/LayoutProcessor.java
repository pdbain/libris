package org.lasalledebain.libris.ui;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;

import javax.servlet.http.HttpServletResponse;
import javax.swing.JComponent;

import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.RecordId;
import org.lasalledebain.libris.RecordList;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.LibrisException;

public abstract class LayoutProcessor<RecordType extends Record>
implements LayoutHtmlProcessor<RecordType>, LayoutSwingProcessor<RecordType>, LibrisHTMLConstants {

	private static final String RECORD_BROWSER = "recordBrowser";
	private static final String ONCHANGE_THIS_FORM_SUBMIT = "\" onchange=\"this.form.submit()\"";
	private static final String BACKGROUND_COLOR_WHITE = "background-color: white;\n";
	private static final String GREY_BORDER = "border: 1px solid LightGrey;\n";
	private static final String CORNER_RADIUS = "5px;";
	private static final String MAIN_FRAME = "mainFrame";
	private static final String NAVIGATION_PANEL_CLASS = "navigationPanel";
	private static final String CONTENT_PANEL_NAME = "contentPanel";
	protected static final String BROWSER_COLUMN_NAME = "browserColumn";
	protected static final String BROWSER_ITEM_CLASS = "browserItem";
	private static final String DISPLAY_COLUMN_CLASS = "displayColumn";

	private static final String BROWSER_PANEL_STYLE = 
			'.'+BROWSER_COLUMN_NAME +
			" {\n" + 
			"float: left;\n" +
			"width:300px;\n" + 
			"margin: 10px;\n" + 
			GREY_BORDER + 
			"border-radius: " + CORNER_RADIUS
			+ "\n"
			+ BACKGROUND_COLOR_WHITE
			+ "}\n";

	private static final String CONTENT_PANEL_STYLE = 
			'.'+CONTENT_PANEL_NAME + " {\n"
					+ "clear: left;\n"
					+ "}\n";

	private static final String MASTER_STYLE = "html {background-color: #B0FFF4;"
			+ "font-family: Arial, Helvetica, sans-serif;\n"
			+ "}\n";

	private static final String STYLE_STRING = MASTER_STYLE
			+ "\n" +
			"h1 {color: blue}\n" + 
			'#'+MAIN_FRAME +
			" {\n"+
			"}\n"
			+ CONTENT_PANEL_STYLE
			+ BROWSER_PANEL_STYLE+
			'.'+NAVIGATION_PANEL_CLASS +
			" {\n" 
			+ "margin: 10px;\n" + 
			"}\n" + 
			'.'+DISPLAY_COLUMN_CLASS +
			" {\n" + 
			"float: left;\n" + 
			"margin: 10px;\n" + 
			GREY_BORDER + 
			"border-radius: " + CORNER_RADIUS
			+ "\n" +
			BACKGROUND_COLOR_WHITE +
			"}\n" + 
			'.'+BROWSER_ITEM_CLASS +
			" {\n" + 
			"font-size: 100%;\n" +
			"}\n" + 
			"</style>\n";

	protected final LibrisLayout<RecordType> myLayout;

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
				"<html>\n" + 
				"<head>\n" + 
				"<title>");
		buffer.append(ui.getUiTitle());
		buffer.append("</title>\n" + 
				"<style>\n" + 
				STYLE_STRING
				+ "</head>\n"
				);
	}

	protected void startBody(StringBuffer buff) {
		buff.append("<body>\n"
				+ "<div id="
				+ MAIN_FRAME
				+ ">\n");
	}
	protected void layoutBrowserPanel(RecordList<RecordType> recList, int start, int currentRecord, LibrisLayout<RecordType> browserLayout, StringBuffer buff) {
		String[] browserFields = browserLayout.getFieldIds();
		startDiv(buff, BROWSER_COLUMN_NAME);
		buff.append("<form action=\".\" method=\"get\">");
		addLayoutSelector(buff);
		Iterator<RecordType> recIter = recList.iterator();
		int recCount = 0;
		int firstRecord = 0;
		int lastRecord = 0;
		buff.append("<select id="
				+ RECORD_BROWSER
				+ " name="
				+ HTTP_PARAM_RECORD_ID
				+ " size=\"20\""
				+ ONCHANGE_THIS_FORM_SUBMIT
				+ ">\n");
		while (recIter.hasNext() && (recCount < BrowserWindow.LIST_LIMIT)) {
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
addNextLastButtons(currentRecord, buff, firstRecord, lastRecord);	

buff.append("</form>\n");
		endDiv(buff);
	}

	private void addNextLastButtons(int currentRecord, StringBuffer buff, int firstRecord, int lastRecord) {
		startDiv(buff, null);
		buff.append("<button onclick=\"document.getElementById('"
				+ RECORD_BROWSER
				+ "').value='"
				+ (currentRecord-1)
				+ "'\""
				+ (currentRecord > firstRecord? "": " disabled")
				+ ">Previous</button>");	
		
		buff.append("<button onclick=\"document.getElementById('"
				+ RECORD_BROWSER
				+ "').value='"
				+ (currentRecord+1)
				+ "'\""
				+ (currentRecord < lastRecord? "": " disabled")
				+ ">Next</button>");
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

	private StringBuffer startDiv(StringBuffer buff, String className) {
		if (null == className) {
			className="none";
		}
		return buff.append("<div class="+className+">\n");
	}

	public void layOutPage(RecordList<RecordType> recList, int recId, LibrisLayout<RecordType> browserLayout, 
			DatabaseUi<RecordType> ui, HttpServletResponse resp) throws InputException, IOException {
		StringBuffer buff = new StringBuffer(1000);
		generateHeaderAndStylesheet(ui, buff);
		startBody(buff);
		{
			startContentPanel(buff);
			{
				layoutBrowserPanel(recList, 0, recId, browserLayout, buff);
				startDiv(buff, DISPLAY_COLUMN_CLASS);
				{
					layoutDisplayPanel(recList, recId, buff);
				}
				endDiv(buff);
			}
			endContentPanel(buff);
		}
		endBody(buff);
		PrintWriter myWriter = resp.getWriter();
		String htmlString = buff.toString();
		myWriter.append(htmlString);
	}

	private void startContentPanel(StringBuffer buff) {
		buff.append("<div class="+CONTENT_PANEL_NAME+">\n");
	}

	private void endContentPanel(StringBuffer buff) {
		endDiv(buff);
	}

	private final void endDiv(StringBuffer buff) {
		buff.append("</div>\n");
	}

	protected void endBody(StringBuffer buff) {
		endDiv(buff);
		buff.append("</body>\n");
	}

}
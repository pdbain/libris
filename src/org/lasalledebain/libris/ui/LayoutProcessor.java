package org.lasalledebain.libris.ui;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;

import javax.servlet.http.HttpServletResponse;
import javax.swing.JComponent;

import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.RecordList;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.LibrisException;

public abstract class LayoutProcessor<RecordType extends Record>  implements LayoutHtmlProcessor<RecordType>, LayoutSwingProcessor<RecordType> {

	private static final String CORNER_RADIUS = "10px;";
	private static final String MAIN_FRAME = "mainFrame";
	protected static final String BROWSER_COLUMN_NAME = "browserColumn";
	protected static final String BROWSER_ITEM_CLASS = "browserItem";
	private static final String DISPLAY_COLUMN_NAME = "displayColumn";

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
				"html {background-color: #B0FFF4;}\n" +
				"\n" +
				"h1 {color: blue}\n" + 
				'#'+MAIN_FRAME +
				" {\n"+
				"}\n" +
				'.'+BROWSER_COLUMN_NAME +
				" {\n" + 
				"float: left;\n" +
				"width:300px;\n" + 
				"margin: 10px;\n" + 
				"border: 1px solid black;\n" + 
				"border-radius: "
				+ CORNER_RADIUS
				+ "\n" +
				"background-color: white;\n" +
				"}\n" + 
				'.'+DISPLAY_COLUMN_NAME +
				" {\n" + 
				"float: left;\n" + 
				"margin: 10px;\n" + 
				"border: 1px solid black;\n" + 
				"border-radius: "
				+ CORNER_RADIUS
				+ "\n" +
				"background-color: white;\n" +
				"}\n" + 
				'.'+BROWSER_ITEM_CLASS +
				" {\n" + 
				"font-family: Arial, Helvetica, sans-serif;\n"
				+ "font-size: 100%;\n" +
				"}\n" + 
				"</style>\n" + 
				"</head>\n"
				);
	}

	protected void startBody(StringBuffer buff) {
		buff.append("<body>\n"
				+ "<div id="
				+ MAIN_FRAME
				+ ">\n");
	}
	protected void endBody(StringBuffer buff) {
		buff.append("</div>\n</body>\n");
	}

	protected void layoutBrowserPanel(RecordList<RecordType> recList, int start, LibrisLayout<RecordType> browserLayout, StringBuffer buff) {
		String[] browserFields = browserLayout.getFieldIds();
		buff.append("<div class="+BROWSER_COLUMN_NAME+">\n"
				+ "<ul>\n");
		Iterator<RecordType> recIter = recList.iterator();
		int recCount = 0;
		while (recIter.hasNext() && (recCount < BrowserWindow.LIST_LIMIT)) {
			RecordType rec = recIter.next();
			if (rec.getRecordId() < start) {
				continue;
			}
			buff.append("<li class="+BROWSER_ITEM_CLASS+">"+rec.generateTitle(browserFields)+"</li>\n");
			++recCount;
		}
		buff.append("</ul>\n");
		buff.append(" </div>\n");
	}

	public void layOutPage(RecordList<RecordType> recList, int recId, LibrisLayout<RecordType> browserLayout, 
			DatabaseUi<RecordType> ui, HttpServletResponse resp) throws InputException, IOException {
		StringBuffer buff = new StringBuffer(1000);
		generateHeaderAndStylesheet(ui, buff);
		startBody(buff);
		layoutBrowserPanel(recList, 0, browserLayout, buff);
		startDisplayPanel(buff);
		layoutDisplayPanel(recList, recId, buff);
		endDisplayPanel(buff);
		endBody(buff);
		PrintWriter myWriter = resp.getWriter();
		String htmlString = buff.toString();
		myWriter.append(htmlString);
	}

	protected void startDisplayPanel(StringBuffer buff) {
		buff.append("<div class="+DISPLAY_COLUMN_NAME+">\n");
	}

	protected void endDisplayPanel(StringBuffer buff) {
		buff.append("</div>\n");
	}

}
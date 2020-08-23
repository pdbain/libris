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

	protected static final String BROWSER_COLUMN_NAME = ".browserColumn";
	protected static final String BROWSER_ITEM_CLASS = ".browserItem";
	private static final String DISPLAY_COLUMN_NAME = ".displayColumn";

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
	protected void generateHeaderAndStylesheet(StringBuffer buffer) {
		buffer.append("<!DOCTYPE html>\n" + 
				"<html>\n" + 
				"<head>\n" + 
				"<title>");
		buffer.append(myLayout.getTitle());
		buffer.append("</title>\n" + 
				"<style>\n" + 
				"h1 {color: blue}\n" + 
				BROWSER_COLUMN_NAME +
				" {\n" + 
				"float: left;\n"
				+ "width:300px;\n" + 
				"margin: 10px;\n" + 
				"border: 1px solid black;\n" + 
				"}\n" + 
				DISPLAY_COLUMN_NAME +
				" {\n" + 
				"float: left;\n" + 
				"margin: 10px;\n" + 
				"border: 1px solid black;\n" + 
				"}\n" + 
				BROWSER_ITEM_CLASS +
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
				+ "<div id=\"mainFrame\">\n");
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
			buff.append("<li>"+rec.generateTitle(browserFields)+"</li>\n");
			++recCount;
		}
		buff.append("</ul>\n");
		buff.append(" </div>\n");
	}
	
	public void layOutPage(RecordList<RecordType> recList, int recId, LibrisLayout<RecordType> browserLayout, 
			DatabaseUi<RecordType> ui, HttpServletResponse resp) throws InputException, IOException {
		StringBuffer buff = new StringBuffer(1000);
		generateHeaderAndStylesheet(buff);
		startBody(buff);
		layoutBrowserPanel(recList, 0, browserLayout, buff);
		layoutDisplayPanel(recList, recId, buff);
		endBody(buff);
		PrintWriter myWriter = resp.getWriter();
		myWriter.append(buff.toString());
	}
	
}
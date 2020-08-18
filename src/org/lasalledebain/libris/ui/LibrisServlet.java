package org.lasalledebain.libris.ui;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.HttpStatus;
import org.lasalledebain.libris.DatabaseRecord;
import org.lasalledebain.libris.LibrisDatabase;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.RecordId;
import org.lasalledebain.libris.exception.Assertion;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.util.StringUtils;
import org.lasalledebain.libris.xmlUtils.LibrisXMLConstants;

import static java.util.Objects.isNull;
import static org.lasalledebain.libris.exception.Assertion.assertNotNullInputException;

public class LibrisServlet<RecordType extends Record> extends HttpServlet {
	LibrisUi myUi;
	private final Layouts<DatabaseRecord> myLayouts;
	String[] layoutIds;
	private LibrisHtmlLayout<DatabaseRecord> summaryDisplay;
	public LibrisServlet(LibrisUi myUi) throws DatabaseException, InputException {
		this.myUi = myUi;
		database = myUi.getDatabase();
		myLayouts = database.getLayouts();
		layoutIds = null;
		summaryDisplay = myLayouts.getHtmlLayoutByUsage(LibrisXMLConstants.XML_LAYOUT_USAGE_SUMMARYDISPLAY);
		assertNotNullInputException("No layout defined: "+LibrisXMLConstants.XML_LAYOUT_USAGE_SUMMARYDISPLAY, summaryDisplay);
	}

	public static final String HTTP_PARAM_RECORD_ID="recId";
	public static final String HTTP_PARAM_LAYOUT_ID="layout";
	/**
	 * 
	 */
	private static final long serialVersionUID = 4901606755595619744L;
	private final LibrisDatabase database;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		PrintWriter writer = resp.getWriter();
		try {
			String recId = req.getParameter(HTTP_PARAM_RECORD_ID);
			int id;
			LibrisHtmlLayout<DatabaseRecord> theLayout;
			if (StringUtils.isStringEmpty(recId)) {
				id = RecordId.NULL_RECORD_ID;
				theLayout = summaryDisplay;
			} else {
				id = Integer.parseInt(recId);
				String layoutId = req.getParameter(HTTP_PARAM_LAYOUT_ID);
				if (isNull(layoutId)) {
					if (isNull(layoutIds)) layoutIds = myLayouts.getHtmlLayoutIds();
					if (layoutIds.length == 0) throw new InputException("no HTML layouts defined");
					layoutId = layoutIds[0];
				}
				theLayout = myLayouts.getHtmlLayout(layoutId);
				Assertion.assertNotNullInputException("Layout not found: ",  layoutId, theLayout);
			}
			resp.setStatus(HttpStatus.OK_200);
			DatabaseRecord rec = database.getRecord(id);
			LibrisHtmlLayout<DatabaseRecord> summaryLayout = database.getLayouts().getHtmlLayout(LibrisXMLConstants.XML_LAYOUT_USAGE_SUMMARYDISPLAY);
			theLayout.layOutPage(database.getRecords(), id, summaryLayout, myUi, resp);
		} catch (Throwable t) {
			writer.append("Error: "+t.toString());
			database.log(Level.SEVERE, "Error formatting web page: ", t);
			resp.setStatus(HttpStatus.BAD_REQUEST_400);
		}
	}

}

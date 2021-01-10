package org.lasalledebain.libris.ui;

import static java.util.Objects.isNull;
import static org.lasalledebain.libris.exception.Assertion.assertNotNullInputException;

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

public class LibrisServlet<RecordType extends Record> extends HttpServlet implements LibrisHTMLConstants{
	LibrisUi myUi;
	private final Layouts<DatabaseRecord> myLayouts;
	String[] layoutIds;
	private LibrisLayout<DatabaseRecord> summaryDisplay;
	public LibrisServlet(LibrisUi myUi) throws DatabaseException, InputException {
		this.myUi = myUi;
		database = myUi.getLibrisDatabase();
		myLayouts = database.getLayouts();
		layoutIds = null;
		summaryDisplay = myLayouts.getLayoutByUsage(LibrisXMLConstants.XML_LAYOUT_USAGE_SUMMARYDISPLAY);
		assertNotNullInputException("No layout defined: "+LibrisXMLConstants.XML_LAYOUT_USAGE_SUMMARYDISPLAY, summaryDisplay);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 4901606755595619744L;
	private final LibrisDatabase database;

	@Override
	protected synchronized void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		PrintWriter writer = resp.getWriter();
		try {
			int recId;
			LibrisLayout<DatabaseRecord> theLayout;
			{
				String recIdString = req.getParameter(HTTP_PARAM_RECORD_ID);
				if (StringUtils.isStringEmpty(recIdString)) {
					recId = RecordId.NULL_RECORD_ID;
				} else {
					recId = Integer.parseInt(recIdString);
				}
			}

			int startId;
			{
				String startIdString = req.getParameter(HTTP_BROWSER_STARTING_RECORD);
				startId = (StringUtils.isStringEmpty(startIdString)) ? RecordId.NULL_RECORD_ID:  Integer.parseInt(startIdString);
			}
			theLayout = summaryDisplay;
			String layoutId = req.getParameter(HTTP_PARAM_LAYOUT_ID);
			if (isNull(layoutId)) {
				if (isNull(layoutIds)) layoutIds = myLayouts.getLayoutIds();
				layoutId = layoutIds[0];
			}
			theLayout = myLayouts.getLayout(layoutId);
			Assertion.assertNotNullInputException("Layout not found: ",  layoutId, theLayout);
			resp.setStatus(HttpStatus.OK_200);
			LibrisLayout<DatabaseRecord> summaryLayout = database.getLayouts().getLayoutByUsage(LibrisXMLConstants.XML_LAYOUT_USAGE_SUMMARYDISPLAY);
			Assertion.assertNotNull(myUi, "No layout found for "+LibrisXMLConstants.XML_LAYOUT_USAGE_SUMMARYDISPLAY, summaryLayout);
			theLayout.layOutPage(database.getRecords(), new HttpParameters(recId, startId, resp), summaryLayout, myUi);
		} catch (Throwable t) {
			writer.append("Error: "+t.toString());
			LibrisDatabase.log(Level.SEVERE, "Error formatting web page: ", t);
			resp.setStatus(HttpStatus.BAD_REQUEST_400);
		}
	}

}

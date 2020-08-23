package org.lasalledebain.libris.ui;

import static org.lasalledebain.Utilities.testLogger;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import org.junit.Test;
import org.lasalledebain.Utilities;
import org.lasalledebain.libris.DatabaseRecord;
import org.lasalledebain.libris.LibrisDatabase;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.xmlUtils.LibrisXMLConstants;

import junit.framework.TestCase;

public class HtmlTests extends TestCase {

	private static final String ANYSTRING = "(\\R|.)*";
	private File workingDirectory;
	private LibrisDatabase db;

	@Test
	public void test() throws InputException, IOException, DatabaseException {
		TestResponse resp = new TestResponse();
		Layouts<DatabaseRecord> myLayouts = db.getLayouts();
		LibrisLayout<DatabaseRecord> myLayout = myLayouts.getLayoutByUsage(LibrisXMLConstants.XML_LAYOUT_TYPE_PARAGRAPH);
		LibrisLayout<DatabaseRecord> browserLayout = myLayouts.getLayoutByUsage(LibrisXMLConstants.XML_LAYOUT_USAGE_SUMMARYDISPLAY);
		myLayout.layOutPage(db.getRecords(), 2, browserLayout, db.getUi(), resp);
		String result = resp.getResponseText();
		if (!result.matches(ANYSTRING+"<head>"
				+ANYSTRING+"<style>"
				+ANYSTRING+"</style>"
				+ANYSTRING+"</head>"
				+ANYSTRING+"<body>"
				+ANYSTRING+"A First History of France"
				+ANYSTRING+"</body>"
				+ANYSTRING
				)) {
			fail("Missing body text in HTML output;");
		}
	}

	@Override
	protected void setUp() throws Exception {
		testLogger.log(Level.INFO, "Starting "+getName());
		workingDirectory = Utilities.makeTempTestDirectory();
		db = Utilities.buildTestDatabase( workingDirectory, Utilities.HTML_TEST_DATABASE);
	}

	@Override
	protected void tearDown() throws Exception {
		testLogger.log(Level.INFO, "Ending "+getName());
		Utilities.deleteWorkingDirectory();
	}
}

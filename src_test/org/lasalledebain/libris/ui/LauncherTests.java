package org.lasalledebain.libris.ui;

import static org.lasalledebain.Utilities.testLogger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.http.HttpClient;
import java.util.logging.Level;

import org.junit.Test;
import org.lasalledebain.Utilities;
import org.lasalledebain.libris.DatabaseRecord;
import org.lasalledebain.libris.Libris;
import org.lasalledebain.libris.LibrisDatabase;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.util.LibrisTestLauncher;

import junit.framework.TestCase;

public class LauncherTests extends TestCase {
	private File workingDirectory;

	@Test
	public void testSanity() throws FileNotFoundException, IOException, DatabaseException, InputException {
		LibrisDatabase db = Utilities.buildTestDatabase( workingDirectory, Utilities.HTML_TEST_DATABASE);
		String directoryFilePath = db.getDatabaseFile().getAbsolutePath();
		assertTrue("Could not close database", db.closeDatabase(false));
		LibrisUi<DatabaseRecord> ui = LibrisTestLauncher.testMain(new String[] {directoryFilePath});
		assertNotNull("Failed to open database UI", ui);
		db = ui.getDatabase();
		assertNotNull("Failed to open database", db);
		DatabaseRecord rec = db.getRecord(2);
		assertNotNull("Failed to get record", rec);
		ui.stop();
	}

	@Test
	public void testHtmlServer() throws FileNotFoundException, IOException, DatabaseException, InputException {
		LibrisDatabase db = Utilities.buildTestDatabase( workingDirectory, Utilities.HTML_TEST_DATABASE);
		String directoryFilePath = db.getDatabaseFile().getAbsolutePath();
		assertTrue("Could not close database", db.closeDatabase(false));
		LibrisUi<DatabaseRecord> ui = LibrisTestLauncher.testMain(new String[] {"-w", directoryFilePath});
		assertNotNull("Failed to open database UI", ui);
		HttpClient client = HttpClient.newHttpClient();
		String responseString = HtmlTests.sendHttpRequest(client, "?layout=LO_paragraphDisplay&recId=3");
		HtmlTests.checkExpectedStrings(responseString, HtmlTests.expectedBasicWords);
		ui.stop();
	}

	@Test
	public void testHtmlPort() throws FileNotFoundException, IOException, DatabaseException, InputException {
		LibrisDatabase db = Utilities.buildTestDatabase( workingDirectory, Utilities.HTML_TEST_DATABASE);
		String directoryFilePath = db.getDatabaseFile().getAbsolutePath();
		assertTrue("Could not close database", db.closeDatabase(false));
		final String port = "8123";
		LibrisUi<DatabaseRecord> ui = LibrisTestLauncher.testMain(new String[] {Libris.OPTION_WEB, Libris.OPTION_PORT,
				port, directoryFilePath});
		assertNotNull("Failed to open database UI", ui);
		HttpClient client = HttpClient.newHttpClient();
		String responseString = HtmlTests.sendHttpRequest(client, port, "?layout=LO_paragraphDisplay&recId=3");
		HtmlTests.checkExpectedStrings(responseString, HtmlTests.expectedBasicWords);
		ui.stop();
	}

	@Override
	protected void setUp() throws Exception {
		testLogger.log(Level.INFO, "Starting "+getName());
		workingDirectory = Utilities.makeTempTestDirectory();
	}

	@Override
	protected void tearDown() throws Exception {
		testLogger.log(Level.INFO, "Ending "+getName());
		Utilities.deleteWorkingDirectory();
	}
}
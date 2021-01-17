package org.lasalledebain.libris.ui;

import static org.lasalledebain.libris.util.Utilities.testLogger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.http.HttpClient;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;

import org.junit.Test;
import org.lasalledebain.libris.DatabaseRecord;
import org.lasalledebain.libris.Field;
import org.lasalledebain.libris.Libris;
import org.lasalledebain.libris.LibrisDatabase;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.util.LibrisTestLauncher;
import org.lasalledebain.libris.util.Utilities;

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
		db = ui.getLibrisDatabase();
		assertNotNull("Failed to open database", db);
		DatabaseRecord rec = db.getRecord(2);
		assertNotNull("Failed to get record", rec);
		ui.stop();
		assertTrue("Failed to close database", ui.closeDatabase(false));
		assertTrue("Failed to quit", ui.quit(false));
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
		LibrisUi<DatabaseRecord> ui = LibrisTestLauncher.testMain(new String[] {Libris.OPTION_WEBUI, Libris.OPTION_PORT,
				port, directoryFilePath});
		assertNotNull("Failed to open database UI", ui);
		HttpClient client = HttpClient.newHttpClient();
		String responseString = HtmlTests.sendHttpRequest(client, port, "?layout=LO_paragraphDisplay&recId=3");
		HtmlTests.checkExpectedStrings(responseString, HtmlTests.expectedBasicWords);
		ui.stop();
	}

	@Test
	public void testRebuild() throws Exception {
		tearDown();
		setUp();
		Utilities.copyTestDatabaseFile(Utilities.TEST_DB4_NOMETADATA_FILE, workingDirectory);
		Utilities.copyTestDatabaseFile(Utilities.TEST_DB4_METADATAONLY_FILE, workingDirectory);
		String directoryFilePath = (new File(workingDirectory, Utilities.TEST_DB4_NOMETADATA_FILE)).getAbsolutePath();
		LibrisUi<DatabaseRecord> ui = LibrisTestLauncher.testMain(new String[] {Libris.OPTION_REBUILD, directoryFilePath});
		assertNotNull("Failed to open database UI", ui);
		ui = LibrisTestLauncher.testMain(new String[] {Libris.OPTION_CMDLINEUI, directoryFilePath});
		LibrisDatabase db = ui.getLibrisDatabase();
		String databaseCopy = (new File(workingDirectory, "databaseCopy.libr")).getAbsolutePath();
		db.exportDatabaseXml(new FileOutputStream(databaseCopy));
		String databaseCopyContent = Files.readString(Path.of(databaseCopy));
		assertFalse("Database copy contains metadata", databaseCopyContent.contains("<schema>") || databaseCopyContent.contains("<metadata>"));
		DatabaseRecord rec = db.getRecord(2);
		assertNotNull("Failed to get record", rec);
		Field fld = rec.getField(5);
		assertNotNull("Missing record field", fld);
		String value = fld.getValuesAsString();
		assertEquals("Wrong field value", "Ulysses Odysseus Trojan war", value);
	}
	
	@Test
	public void testSeparateSchema() throws Exception {
		
	}
	
	@Override
	protected void setUp() throws Exception {
		testLogger.log(Level.INFO,this.getClass().getName()+" running "+getName());
		workingDirectory = Utilities.makeTempTestDirectory();
	}

	@Override
	protected void tearDown() throws Exception {
		testLogger.log(Level.INFO, "Ending "+getName());
		Utilities.deleteWorkingDirectory();
	}
}
package org.lasalledebain.libris.ui;

import static org.lasalledebain.Utilities.testLogger;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.logging.Level;

import org.junit.Test;
import org.lasalledebain.Utilities;
import org.lasalledebain.libris.DatabaseRecord;
import org.lasalledebain.libris.LibrisDatabase;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.indexes.LibrisDatabaseConfiguration;
import org.lasalledebain.libris.xmlUtils.LibrisXMLConstants;

import junit.framework.TestCase;

public class HtmlTests extends TestCase {

	private File workingDirectory;
	private LibrisDatabase db;

	@Test
	public void testBasicLayout() throws InputException, IOException, DatabaseException {
		TestResponse resp = new TestResponse();
		Layouts<DatabaseRecord> myLayouts = db.getLayouts();
		LibrisLayout<DatabaseRecord> myLayout = myLayouts.getLayoutByUsage(LibrisXMLConstants.XML_LAYOUT_TYPE_PARAGRAPH);
		LibrisLayout<DatabaseRecord> browserLayout = myLayouts.getLayoutByUsage(LibrisXMLConstants.XML_LAYOUT_USAGE_SUMMARYDISPLAY);
		myLayout.layOutPage(db.getRecords(), new HttpParameters(2, 0, resp), browserLayout, db.getUi());
		String result = resp.getResponseText();
		checkResult(result);
	}

	private void checkResult(String result) {
		String expectedWords[] = {"<head>", "<style>", "</style>", "</head>", "<body>", "A First History of France", "</body>"};
		int pos = 0;
		for (String expWord: expectedWords) {
			pos = result.indexOf(expWord, pos);
			if (pos < 0) {
				fail("Missing body text \""
						+expWord
						+ "\" in HTML output:\n"+result);
			}
		}
	}

	@Test
	public void testHtmlServer() throws Exception {
		LibrisHttpServer<DatabaseRecord> ui = new LibrisHttpServer<DatabaseRecord>(LibrisHttpServer.default_port, LibrisHttpServer.DEFAULT_CONTEXT);
		File dbFile = db.getDatabaseFile();
		assertTrue("Cannot close database", db.closeDatabase(false));
		LibrisDatabaseConfiguration config = new LibrisDatabaseConfiguration(dbFile);
		ui.openDatabase(config);
		StringBuffer response = new StringBuffer();
		ui.startServer();
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create("http://localhost:8080/libris/?layout=LO_paragraphDisplay&recId=2"))
				.build();
		client.sendAsync(request, BodyHandlers.ofString())
		.thenApply(HttpResponse::body)
		.thenAccept(s->response.append(s))
		.join();
		ui.stop();
		checkResult(response.toString());
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

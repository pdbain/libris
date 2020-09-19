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

public class HtmlTests extends TestCase implements LibrisHTMLConstants {

	private File workingDirectory;
	private LibrisDatabase db;
	static final String expectedBasicWords[] = {"<head>", "<style>", "</style>", "</head>", "<body>", "A First History of France", "</body>"};
	private static final String expectedFormWords[] = {"<head>", "<style>", "</style>", "</head>", "<body>", "<div>", 
			FIELD_TITLE_TEXT_CLASS, "Title", FIELD_TEXT_CLASS, "Rec4Fld2Val1", "</body>"};
	private static final String expectedTableWords[] = {"<body>", "<div>", 
			FIELD_TITLE_TEXT_CLASS, "Keywords", FIELD_TEXT_CLASS, "Ulysses", "</body>"};
	private static final String expectedXmlWords[] = {"<record id=\"2\">",
			"<field id=\"ID_auth\" value=\"Rec2Fld1Val1\"/>",
			"<field id=\"ID_title\" value=\"testMissingField\"/>",
			"<field id=\"ID_pages\" value=\"365\"/>",
			"<field id=\"ID_keywords\" value=\"Ulysses Odysseus Trojan war\"/>", 
			"<field id=\"ID_location\" value=\"https://httpbin.org/html\"/>", 
			"<field id=\"ID_hardcopy\" value=\"true\"/>", 
			"</record>", 
			""};
	private static final String expectedListWords[] = {
			"1", "Rec1Fld1Val1", "Rec1Fld2Val1", "1-2", "Enumv1", "true", "k1 k2 k3", "2", "Rec2Fld1Val1",
			"testMissingField", "365", "true", "Ulysses", "Odysseus", "Trojan", "war", "3", "Rec3Fld1Val1",
			"A First History of France", "301", "other", "true", "Ulysses", "Odysseus", "Trojan", "war", "4",
			"Rec4Fld1Val1, Rec4Fld1Val2", "Rec4Fld2Val1", "Rec4Fld3Val1.1-Rec4Fld3Val1.2", "NS_e6", "true",
			"k13 k14 k15"
			};
	@Test
	public void testBasicLayout() throws InputException, IOException, DatabaseException {
		TestResponse resp = new TestResponse();
		Layouts<DatabaseRecord> myLayouts = db.getLayouts();
		LibrisLayout<DatabaseRecord> myLayout = myLayouts.getLayoutByUsage(LibrisXMLConstants.XML_LAYOUT_TYPE_PARAGRAPH);
		LibrisLayout<DatabaseRecord> browserLayout = myLayouts.getLayoutByUsage(LibrisXMLConstants.XML_LAYOUT_USAGE_SUMMARYDISPLAY);
		myLayout.layOutPage(db.getRecords(), new HttpParameters(2, 0, resp), browserLayout, db.getUi());
		String result = resp.getResponseText();
		checkExpectedStrings(result, expectedBasicWords);
	}

	protected static void checkExpectedStrings(String result, String[] expectedWords) {
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
		ui.startServer();
		HttpClient client = HttpClient.newHttpClient();
		String responseString = sendHttpRequest(client, "?layout=LO_paragraphDisplay&recId=3");
		checkExpectedStrings(responseString, expectedBasicWords);
		responseString = sendHttpRequest(client, "?layout=LO_formDisplay&recId=4");
		checkExpectedStrings(responseString, expectedFormWords);
		responseString = sendHttpRequest(client, "?layout=LO_tableDisplay&recId=2");
		checkExpectedStrings(responseString, expectedTableWords);
		responseString = sendHttpRequest(client, "?layout=LO_xmlDisplay&recId=2");
		checkExpectedStrings(responseString, expectedXmlWords);
		responseString = sendHttpRequest(client, "?layout=LO_listDisplay&recId=3");
		checkExpectedStrings(responseString, expectedListWords);
		ui.stop();
	}

	public static String sendHttpRequest(HttpClient client, String urlParams) {
		String port = "8080";
		return sendHttpRequest(client, port, urlParams);
	}

	static String sendHttpRequest(HttpClient client, String port, String urlParams) {
		StringBuffer response = new StringBuffer();
		String serverAddress = "http://localhost:"
				+ port
				+ "/libris/";
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(serverAddress
						+ urlParams))
				.build();
		client.sendAsync(request, BodyHandlers.ofString())
		.thenApply(HttpResponse::body)
		.thenAccept(s->response.append(s))
		.join();
		String responseString = response.toString();
		return responseString;
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

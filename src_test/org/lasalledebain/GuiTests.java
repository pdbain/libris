package org.lasalledebain;

import static org.lasalledebain.libris.util.Utilities.testLogger;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import org.lasalledebain.libris.LibrisDatabase;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.Schema;
import org.lasalledebain.libris.XmlSchema;
import org.lasalledebain.libris.ui.Layouts;
import org.lasalledebain.libris.util.Utilities;
import org.lasalledebain.libris.xmlUtils.ElementManager;

import junit.framework.TestCase;


public class GuiTests extends TestCase {
	private Schema mySchema;
	private File workingDirectory;
	public void testGuiLayoutXml() {
		File testDir = Utilities.getTestDataDirectory();
		File inputFile = new File(testDir, "layoutDeclarations.xml");
		try {
			ElementManager mgr = Utilities.makeElementManagerFromFile(inputFile, "layouts");

			loadSchema();
			Layouts<Record> myLayouts = new Layouts<Record>(mySchema);
			myLayouts.fromXml(mgr);
		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception: "+e);
		}

	}

	public void testDisplayRecord() {
		try {
			LibrisDatabase db = buildDatabase();
			for (int i=1; i <= 3; ++i) {
				db.viewRecord(i);
			}
			db.getUi().quit(true);
		} catch (Throwable e) {
			e.printStackTrace();
			fail("Unexpected exception: "+e.getMessage());
		}
		

	}

	private LibrisDatabase buildDatabase() throws IOException {
		File testDatabaseFileCopy1 = Utilities.copyTestDatabaseFile(Utilities.TEST_DB1_XML_FILE, workingDirectory);
		File testDatabaseFileCopy = testDatabaseFileCopy1;
		LibrisDatabase db = null;
		try {
			db = Utilities.buildAndOpenDatabase(testDatabaseFileCopy);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Unexpected exception "+e);
		}
		return db;
	}
	
	void loadSchema()  {

		File testDir = Utilities.getTestDataDirectory();
		File inputFile = new File(testDir, "schema.xml");
		try {
			mySchema = XmlSchema.loadSchema(inputFile);
		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception: "+e);
		}
	}

	@Override
	protected void setUp() throws Exception {
		workingDirectory = Utilities.makeTempTestDirectory();
		testLogger.log(Level.INFO,this.getClass().getName()+" running "+getName());
	}
// TODO test required fields
}

package org.lasalledebain;

import static org.lasalledebain.libris.util.Utilities.testLogger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;

import org.lasalledebain.libris.Libris;
import org.lasalledebain.libris.LibrisDatabase;
import org.lasalledebain.libris.Schema;
import org.lasalledebain.libris.XmlSchema;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.field.FieldSingleStringValue;
import org.lasalledebain.libris.ui.AbstractUi;
import org.lasalledebain.libris.ui.DatabaseUi;
import org.lasalledebain.libris.ui.Layouts;
import org.lasalledebain.libris.ui.LibrisGui;
import org.lasalledebain.libris.ui.LibrisWindowedUi;
import org.lasalledebain.libris.util.LibrisTestLauncher;
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
			Layouts myLayouts = new Layouts(mySchema);
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
	public void testModifySaveRecord() throws FileNotFoundException, IOException, LibrisException {
		try (LibrisDatabase db = Utilities.buildTestDatabase( workingDirectory, Utilities.TEST_DB1_XML_FILE)) {
			String databaseFilePath = db.getDatabaseFile().getAbsolutePath();
			assertTrue("Could not close database", db.closeDatabase(false));
			AbstractUi ui = LibrisTestLauncher.testMain(new String[] {Libris.OPTION_GUI,  databaseFilePath});
			assertNotNull("Failed to open database UI", ui);
			var rec = ui.displayRecord(1);
			assertNotNull("Failed to display record", ui);
			DatabaseUi mainUi = ui.getMainUi();
			assertTrue("wrong UI type", mainUi instanceof LibrisWindowedUi);
			LibrisGui theGui = (LibrisGui) mainUi;
			var theWindow = theGui.getCurrentRecordWindow();
			final String testFieldName = "ID_auth";
			var theField = theWindow.getField(testFieldName);
			final String newFieldValue = "newAuthor";
			FieldSingleStringValue val = new FieldSingleStringValue(newFieldValue);
			theField.setFieldValues(new FieldSingleStringValue[] {val});
			assertEquals("Wrong number of values in GUI field", theField.getNumValues(), 1);
			theWindow.enter();
			theWindow.close();
			rec = ui.displayRecord(1);
			assertEquals("Wrong number values in record", rec.getFieldValue(testFieldName).getMainValueAsString(), newFieldValue);
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

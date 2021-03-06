package org.lasalledebain.libris.ui;

import static org.lasalledebain.LibrisTestSuite.ignoreUnimplemented;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.lasalledebain.libris.DatabaseRecord;
import org.lasalledebain.libris.GenericDatabase;
import org.lasalledebain.libris.LibrisDatabase;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.indexes.LibrisDatabaseConfiguration;
import org.lasalledebain.libris.util.Utilities;

import junit.framework.TestCase;;

public class ImportTests extends TestCase {
	private File testDatabaseFileCopy;
	private GenericDatabase<DatabaseRecord> db;
	private File workingDirectory;
	
	public void setUp() {
		testDatabaseFileCopy = null;
		workingDirectory = Utilities.makeTempTestDirectory();
		try {
			File testDatabaseFileCopy1 = Utilities.copyTestDatabaseFile(Utilities.TEST_DB1_XML_FILE, workingDirectory);
			testDatabaseFileCopy = testDatabaseFileCopy1;
		} catch (IOException e1) {
			e1.printStackTrace();
			fail("test database missing");
		}
	
		try {
			db = Utilities.buildAndOpenDatabase(testDatabaseFileCopy);
		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception "+e);
		}
	}
	
	public void testImportBasic() throws FileNotFoundException, IOException {
		File importData = Utilities.copyTestDatabaseFile(Utilities.TEST_DELIM_TEXT_FILE_WITH_FIELD_IDS, workingDirectory);
		try {
			Record[] recList = LibrisDatabase.importDelimitedTextFile(db, importData, null, ',');
			db.save();
			for (Record rec: recList) {
				Utilities.trace(rec.toString());
			}
			db.getUi().quit(true);
		} catch (DatabaseException e) {
			e.printStackTrace();
			fail("Unexpected exception");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			fail("Unexpected exception");
		} catch (LibrisException e) {
			e.printStackTrace();
			fail("Unexpected exception");
		}
	}
	
	public void testBuildFromDatabaseFile() throws LibrisException, Exception {
		File databaseFile = Utilities.copyTestDatabaseFile(Utilities.EXAMPLE_DATABASE1_FILE, workingDirectory);
		try (TestGUI ui = new TestGUI(databaseFile)) {
			LibrisDatabaseConfiguration config = new LibrisDatabaseConfiguration(databaseFile);
			ui.buildDatabase(config);
		}
	}
	public void testMalformedTextFile() {
		if (!ignoreUnimplemented) {
			fail("not implememented");
		}
	}
	public void testTabDelimitedTextFile() {
		if (!ignoreUnimplemented) {
			fail("not implememented");
		}
	}
	public void testCommaTextFile() {
		if (!ignoreUnimplemented) {
			fail("not implememented");
		}
	}
	public void testAssignFields() {
		if (!ignoreUnimplemented) {
			fail("not implememented");
		}
	}
	public void testUnassignedFields() {
		if (!ignoreUnimplemented) {
			fail("not implememented");
		}
	}
	public void testEmptyFile() {
		if (!ignoreUnimplemented) {
			fail("not implememented");
		}
	}
	public void testFileWithOnlyFieldIds() {
		if (!ignoreUnimplemented) {
			fail("not implememented");
		}
	}
	public void testBogusFieldIds() {
		if (!ignoreUnimplemented) {
			fail("not implememented");
		}
	}
	public void testExtraData() {
		if (!ignoreUnimplemented) {
			fail("not implememented");
		}
	}
}

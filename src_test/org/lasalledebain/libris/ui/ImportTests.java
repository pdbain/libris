package org.lasalledebain.libris.ui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import junit.framework.TestCase;

import org.lasalledebain.Utilities;
import org.lasalledebain.libris.Libris;
import org.lasalledebain.libris.LibrisDatabase;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.LibrisException;
import static org.lasalledebain.LibrisTestSuite.ignoreUnimplemented;;

public class ImportTests extends TestCase {
	private File testDatabaseFileCopy;
	private LibrisDatabase db;
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
			db = Libris.buildAndOpenDatabase(testDatabaseFileCopy);
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

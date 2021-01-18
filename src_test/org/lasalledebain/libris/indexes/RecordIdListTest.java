package org.lasalledebain.libris.indexes;


import static org.lasalledebain.libris.util.Utilities.DATABASE_WITH_GROUPS_XML;

import java.io.File;
import java.util.ArrayList;

import junit.framework.TestCase;

import org.junit.After;
import org.lasalledebain.libris.DatabaseRecord;
import org.lasalledebain.libris.GenericDatabase;
import org.lasalledebain.libris.util.Utilities;

public class RecordIdListTest extends TestCase {
	File testDatabaseFileCopy;
	GenericDatabase<DatabaseRecord> db;
	private File workingDirectory;

	public void testNameList() {
		SortedKeyValueFileManager<KeyIntegerTuple> nameList = db.getNamedRecordIndex();
		assertNotNull("nameList null", nameList);
		ArrayList<KeyIntegerTuple> actualNames = new ArrayList<KeyIntegerTuple>();
		for(KeyIntegerTuple recName: nameList) {
			actualNames.add(recName);
		}
	}
	
	public void setUp() {
		try {
			workingDirectory = Utilities.makeTempTestDirectory();
			testDatabaseFileCopy = Utilities.copyTestDatabaseFile(DATABASE_WITH_GROUPS_XML, workingDirectory);
			db = Utilities.buildAndOpenDatabase(testDatabaseFileCopy);
			System.out.println("database rebuilt");
		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception "+e.getMessage());
		}
	}
	@After
	public void tearDown() throws Exception {
		Utilities.deleteTestDatabaseFiles(DATABASE_WITH_GROUPS_XML);
	}

}

package org.lasalledebain.group;

import java.io.File;

import junit.framework.TestCase;

import org.junit.Test;
import org.lasalledebain.libris.DatabaseRecord;
import org.lasalledebain.libris.LibrisDatabase;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.indexes.GroupManager;
import org.lasalledebain.libris.util.Utilities;

public class GroupManagerTests extends TestCase {

	private LibrisDatabase db;
	private File workingDirectory;

	@Override
	protected void setUp() throws Exception {
		workingDirectory = Utilities.makeTempTestDirectory();
		File testDatabaseFileCopy1 = Utilities.copyTestDatabaseFile(Utilities.TEST_DB1_XML_FILE, workingDirectory);
		File testDatabaseFileCopy = testDatabaseFileCopy1;
		db = Utilities.buildAndOpenDatabase(testDatabaseFileCopy);
	}

	@Test
	public void testGetRecord() {
		GroupManager<DatabaseRecord> mgr = db.getGroupMgr();
		try {
			Record dbRec = db.getRecord(3);
			Record gmRec = mgr.getRecord(3);
			assertEquals(dbRec, gmRec);
		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception");
		}
	}

}

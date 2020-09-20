package org.lasalledebain;


import static org.lasalledebain.Utilities.getRecordIdString;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.junit.Before;
import org.lasalledebain.libris.DatabaseRecord;
import org.lasalledebain.libris.Field;
import org.lasalledebain.libris.GenericDatabase;
import org.lasalledebain.libris.Libris;
import org.lasalledebain.libris.LibrisDatabase;
import org.lasalledebain.libris.ModifiedRecordList;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.RecordList;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.ui.DatabaseUi;
import org.lasalledebain.libris.ui.HeadlessUi;

import junit.framework.TestCase;

@SuppressWarnings("rawtypes")
public class RecordListTests extends TestCase {

	private static final String EXTRA_DATA = "extra data";
	private static final String ID_AUTH = "ID_auth";
	private static final String[] expectedIds = {"1", "2", "3", "4"};
	private LibrisDatabase testDb;
	private File workingDirectory;

	@Before
	public void setUp() throws Exception {
		workingDirectory = Utilities.makeTempTestDirectory();
		Utilities.deleteTestDatabaseFiles();
		testDb = null;
	}

	@Override
	protected void tearDown() throws Exception {
		if (null != testDb) {
			testDb.getUi().quit(true);
		}
		Utilities.deleteTestDatabaseFiles();
	}

	public void testDatabaseRecordList () {
		try {
			testDb = Libris.buildAndOpenDatabase(Utilities.copyTestDatabaseFile(Utilities.TEST_DB1_XML_FILE, workingDirectory));
			RecordList<DatabaseRecord> list = testDb.getRecords();
			int recordCount = 0;
			for (Record r: list) {
				String id = getRecordIdString(r);
			}
			for (Record r: list) {
				String id = getRecordIdString(r);
				assertEquals("wrong record ID", expectedIds[recordCount], id);
				recordCount++;
			}
			assertEquals("wrong number of records", expectedIds.length, recordCount);
		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception "+e);
		}
	}

	public void testModifiedList () {
		try {
			testDb = Libris.buildAndOpenDatabase(Utilities.copyTestDatabaseFile(Utilities.TEST_DB1_XML_FILE, workingDirectory));
			RecordList<DatabaseRecord> list = testDb.getRecords();
			ModifiedRecordList modList = new ModifiedRecordList();
			ArrayList<Record> foundRecords = new ArrayList<Record>();

			for (Record r: list) {
				foundRecords.add(r);
				modList.addRecord(r);
			}
			for (Record r: foundRecords) {
				Record rr = modList.getRecord(r.getRecordId());
				assertTrue("record not retrieved using record's own RecordId", r == rr);
			}
			for (Record r: foundRecords) {
				int oldId = r.getRecordId();
				int newId = oldId;
				assertEquals("RecordId.equals fails", newId, oldId);
				Record rr = modList.getRecord(newId);
				assertTrue("record not retrieved using new RecordId", r == rr);
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception "+e);
		}
	}
	
	public void testAddNewRecord() {

		File testDatabaseFileCopy;
		try {
			testDatabaseFileCopy = getTestDatabase();
			testDb = Libris.buildAndOpenDatabase(testDatabaseFileCopy);
			final DatabaseUi myUi = testDb.getUi();
			DatabaseRecord rec = testDb.newRecord();
			rec.addFieldValue(ID_AUTH, "new record");
			int id = testDb.putRecord(rec);
			testDb.save();
			testDb.getUi().quit(true);
			testDb = myUi.openDatabase();
			Record newRec = testDb.getRecord(id);
			assertEquals("New record does not match original", rec, newRec);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Unexpected exception");
		}
	}

	private File getTestDatabase() throws IOException {
		File testDatabaseFileCopy1 = Utilities.copyTestDatabaseFile(Utilities.TEST_DB1_XML_FILE, workingDirectory);
		File testDatabaseFileCopy = testDatabaseFileCopy1;
		return testDatabaseFileCopy;
	}
	
	public void testMultipleSaves() {

		File testDatabaseFileCopy;
		try {
			testDatabaseFileCopy = getTestDatabase();
			testDb = Libris.buildAndOpenDatabase(testDatabaseFileCopy);
			final DatabaseUi myUi = testDb.getUi();
			DatabaseRecord rec = testDb.newRecord();
			rec.addFieldValue(ID_AUTH, "new record");
			testDb.putRecord(rec);
			testDb.save();
			testDb.save();
			testDb.save();
			testDb.getUi().quit(true);
			testDb = myUi.openDatabase();
			RecordList<DatabaseRecord> list = testDb.getRecords();
			int recordCount = 0;
			for (@SuppressWarnings("unused") Record r: list) {
				recordCount++;
			}
			assertEquals("wrong number of records", expectedIds.length+1, recordCount);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Unexpected exception");
		}
	
	}
	
	public void testModifyRecord() {

		String shortValue = "x";
		String longvalue = "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX" +
				"XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX" +
				"XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX" +
				"XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX";
		File testDatabaseFileCopy;
		try {
			testDatabaseFileCopy = getTestDatabase();
			testDb = Libris.buildAndOpenDatabase(testDatabaseFileCopy);
			testDb.getUi().quit(true);
			final DatabaseUi myUi = testDb.getUi();
			for (int r=1; r <= expectedIds.length; ++r) {
				int oldId = r;
				testDb = myUi.openDatabase();
				DatabaseRecord rec = testDb.getRecord(oldId);
				rec.addFieldValue("ID_title", ((r % 2) == 0)? shortValue: longvalue);
				int newId = testDb.putRecord(rec);
				assertEquals("new ID != oldId", newId, oldId);
				testDb.save();
				testDb.getUi().quit(true);
				testDb = myUi.openDatabase();
				DatabaseRecord newRec = testDb.getRecord(newId);
				assertEquals("New record does not match original", rec, newRec);
				testDb.getUi().quit(true);
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail("Unexpected exception");
		}
	}
	
	public void testRecordOrder() {
		String dbName = Utilities.TEST_DATABASE_UNORDERED_XML;
		File testDatabaseFileCopy;
		String expectedData[] = {null, "record1", "record2", "record3", "record4"};
		try {
			testDatabaseFileCopy = Utilities.copyTestDatabaseFile(dbName, workingDirectory);
			 testDb = Libris.buildAndOpenDatabase(testDatabaseFileCopy);

			checkRecordOrder(testDb, expectedData);

			DatabaseRecord r = testDb.getRecord(3);
			Field f = r.getField(ID_AUTH);
			f.addValue(EXTRA_DATA);
			expectedData[3] += ", "+EXTRA_DATA;
			testDb.putRecord(r);
			testDb.save();
			testDb.getUi().quit(true);
			testDb = null;
			
			HeadlessUi ui = new HeadlessUi(testDatabaseFileCopy, false);
			testDb = ui.openDatabase();
			checkRecordOrder(ui.getDatabase(), expectedData);
			
			testDb.exportDatabaseXml(new FileOutputStream(testDatabaseFileCopy), true, true, false);
			ui.quit(true);
			testDb = Libris.buildAndOpenDatabase(testDatabaseFileCopy);
			checkRecordOrder(testDb, expectedData);
			/*
			 * load database with unordered records
			 * read records and check that they are in order
			 * edit a record to make it larger and save it
			 * read & check records
			 * save, close, and reopen database
			 * check order
			 * export database and rebuild
			 * check records
			 */
			testDb.getUi().quit(true);
		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception "+e.getMessage());
		}			
	}

	private void checkRecordOrder(GenericDatabase<DatabaseRecord> db, String[] expectedData)
			throws LibrisException {
		int expectedId = 1;
		for (Record r: db.getDatabaseRecords()) {
			assertEquals("Wrong record ID", expectedId, r.getRecordId());
			assertEquals("wrong data", expectedData[expectedId], r.getField(ID_AUTH).getValuesAsString());
			++expectedId;
		}
	}
}

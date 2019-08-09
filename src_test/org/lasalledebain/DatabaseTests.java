package org.lasalledebain;

import static org.lasalledebain.Utilities.DATABASE_WITH_GROUPS_AND_RECORDS_XML;
import static org.lasalledebain.Utilities.DATABASE_WITH_GROUPS_XML;
import static org.lasalledebain.Utilities.checkRecords;
import static org.lasalledebain.Utilities.testLogger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Level;

import org.junit.Test;
import org.lasalledebain.libris.DatabaseInstance;
import org.lasalledebain.libris.DatabaseRecord;
import org.lasalledebain.libris.Field;
import org.lasalledebain.libris.Libris;
import org.lasalledebain.libris.LibrisDatabase;
import org.lasalledebain.libris.LibrisMetadata;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.RecordFactory;
import org.lasalledebain.libris.RecordTemplate;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.ui.LibrisUi;
import org.lasalledebain.libris.xmlUtils.ElementManager;

import junit.framework.TestCase;

public class DatabaseTests extends TestCase {

	private static final String ID_AUTH = "ID_auth";
	private static final String ID_publisher = "ID_publisher";
	private final static String[] authors = {"", "John le Carre", "Homer", "Louise Creighton"};
	private LibrisDatabase rootDb;
	private LibrisDatabase forkDb;
	private File workingDirectory;
	// TODO override auxiliary directory
	public void testReadRecordsFromSingleFile() {
		try {
			File testDatabaseFile = Utilities.copyTestDatabaseFile(Utilities.TEST_DB1_XML_FILE, workingDirectory);
			rootDb =  Libris.buildAndOpenDatabase(testDatabaseFile);
			RecordFactory<DatabaseRecord> rt = RecordTemplate.templateFactory(rootDb.getSchema(), null);
			Vector<Record> recordList = new Vector<Record>();
			ElementManager librisMgr = Utilities.makeElementManagerFromFile(testDatabaseFile, "libris");
			librisMgr.parseOpenTag();
			ElementManager metadataMgr = librisMgr.nextElement();
			metadataMgr.flushElement();
			ElementManager recordsMgr = librisMgr.nextElement();
			recordsMgr.parseOpenTag();
			while (recordsMgr.hasNext()) {
				ElementManager recordMgr = recordsMgr.nextElement();
				Record rec = rt.makeRecord(true);
				rec.fromXml(recordMgr);
				recordList.add(rec);
			}
		} catch (Throwable e) {
			e.printStackTrace();
			fail("unexpected exception");
		}
	}

	public void testGetRecords() {

		final int NUM_RECORDS = 3;
		try {
			File testDatabaseFileCopy = getTestDatabase();
			rootDb = buildTestDatabase(testDatabaseFileCopy);

			for (int i = 1; i <= NUM_RECORDS; ++i) {
				Record rec = rootDb.getRecord(i);
				assertNotNull("Cannot locate "+i, rec);
				String f = rec.getField(ID_AUTH).getValuesAsString();
				assertEquals("Authors field does not match", authors[i], f);
			}
		} catch (Throwable e) {
			e.printStackTrace();
			fail("unexpected exception "+e);
		}
	}

	public void testEnterRecord() {

		final int NUM_RECORDS = 3;
		try {
			File testDatabaseFileCopy = getTestDatabase();
			rootDb = buildTestDatabase(testDatabaseFileCopy);

			for (int i = 1; i <= NUM_RECORDS; ++i) {
				DatabaseRecord rec = rootDb.getRecord(i);
				Field f = rec.getField(ID_AUTH);
				f.changeValue("new value "+i);
				testLogger.log(Level.INFO,rec.toString());
				rootDb.putRecord(rec);
			}
			for (int i = 1; i <= NUM_RECORDS; ++i) {
				Record rec = rootDb.getRecord(i);
				String f = rec.getField(ID_AUTH).getValuesAsString();
				assertEquals("Authors field does not match", "new value "+i, f);
			}
		} catch (Throwable e) {
			e.printStackTrace();
			fail("unexpected exception");
		}
	}

	public void testEnterRecordWithGroupds() {

		final int NUM_RECORDS = 6;
		try {
			File testDatabaseFileCopy = Utilities.copyTestDatabaseFile(Utilities.DATABASE_WITH_GROUPS_AND_RECORDS_XML, workingDirectory);
			rootDb = buildTestDatabase(testDatabaseFileCopy);

			for (int i = 1; i <= NUM_RECORDS; ++i) {
				DatabaseRecord rec = rootDb.getRecord(i);
				rec.setName("Name_"+i);
				rec.addFieldValue(ID_AUTH, "new value "+i);
				testLogger.log(Level.INFO,rec.toString());
				rootDb.putRecord(rec);
			}
			for (int i = 1; i <= NUM_RECORDS; ++i) {
				Record rec = rootDb.getRecord(i);
				String f = rec.getField(ID_AUTH).getValuesAsString();
				assertEquals("Authors field does not match", "new value "+i, f);
				assertEquals("Wrong name", "Name_"+i, rec.getName());
			}
		} catch (Throwable e) {
			e.printStackTrace();
			fail("unexpected exception");
		}
	}

	public void testOpenAndImmediatelyCloseDatabase() {
		try {
			File testDatabaseFileCopy = getTestDatabase();
			rootDb = buildTestDatabase(testDatabaseFileCopy);
			LibrisUi testUi = rootDb.getUi();
			testUi.closeDatabase(false);
			rootDb = testUi.openDatabase();
			testUi.closeDatabase(false);
		} catch (Throwable e) {
			e.printStackTrace();
			fail("unexpected exception");
		}

	}

	public void testOpenIndexAndImmediatelyCloseDatabase() {
		try {
			rootDb = buildTestDatabase(getTestDatabase());
			rootDb.closeDatabase(false);
		} catch (Throwable e) {
			e.printStackTrace();
			fail("unexpected exception");
		}

	}

	public void testMultipleRebuild() {
		try {			
			File testDatabaseFileCopy = getTestDatabase();			
			rootDb = Libris.buildAndOpenDatabase(testDatabaseFileCopy);
			LibrisUi ui = rootDb.getUi();
			LibrisMetadata meta = rootDb.getMetadata();
			int numRecs = meta.getSavedRecords();
			assertEquals("Wrong number of records initial build", 4, numRecs);
			ui.closeDatabase(false);
			rootDb = Libris.buildAndOpenDatabase(testDatabaseFileCopy);
			ui = rootDb.getUi();
			ui.closeDatabase(false);
			forkDb = ui.openDatabase();
			assertEquals("Wrong number of records after rebuild", 4, forkDb.getMetadata().getSavedRecords());
			for (int i = 1; i < authors.length; ++i) {
				Record rec = forkDb.getRecord(i);
				assertNotNull("Cannot locate "+i, rec);
				String f = rec.getField(ID_AUTH).getValuesAsString();
				assertEquals("Authors field does not match", authors[i], f);
			}
		} catch (Throwable e) {
			e.printStackTrace();
			fail("unexpected exception");
		}

	}

	public void testEnumOutOfRange() {
		try {
			rootDb = buildTestDatabase(getTestDatabase());
			LibrisUi testUi = rootDb.getUi();
			String expected = null;
			{
				DatabaseRecord rec = rootDb.getRecord(1);
				Field f = rec.getField(ID_publisher);
				f.addValuePair("", "test data");
				expected = f.getValuesAsString();
				rootDb.putRecord(rec);
				rootDb.save();
				testUi.closeDatabase(false);
			}
			forkDb = testUi.openDatabase();
			Record rec = forkDb.getRecord(1);
			Field f = rec.getField(ID_publisher);
			String actual = f.getValuesAsString();
			assertEquals("Out of range enum wrong", expected, actual);
			testUi.closeDatabase(false);
		} catch (Throwable e) {
			e.printStackTrace();
			fail("unexpected exception");
		}

	}
	public void testXmlExportAll() {
		try {
			rootDb = buildTestDatabase(getTestDatabase());
			File workdir = Utilities.makeTempTestDirectory();
			File copyDbXml = new File (workdir, "database_copy.xml");
			copyDbXml.deleteOnExit();
			FileOutputStream copyStream = new FileOutputStream(copyDbXml);
			testLogger.log(Level.INFO,getName()+": copy database to"+copyDbXml);
			rootDb.exportDatabaseXml(copyStream, true, true, false);
			copyStream.close();

			forkDb = Libris.buildAndOpenDatabase(copyDbXml);
			assertNotNull("Error rebuilding database copy", forkDb);
			assertTrue("database copy does not match original", forkDb.equals(rootDb));
			copyDbXml.delete();
		} catch (Throwable e) {
			e.printStackTrace();
			fail("unexpected exception");
		}
	}

	public void testXmlExportAllWithGroups() {
		try {
			File testDatabaseFileCopy = Utilities.copyTestDatabaseFile(Utilities.DATABASE_WITH_GROUPS_AND_RECORDS_XML, workingDirectory);
			LibrisDatabase db = buildTestDatabase(testDatabaseFileCopy);
			File workdir = Utilities.makeTempTestDirectory();
			File copyDbXml = new File (workdir, "database_copy.xml");
			copyDbXml.deleteOnExit();
			FileOutputStream copyStream = new FileOutputStream(copyDbXml);
			testLogger.log(Level.INFO,getName()+": copy database to"+copyDbXml);
			db.exportDatabaseXml(copyStream, true, true, false);
			copyStream.close();

			LibrisDatabase dbCopy = Libris.buildAndOpenDatabase(copyDbXml);
			assertNotNull("Error rebuilding database copy", dbCopy);
			assertTrue("database copy does not match original", dbCopy.equals(db));
			db.closeDatabase(false);
			dbCopy.closeDatabase(false);
			Utilities.deleteRecursively(testDatabaseFileCopy);
			Utilities.deleteRecursively(copyDbXml);
		} catch (Throwable e) {
			e.printStackTrace();
			fail("unexpected exception");
		}
	}

	public void testAddRecordsToDatabase() {
		final int numRecs = 1024;
		String dbFile = DATABASE_WITH_GROUPS_XML;
		try {
			File testDatabaseFileCopy = Utilities.copyTestDatabaseFile(dbFile, workingDirectory);
			rootDb = Libris.buildAndOpenDatabase(testDatabaseFileCopy);
			testLogger.log(Level.INFO, "database rebuilt");
			int lastId = rootDb.getLastRecordId();
			for (int i = lastId+1; i <= numRecs; ++i) {
				DatabaseRecord rec = rootDb.newRecord();
				int recNum = rootDb.putRecord(rec);
				assertEquals("wrong ID for new record",  i, recNum);
			}
			rootDb.save();
			checkRecords(rootDb, lastId);
		} catch (LibrisException | IOException e) {
			e.printStackTrace();
			fail("unexpected exception "+e.getMessage());
		}
	}

	public void testFork() {
		try {
			File workdir = Utilities.makeTempTestDirectory();
			File dbInstance = new File(workdir, "database_instance.xml");
			{
				rootDb = buildTestDatabase(getTestDatabase());
				File copyDbXml = new File (workdir, "database_copy.xml");
				testLogger.log(Level.INFO,getName()+": copy database to "+copyDbXml);
				copyDbXml.deleteOnExit();
				dbInstance.deleteOnExit();
				FileOutputStream instanceStream = new FileOutputStream(dbInstance);
				testLogger.log(Level.INFO,getName()+": copy database to "+dbInstance);
				rootDb.exportDatabaseXml(instanceStream, true, true, true);
				instanceStream.close();
			}
			int lastId = rootDb.getLastRecordId();
			forkDb = Libris.buildAndOpenDatabase(dbInstance);
			assertNotNull("Error rebuilding database copy", forkDb);
			assertTrue("database copy does not match original", forkDb.equals(rootDb));
			
			rootDb.closeDatabase(false);
			DatabaseInstance inst = forkDb.getMetadata().getInstanceInfo();
			assertNotNull("Database instance information missing", inst);
			assertEquals("Wrong base ID", lastId, inst.getRecordIdBase());
			final int numRecs = 10;
			for (int i = lastId+1; i <= numRecs; ++i) {
				DatabaseRecord rec = forkDb.newRecord();
				int recNum = forkDb.putRecord(rec);
				assertEquals("wrong ID for new record",  i, recNum);
			}
			forkDb.save();
			checkRecords(forkDb, lastId);

			for (int i = 1; i <= numRecs; ++i) {
				try {
					DatabaseRecord r = forkDb.getRecord(i);
					assertNotNull("Cannot find record "+i, r);
					assertEquals("Wrong record ID",  i, r.getRecordId());
					boolean readOnly = forkDb.isRecordReadOnly(i);
					assertEquals("read-only status of record "+i+" incorrect", i <= lastId, readOnly);
					r.addFieldValue(ID_AUTH, "new value "+i);
					int resultId;
					if (i <= lastId) {
						try {
							resultId = forkDb.putRecord(r);
							fail("record " + resultId + " from root database not read-only");
						} catch (DatabaseException e) {

						}
					} else {
						resultId = forkDb.putRecord(r);
						assertEquals("record from fork database wrong ID", i, resultId);
					}
				} catch (InputException e) {
					e.printStackTrace();
					fail("unexpected exception "+e.getMessage());
				}
			}
		
			rootDb.closeDatabase(false);
;
			forkDb.closeDatabase(false);
			dbInstance.delete();
		} catch (Throwable e) {
			e.printStackTrace();
			fail("unexpected exception");
		}
	}

	public void testIncrement() {
		ArrayList<Record> expectedRecords = new ArrayList<>();
		int newRecordNumber;
		try {
			File workdir = Utilities.makeTempTestDirectory();
			File dbInstance = new File(workdir, "database_instance.xml");
			File dbIncrement = new File(workdir, "database_increment.xml");
			rootDb = buildTestDatabase(getTestDatabase());
			for (Record rec: rootDb.getRecords()) {
				expectedRecords.add(rec);
			}
			newRecordNumber = rootDb.getLastRecordId() + 1;
			File copyDbXml = new File (workdir, "database_copy.xml");
			testLogger.log(Level.INFO,getName()+": copy database to "+copyDbXml);
			copyDbXml.deleteOnExit();
			dbInstance.deleteOnExit();
			FileOutputStream instanceStream = new FileOutputStream(dbIncrement);
			testLogger.log(Level.INFO,getName()+": copy database to "+dbIncrement);
			rootDb.exportFork(instanceStream);
			instanceStream.close();
			int lastId = rootDb.getLastRecordId();
			forkDb = Libris.buildAndOpenDatabase(dbIncrement);
			assertNotNull("Error rebuilding database copy", forkDb);
			assertTrue("database copy does not match original", forkDb.equals(rootDb));
			
			DatabaseInstance inst = forkDb.getMetadata().getInstanceInfo();
			assertNotNull("Database instance information missing", inst);
			assertEquals("Wrong base ID", lastId, inst.getRecordIdBase());
			final int numRecs = 10;
			for (int i = lastId+1; i <= numRecs; ++i) {
				DatabaseRecord rec = forkDb.newRecord();
				rec.addFieldValue("ID_title", "Record"+newRecordNumber++);
				int recNum = forkDb.putRecord(rec);
				assertEquals("wrong ID for new record",  i, recNum);
				expectedRecords.add(rec);
			}
			forkDb.save();
			checkDbRecords(forkDb, expectedRecords);
			forkDb.exportIncrement(new FileOutputStream(dbIncrement));
			rootDb.importIncrement(dbIncrement);
			checkDbRecords(rootDb, expectedRecords);
			rootDb.closeDatabase(false);
;
			forkDb.closeDatabase(false);
			dbInstance.delete();
		} catch (Throwable e) {
			e.printStackTrace();
			fail("unexpected exception");
		}
	}

	@Test
	public void testMultipleForks() {
		try {
			ArrayList<Record> expectedRecords = new ArrayList<>();
			ArrayList<File> forkFiles = new ArrayList<>();
			ArrayList<File> incrementFiles = new ArrayList<>();
			int newRecordNumber;
			File workdir = Utilities.makeTempTestDirectory();
			File dbInstance = new File(workdir, "database_instance.xml");
				dbInstance.deleteOnExit();
			rootDb = buildTestDatabase(getTestDatabase());
			for (Record rec: rootDb.getRecords()) {
				expectedRecords.add(rec);
			}
			newRecordNumber = rootDb.getLastRecordId() + 1;
			for (int forkNum = 0; forkNum < 3; ++forkNum) {
				File forkFile = new File (workdir, "database_copy_"+forkNum+".xml");
				testLogger.log(Level.INFO,getName()+": copy database to "+forkFile);
				forkFiles.add(forkFile);
				forkFile.deleteOnExit();
				FileOutputStream forkStream = new FileOutputStream(forkFile);
				testLogger.log(Level.INFO,getName()+": copy database to "+forkFile);
				rootDb.exportFork(forkStream);
				forkStream.close();
				DatabaseRecord newRootRec = rootDb.newRecord();
				String fieldData = "DatabaseRecord"+newRecordNumber;
				newRootRec.addFieldValue("ID_title", fieldData);
				testLogger.log(Level.INFO,"add new record with "+fieldData+" to root database");
				rootDb.putRecord(newRootRec);
				newRecordNumber++;
				rootDb.save();
				expectedRecords.add(newRootRec);
			}
			int incrementNumber = 0;
			for (File forkFile: forkFiles) {
				forkDb = Libris.buildAndOpenDatabase(forkFile);
				DatabaseRecord recCopy;
				{
					DatabaseRecord newForkRec = rootDb.newRecord();
					String fieldData = "Record"+newRecordNumber;
					newForkRec.addFieldValue("ID_title", fieldData);
					int newId = forkDb.putRecord(newForkRec);
					recCopy = newForkRec.duplicate();
					testLogger.log(Level.INFO,"add new record " + newId + " with "+fieldData+" to fork "+incrementNumber);
				}
				forkDb.save();
				recCopy.setRecordId(newRecordNumber);
				expectedRecords.add(recCopy);
				++newRecordNumber;
				File dbIncrement = new File(workdir, "database_increment_"+incrementNumber+".xml");
				dbIncrement.deleteOnExit();
				forkDb.exportIncrement(new FileOutputStream(dbIncrement));
				incrementFiles.add(dbIncrement);
				++incrementNumber;			
			}
			
			for (File incFile: incrementFiles) {
				rootDb.importIncrement(incFile);	
				rootDb.save();
			}
			
			checkDbRecords(rootDb, expectedRecords);
		} catch (IOException | LibrisException e) {
			e.printStackTrace();
			fail("unexpected exception");
		}
	}

	@Test
	public void testAdjustAffiliates() {
		int newRecordNumber;
		try {
			File dbInstance = new File(workingDirectory, "database_instance.xml");
			File dbIncrement = new File(workingDirectory, "database_increment.xml");
			rootDb = buildTestDatabase(Utilities.copyTestDatabaseFile(DATABASE_WITH_GROUPS_AND_RECORDS_XML, workingDirectory));
			newRecordNumber = rootDb.getLastRecordId() + 1;
			File copyDbXml = new File (workingDirectory, "database_copy.xml");
			testLogger.log(Level.INFO,getName()+": copy database to "+copyDbXml);
			copyDbXml.deleteOnExit();
			dbInstance.deleteOnExit();
			FileOutputStream instanceStream = new FileOutputStream(dbInstance);
			testLogger.log(Level.INFO,getName()+": copy database to "+dbInstance);
			rootDb.exportFork(instanceStream);
			int baseId = rootDb.getLastRecordId();
			int baseAffiliate = baseId - 1;
			instanceStream.close();


			DatabaseRecord newRootRec = rootDb.newRecord();
			newRootRec.addFieldValue("ID_title", "Record"+newRecordNumber++);
			rootDb.putRecord(newRootRec);
			rootDb.save();
						
			forkDb = Libris.buildAndOpenDatabase(dbInstance);
			assertNotNull("Error rebuilding database copy", forkDb);

			DatabaseRecord newForkRec1 = forkDb.newRecord();
			newForkRec1.addFieldValue("ID_title", "Record"+newRecordNumber++);
			newForkRec1.setParent(0, baseAffiliate);
			int newRecNum = forkDb.putRecord(newForkRec1);
			
			DatabaseRecord newForkRec2 = forkDb.newRecord();
			newForkRec2.addFieldValue("ID_title", "Record"+newRecordNumber++);
			newForkRec2.setParent(0, newRecNum);
			forkDb.putRecord(newForkRec2);
					
			forkDb.save();
			forkDb.exportIncrement(new FileOutputStream(dbIncrement));
			rootDb.importIncrement(dbIncrement);
			{
				Record rec = rootDb.getRecord(baseId + 1);
				assertEquals("new root record wrong", newRootRec, rec);
				rec = rootDb.getRecord(baseId + 2);
				newForkRec1.setRecordId(baseId + 2);
				assertEquals("new root record wrong", newForkRec1, rec);

				rec = rootDb.getRecord(baseId + 3);
				newForkRec2.setRecordId(baseId + 3);
				int affiliates[] = new int[1];
				affiliates[0] = baseId + 2;
				newForkRec2.setAffiliates(0, affiliates);
				assertEquals("new root record wrong", newForkRec2, rec);
			}
			
			rootDb.closeDatabase(false);
;
			forkDb.closeDatabase(false);
			dbInstance.delete();
		} catch (Throwable e) {
			e.printStackTrace();
			fail("unexpected exception");
		}
	}

	@Test
	public void testAdjustIds() {

		ArrayList<Record> rootExpectedRecords = new ArrayList<>();
		int newRecordNumber;
		try {
			File workdir = Utilities.makeTempTestDirectory();
			File dbInstance = new File(workdir, "database_instance.xml");
			File dbIncrement = new File(workdir, "database_increment.xml");
			rootDb = buildTestDatabase(getTestDatabase());
			for (Record rec: rootDb.getRecords()) {
				rootExpectedRecords.add(rec);
			}
			@SuppressWarnings("unchecked")
			ArrayList<Record> forkExpectedRecords = (ArrayList<Record>) rootExpectedRecords.clone();
			ArrayList<Record> incrementExpectedRecords = new ArrayList<>();
			newRecordNumber = rootDb.getLastRecordId() + 1;
			File copyDbXml = new File (workdir, "database_copy.xml");
			testLogger.log(Level.INFO,getName()+": copy database to "+copyDbXml);
			copyDbXml.deleteOnExit();
			dbInstance.deleteOnExit();
			FileOutputStream instanceStream = new FileOutputStream(dbInstance);
			testLogger.log(Level.INFO,getName()+": copy database to "+dbInstance);
			rootDb.exportFork(instanceStream);
			int baseId = rootDb.getLastRecordId();
			instanceStream.close();
			int extraBaseRecords = 5;
			for (int i = baseId+1; i <= baseId+extraBaseRecords; ++i) {
				DatabaseRecord rec = rootDb.newRecord();
				rec.addFieldValue("ID_title", "Record"+newRecordNumber++);
				int recNum = rootDb.putRecord(rec);
				assertEquals("wrong ID for new record",  i, recNum);
				rootExpectedRecords.add(rec);
			}
			checkDbRecords(rootDb, rootExpectedRecords);
			rootDb.save();
						
			forkDb = Libris.buildAndOpenDatabase(dbInstance);
			assertNotNull("Error rebuilding database copy", forkDb);
			assertFalse("database copy not changed from original", forkDb.equals(rootDb));
			
			DatabaseInstance inst = forkDb.getMetadata().getInstanceInfo();
			assertNotNull("Database instance information missing", inst);
			assertEquals("Wrong base ID", baseId, inst.getRecordIdBase());
			final int numIncrementRecs = 10;
			for (int i = baseId+1; i <= numIncrementRecs; ++i) {
				DatabaseRecord rec = forkDb.newRecord();
				rec.addFieldValue("ID_title", "DatabaseRecord"+newRecordNumber++);
				int recNum = forkDb.putRecord(rec);
				assertEquals("wrong ID for new record",  i, recNum);
				incrementExpectedRecords.add(rec);
			}
			forkDb.save();
			forkExpectedRecords.addAll(forkExpectedRecords.size(), incrementExpectedRecords);
			checkDbRecords(forkDb, forkExpectedRecords);
			forkDb.exportIncrement(new FileOutputStream(dbIncrement));
			rootDb.importIncrement(dbIncrement);
			for (Record r: incrementExpectedRecords) {
				r.setRecordId(r.getRecordId() + extraBaseRecords);
			}
			
			rootExpectedRecords.addAll(rootExpectedRecords.size(), incrementExpectedRecords);
			checkDbRecords(rootDb, rootExpectedRecords);
			rootDb.closeDatabase(false);
;
			forkDb.closeDatabase(false);
			dbInstance.delete();
		} catch (Throwable e) {
			e.printStackTrace();
			fail("unexpected exception");
		}
	}

	@Override
	protected void setUp() throws Exception {
		testLogger.log(Level.INFO,"running "+getName());
		workingDirectory = Utilities.makeTempTestDirectory();
	}

	@Override
	protected void tearDown() throws Exception {
		if (null != rootDb) {
			rootDb.closeDatabase(true);
			rootDb = null;
		}
		if (null != forkDb) {
			forkDb.closeDatabase(true);
			forkDb = null;
		}
		Utilities.deleteWorkingDirectory();
	}

	private void checkDbRecords(LibrisDatabase testDb, ArrayList<Record> expectedRecords) {
		Iterator<Record> expectedRecordIterator = expectedRecords.iterator();
		for (Record actualRecord: testDb.getRecords()) {
			assertTrue("too many records", expectedRecordIterator.hasNext());
			Record e = expectedRecordIterator.next();
			assertEquals("wrong record", e, actualRecord);
		}
		assertFalse("too few records", expectedRecordIterator.hasNext());
	}

	private LibrisDatabase buildTestDatabase(File testDatabaseFileCopy) throws IOException {			
		rootDb = null;
		try {
			rootDb = Libris.buildAndOpenDatabase(testDatabaseFileCopy);
			rootDb.getUi();
		} catch (Throwable e) {
			e.printStackTrace();
			fail("Cannot open database");
		}
		return rootDb;
	}

	private File getTestDatabase() throws IOException {
		String testDbName = Utilities.TEST_DB1_XML_FILE;
		File testDatabaseFileCopy = Utilities.copyTestDatabaseFile(testDbName, workingDirectory);
		return testDatabaseFileCopy;
	}
}

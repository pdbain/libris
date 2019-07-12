package org.lasalledebain;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import org.lasalledebain.libris.DatabaseRecord;
import org.lasalledebain.libris.Field;
import org.lasalledebain.libris.Field.FieldType;
import org.lasalledebain.libris.FileAccessManager;
import org.lasalledebain.libris.LibrisConstants;
import org.lasalledebain.libris.LibrisDatabase;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.RecordFactory;
import org.lasalledebain.libris.Schema;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.FieldDataException;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.exception.RecordDataException;
import org.lasalledebain.libris.indexes.LibrisJournalFileManager;
import org.lasalledebain.libris.util.DiagnosticDatabase;
import org.lasalledebain.libris.xmlUtils.ElementManager;
import org.lasalledebain.libris.xmlUtils.LibrisXMLConstants;

import junit.framework.TestCase;

public class JournalTest extends TestCase implements LibrisConstants, LibrisXMLConstants {

	private File workDir;
	private File journalFile;
	private LibrisDatabase testDatabase;
	private ArrayList<Record> testRecords;
	private String fieldNames[] = {"booltrue", "intfield1", "textfield"};
	private FieldType fts[] = {FieldType.T_FIELD_BOOLEAN, FieldType.T_FIELD_INTEGER, FieldType.T_FIELD_STRING};
	private FileAccessManager journalFileMgr;
	
	
	@Override
	protected void setUp() throws Exception {
		Utilities.deleteWorkingDirectory();
		workDir = Utilities.getTempTestDirectory();
		if (null == workDir) {
			fail("could not create working directory ");
		}
		testDatabase = new DiagnosticDatabase(Utilities.getTestDatabase(Utilities.TEST_DB1_XML_FILE));
		File schemaFile = new File(Utilities.getTestDataDirectory(), Utilities.TEST_SCHEMA2_XML_FILE);
		Schema schem = Utilities.loadSchema(schemaFile);
		testDatabase.setSchema(schem);

		journalFile = new File(workDir, "JournalTest"+'.'+FILENAME_JOURNAL_SUFFIX);
		journalFileMgr = testDatabase.getFileMgr().makeAccessManager(getName(), journalFile);
		Utilities.trace("Journal file: "+journalFile.getPath());
	}
	public void testAddRecord() {
		try {
			String recData[][] = {{"true", "1", "foo"}, {"false", "2", "bar"}};
			int numTestRecords = 2;

			LibrisJournalFileManager<DatabaseRecord> journal = Utilities.createLibrisJournalFileManager(testDatabase, journalFileMgr);
			
			RecordFactory<DatabaseRecord> rt = makeTestRecords(recData, numTestRecords, journal);
			
			journal.close();
			
			ElementManager dbMgr = Utilities.makeElementManagerFromFile(journalFile, XML_LIBRIS_TAG);
			dbMgr.parseOpenTag();
			ElementManager recsMgr = dbMgr.nextElement();
			recsMgr.parseOpenTag();
			
			Iterator<Record> expectedRecords = testRecords.iterator();
			while (recsMgr.hasNext()) {
				ElementManager recMgr = recsMgr.nextElement();
				Record rec = rt.makeRecord(true);
				rec.fromXml(recMgr);
				assertEquals(expectedRecords.next(), rec);
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception "+e);
		}
	}
	@Override
	protected void tearDown() throws Exception {
		journalFileMgr.close();
		Utilities.deleteWorkingDirectory();
	}
	public void testIterator() {
		try {
			String recData[][] = {{"true", "1", "foo"}, {"false", "2", "bar"}};
			int numTestRecords = 2;

			LibrisJournalFileManager<DatabaseRecord> journal = Utilities.createLibrisJournalFileManager(testDatabase, journalFileMgr);
			makeTestRecords(recData, numTestRecords, journal);
			Iterator<DatabaseRecord> journalRecords = journal.iterator();
			Iterator<Record> expectedRecords = testRecords.iterator();
			while (journalRecords.hasNext()) {
				Record rec = journalRecords.next();
				assertEquals(expectedRecords.next(), rec);
			}

		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception "+e);
		}
			
	}
	public void testReplaceRecordInPlace() {
		try {
			String recData[][] = {{"true", "1", "foo"}, {"false", "2", "bar"}};
			int numTestRecords = 2;

			LibrisJournalFileManager<DatabaseRecord> journal = Utilities.createLibrisJournalFileManager(testDatabase, journalFileMgr);
			makeTestRecords(recData, numTestRecords, journal);
			Record r = testRecords.get(0);
			Field f = r.getField(fieldNames[2]);
			f.changeValue("x");
			journal.put(r);
			Iterator<DatabaseRecord> journalRecords = journal.iterator();
			Iterator<Record> expectedRecords = testRecords.iterator();
			while (journalRecords.hasNext()) {
				Record rec = journalRecords.next();
				assertEquals(expectedRecords.next(), rec);
			}

		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception "+e);
		}		
	}
	
	public void testReplaceRecordAtEnd() {
		try {
			String recData[][] = {{"true", "1", "short"}, {"false", "2", "bar"}};
			int numTestRecords = 2;

			LibrisJournalFileManager<DatabaseRecord> journal = Utilities.createLibrisJournalFileManager(testDatabase, journalFileMgr);
			makeTestRecords(recData, numTestRecords, journal);
			Record r = testRecords.get(0);
			r.addFieldValue(fieldNames[2], "some really long data");
			journal.put(r);
			Iterator<DatabaseRecord> journalRecords = journal.iterator();
			Record rec = journalRecords.next();
			assertEquals(rec, testRecords.get(1));
			rec = journalRecords.next();
			assertEquals(rec, testRecords.get(0));

		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception "+e);
		}		
	}
	
	@SuppressWarnings("unchecked")
	public void testRemoveRecord() {
		try {
			String recData[][] = {{"true", "1", "short"}, {"false", "2", "bar"}, {"true", "99", "toe"}};
			int numTestRecords = 3;

			LibrisJournalFileManager<DatabaseRecord> journal = Utilities.createLibrisJournalFileManager(testDatabase, journalFileMgr);
			makeTestRecords(recData, numTestRecords, journal);
			ArrayList<Record>  expectedRecordList = (ArrayList<Record>) testRecords.clone();
			for (Record r: testRecords) {
				assertTrue("could not delete record", journal.removeRecord(r.getRecordId()));
				expectedRecordList.remove(r);
				Iterator<DatabaseRecord> journalRecords = journal.iterator();
				Iterator<Record> expectedRecords = expectedRecordList.iterator();
				int recCount = 0;
				while (journalRecords.hasNext()) {
					Record rec = journalRecords.next();
					assertEquals(expectedRecords.next(), rec);
					++recCount;
				}
				assertEquals("wrong number of records", expectedRecordList.size(), recCount);
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception "+e);
		}		
	}
	public void testReopenJournal() {
		try {
			String recData[][] = {{"true", "1", "foo"}, {"false", "2", "bar"}};
			String recData2[][] = {{"true", "42", "Adams"}, {"false", "86", "Smart"}};
			int numTestRecords = 2;
			

			LibrisJournalFileManager<DatabaseRecord> journal = Utilities.createLibrisJournalFileManager(testDatabase, journalFileMgr);
			makeTestRecords(recData, numTestRecords, journal);
			journal.close();
			journal = Utilities.createLibrisJournalFileManager(testDatabase, journalFileMgr);
			makeTestRecords(recData2, numTestRecords, journal);
			
			Iterator<DatabaseRecord> journalRecords = journal.iterator();
			Iterator<Record> expectedRecords = testRecords.iterator();
			while (journalRecords.hasNext()) {
				Record rec = journalRecords.next();
					assertEquals(expectedRecords.next(), rec);
			}

		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception "+e);
		}
			
	}
	private RecordFactory<DatabaseRecord> makeTestRecords(String[][] recData,
			int numTestRecords, LibrisJournalFileManager<DatabaseRecord> journal)
			throws RecordDataException, DatabaseException, FieldDataException,
			LibrisException {
		RecordFactory<DatabaseRecord> rt = Utilities.makeRecordTemplate(fieldNames, fts);
		if (null == testRecords) {
			testRecords = new ArrayList<Record>(numTestRecords);
		}
		for (int i = 0; i < numTestRecords; ++i) {
			Record r = rt.makeRecord(true);
			testRecords.add(r);
			r.setAllFields(recData[i]);
			r.setRecordId(i+1);
			journal.put(r);
		}
		return rt;
	}
	public int getFieldNum() {
		return 0;
	}
	public String getId() {
		return null;
	}
	public String getTitle() {
		return null;
	}
}

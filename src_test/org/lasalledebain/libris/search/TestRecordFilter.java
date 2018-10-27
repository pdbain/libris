package org.lasalledebain.libris.search;


import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;

import org.junit.After;
import org.junit.Before;
import org.lasalledebain.Utilities;
import org.lasalledebain.libris.FilteredRecordList;
import org.lasalledebain.libris.LibrisDatabase;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.exception.UserErrorException;
import org.lasalledebain.libris.index.IndexField;
import org.lasalledebain.libris.search.RecordFilter.MATCH_TYPE;
import org.lasalledebain.libris.util.RandomFieldGenerator;
import org.lasalledebain.libris.xmlUtils.LibrisXMLConstants;

import junit.framework.TestCase;

public class TestRecordFilter extends TestCase {

	private LibrisDatabase db;

	@Before
	public void setUp() throws Exception {
		db = Utilities.buildTestDatabase(Utilities.KEYWORD_DATABASE4_XML);
	}

	public void testNullFilter() {
		int recordCount = 0;
		for (@SuppressWarnings("unused") Record r: db.getRecords()) {
			++recordCount;
		}
		assertEquals("Wrong number of records", 4, recordCount);
	}

	public void testSingleWordFilter() {
		KeywordFilter filter = new KeywordFilter(MATCH_TYPE.MATCH_EXACT, true, new int[] {0, 1}, new String[] {"k1", "k3"});
		FilteredRecordList filteredList = new FilteredRecordList(db.getRecords(), filter);
		Integer[] ids = new Integer[] {1,4};
		Iterator<Integer> expectedIds = Arrays.asList(ids).iterator();
		for (Record r: filteredList) {
			assertTrue("too many records found", expectedIds.hasNext());
			int expectedId = expectedIds.next();
			assertEquals("Wrong record returned", expectedId, r.getRecordId());
		}
		assertFalse("too few records found", expectedIds.hasNext());
	}

	public void testCascadeWordFilter() {
		KeywordFilter filter1 = new KeywordFilter(MATCH_TYPE.MATCH_EXACT, true, new int[] {0, 1}, new String[] {"k1"});
		FilteredRecordList filteredList1 = new FilteredRecordList(db.getRecords(), filter1);
		KeywordFilter filter2 = new KeywordFilter(MATCH_TYPE.MATCH_EXACT, true, new int[] {0, 1}, new String[] {"k3"});
		FilteredRecordList filteredList2 = new FilteredRecordList(filteredList1, filter2);
		Integer[] ids = new Integer[] {1,4};
		Iterator<Integer> expectedIds = Arrays.asList(ids).iterator();
		for (Record r: filteredList2) {
			assertTrue("too many records found", expectedIds.hasNext());
			int expectedId = expectedIds.next();
			assertEquals("Wrong record returned", expectedId, r.getRecordId());
		}
		assertFalse("too few records found", expectedIds.hasNext());
	}

	public void testAddRecord() throws LibrisException {
		Record rec = db.newRecord();
		rec.addFieldValue("ID_keywords", "k2 k4 k1 k3");
		int newId = db.put(rec);
		KeywordFilter filter = new KeywordFilter(MATCH_TYPE.MATCH_EXACT, true, new int[] {0, 1}, new String[] {"k1", "k3"});
		FilteredRecordList filteredList = new FilteredRecordList(db.getRecords(), filter);
		Integer[] ids = new Integer[] {1,4, newId};
		Iterator<Integer> expectedIds = Arrays.asList(ids).iterator();
		for (Record r: filteredList) {
			assertTrue("too many records found", expectedIds.hasNext());
			int expectedId = expectedIds.next();
			assertEquals("Wrong record returned", expectedId, r.getRecordId());
		}
		assertFalse("too few records found", expectedIds.hasNext());
		db.closeDatabase(true);
	}

	public void testGetIndexFields() {
		IndexField[] indexFieldList = db.getSchema().getIndexFields(LibrisXMLConstants.XML_INDEX_NAME_KEYWORDS);
		int expectedFieldNums[] = new int[] {0,1,3};
		Iterator<IndexField> actualFields = Arrays.asList(indexFieldList).iterator();
		for (int fld: expectedFieldNums) {
			assertTrue ("missing field "+fld, actualFields.hasNext());
			IndexField actualFld = (IndexField) actualFields.next();
			assertEquals("wrong index field",  fld, actualFld.getFieldNum());
		}
		assertFalse ("excess field", actualFields.hasNext());
	}

	public void testKeywordAndBloomFilter() throws UserErrorException, IOException {

		final String[] searchTerms = new String[] {"k1", "k3"};
		FilteredRecordList filteredList = db.makeKeywordFilteredRecordList(MATCH_TYPE.MATCH_EXACT, true, 
				new int[] {0, 1}, Arrays.asList(searchTerms));
		Integer[] ids = new Integer[] {1,4};
		Iterator<Integer> expectedIds = Arrays.asList(ids).iterator();
		for (Record r: filteredList) {
			assertTrue("too many records found", expectedIds.hasNext());
			int expectedId = expectedIds.next();
			assertEquals("Wrong record returned", expectedId, r.getRecordId());
		}
		assertFalse("too few records found", expectedIds.hasNext());
	}
	
	public void testSearch () throws FileNotFoundException, IOException, LibrisException {
		Random rand = new Random(314159);
		final int numRecs = 100;
		LibrisDatabase database = Utilities.buildTestDatabase(Utilities.KEYWORD_DATABASE0_XML);
		int fieldNums[] = new int[] {0, 1, 3};
		RandomFieldGenerator generators[] = new RandomFieldGenerator[fieldNums.length];
		generators[0] = new RandomFieldGenerator(4, 12, 2, 8, rand, 4 * numRecs);
		generators[1] = new RandomFieldGenerator(2, 10, 4, 16, rand, 8 * numRecs);
		generators[2] = new RandomFieldGenerator(2, 10, 20, 40, rand, 25 * numRecs);
		HashMap<String, ArrayList<Integer>> keyWordsAndRecords = makeRandomDatabase(database, numRecs, generators, fieldNums);
		database.getDatabaseFile();
		database.exportDatabaseXml(new FileOutputStream(database.getDatabaseFile()), true, true, true);
		database.getUi().closeDatabase(false);

		fail("not implemented");
	}
	
	 Record makeRandomRecord (LibrisDatabase database, RandomFieldGenerator[] fieldGenerators, int fieldNums[], HashSet<String> keyWords) throws InputException {
		Record rec = database.newRecord();
		keyWords.clear();
		for (int i = 0; i < fieldNums.length; ++i) {
			String fieldText = fieldGenerators[i].makeFieldString(keyWords);
			rec.addFieldValue(fieldNums[i], fieldText);
		}
		return rec;
	}
	
	 HashMap<String, ArrayList<Integer>> makeRandomDatabase(LibrisDatabase database, int numRecs, RandomFieldGenerator[] fieldGenerators, int fieldNums[]) throws LibrisException {
		 HashMap<String, ArrayList<Integer>> keyWordsAndRecords = new HashMap<>();
		 HashSet<String> keyWords = new HashSet<>();
		 for(int i = 1; i <= numRecs; i++) {
			 Record rec = makeRandomRecord(database, fieldGenerators, fieldNums, keyWords);
			 int recNum = database.put(rec);
			 for (String s: keyWords) {
				 ArrayList<Integer> list = keyWordsAndRecords.get(s);
				 if (null == list) {
					 list = new ArrayList<>();
					 keyWordsAndRecords.put(s, list);
				 }
				 list.add(recNum);
			 }
		 }
		 database.save();
		 return keyWordsAndRecords;
	 }

	@After
	public void tearDown() throws Exception {
		assertTrue("Could not close database", db.closeDatabase(false));
		db = null;
		Utilities.deleteTestDatabaseFiles();
	}

}

package org.lasalledebain.libris.search;


import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;

import org.junit.After;
import org.junit.Before;
import org.lasalledebain.Utilities;
import org.lasalledebain.libris.FilteredRecordList;
import org.lasalledebain.libris.LibrisDatabase;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.exception.UserErrorException;
import org.lasalledebain.libris.index.IndexField;
import org.lasalledebain.libris.search.RecordFilter.MATCH_TYPE;
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

	@After
	public void tearDown() throws Exception {
		assertTrue("Could not close database", db.closeDatabase(false));
		db = null;
		Utilities.deleteTestDatabaseFiles();
	}

}

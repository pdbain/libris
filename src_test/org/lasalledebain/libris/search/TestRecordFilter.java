package org.lasalledebain.libris.search;


import java.util.Arrays;
import java.util.Iterator;

import org.junit.After;
import org.junit.Before;
import org.lasalledebain.Utilities;
import org.lasalledebain.libris.FilteredRecordList;
import org.lasalledebain.libris.LibrisDatabase;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.RecordList;
import org.lasalledebain.libris.index.IndexField;
import org.lasalledebain.libris.search.RecordFilter.MATCH_TYPE;
import org.lasalledebain.libris.xmlUtils.LibrisXMLConstants;

import junit.framework.TestCase;

public class TestRecordFilter extends TestCase {

	private LibrisDatabase db;
	private RecordList recList;

	@Before
	public void setUp() throws Exception {
		db = Utilities.buildTestDatabase(Utilities.KEYWORD_DATABASE4_XML);
		recList = db.getRecords();
	}

	public void testNullFilter() {
		int recordCount = 0;
		for (Record r: recList) {
			++recordCount;
		}
		assertEquals("Wrong number of records", 4, recordCount);
	}
	
	public void testSingleWordFilter() {
		KeywordFilter filter = new KeywordFilter(MATCH_TYPE.MATCH_EXACT, true, new int[] {0, 1}, new String[] {"k1", "k3"});
		FilteredRecordList filteredList = new FilteredRecordList(recList, filter);
		Integer[] ids = new Integer[] {1,4};
		 Iterator<Integer> expectedIds = Arrays.asList(ids).iterator();
		for (Record r: filteredList) {
			assertTrue("too many records found", expectedIds.hasNext());
			int expectedId = expectedIds.next();
			assertEquals("Wrong record returned", expectedId, r.getRecordId());
		}
		assertFalse("too few records found", expectedIds.hasNext());
	}
	
	public void testGetIndexFields() {
		IndexField[] indexFieldList = db.getSchema().getIndexFields(LibrisXMLConstants.XML_INDEX_NAME_KEYWORDS);
		int expectedFieldNums[] = new int[] {0,1,3};
		Iterator actualFields = Arrays.asList(indexFieldList).iterator();
		for (int fld: expectedFieldNums) {
			assertTrue ("missing field "+fld, actualFields.hasNext());
			IndexField actualFld = (IndexField) actualFields.next();
			assertEquals("wrong index field",  fld, actualFld.getFieldNum());
		}
		assertFalse ("excess field", actualFields.hasNext());
	}
	
	public void testKeywordAndBloomFilter() {
		fail("not implememted");
	}
	
	@After
	public void tearDown() throws Exception {
		assertTrue("Could not close database", db.closeDatabase(false));
		db = null;
		Utilities.deleteTestDatabaseFiles();
	}

}

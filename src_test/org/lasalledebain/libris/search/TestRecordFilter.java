package org.lasalledebain.libris.search;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.TreeSet;

import org.junit.After;
import org.lasalledebain.Utilities;
import org.lasalledebain.libris.FilteredRecordList;
import org.lasalledebain.libris.LibrisDatabase;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.exception.UserErrorException;
import org.lasalledebain.libris.index.IndexField;
import org.lasalledebain.libris.indexes.IndexConfiguration;
import org.lasalledebain.libris.search.RecordFilter.MATCH_TYPE;
import org.lasalledebain.libris.ui.LibrisUi;
import org.lasalledebain.libris.util.DeterministicFieldGenerator;
import org.lasalledebain.libris.util.FieldGenerator;
import org.lasalledebain.libris.util.Lorem;
import org.lasalledebain.libris.util.RandomFieldGenerator;
import org.lasalledebain.libris.xmlUtils.LibrisXMLConstants;

import junit.framework.TestCase;

public class TestRecordFilter extends TestCase {

	private LibrisDatabase db;
	private LibrisUi ui;
	private LibrisDatabase database;
	private final static int keywordFieldNums[] = new int[] {0,1,3};

	public void testNullFilter() throws FileNotFoundException, IOException {
		db = Utilities.buildTestDatabase(Utilities.KEYWORD_DATABASE4_XML);
		int recordCount = 0;
		for (@SuppressWarnings("unused") Record r: db.getRecords()) {
			++recordCount;
		}
		assertEquals("Wrong number of records", 4, recordCount);
	}

	public void testSingleWordFilter() throws FileNotFoundException, IOException {
		db = Utilities.buildTestDatabase(Utilities.KEYWORD_DATABASE4_XML);
		KeywordFilter filter = new KeywordFilter(MATCH_TYPE.MATCH_EXACT, true, new int[] {0, 1}, new String[] {"k1", "k3"});
		FilteredRecordList filteredList = new FilteredRecordList(db.getRecords(), filter);
		Integer[] ids = new Integer[] {1,4};
		checkReturnedRecords(filteredList, Arrays.asList(ids));
	}

	public void testCascadeWordFilter() throws FileNotFoundException, IOException {
		db = Utilities.buildTestDatabase(Utilities.KEYWORD_DATABASE4_XML);
		KeywordFilter filter1 = new KeywordFilter(MATCH_TYPE.MATCH_EXACT, true, new int[] {0, 1}, new String[] {"k1"});
		FilteredRecordList filteredList1 = new FilteredRecordList(db.getRecords(), filter1);
		KeywordFilter filter2 = new KeywordFilter(MATCH_TYPE.MATCH_EXACT, true, new int[] {0, 1}, new String[] {"k3"});
		FilteredRecordList filteredList2 = new FilteredRecordList(filteredList1, filter2);
		Integer[] ids = new Integer[] {1,4};
		checkReturnedRecords(filteredList2, Arrays.asList(ids));
	}

	public void testAddRecord() throws LibrisException, FileNotFoundException, IOException {
		db = Utilities.buildTestDatabase(Utilities.KEYWORD_DATABASE4_XML);
		Record rec = db.newRecord();
		rec.addFieldValue("ID_keywords", "k2 k4 k1 k3");
		int newId = db.put(rec);
		KeywordFilter filter = new KeywordFilter(MATCH_TYPE.MATCH_EXACT, true, new int[] {0, 1}, new String[] {"k1", "k3"});
		FilteredRecordList filteredList = new FilteredRecordList(db.getRecords(), filter);
		Integer[] ids = new Integer[] {1,4, newId};
		checkReturnedRecords(filteredList, Arrays.asList(ids));
		db.closeDatabase(true);
	}

	public void testGetIndexFields() throws FileNotFoundException, IOException {
		db = Utilities.buildTestDatabase(Utilities.KEYWORD_DATABASE4_XML);
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
		db = Utilities.buildTestDatabase(Utilities.KEYWORD_DATABASE4_XML);
		final String[] searchTerms = new String[] {"k1", "k3"};
		FilteredRecordList filteredList = db.makeKeywordFilteredRecordList(MATCH_TYPE.MATCH_EXACT, true, 
				new int[] {0, 1}, Arrays.asList(searchTerms));
		Integer[] ids = new Integer[] {1,4};
		checkReturnedRecords(filteredList, Arrays.asList(ids));
	}

	public void testSearchSanity () throws FileNotFoundException, IOException, LibrisException {
		Random rand = new Random(314159);
		final int numRecs = 2;
		File testDatabaseFileCopy = Utilities.copyTestDatabaseFile(Utilities.KEYWORD_DATABASE0_XML);
		IndexConfiguration config = new IndexConfiguration(testDatabaseFileCopy);
		LibrisUi ui = config.getDatabaseUi();
		LibrisDatabase database = Utilities.buildTestDatabase(config);
		int fieldNums[] = new int[] {0};
		RandomFieldGenerator generators[] = new RandomFieldGenerator[fieldNums.length];
		generators[0] = new RandomFieldGenerator(4, 12, 4, 4, rand, numRecs);
		HashMap<String, List<Integer>> keyWordsAndRecords = makeDatabase(database, numRecs, generators, fieldNums);
		database.exportDatabaseXml(new FileOutputStream(testDatabaseFileCopy), true, true, true);
		ui.closeDatabase(false);
		ui.rebuildDatabase();
		database = ui.openDatabase();
		for (String term: keyWordsAndRecords.keySet()) {
			FilteredRecordList filteredList = database.makeKeywordFilteredRecordList(MATCH_TYPE.MATCH_EXACT, true, 
					fieldNums, term);
			checkReturnedRecords(filteredList, keyWordsAndRecords.get(term));
		}
	}
	
	public void testSearch () throws FileNotFoundException, IOException, LibrisException {
		Random rand = new Random(314159);
		final int numRecs = 100;
		File testDatabaseFileCopy = Utilities.copyTestDatabaseFile(Utilities.KEYWORD_DATABASE0_XML);
		IndexConfiguration config = new IndexConfiguration(testDatabaseFileCopy);
		config.setSignatureLevels(2);
		LibrisUi ui = config.getDatabaseUi();
		LibrisDatabase database = Utilities.buildTestDatabase(config);
		RandomFieldGenerator generators[] = new RandomFieldGenerator[keywordFieldNums.length];
		generators[0] = new RandomFieldGenerator(4, 12, 2, 8, rand, 4 * numRecs);
		generators[1] = new RandomFieldGenerator(2, 10, 4, 16, rand, 8 * numRecs);
		generators[2] = new RandomFieldGenerator(2, 10, 20, 40, rand, 25 * numRecs);
		HashMap<String, List<Integer>> keyWordsAndRecords = makeDatabase(database, numRecs, generators, keywordFieldNums);
		database.exportDatabaseXml(new FileOutputStream(testDatabaseFileCopy), true, true, true);
		ui.closeDatabase(false);
		ui.rebuildDatabase(config);
		database = ui.openDatabase();
		for (String term: keyWordsAndRecords.keySet()) {
			FilteredRecordList filteredList = database.makeKeywordFilteredRecordList(MATCH_TYPE.MATCH_EXACT, true, 
					keywordFieldNums, term);
			checkReturnedRecords(filteredList, keyWordsAndRecords.get(term));
		}
	}
	
	public void testSearchMultipleResults() throws FileNotFoundException, IOException, LibrisException {
		File testDatabaseFileCopy = Utilities.copyTestDatabaseFile(Utilities.KEYWORD_DATABASE0_XML);
		IndexConfiguration config = new IndexConfiguration(testDatabaseFileCopy);
		config.setSignatureLevels(2);
		LibrisUi ui = config.getDatabaseUi();
		LibrisDatabase database = Utilities.buildTestDatabase(config);
		String singleKeyword = "singleKeyword";
		String multipleKeyword = "multipleKeyword";
		
		final int numRecs = 50;
		String fields0[] = Arrays.copyOf(Lorem.fields, numRecs);
		String fields1[] = Arrays.copyOfRange(Lorem.fields, numRecs, 2 * numRecs);
		String fields2[] = Arrays.copyOfRange(Lorem.fields, 2 * numRecs, 3 * numRecs);
		int singleKeywordRecord = 10;
		Integer multipleKeywordRecords[] = new Integer[] {5, 15, 27, 45, 49};
		fields0[singleKeywordRecord-1] += " " + singleKeyword;
		for (int i: multipleKeywordRecords) {
			fields0[i-1] += " " + multipleKeyword;
		}
		FieldGenerator generators[] = new FieldGenerator[keywordFieldNums.length];
		generators[0] = new DeterministicFieldGenerator(fields0);
		generators[1] = new DeterministicFieldGenerator(fields1);
		generators[2] = new DeterministicFieldGenerator(fields2);
		makeDatabase(database, numRecs, generators, keywordFieldNums);
		database.exportDatabaseXml(new FileOutputStream(testDatabaseFileCopy), true, true, true);
		ui.closeDatabase(false);
		ui.rebuildDatabase(config);
		database = ui.openDatabase();

		FilteredRecordList filteredList = database.makeKeywordFilteredRecordList(MATCH_TYPE.MATCH_EXACT, true, 
				keywordFieldNums, singleKeyword);
		checkReturnedRecords(filteredList, Arrays.asList(singleKeywordRecord));
		filteredList = database.makeKeywordFilteredRecordList(MATCH_TYPE.MATCH_EXACT, true, 
				keywordFieldNums, multipleKeyword);
		checkReturnedRecords(filteredList, Arrays.asList(multipleKeywordRecords));
	}
	
	/**
	 * Search for multiple words
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 * @throws LibrisException 
	 */
	public void testSearchCompoundQuery() throws FileNotFoundException, IOException, LibrisException {
		IndexConfiguration config = copyAndBuildDatabase();
		
		final String[] fieldSource = Lorem.fields;
		final int numRecs = fieldSource.length/keywordFieldNums.length;
		final FieldGenerator generators[] = new FieldGenerator[keywordFieldNums.length];
		for (int i = 0; i < keywordFieldNums.length; ++i) {
			String fields[] = Arrays.copyOfRange(fieldSource, i * numRecs, (i + 1) * numRecs);
			generators[i] = new DeterministicFieldGenerator(fields);			
		}
		HashMap<String, List<Integer>> keyWordsAndRecords = makeDatabase(database, numRecs, generators, keywordFieldNums);
		database.exportDatabaseXml(new FileOutputStream(config.getDatabaseFile()), true, true, true);
		ui = config.getDatabaseUi();
		ui.closeDatabase(false);
		ui.rebuildDatabase(config);
		database = ui.openDatabase();

		doCompoundQuery(database, keywordFieldNums, keyWordsAndRecords, new String[] {"termtwo", "termthree"});
		doCompoundQuery(database, keywordFieldNums, keyWordsAndRecords, new String[] {"termtwo", "termthree", "termone"});
		
	}

	/**
	 * Search for word prefixes
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 * @throws LibrisException 
	 */
	public void testSearchSubstring() throws FileNotFoundException, IOException, LibrisException {
		IndexConfiguration config = copyAndBuildDatabase();
		
		final String[] fieldSource = Lorem.fields;
		final int numRecs = fieldSource.length/keywordFieldNums.length;
		final FieldGenerator generators[] = new FieldGenerator[keywordFieldNums.length];
		for (int i = 0; i < keywordFieldNums.length; ++i) {
			String fields[] = Arrays.copyOfRange(fieldSource, i * numRecs, (i + 1) * numRecs);
			generators[i] = new DeterministicFieldGenerator(fields);			
		}
		HashMap<String, List<Integer>> keyWordsAndRecords = makeDatabase(database, numRecs, generators, keywordFieldNums);
		database.exportDatabaseXml(new FileOutputStream(config.getDatabaseFile()), true, true, true);
		ui = config.getDatabaseUi();
		ui.closeDatabase(false);
		ui.rebuildDatabase(config);
		database = ui.openDatabase();

		doSubstringQuery(database, keywordFieldNums, keyWordsAndRecords, new String[] {"termtwo", "termthree"}, "termt");
		doSubstringQuery(database, keywordFieldNums, keyWordsAndRecords, new String[] {"termone", "termtwo", "termthree", "terminate", "terminal", "terminate", "terminates"}, "term");

	}
	
	/**
	 * Search for lowercase and uppercase with lowercase and uppercase text
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 * @throws LibrisException 
	 */
	public void testSearchCaseInsensitive() throws FileNotFoundException, IOException, LibrisException {

		IndexConfiguration config = copyAndBuildDatabase();
		
		final String[] fieldSource = Lorem.fields;
		final int numRecs = fieldSource.length/keywordFieldNums.length;
		final FieldGenerator generators[] = new FieldGenerator[keywordFieldNums.length];
		for (int i = 0; i < keywordFieldNums.length; ++i) {
			String fields[] = Arrays.copyOfRange(fieldSource, i * numRecs, (i + 1) * numRecs);
			generators[i] = new DeterministicFieldGenerator(fields);			
		}
		HashMap<String, List<Integer>> keyWordsAndRecords = makeDatabase(database, numRecs, generators, keywordFieldNums);
		database.exportDatabaseXml(new FileOutputStream(config.getDatabaseFile()), true, true, true);
		ui = config.getDatabaseUi();
		ui.closeDatabase(false);
		ui.rebuildDatabase(config);
		database = ui.openDatabase();
		String searchTerm = "UPPERCASEWORD";

		List<Integer> recordList = keyWordsAndRecords.get(searchTerm);
		FilteredRecordList filteredList = database.makeKeywordFilteredRecordList(MATCH_TYPE.MATCH_EXACT, false, 
				keywordFieldNums, Arrays.asList(searchTerm.toLowerCase()));
		checkReturnedRecords(filteredList, recordList);
		
		searchTerm = "MixedCaseWord";
		recordList = keyWordsAndRecords.get(searchTerm);
		filteredList = database.makeKeywordFilteredRecordList(MATCH_TYPE.MATCH_EXACT, false, 
				keywordFieldNums, Arrays.asList(searchTerm.toUpperCase()));
		checkReturnedRecords(filteredList, recordList);
		}
	
	/**
	 * Search for words in newly added records
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 * @throws LibrisException 
	 */
	public void testSearchNewRecords() throws FileNotFoundException, IOException, LibrisException {
		IndexConfiguration config = copyAndBuildDatabase();
		
		final String[] fieldSource = Lorem.fields;
		final int numRecs = fieldSource.length/keywordFieldNums.length;
		final FieldGenerator generators[] = new FieldGenerator[keywordFieldNums.length];
		for (int i = 0; i < keywordFieldNums.length; ++i) {
			String fields[] = Arrays.copyOfRange(fieldSource, i * numRecs, (i + 1) * numRecs);
			generators[i] = new DeterministicFieldGenerator(fields);			
		}
		makeDatabase(database, numRecs, generators, keywordFieldNums);
		database.exportDatabaseXml(new FileOutputStream(config.getDatabaseFile()), true, true, true);
		ui = config.getDatabaseUi();
		ui.closeDatabase(false);
		ui.rebuildDatabase(config);
		database = ui.openDatabase();
		Record rec = database.newRecord();
		final String newKeyWords[] = {"FirstKeyword", "SecondKeyword", "ThirdKeyword"};
		for (int i = 0; i < newKeyWords.length; ++i) {
			rec.addFieldValue(keywordFieldNums[i], newKeyWords[i]);
		}
		int newId = database.put(rec);
		HashMap<String, List<Integer>> keyWordsAndRecords = new HashMap<>();
		for (String s: newKeyWords) {
			keyWordsAndRecords.put(s, Arrays.asList(newId));
		}

		doCompoundQuery(database, keywordFieldNums, keyWordsAndRecords, newKeyWords);

	}
	
	public void testIndexStress() throws FileNotFoundException, IOException, LibrisException {
		IndexConfiguration config = copyAndBuildDatabase();
		
		Random rand = new Random(3141592);
		final int numRecs = 10000;
		final FieldGenerator generators[] = new RandomFieldGenerator[keywordFieldNums.length];
		generators[0] = new RandomFieldGenerator(4, 12, 2, 8, rand, 4 * numRecs);
		generators[1] = new RandomFieldGenerator(2, 10, 4, 16, rand, 8 * numRecs);
		generators[2] = new RandomFieldGenerator(2, 10, 20, 40, rand, 25 * numRecs);
		HashMap<String, List<Integer>> keyWordsAndRecords = makeDatabase(database, numRecs, generators, keywordFieldNums);
		database.exportDatabaseXml(new FileOutputStream(config.getDatabaseFile()), true, true, true);
		ui = config.getDatabaseUi();
		ui.closeDatabase(false);
		ui.rebuildDatabase(config);
		database = ui.openDatabase();

		for (String term: keyWordsAndRecords.keySet()) {
			FilteredRecordList filteredList = database.makeKeywordFilteredRecordList(MATCH_TYPE.MATCH_EXACT, true, 
					keywordFieldNums, term);
			checkReturnedRecords(filteredList, keyWordsAndRecords.get(term));
		}
		
	}
	
	private IndexConfiguration copyAndBuildDatabase() throws FileNotFoundException, IOException {
		File testDatabaseFileCopy = Utilities.copyTestDatabaseFile(Utilities.KEYWORD_DATABASE0_XML);
		IndexConfiguration config = new IndexConfiguration(testDatabaseFileCopy);
		config.setSignatureLevels(2);
		database = Utilities.buildTestDatabase(config);
		return config;
	}

	private void doCompoundQuery(LibrisDatabase database, int[] fieldNums,
			HashMap<String, List<Integer>> keyWordsAndRecords, String[] searchTerms)
					throws UserErrorException, IOException {
		List<Integer> recordList = keyWordsAndRecords.get(searchTerms[0]);
		TreeSet<Integer> recordSet = new TreeSet<>(recordList);
		for (int i = 1; i < searchTerms.length; ++i) {
			recordList = keyWordsAndRecords.get(searchTerms[i]);
			recordSet.retainAll(recordList);
		}
		FilteredRecordList filteredList = database.makeKeywordFilteredRecordList(MATCH_TYPE.MATCH_EXACT, true, 
				fieldNums, Arrays.asList(searchTerms));
		checkReturnedRecords(filteredList, recordSet);
	}

	private void doSubstringQuery(LibrisDatabase database, int[] fieldNums,
			HashMap<String, List<Integer>> keyWordsAndRecords, String[] searchTerms, String key)
					throws UserErrorException, IOException {
		List<Integer> recordList = keyWordsAndRecords.get(searchTerms[0]);
		TreeSet<Integer> recordSet = new TreeSet<>(recordList);
		for (int i = 1; i < searchTerms.length; ++i) {
			recordList = keyWordsAndRecords.get(searchTerms[i]);
			if (Objects.nonNull(recordList)) {
				recordSet.addAll(recordList);
			}
		}
		FilteredRecordList filteredList = database.makeKeywordFilteredRecordList(MATCH_TYPE.MATCH_PREFIX, true, 
				fieldNums, Arrays.asList(key));
		checkReturnedRecords(filteredList, recordSet);
	}

	private void checkReturnedRecords(FilteredRecordList filteredList, Collection<Integer> expectedIdCollection) {
		Iterator<Integer> expectedIds = expectedIdCollection.iterator();
		for (Record r: filteredList) {
			assertTrue("too many records found", expectedIds.hasNext());
			int expectedId = expectedIds.next();
			assertEquals("Wrong record returned", expectedId, r.getRecordId());
		}
		assertFalse("too few records found", expectedIds.hasNext());
	}
	
	 Record makeRecord (LibrisDatabase database, FieldGenerator[] fieldGenerators, int fieldNums[], HashSet<String> keyWords) throws InputException {
		Record rec = database.newRecord();
		keyWords.clear();
		for (int i = 0; i < fieldNums.length; ++i) {
			String fieldText = fieldGenerators[i].makeFieldString(keyWords);
			rec.addFieldValue(fieldNums[i], fieldText);
		}
		return rec;
	}
	
	 HashMap<String, List<Integer>> makeDatabase(LibrisDatabase database, int numRecs, FieldGenerator[] fieldGenerators, int fieldNums[]) throws LibrisException {
		 HashMap<String, List<Integer>> keyWordsAndRecords = new HashMap<>();
		 HashSet<String> keyWords = new HashSet<>();
		 for(int i = 1; i <= numRecs; i++) {
			 Record rec = makeRecord(database, fieldGenerators, fieldNums, keyWords);
			 int recNum = database.put(rec);
			 for (String s: keyWords) {
				 List<Integer> list = keyWordsAndRecords.get(s);
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
		if (Objects.nonNull(db)) {
			assertTrue("Could not close database", db.closeDatabase(false));
		}
		db = null;
		Utilities.deleteTestDatabaseFiles();
	}

}

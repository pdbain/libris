package org.lasalledebain.libris.search;

import java.io.BufferedOutputStream;
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
import org.junit.Before;
import org.lasalledebain.Utilities;
import org.lasalledebain.libris.DatabaseRecord;
import org.lasalledebain.libris.FilteredRecordList;
import org.lasalledebain.libris.GenericDatabase;
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

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

public class TestRecordFilter extends TestCase {

	private LibrisDatabase db;
	private LibrisUi ui;
	private LibrisDatabase database;
	private File workingDirectory;
	private final static int keywordFieldNums[] = new int[] { 0, 1, 3 };

	public void testNullFilter() throws FileNotFoundException, IOException {
		getDatabase();
		int recordCount = 0;
		for (@SuppressWarnings("unused")
		Record r : db.getRecords()) {
			++recordCount;
		}
		assertEquals("Wrong number of records", 4, recordCount);
	}

	public void testSingleWordFilter() throws FileNotFoundException, IOException {
		getDatabase();
		KeywordFilter filter = new KeywordFilter(MATCH_TYPE.MATCH_EXACT, true, new int[] { 0, 1 },
				new String[] { "k1", "k3" });
		FilteredRecordList<DatabaseRecord> filteredList = new FilteredRecordList<DatabaseRecord>(db.getRecords(),
				filter);
		Integer[] ids = new Integer[] { 1, 4 };
		checkReturnedRecords(filteredList, Arrays.asList(ids));
	}

	public void testCascadeWordFilter() throws FileNotFoundException, IOException {
		getDatabase();
		KeywordFilter filter1 = new KeywordFilter(MATCH_TYPE.MATCH_EXACT, true, new int[] { 0, 1 },
				new String[] { "k1" });
		FilteredRecordList<DatabaseRecord> filteredList1 = new FilteredRecordList<DatabaseRecord>(db.getRecords(),
				filter1);
		KeywordFilter filter2 = new KeywordFilter(MATCH_TYPE.MATCH_EXACT, true, new int[] { 0, 1 },
				new String[] { "k3" });
		FilteredRecordList<DatabaseRecord> filteredList2 = new FilteredRecordList<DatabaseRecord>(filteredList1,
				filter2);
		Integer[] ids = new Integer[] { 1, 4 };
		checkReturnedRecords(filteredList2, Arrays.asList(ids));
	}

	public void testAddRecord() throws LibrisException, FileNotFoundException, IOException {
		getDatabase();
		DatabaseRecord rec = db.newRecord();
		rec.addFieldValue("ID_keywords", "k2 k4 k1 k3");
		int newId = db.putRecord(rec);
		KeywordFilter filter = new KeywordFilter(MATCH_TYPE.MATCH_EXACT, true, new int[] { 0, 1 },
				new String[] { "k1", "k3" });
		FilteredRecordList<DatabaseRecord> filteredList = new FilteredRecordList<DatabaseRecord>(db.getRecords(),
				filter);
		Integer[] ids = new Integer[] { 1, 4, newId };
		checkReturnedRecords(filteredList, Arrays.asList(ids));
		db.closeDatabase(true);
	}

	public void testGetIndexFields() throws FileNotFoundException, IOException {
		getDatabase();
		IndexField[] indexFieldList = db.getSchema().getIndexFields(LibrisXMLConstants.XML_INDEX_NAME_KEYWORDS);
		int expectedFieldNums[] = new int[] { 0, 1, 3 };
		Iterator<IndexField> actualFields = Arrays.asList(indexFieldList).iterator();
		for (int fld : expectedFieldNums) {
			assertTrue("missing field " + fld, actualFields.hasNext());
			IndexField actualFld = (IndexField) actualFields.next();
			assertEquals("wrong index field", fld, actualFld.getFieldNum());
		}
		assertFalse("excess field", actualFields.hasNext());
	}

	public void testKeywordAndBloomFilter() throws UserErrorException, IOException {
		getDatabase();
		final String[] searchTerms = new String[] { "k1", "k3" };
		FilteredRecordList<DatabaseRecord> filteredList = db.makeKeywordFilteredRecordList(MATCH_TYPE.MATCH_EXACT, true,
				new int[] { 0, 1 }, Arrays.asList(searchTerms));
		Integer[] ids = new Integer[] { 1, 4 };
		checkReturnedRecords(filteredList, Arrays.asList(ids));
	}

	public void testSearchSanity() throws FileNotFoundException, IOException, LibrisException {
		Random rand = new Random(314159);
		final int numRecs = 2;
		File testDatabaseFileCopy = Utilities.copyTestDatabaseFile(Utilities.KEYWORD_DATABASE0_XML, workingDirectory);
		IndexConfiguration config = new IndexConfiguration(testDatabaseFileCopy);
		LibrisUi ui = config.getDatabaseUi();
		LibrisDatabase database = Utilities.buildTestDatabase(config);
		int fieldNums[] = new int[] { 0 };
		RandomFieldGenerator generators[] = new RandomFieldGenerator[fieldNums.length];
		generators[0] = new RandomFieldGenerator(4, 12, 4, 4, rand, numRecs);
		HashMap<String, List<Integer>> keyWordsAndRecords = makeDatabase(database, numRecs, generators, fieldNums);
		database.exportDatabaseXml(new FileOutputStream(testDatabaseFileCopy), true, true, true);
		ui.closeDatabase(false);
		ui.rebuildDatabase();
		database = ui.openDatabase();
		for (String term : keyWordsAndRecords.keySet()) {
			FilteredRecordList<DatabaseRecord> filteredList = database
					.makeKeywordFilteredRecordList(MATCH_TYPE.MATCH_EXACT, true, fieldNums, term);
			checkReturnedRecords(filteredList, keyWordsAndRecords.get(term));
		}
	}

	public void testSearch() throws FileNotFoundException, IOException, LibrisException {
		Random rand = new Random(314159);
		final int numRecs = 100;
		File testDatabaseFileCopy = Utilities.copyTestDatabaseFile(Utilities.KEYWORD_DATABASE0_XML, workingDirectory);
		IndexConfiguration config = new IndexConfiguration(testDatabaseFileCopy);
		config.setAttribute(IndexConfiguration.SIGNATURE_LEVELS, 2);
		LibrisUi ui = config.getDatabaseUi();
		LibrisDatabase database = Utilities.buildTestDatabase(config);
		RandomFieldGenerator generators[] = new RandomFieldGenerator[keywordFieldNums.length];
		final int keywordRatio = 15 * numRecs;
		generators[0] = new RandomFieldGenerator(4, 12, 2, 8, rand, keywordRatio);
		generators[1] = new RandomFieldGenerator(2, 10, 4, 16, rand, keywordRatio);
		generators[2] = new RandomFieldGenerator(2, 10, 20, 40, rand, keywordRatio);
		HashMap<String, List<Integer>> keyWordsAndRecords = makeDatabase(database, numRecs, generators,
				keywordFieldNums);
		database.exportDatabaseXml(new FileOutputStream(testDatabaseFileCopy), true, true, true);
		ui.closeDatabase(false);
		ui.rebuildDatabase(config);
		database = ui.openDatabase();
		for (String term : keyWordsAndRecords.keySet()) {
			FilteredRecordList<DatabaseRecord> filteredList = database
					.makeKeywordFilteredRecordList(MATCH_TYPE.MATCH_EXACT, true, keywordFieldNums, term);
			checkReturnedRecords(filteredList, keyWordsAndRecords.get(term));
		}
	}

	public void testSearchMultipleResults() throws FileNotFoundException, IOException, LibrisException {
		File testDatabaseFileCopy = Utilities.copyTestDatabaseFile(Utilities.KEYWORD_DATABASE0_XML, workingDirectory);
		IndexConfiguration config = new IndexConfiguration(testDatabaseFileCopy);
		config.setAttribute(IndexConfiguration.SIGNATURE_LEVELS, 2);
		LibrisUi ui = config.getDatabaseUi();
		LibrisDatabase database = Utilities.buildTestDatabase(config);
		String singleKeyword = "singleKeyword";
		String multipleKeyword = "multipleKeyword";

		final int numRecs = 50;
		String fields0[] = Arrays.copyOf(Lorem.fields, numRecs);
		String fields1[] = Arrays.copyOfRange(Lorem.fields, numRecs, 2 * numRecs);
		String fields2[] = Arrays.copyOfRange(Lorem.fields, 2 * numRecs, 3 * numRecs);
		int singleKeywordRecord = 10;
		Integer multipleKeywordRecords[] = new Integer[] { 5, 15, 27, 45, 49 };
		fields0[singleKeywordRecord - 1] += " " + singleKeyword;
		for (int i : multipleKeywordRecords) {
			fields0[i - 1] += " " + multipleKeyword;
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

		FilteredRecordList<DatabaseRecord> filteredList = database.makeKeywordFilteredRecordList(MATCH_TYPE.MATCH_EXACT,
				true, keywordFieldNums, singleKeyword);
		checkReturnedRecords(filteredList, Arrays.asList(singleKeywordRecord));
		filteredList = database.makeKeywordFilteredRecordList(MATCH_TYPE.MATCH_EXACT, true, keywordFieldNums,
				multipleKeyword);
		checkReturnedRecords(filteredList, Arrays.asList(multipleKeywordRecords));
	}

	/**
	 * Search for multiple words
	 * 
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws LibrisException
	 */
	public void testSearchCompoundQuery() throws FileNotFoundException, IOException, LibrisException {
		IndexConfiguration config = copyAndBuildDatabase();

		final String[] fieldSource = Lorem.fields;
		final int numRecs = fieldSource.length / keywordFieldNums.length;
		final FieldGenerator generators[] = new FieldGenerator[keywordFieldNums.length];
		for (int i = 0; i < keywordFieldNums.length; ++i) {
			String fields[] = Arrays.copyOfRange(fieldSource, i * numRecs, (i + 1) * numRecs);
			generators[i] = new DeterministicFieldGenerator(fields);
		}
		HashMap<String, List<Integer>> keyWordsAndRecords = makeDatabase(database, numRecs, generators,
				keywordFieldNums);
		database.exportDatabaseXml(new FileOutputStream(config.getDatabaseFile()), true, true, true);
		ui = config.getDatabaseUi();
		ui.closeDatabase(false);
		ui.rebuildDatabase(config);
		database = ui.openDatabase();

		doCompoundQuery(database, keywordFieldNums, keyWordsAndRecords, new String[] { "termtwo", "termthree" });
		doCompoundQuery(database, keywordFieldNums, keyWordsAndRecords,
				new String[] { "termtwo", "termthree", "termone" });

	}

	/**
	 * Search for word prefixes
	 * 
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws LibrisException
	 */
	public void testSearchSubstring() throws FileNotFoundException, IOException, LibrisException {
		IndexConfiguration config = copyAndBuildDatabase();

		final String[] fieldSource = Lorem.fields;
		final int numRecs = fieldSource.length / keywordFieldNums.length;
		final FieldGenerator generators[] = new FieldGenerator[keywordFieldNums.length];
		for (int i = 0; i < keywordFieldNums.length; ++i) {
			String fields[] = Arrays.copyOfRange(fieldSource, i * numRecs, (i + 1) * numRecs);
			generators[i] = new DeterministicFieldGenerator(fields);
		}
		HashMap<String, List<Integer>> keyWordsAndRecords = makeDatabase(database, numRecs, generators,
				keywordFieldNums);
		database.exportDatabaseXml(new FileOutputStream(config.getDatabaseFile()), true, true, true);
		ui = config.getDatabaseUi();
		ui.closeDatabase(false);
		ui.rebuildDatabase(config);
		database = ui.openDatabase();

		doSubstringQuery(database, keywordFieldNums, keyWordsAndRecords, new String[] { "termtwo", "termthree" },
				"termt");
		doSubstringQuery(database, keywordFieldNums, keyWordsAndRecords,
				new String[] { "termone", "termtwo", "termthree", "terminate", "terminal", "terminate", "terminates" },
				"term");

	}

	/**
	 * Search for lowercase and uppercase with lowercase and uppercase text
	 * 
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws LibrisException
	 */
	public void testSearchCaseInsensitive() throws FileNotFoundException, IOException, LibrisException {

		IndexConfiguration config = copyAndBuildDatabase();

		final String[] fieldSource = Lorem.fields;
		final int numRecs = fieldSource.length / keywordFieldNums.length;
		final FieldGenerator generators[] = new FieldGenerator[keywordFieldNums.length];
		for (int i = 0; i < keywordFieldNums.length; ++i) {
			String fields[] = Arrays.copyOfRange(fieldSource, i * numRecs, (i + 1) * numRecs);
			generators[i] = new DeterministicFieldGenerator(fields);
		}
		HashMap<String, List<Integer>> keyWordsAndRecords = makeDatabase(database, numRecs, generators,
				keywordFieldNums);
		database.exportDatabaseXml(new FileOutputStream(config.getDatabaseFile()), true, true, true);
		ui = config.getDatabaseUi();
		ui.closeDatabase(false);
		ui.rebuildDatabase(config);
		database = ui.openDatabase();
		String searchTerm = "UPPERCASEWORD";

		List<Integer> recordList = keyWordsAndRecords.get(searchTerm);
		FilteredRecordList<DatabaseRecord> filteredList = database.makeKeywordFilteredRecordList(MATCH_TYPE.MATCH_EXACT,
				false, keywordFieldNums, Arrays.asList(searchTerm.toLowerCase()));
		checkReturnedRecords(filteredList, recordList);

		searchTerm = "MixedCaseWord";
		recordList = keyWordsAndRecords.get(searchTerm);
		filteredList = database.makeKeywordFilteredRecordList(MATCH_TYPE.MATCH_EXACT, false, keywordFieldNums,
				Arrays.asList(searchTerm.toUpperCase()));
		checkReturnedRecords(filteredList, recordList);
	}

	/**
	 * Search for words in newly added records
	 * 
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws LibrisException
	 */
	public void testSearchNewRecords() throws FileNotFoundException, IOException, LibrisException {
		IndexConfiguration config = copyAndBuildDatabase();

		final String[] fieldSource = Lorem.fields;
		final int numRecs = fieldSource.length / keywordFieldNums.length;
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
		DatabaseRecord rec = database.newRecord();
		final String newKeyWords[] = { "FirstKeyword", "SecondKeyword", "ThirdKeyword" };
		for (int i = 0; i < newKeyWords.length; ++i) {
			rec.addFieldValue(keywordFieldNums[i], newKeyWords[i]);
		}
		int newId = database.putRecord(rec);
		HashMap<String, List<Integer>> keyWordsAndRecords = new HashMap<>();
		for (String s : newKeyWords) {
			keyWordsAndRecords.put(s, Arrays.asList(newId));
		}

		doCompoundQuery(database, keywordFieldNums, keyWordsAndRecords, newKeyWords);

	}

	public void testIndexStress() throws FileNotFoundException, IOException, LibrisException {
		IndexConfiguration config = copyAndBuildDatabase();

		Random rand = new Random(3141592);
		final int numRecs = 40000; // TODO increase to 10^6
		config.setAttribute(IndexConfiguration.TERMCOUNT_BUCKETS, numRecs / 4);
		final FieldGenerator generators[] = new RandomFieldGenerator[keywordFieldNums.length];
		final int keywordRatio = 15 * numRecs;
		generators[0] = new RandomFieldGenerator(4, 12, 2, 8, rand, keywordRatio);
		generators[1] = new RandomFieldGenerator(2, 10, 4, 16, rand, keywordRatio);
		generators[2] = new RandomFieldGenerator(2, 10, 20, 40, rand, keywordRatio);
		HashMap<String, List<Integer>> keyWordsAndRecords = makeDatabase(database, numRecs, generators,
				keywordFieldNums);
		database.exportDatabaseXml(new BufferedOutputStream(new FileOutputStream(config.getDatabaseFile())), true, true,
				true);
		for (int i=1 ; i<4; ++i) {
			Record r = database.getRecord(i);
			assertNotNull("Record "+i+" null", r);
		}
		ui = config.getDatabaseUi();
		assertTrue("Database not closed", ui.closeDatabase(false));
		ui.rebuildDatabase(config);
		database = ui.openDatabase();

		for (String term : keyWordsAndRecords.keySet()) {
			FilteredRecordList<DatabaseRecord> filteredList = database
					.makeKeywordFilteredRecordList(MATCH_TYPE.MATCH_EXACT, true, keywordFieldNums, term);
			try {
				checkReturnedRecords(filteredList, keyWordsAndRecords.get(term));
			} catch (AssertionFailedError e) {
				AssertionFailedError afe = new AssertionFailedError("missing term " + term);
				afe.initCause(e);
				throw afe;
			}
		}

	}

	private IndexConfiguration copyAndBuildDatabase() throws FileNotFoundException, IOException {
		File testDatabaseFileCopy = Utilities.copyTestDatabaseFile(Utilities.KEYWORD_DATABASE0_XML, workingDirectory);
		IndexConfiguration config = new IndexConfiguration(testDatabaseFileCopy);
		config.setAttribute(IndexConfiguration.SIGNATURE_LEVELS, 2);
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
		FilteredRecordList<DatabaseRecord> filteredList = database.makeKeywordFilteredRecordList(MATCH_TYPE.MATCH_EXACT,
				true, fieldNums, Arrays.asList(searchTerms));
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
		FilteredRecordList<DatabaseRecord> filteredList = database
				.makeKeywordFilteredRecordList(MATCH_TYPE.MATCH_PREFIX, true, fieldNums, Arrays.asList(key));
		checkReturnedRecords(filteredList, recordSet);
	}

	private void checkReturnedRecords(FilteredRecordList<DatabaseRecord> filteredList,
			Collection<Integer> expectedIdCollection) {
		// Iterator<Integer> expectedIds = expectedIdCollection.iterator();
		Iterator<Integer> expectedIds = expectedIdCollection.stream().sorted().iterator();
		// int recordCount = 0;
		for (Record r : filteredList) {
			// ++recordCount;
			assertTrue("too few records found", expectedIds.hasNext());
			int nextExpected = expectedIds.next();
			final int nextActual = r.getRecordId();
			assertEquals("missing record ", nextExpected, nextActual);
			// assertTrue("missing record "+actualId,
			// expectedIdCollection.contains(Integer.valueOf(actualId)));
		}
		assertFalse("too many records found", expectedIds.hasNext());
		// assertTrue("too few records found", recordCount ==
		// expectedIdCollection.size());
	}

	DatabaseRecord makeRecord(GenericDatabase<DatabaseRecord> database, FieldGenerator[] fieldGenerators,
			int fieldNums[], HashSet<String> keyWords) throws InputException {
		DatabaseRecord rec = database.newRecord();
		keyWords.clear();
		for (int i = 0; i < fieldNums.length; ++i) {
			String fieldText = fieldGenerators[i].makeFieldString(keyWords);
			rec.addFieldValue(fieldNums[i], fieldText);
		}
		return rec;
	}

	HashMap<String, List<Integer>> makeDatabase(LibrisDatabase database, int numRecs, FieldGenerator[] fieldGenerators,
			int fieldNums[]) throws LibrisException {
		HashMap<String, List<Integer>> keyWordsAndRecords = new HashMap<>();
		HashSet<String> keyWords = new HashSet<>();
		for (int i = 1; i <= numRecs; i++) {
			DatabaseRecord rec = makeRecord(database, fieldGenerators, fieldNums, keyWords);
			int recNum = database.putRecord(rec);
			for (String s : keyWords) {
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

	@Before
	public void setUp() throws Exception {
		workingDirectory = Utilities.makeTempTestDirectory();
	}

	@After
	public void tearDown() throws Exception {
		if (Objects.nonNull(db)) {
			assertTrue("Could not close database", db.closeDatabase(false));
		}
		db = null;
		Utilities.deleteTestDatabaseFiles();
	}

	private void getDatabase() throws FileNotFoundException, IOException {
		db = Utilities.buildTestDatabase(workingDirectory, Utilities.KEYWORD_DATABASE4_XML);
	}

	public static void main(String args[]) {

		TestRecordFilter testObj = new TestRecordFilter();
		if (true)
			try {
				testObj.setUp();
				testObj.testIndexStress();
				testObj.tearDown();
			} catch (Exception e) {
				e.printStackTrace();
			}
	}
}

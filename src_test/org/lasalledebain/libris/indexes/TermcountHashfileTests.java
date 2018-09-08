package org.lasalledebain.libris.indexes;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Objects;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.lasalledebain.Utilities;
import org.lasalledebain.hashtable.HashUtils;
import org.lasalledebain.libris.FileAccessManager;
import org.lasalledebain.libris.Libris;
import org.lasalledebain.libris.LibrisDatabase;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.hashfile.StringKeyHashFile;
import org.lasalledebain.libris.hashfile.TermCountHashBucket;
import org.lasalledebain.libris.hashfile.TermCountHashBucket.TermCountBucketFactory;
import org.lasalledebain.libris.hashfile.TermCountHashFile;
import org.lasalledebain.libris.index.IndexField;
import org.lasalledebain.libris.index.TermCountEntry;
import org.lasalledebain.libris.index.TermCountEntry.TermCountEntryFactory;
import org.lasalledebain.libris.records.Records;
import org.lasalledebain.libris.ui.LibrisUi;
import org.lasalledebain.libris.util.ByteArraySlice;
import org.lasalledebain.libris.util.DiagnosticDatabase;
import org.lasalledebain.libris.util.Lorem;
import org.lasalledebain.libris.xmlUtils.LibrisXMLConstants;

import junit.framework.TestCase;

public class TermcountHashfileTests extends TestCase {
	private RandomAccessFile backingStore;
	private File testFile;
	FileSpaceManager mgr;
	StringKeyHashFile<TermCountEntry, TermCountHashBucket, TermCountEntryFactory> hashFile;

	@Before
	protected void setUp() throws Exception {
		if (null == testFile) {
			testFile = Utilities.makeTestFileObject("termCountHashFile");
		}
		backingStore = HashUtils.MakeHashFile(testFile);
		mgr = Utilities.makeFileSpaceManager(getName()+"_mgr");
		hashFile = new TermCountHashFile(backingStore);
	}

	@After
	protected void tearDown() throws Exception {
		hashFile.clear();
		testFile.delete();
	}

	@Test
	public void testAddAndRetrieve() throws DatabaseException, IOException {
		final String[] testStrings = new String[] {"abc", "def", "ghi"};
		for (String s: testStrings) {
			final TermCountEntry entry = new TermCountEntry(s, 0);
			hashFile.addEntry(entry);
		}
		for (String s: testStrings) {
			TermCountEntry e = hashFile.getEntry(s);
			assertNotNull("missing entry for "+s, e);
			ByteArraySlice sBytes = new ByteArraySlice(s);
			assertEquals(sBytes, e.getKey());
		}
	}

	@Test
	public void testRandomWords() throws DatabaseException, IOException {
		HashMap<String, Integer> expected = new HashMap<>(); 
		for (String key: Lorem.getWords()) {
			Integer count = expected.get(key);
			if (Objects.isNull(count)) {
				expected.put(key, 1);
			} else {
				expected.put(key, count.intValue() + 1);
			}
			TermCountEntry entry = hashFile.getEntry(key);
			if (null == entry) {
				entry = new TermCountEntry(key, 1);
				hashFile.addEntry(entry);
			} else {
				entry.incrementCount();
			}
		}
		for (String key: Lorem.getWords()) {
			Integer count = expected.get(key);

			TermCountEntry entry = hashFile.getEntry(key);
			assertNotNull("key "+key+" missing", entry);
			assertEquals("key "+key+" wrong value", count.intValue(), entry.getTermCount());
		}
	}

	@Test
	public void testMultipleBuckets() throws DatabaseException, IOException {
		HashMap<String, Integer> expected = new HashMap<>();
		for (int i = 0; i < 100; ++i) {
			for (String baseKey: Lorem.getWords()) {
				String key = baseKey+"_"+i;
				Integer count = expected.get(key);
				if (Objects.isNull(count)) {
					expected.put(key, 1);
				} else {
					expected.put(key, count.intValue() + 1);
				}
				TermCountEntry entry = hashFile.getEntry(key);
				if (null == entry) {
					entry = new TermCountEntry(key, 1);
					hashFile.addEntry(entry);
				} else {
					entry.incrementCount();
				}
			}
		}
		for (String key: expected.keySet()) {
			Integer count = expected.get(key);

			TermCountEntry entry = hashFile.getEntry(key);
			assertNotNull("key "+key+" missing", entry);
			assertEquals("key "+key+" wrong value", count.intValue(), entry.getTermCount());
		}
	}
	
	@Test
	public void testTermCountIndex() throws DatabaseException, LibrisException, IOException {
		Utilities.deleteWorkingDirectory();
		File workDir = Utilities.getTempTestDirectory();
		if (null == workDir) {
			fail("could not create working directory ");
		}
		File tcFile = new File(workDir, "TermCountTest");
		tcFile.createNewFile();
		FileAccessManager mgr = new FileAccessManager(tcFile);
		TermCountIndex index = new TermCountIndex(mgr, true);
		final String[] terms = new String[] {"One", "Two", "three", "four", "one", "two", "three"};
		final int termCounts[] = new int[] {2, 2, 2, 1, 2, 2, 2};
		for (int i = 0; i < terms.length; ++i) {
			index.incrementTermCount(terms[i], true);
		}
		for (int i = 0; i < terms.length; ++i) {
			int tc = index.getTermCount(terms[i], true);
			assertEquals("wrong count for "+terms[i],  termCounts[i], tc);
		}
	}

	public void testBuildIndex() {
		try {
			File testDatabaseFile = Utilities.getTestDatabase(Utilities.KEYWORD_DATABASE4_XML);
			LibrisDatabase db = Libris.buildAndOpenDatabase(testDatabaseFile);
			IndexField[] indexFieldList = db.getSchema().getIndexFields(LibrisXMLConstants.XML_INDEX_NAME_KEYWORDS);
			final LibrisUi myUi = db.getUi();
			
			assertTrue("Could not close database", myUi.closeDatabase(false));
			db = myUi.openDatabase();
			Records recs = db.getDatabaseRecords();	
			for (Record r: recs) {
				RecordKeywords kw = new ExactKeywordList(true);
				r.getKeywords(indexFieldList, kw);
				for (String term: kw.getKeywords()) {
					int count = db.getTermCount(term, true);
					assertTrue("Missing count for "+term, count > 0);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception");
		}
	}
	

}

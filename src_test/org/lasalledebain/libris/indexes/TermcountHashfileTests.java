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
import org.lasalledebain.libris.DatabaseRecord;
import org.lasalledebain.libris.FileAccessManager;
import org.lasalledebain.libris.Libris;
import org.lasalledebain.libris.LibrisDatabase;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.hashfile.StringKeyHashFile;
import org.lasalledebain.libris.hashfile.TermCountHashBucket;
import org.lasalledebain.libris.hashfile.TermCountHashFile;
import org.lasalledebain.libris.index.IndexField;
import org.lasalledebain.libris.index.TermCountEntry;
import org.lasalledebain.libris.records.Records;
import org.lasalledebain.libris.ui.DatabaseUi;
import org.lasalledebain.libris.util.ByteArraySlice;
import org.lasalledebain.libris.util.Lorem;
import org.lasalledebain.libris.xmlUtils.LibrisXMLConstants;
import junit.framework.TestCase;

import static org.lasalledebain.libris.util.StringUtils.normalize;

public class TermcountHashfileTests extends TestCase {
	private RandomAccessFile backingStore;
	private File testFile;
	FileSpaceManager mgr;
	StringKeyHashFile<TermCountEntry, TermCountHashBucket> hashFile;
	private File workingDirectory;

	@Before
	protected void setUp() throws Exception {
		workingDirectory = Utilities.makeTempTestDirectory();
		if (null == testFile) {
			testFile = Utilities.makeTestFileObject(workingDirectory, "termCountHashFile");
		}
		backingStore = HashUtils.MakeHashFile(testFile);
		mgr = Utilities.makeFileSpaceManager(workingDirectory, getName()+"_mgr");
		hashFile = new TermCountHashFile(backingStore);
	}

	@After
	protected void tearDown() throws Exception {
		hashFile.clear();
		testFile.delete();
		Utilities.deleteWorkingDirectory();
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
		File workDir = Utilities.makeTempTestDirectory();
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
			index.incrementTermCount(normalize(terms[i]));
		}
		for (int i = 0; i < terms.length; ++i) {
			int tc = index.getTermCount(normalize(terms[i]));
			assertEquals("wrong count for "+terms[i],  termCounts[i], tc);
		}
	}

	public void testBuildIndex() {
		try {
			File testDatabaseFile = Utilities.copyTestDatabaseFile(Utilities.KEYWORD_DATABASE4_XML, workingDirectory);
			LibrisDatabase db = Libris.buildAndOpenDatabase(testDatabaseFile);
			IndexField[] indexFieldList = db.getSchema().getIndexFields(LibrisXMLConstants.XML_INDEX_NAME_KEYWORDS);
			final DatabaseUi myUi = db.getUi();
			
			assertTrue("Could not close database", myUi.closeDatabase(false));
			db = myUi.openDatabase();
			Records<DatabaseRecord> recs = db.getDatabaseRecords();	
			for (Record r: recs) {
				RecordKeywords kw = new ExactKeywordList(true);
				r.getKeywords(indexFieldList, kw);
				for (String term: kw.getKeywords()) {
					int count = db.getTermCount(term);
					assertTrue("Missing count for "+term, count > 0);
				}
			}
			assertTrue("could not close database", db.closeDatabase(false));
		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception "+e.getMessage());
		}
	}
	

}

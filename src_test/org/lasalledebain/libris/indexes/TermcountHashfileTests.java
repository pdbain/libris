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
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.hashfile.StringKeyHashFile;
import org.lasalledebain.libris.hashfile.TermCountHashBucket;
import org.lasalledebain.libris.hashfile.TermCountHashBucket.TermCountBucketFactory;
import org.lasalledebain.libris.hashfile.TermCountHashFile;
import org.lasalledebain.libris.index.TermCountEntry;
import org.lasalledebain.libris.index.TermCountEntry.TermCountEntryFactory;
import org.lasalledebain.libris.util.ByteArraySlice;
import org.lasalledebain.libris.util.Lorem;

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
	//	hashFile.resize(16);
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
}

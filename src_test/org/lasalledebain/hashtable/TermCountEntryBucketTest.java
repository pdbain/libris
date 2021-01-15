package org.lasalledebain.hashtable;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.hashfile.TermCountHashBucket;
import org.lasalledebain.libris.index.TermCountEntry;
import org.lasalledebain.libris.indexes.FileSpaceManager;
import org.lasalledebain.libris.util.Utilities;

import junit.framework.TestCase;

public class TermCountEntryBucketTest extends TestCase {
	private static final String TESTSTRING1 = "teststring1";
	private RandomAccessFile backingStore;
	private File testFile = null;
	FileSpaceManager mgr;
	private TermCountHashBucket buck;
	@Before
	protected void setUp() throws Exception {
		File workingDirectory = Utilities.makeTempTestDirectory("termCountHashFile");
		testFile = Utilities.makeTestFileObject(workingDirectory, "testIndexFile");
		backingStore = HashUtils.MakeHashFile(testFile);
		mgr = Utilities.makeFileSpaceManager(workingDirectory, getName()+"_mgr");
		buck = new TermCountHashBucket(backingStore, 0);
	}

	@After
	protected void tearDown() throws Exception {
		testFile.delete();
		Utilities.deleteWorkingDirectory();
	}

	@Test
	public void testSanity() throws DatabaseException {
		TermCountEntry e = TermCountEntry.makeEntry(TESTSTRING1, 0);
		buck.addEntry(e);
		TermCountEntry e2 = buck.getEntry(TESTSTRING1);
		assertEquals(e, e2);
		e.incrementCount();
		assertEquals("Increment failed",  e2.getTermCount(), 1);
	}

	@Test
	public void testOverflow() throws DatabaseException {
		HashMap<String, TermCountEntry> expected = new HashMap<>();
		boolean success = true;
		int count = 0;
		while (success) {
			final String key = TESTSTRING1+"_"+count;
			TermCountEntry e = TermCountEntry.makeEntry(key, 0);
			success = buck.addEntry(e);
			if (success) expected.put(key, e);
			++count;
		}
		for (TermCountEntry e: expected.values()) {
			TermCountEntry actual = buck.get(e.getKey());
			assertNotNull(e.toString()+" missing (byte array)", actual);
			actual = buck.getEntry(e.getKey().toString());
			assertNotNull(e.toString()+" missing (string):", actual);
		}
	}

	@Test
	public void testWriteRead() throws DatabaseException, IOException {
		HashMap<String, TermCountEntry> expected = new HashMap<>();
		boolean success = true;
		int count = 0;
		while (success) {
			final String key = TESTSTRING1+"_"+count;
			TermCountEntry e = TermCountEntry.makeEntry(key, 0);
			success = buck.addEntry(e);
			if (success) expected.put(key, e);
			++count;
		}
		buck.write();
		backingStore.getChannel().force(true);
		RandomAccessFile raf = new RandomAccessFile(testFile, "rw");

		TermCountHashBucket newBucket = new TermCountHashBucket(raf, 0);
		newBucket.read();
		for (TermCountEntry e: expected.values()) {
			TermCountEntry actual = newBucket.get(e.getKey());
			assertNotNull(e.toString()+" missing (byte array)", actual);
		}
	}

}

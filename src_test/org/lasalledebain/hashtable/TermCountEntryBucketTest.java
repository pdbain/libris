package org.lasalledebain.hashtable;

import java.io.File;
import java.io.RandomAccessFile;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.lasalledebain.Utilities;
import org.lasalledebain.libris.hashfile.TermCountHashBucket;
import org.lasalledebain.libris.hashfile.TermCountHashBucket.TermCountBucketFactory;
import org.lasalledebain.libris.index.TermCountEntry;
import org.lasalledebain.libris.indexes.FileSpaceManager;

import junit.framework.TestCase;

public class TermCountEntryBucketTest extends TestCase {
	private RandomAccessFile backingStore;
	private File testFile;
	FileSpaceManager mgr;
	private TermCountHashBucket buck;
	private TermCountEntry.TermCountEntryFactory entryFactory;

	@Before
	protected void setUp() throws Exception {
		if (null == testFile) {
			testFile = Util.makeTestFileObject("termCountHashFile");
		}
		backingStore = Util.MakeHashFile(testFile);
		mgr = Utilities.makeFileSpaceManager(getName()+"_mgr");
		TermCountBucketFactory bfact = new TermCountHashBucket.TermCountBucketFactory();
		entryFactory = new TermCountEntry.TermCountEntryFactory();
		buck = bfact.createBucket(backingStore, 0, entryFactory);
	}

	@After
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	@Test
	public void test() {
		fail("Not yet implemented");
	}

}

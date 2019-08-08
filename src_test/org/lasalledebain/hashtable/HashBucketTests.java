package org.lasalledebain.hashtable;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

import org.junit.Test;
import org.lasalledebain.Utilities;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.hashfile.HashBucket;
import org.lasalledebain.libris.hashfile.NumericKeyHashBucket;
import org.lasalledebain.libris.hashfile.NumericKeyHashEntry;
import org.lasalledebain.libris.indexes.FileSpaceManager;
import org.lasalledebain.libris.indexes.MockFixedSizeEntryBucket;

import junit.framework.TestCase;

public class HashBucketTests extends TestCase {

	private File testFile = null;
	private File workingDirectory;

	@Test
	public void testAddEntry() {
		HashBucket buck = new MockFixedSizeEntryBucket(null, 0);
		ArrayList<NumericKeyHashEntry> entries;
		try {
			entries = HashUtils.fixedSizeFillBucket(buck, 42,(byte) 1);
			HashUtils.checkBucket(buck, entries);
		} catch (DatabaseException e) {
			e.printStackTrace();
			fail("Unexpected exception on hashfile");
		}
		
	}

	@Test
	public void testReadAndWrite() {
		RandomAccessFile hashFile = HashUtils.MakeHashFile(testFile);
		HashBucket writeBucket = new MockFixedSizeEntryBucket(hashFile,0,10);
		ArrayList<NumericKeyHashEntry> entries = null;
		try {
			entries = HashUtils.fixedSizeFillBucket(writeBucket, 10, (byte) 2);
			writeBucket.write();
		} catch (DatabaseException e) {
			e.printStackTrace();
			fail("Unexpected exception on hashfile");
		}
		
		HashBucket<?> readBucket = new MockFixedSizeEntryBucket(hashFile,0,10);
		try {
			readBucket.read();
		} catch (Exception e1) {
			e1.printStackTrace();
			fail("Unexpected exception in read hash bucket: "+e1.getMessage());
		}
		
		try {
		HashUtils.checkBucket((NumericKeyHashBucket<NumericKeyHashEntry>) readBucket, entries);
			hashFile.close();
		} catch (IOException e) {
			e.printStackTrace();
			fail("IOException closing hashFile: "+e);
		}
		testFile.delete();
	}

	@Test
	public void testVariableSizeReadAndWrite() {
		FileSpaceManager mgr = Utilities.makeFileSpaceManager(workingDirectory, getName()+"_mgr");
		MockOverflowManager overflowManager = new MockOverflowManager(mgr);

		RandomAccessFile backingStore = HashUtils.MakeHashFile(testFile);
		HashBucket writeBucket = new MockVariableSizeEntryBucket(backingStore, 0, overflowManager);
		ArrayList<NumericKeyHashEntry> entries = null;
		try {
			entries = HashUtils.variableSizeFillBucket(writeBucket, (byte) 2);
			writeBucket.write();
		} catch (DatabaseException e) {
			e.printStackTrace();
			fail("Unexpected exception on hashfile");
		}
		
		HashBucket<?> readBucket = new MockVariableSizeEntryBucket(backingStore, 0, overflowManager);
		try {
			readBucket.read();
		} catch (Exception e1) {
			e1.printStackTrace();
			fail("Unexpected exception in read hash bucket: "+e1.getMessage());
		}
		
		try {
		HashUtils.checkBucket((NumericKeyHashBucket<NumericKeyHashEntry>) readBucket, entries);
			backingStore.close();
		} catch (IOException e) {
			e.printStackTrace();
			fail("IOException closing hashFile: "+e);
		}
		testFile.delete();
	}

	@Override
	protected void setUp() throws Exception {
		workingDirectory = Utilities.makeTempTestDirectory("TestFileRecordMap");
		testFile = Utilities.makeTestFileObject(workingDirectory, "testIndexFile");
	}

	@Override
	protected void tearDown() throws Exception {
		Utilities.deleteWorkingDirectory();
	}

}

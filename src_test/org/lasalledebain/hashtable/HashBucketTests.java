package org.lasalledebain.hashtable;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Iterator;

import org.junit.Test;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.hashfile.FixedSizeHashEntry;
import org.lasalledebain.libris.hashfile.HashBucket;
import org.lasalledebain.libris.hashfile.NumericKeyHashBucket;
import org.lasalledebain.libris.hashfile.NumericKeyHashEntry;
import org.lasalledebain.libris.hashfile.VariableSizeHashEntry;
import org.lasalledebain.libris.indexes.FileSpaceManager;
import org.lasalledebain.libris.indexes.MockFixedSizeEntryBucket;
import org.lasalledebain.libris.indexes.MockFixedSizeEntryBucket2;
import org.lasalledebain.libris.util.Utilities;

import junit.framework.TestCase;

public class HashBucketTests extends TestCase {

	private File testFile = null;
	private File workingDirectory;

	@Test
	public void testAddEntry() {
		HashBucket<FixedSizeHashEntry> buck = new MockFixedSizeEntryBucket2(null, 0);
		ArrayList<NumericKeyHashEntry> entries;
		try {
			entries = HashUtils.fixedSizeFillBucket2(buck, 42,(byte) 1);
			HashUtils.checkBucket2(buck, entries);
		} catch (DatabaseException e) {
			e.printStackTrace();
			fail("Unexpected exception on hashfile");
		}
		
	}

	@Test
	public void testReadAndWrite() {
		RandomAccessFile hashFile = HashUtils.MakeHashFile(testFile);
		HashBucket<FixedSizeHashEntry> writeBucket = new MockFixedSizeEntryBucket2(hashFile,0,10);
		ArrayList<NumericKeyHashEntry> entries = null;
		try {
			entries = HashUtils.fixedSizeFillBucket2(writeBucket, 10, (byte) 2);
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
		HashUtils.checkBucket3(readBucket, entries);
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
		HashBucket<VariableSizeHashEntry> writeBucket = new MockVariableSizeEntryBucket(backingStore, 0, overflowManager);
		ArrayList<NumericKeyHashEntry> entries = null;
		try {
			byte initialData = (byte) 2;
			int bucketSize = NumericKeyHashBucket.getBucketSize();
			int entryCount = 0;
			int entryLength = 10; 
			MockVariableSizeHashEntry newEntry = null;
			entries = new ArrayList<NumericKeyHashEntry>();
			
			do {
				if (null != newEntry) {
					entries.add(newEntry);
				}
				int occupancy = writeBucket.getOccupancy();
				newEntry = new MockVariableSizeHashEntry(entryCount+1, entryLength, initialData);
				/* all buckets have at least 4 bytes */
				HashUtils.assertEquals("wrong value for occupancy for key "+entryCount, 
						entryCount*newEntry.getTotalLength()+2, occupancy);
				++entryCount;
				++initialData;
				boolean expectedOccupancy = entryCount <= ((bucketSize/newEntry.getTotalLength()) + 1);
				HashUtils.assertTrue("bucket overfilled: "+entryCount, expectedOccupancy);
			} while (writeBucket.addEntry(newEntry));
			writeBucket.write();
		} catch (DatabaseException e) {
			e.printStackTrace();
			fail("Unexpected exception on hashfile");
		}
		
		MockVariableSizeEntryBucket readBucket = new MockVariableSizeEntryBucket(backingStore, 0, overflowManager);
		try {
			readBucket.read();
		} catch (Exception e1) {
			e1.printStackTrace();
			fail("Unexpected exception in read hash bucket: "+e1.getMessage());
		}
		
		try {
		Iterator<NumericKeyHashEntry> ti = entries.iterator();
		int entryCount = 0;
		for (NumericKeyHashEntry e: readBucket) {
			HashUtils.assertTrue("too many entries in the bucket", ti.hasNext());
			NumericKeyHashEntry t = ti.next();
			HashUtils.assertTrue("mismatch in hash entries", t.equals(e));
			++entryCount;
		}
		HashUtils.assertFalse("too few entries in the bucket.  Expected "+entries.size()+" got "+entryCount, ti.hasNext());
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

package org.lasalledebain.hashtable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Iterator;

import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.hashfile.FixedSizeHashEntry;
import org.lasalledebain.libris.hashfile.HashBucket;
import org.lasalledebain.libris.hashfile.HashEntry;
import org.lasalledebain.libris.hashfile.NumericKeyHashBucket;
import org.lasalledebain.libris.hashfile.NumericKeyHashEntry;

import junit.framework.TestCase;

public class HashUtils extends TestCase {

	static void checkBucket2(HashBucket<FixedSizeHashEntry> buck,
			ArrayList<NumericKeyHashEntry> entries) {
		Iterator<NumericKeyHashEntry> ti = entries.iterator();
		int entryCount = 0;
		for (NumericKeyHashEntry e: buck) {
			assertTrue("too many entries in the bucket", ti.hasNext());
			NumericKeyHashEntry t = ti.next();
			assertTrue("mismatch in hash entries", t.equals(e));
			++entryCount;
		}
		assertFalse("too few entries in the bucket.  Expected "+entries.size()+" got "+entryCount, ti.hasNext());
	}

	static void checkBucket3(HashBucket<?> readBucket,
			ArrayList<NumericKeyHashEntry> entries) {
		Iterator<NumericKeyHashEntry> ti = entries.iterator();
		int entryCount = 0;
		for (HashEntry e: readBucket) {
			assertTrue("too many entries in the bucket", ti.hasNext());
			NumericKeyHashEntry t = ti.next();
			assertTrue("mismatch in hash entries", t.equals(e));
			++entryCount;
		}
		assertFalse("too few entries in the bucket.  Expected "+entries.size()+" got "+entryCount, ti.hasNext());
	}

	/**
	 * @param buck
	 * @param initialData
	 * @return
	 * @throws DatabaseException 
	 */
	static ArrayList<FixedSizeHashEntry> fixedSizeFillBucket(HashBucket<NumericKeyHashEntry> buck, int entryLength,
			byte initialData) throws DatabaseException {
		int bucketSize = NumericKeyHashBucket.getBucketSize();
		ArrayList<FixedSizeHashEntry> entries;
		int entryCount = 0;
		MockFixedSizeHashEntry newEntry = null;
		entries = new ArrayList<FixedSizeHashEntry>();
		
		do {
			if (null != newEntry) {
				entries.add(newEntry);
			}
			int occupancy = buck.getOccupancy();
			newEntry = new MockFixedSizeHashEntry(entryCount+1, entryLength, initialData);
			/* all buckets have at least 4 bytes */
			int expectedOccupancy = entryCount*newEntry.getTotalLength()+4;
			assertEquals("wrong value for occupancy for key "+entryCount, 
					expectedOccupancy, occupancy);
			++entryCount;
			++initialData;
			boolean notOverfilled = entryCount <= ((bucketSize/newEntry.getTotalLength()) + 1);
			assertTrue("bucket overfilled: "+entryCount, notOverfilled);
		} while (buck.addEntry(newEntry));
		return entries;
	}

	/**
	 * @param buck
	 * @param initialData
	 * @return
	 * @throws DatabaseException 
	 */
	static ArrayList<NumericKeyHashEntry> fixedSizeFillBucket2(HashBucket<FixedSizeHashEntry> buck, int entryLength,
			byte initialData) throws DatabaseException {
		int bucketSize = NumericKeyHashBucket.getBucketSize();
		ArrayList<NumericKeyHashEntry> entries;
		int entryCount = 0;
		MockFixedSizeHashEntry newEntry = null;
		entries = new ArrayList<NumericKeyHashEntry>();
		
		do {
			if (null != newEntry) {
				entries.add(newEntry);
			}
			int occupancy = buck.getOccupancy();
			newEntry = new MockFixedSizeHashEntry(entryCount+1, entryLength, initialData);
			/* all buckets have at least 4 bytes */
			int expectedOccupancy = entryCount*newEntry.getTotalLength()+4;
			assertEquals("wrong value for occupancy for key "+entryCount, 
					expectedOccupancy, occupancy);
			++entryCount;
			++initialData;
			boolean notOverfilled = entryCount <= ((bucketSize/newEntry.getTotalLength()) + 1);
			assertTrue("bucket overfilled: "+entryCount, notOverfilled);
		} while (buck.addEntry(newEntry));
		return entries;
	}

	public static RandomAccessFile MakeHashFile(File tf) {
		try {
			tf.delete();
			RandomAccessFile f = new RandomAccessFile(tf, "rw");
			tf.deleteOnExit();
			return f;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			fail("cannot create file "+tf.getAbsolutePath());
			return null;
		}
	}

}

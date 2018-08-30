package org.lasalledebain.hashtable;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

import org.junit.Test;
import org.lasalledebain.Utilities;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.hashfile.FixedSizeEntryHashBucket;
import org.lasalledebain.libris.hashfile.HashBucket;
import org.lasalledebain.libris.hashfile.NumericKeyHashBucketFactory;
import org.lasalledebain.libris.hashfile.NumericKeyHashBucket;
import org.lasalledebain.libris.hashfile.NumericKeyHashEntry;
import org.lasalledebain.libris.hashfile.VariableSizeEntryHashBucket;
import org.lasalledebain.libris.indexes.FileSpaceManager;

import junit.framework.TestCase;

public class HashBucketTests extends TestCase {

	private File testFile = null;

	@Test
	public void testAddEntry() {
		NumericKeyHashBucketFactory bfact = FixedSizeEntryHashBucket.getFactory();
		HashBucket buck = bfact.createBucket(null, 0, null);
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
		NumericKeyHashBucketFactory bfact = FixedSizeEntryHashBucket.getFactory();
		MockFixedSizeEntryFactory fact = new MockFixedSizeEntryFactory(10);
		RandomAccessFile hashFile = HashUtils.MakeHashFile(testFile);
		HashBucket writeBucket = bfact.createBucket(hashFile,0,fact);
		ArrayList<NumericKeyHashEntry> entries = null;
		try {
			entries = HashUtils.fixedSizeFillBucket(writeBucket, 10, (byte) 2);
			writeBucket.write();
		} catch (DatabaseException e) {
			e.printStackTrace();
			fail("Unexpected exception on hashfile");
		}
		
		HashBucket<?> readBucket = bfact.createBucket(hashFile,0,fact);
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
		FileSpaceManager mgr = Utilities.makeFileSpaceManager(getName()+"_mgr");
		MockOverflowManager oversizeEntryManager = new MockOverflowManager(mgr);

		NumericKeyHashBucketFactory bfact = VariableSizeEntryHashBucket.getFactory(oversizeEntryManager);
		MockVariableSizeEntryFactory fact = new MockVariableSizeEntryFactory(10);
		RandomAccessFile hashFile = HashUtils.MakeHashFile(testFile);
		HashBucket writeBucket = bfact.createBucket(hashFile,0,fact);
		ArrayList<NumericKeyHashEntry> entries = null;
		try {
			entries = HashUtils.variableSizeFillBucket(writeBucket, (byte) 2);
			writeBucket.write();
		} catch (DatabaseException e) {
			e.printStackTrace();
			fail("Unexpected exception on hashfile");
		}
		
		HashBucket<?> readBucket = bfact.createBucket(hashFile,0,fact);
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

	@Override
	protected void setUp() throws Exception {
		if (null == testFile) {
			testFile = Utilities.makeTestFileObject("TestFileRecordMap");
		}
	}

}

package org.lasalledebain.hashtable;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.hashfile.AffiliateHashBucket;
import org.lasalledebain.libris.hashfile.NumericKeyHashBucket;
import org.lasalledebain.libris.hashfile.VariableSizeHashEntry;
import org.lasalledebain.libris.index.AffiliateListEntry;
import org.lasalledebain.libris.indexes.FileSpaceManager;
import org.lasalledebain.libris.util.Utilities;

import junit.framework.TestCase;

public class VariableSizeEntryHashBucketTest extends TestCase{

	private File workingDirectory;
	private File testFile;
	private MockVariableSizeEntryBucket buck;
	private RandomAccessFile backingStore;
	FileSpaceManager mgr;
	private MockOverflowManager oversizeEntryManager;
	private int oversizeThreshold;

	@Test
	public void testAddElement() {
		try {
			final int numEntries = 16;
			HashMap<Integer, VariableSizeHashEntry> entries= new HashMap<Integer, VariableSizeHashEntry>(numEntries);
			populateBucket(numEntries, entries);
			checkEntries(buck, numEntries, entries);

		} catch (DatabaseException | IOException e) {
			e.printStackTrace();
			fail("unexpected exception:"+e.getMessage());
		}
	}

	@Test
	public void testOverflow() {
		try {
			final int numEntries = 128;
			HashMap<Integer, VariableSizeHashEntry> entries= new HashMap<Integer, VariableSizeHashEntry>(numEntries);
			int entryCount = populateBucket(numEntries, entries);
			checkEntries(buck, entryCount, entries);

		} catch (DatabaseException | IOException e) {
			e.printStackTrace();
			fail("unexpected exception:"+e.getMessage());
		}
	}

	@Test
	public void testVariableSize() {
		try {
			final int numEntries = 128;
			int entryCount = 0;
			HashMap<Integer, VariableSizeHashEntry> entries= new HashMap<Integer, VariableSizeHashEntry>(numEntries);
			int expectedOccupancy = 2;
			for (int key = 1; key < numEntries; key++ ) {
				int length = entryCount + 1;
				VariableSizeHashEntry newEntry = makeVariableSizeEntry(key, length);
				entries.put(Integer.valueOf(key), newEntry);
				boolean result = buck.addEntry(newEntry);
				++entryCount;
				expectedOccupancy += 4 + 2 + length;
				if (expectedOccupancy <= NumericKeyHashBucket.getBucketSize()) {
					assertTrue("bucket add failed on key "+key, result);
				} else {
					assertFalse("overflow not detected on key "+key, result);
					break;
				}
			}
			checkEntries(buck, entryCount - 1, entries);

		} catch (DatabaseException e) {
			e.printStackTrace();
			fail("unexpected exception:"+e.getMessage());
		}
	}

	@Test
	public void testAffiliatesSanity() {
		try {
			final int numEntries = 20;
			final int affiliateScale = 10;
			Random r = new Random(314126535);
			HashMap<Integer, int[][]> entries= new HashMap<Integer, int[][]>(numEntries);
			AffiliateHashBucket affiliateBucket = new AffiliateHashBucket(null, 0, null);
// TODO remove duplicate from expected
			for (int key = 1; key < numEntries; key++ ) {
				int childrenAndAffiliates[][] = new int[2][];
				AffiliateListEntry newEntry = new AffiliateListEntry(key);
				double ng = r.nextGaussian();
				int numChildren = Math.abs((int) (ng * affiliateScale));
				HashSet<Integer> affiliateList = new HashSet<>(numChildren);
				for (int i = 0; i < numChildren; ++i) {
					int newChild = r.nextInt(numEntries) + 1;
					affiliateList.add(newChild);
					newEntry = new AffiliateListEntry(newEntry, newChild, true);
				}
				childrenAndAffiliates[0] = Utilities.toIntList(affiliateList);
				Arrays.sort(childrenAndAffiliates[0]);
				
				ng = r.nextGaussian();
				int numAffiliates = Math.abs((int) (ng * affiliateScale));
				affiliateList = new HashSet<>(numAffiliates);				
				for (int i = 0; i < numAffiliates; ++i) {
					int newAffiliate = r.nextInt(numEntries) + 1;
					affiliateList.add(newAffiliate);
					newEntry = new AffiliateListEntry(newEntry, newAffiliate, false);
				}
				childrenAndAffiliates[1] = Utilities.toIntList(affiliateList);
				Arrays.sort(childrenAndAffiliates[1]);
				entries.put(key, childrenAndAffiliates);
				affiliateBucket.addEntry(newEntry);
			}
			for (int key = 1; key < numEntries; key++ ) {
				AffiliateListEntry e = affiliateBucket.getEntry(key);
				int[][] expectedData = entries.get(key);
				assertTrue("Lists do not match", Utilities.compareIntLists("children", expectedData[0], e.getChildren()));
				assertTrue("Lists do not match", 
						Utilities.compareIntLists("affiliates", expectedData[1], e.getAffiliates()));
			}

		} catch (DatabaseException e) {
			e.printStackTrace();
			fail("unexpected exception:"+e.getMessage());
		}
	}

	@Test
	public void testExactFill() {
		try {
			final int numEntries = 128;
			int entryCount = 0;
			HashMap<Integer, VariableSizeHashEntry> entries= new HashMap<Integer, VariableSizeHashEntry>(numEntries);
			int expectedOccupancy = 2;
			for (int key = 1; key < numEntries; key++ ) {
				int length = Math.min(32, NumericKeyHashBucket.getBucketSize()-(expectedOccupancy + 6));
				VariableSizeHashEntry newEntry = makeVariableSizeEntry(key, length);
				entries.put(Integer.valueOf(key), newEntry);
				boolean result = buck.addEntry(newEntry);
				++entryCount;
				expectedOccupancy += 4 + 2 + length;
				if (expectedOccupancy >= NumericKeyHashBucket.getBucketSize()) {
					break;
				}
				assertTrue("bucket add failed on key "+key, result);
				int actualOccupancy = buck.getOccupancy();
				assertEquals("Wrong occupancy on key "+key+": ", expectedOccupancy, actualOccupancy);
			}
			assertEquals("bucket not exactly filled.  Occupancy: ", expectedOccupancy, NumericKeyHashBucket.getBucketSize());
			checkEntries(buck, entryCount, entries);

		} catch (DatabaseException e) {
			e.printStackTrace();
			fail("unexpected exception:"+e.getMessage());
		}
	}

	private MockVariableSizeHashEntry makeVariableSizeEntry(int key, int length) {
		byte[] dat = new byte[length];
		for (int i = 0; i < length; ++i) {
			dat[i] = (byte) (key + i);
		}
		MockVariableSizeHashEntry newEntry = new MockVariableSizeHashEntry(key, dat);
		newEntry.setOversize((oversizeThreshold > 0) && (length >= oversizeThreshold));
		return newEntry;
	}

	@Test
	public void testUpdate() {
		try {
			int expectedOccupancy = 2;
			int initialSize = 128;
			final int bucketSize = NumericKeyHashBucket.getBucketSize();
			int totalLength = makeVariableSizeEntry(1, initialSize).getTotalLength();
			final int numEntries = (bucketSize - expectedOccupancy)/totalLength;
			VariableSizeHashEntry newEntry;
			for (int key = 1; key <= numEntries; key++ ) {
				newEntry = makeVariableSizeEntry(key, initialSize);
				boolean result = buck.addEntry(newEntry);
				assertTrue("unexpected overflow", result);
			}
			int testKey = numEntries/2;
			byte newData[] = new byte[initialSize];
			Arrays.fill(newData, (byte) 0x11);
			VariableSizeHashEntry testEntry = makeEntry(testKey, newData);

			boolean result = buck.addEntry(testEntry);
			assertTrue("unexpected overflow", result);
			testEntry = buck.getEntry(testKey);
			assertTrue(Arrays.equals(newData, testEntry.getData()));

			newData = new byte[2*initialSize];
			Arrays.fill(newData, (byte) 0x22);
			testEntry = makeEntry(testKey, newData);
			result = buck.addEntry(testEntry);
			assertFalse("missing overflow", result);	

			newData = new byte[initialSize];
			Arrays.fill(newData, (byte) 0x33);
			testEntry = makeEntry(testKey, newData);
			result = buck.addEntry(testEntry);
			assertTrue("unexpected overflow", result);
			testEntry = buck.getEntry(testKey);
			assertTrue(Arrays.equals(newData, testEntry.getData()));

		} catch (DatabaseException | IOException e) {
			e.printStackTrace();
			fail("unexpected exception:"+e.getMessage());
		}
	}

	private MockVariableSizeHashEntry makeEntry(int testKey, byte[] newData) {
		return new MockVariableSizeHashEntry(testKey, newData);
	}

	@Test
	public void testWriteRead() {
		try {
			final int numEntries = 16;
			HashMap<Integer, VariableSizeHashEntry> entries= new HashMap<Integer, VariableSizeHashEntry>(numEntries);
			populateBucket(numEntries, entries);
			buck.write();
			checkEntries(buck, numEntries, entries);
			NumericKeyHashBucket<VariableSizeHashEntry> newBuck = makeBucket();
			newBuck.read();
			checkEntries(newBuck, numEntries, entries);
		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception:"+e.getMessage());
		}
	}

	@Test
	public void testOversize() {
		try {
			final int numEntries = 16;
			HashMap<Integer, VariableSizeHashEntry> entries= new HashMap<Integer, VariableSizeHashEntry>(numEntries);

			oversizeThreshold = 64;
			int length = 2;
			for (int key = 1; key <= numEntries; key++ ) {
				VariableSizeHashEntry newEntry = createEntry(key, length);
				entries.put(key, newEntry);
				boolean result = buck.addEntry(newEntry);
				assertTrue("bucket add failed on key "+key, result);
				length *= 2;
			}			
			buck.write();
			checkEntries(buck, numEntries, entries);
			NumericKeyHashBucket<VariableSizeHashEntry> newBuck = makeBucket();
			newBuck.read();
			checkEntries(newBuck, numEntries, entries);
		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception:"+e.getMessage());
		}
	}

	@Test
	public void testRandomSizes() {
		try {
			int numEntries = 0;
			HashMap<Integer, VariableSizeHashEntry> entries= new HashMap<Integer, VariableSizeHashEntry>(numEntries);

			oversizeThreshold = 64;
			Random lengthGen = new Random(271828);
			boolean addMore = true;
			for (int key = 1; addMore; key++ ) {
				int range = lengthGen.nextInt(10) + 1;
				int length = Math.abs(lengthGen.nextInt() % (1 << range));
				VariableSizeHashEntry newEntry = createEntry(key, length);
				addMore = buck.addEntry(newEntry);
				if (addMore) {
					entries.put(key, newEntry);
					++numEntries;
				}
			}			
			buck.write();
			checkEntries(buck, numEntries, entries);
			NumericKeyHashBucket<VariableSizeHashEntry> newBuck = makeBucket();
			newBuck.read();
			checkEntries(newBuck, numEntries, entries);
		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception:"+e.getMessage());
		}
	}

	@Test
	public void testIterator() {
		try {
			final int numEntries = 16;
			HashMap<Integer, VariableSizeHashEntry> entries= new HashMap<Integer, VariableSizeHashEntry>(numEntries);
			populateBucket(numEntries, entries);
			buck.write();
			checkEntries(buck, numEntries, entries);
			 MockVariableSizeEntryBucket newBuck = makeBucket();
			newBuck.read();
			int actualCount = 0;
			Iterator<VariableSizeHashEntry> iter = newBuck.iterator();
			while (iter.hasNext()) {
				VariableSizeHashEntry e = iter.next();
				assertNotNull("missing entry after "+actualCount+" entries", e);
				int key = e.getKey();
				VariableSizeHashEntry comparand = entries.get(Integer.valueOf(key));
				assertNotNull("key "+key+" missing", comparand);
				assertEquals(e, comparand);
				iter.remove();
				++actualCount ;
			}
			assertEquals("Wrong number of entries", numEntries, actualCount);
			iter = newBuck.iterator();
			assertFalse("remove didn't work", iter.hasNext());
			newBuck.write();
			newBuck = makeBucket();
			newBuck.read();
			iter = newBuck.iterator();
			assertFalse("remove didn't work", iter.hasNext());
		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception:"+e.getMessage());
		}
	}

	@Test
	public void testRemoveEntries() {
		try {
			final int numEntries = 8;
			HashMap<Integer, VariableSizeHashEntry> entries= new HashMap<Integer, VariableSizeHashEntry>(numEntries);

			oversizeThreshold = 64;
			int length = 4;
			for (int key = 1; key <= numEntries; key++ ) {
				VariableSizeHashEntry newEntry = createEntry(key, length);
				entries.put(key, newEntry);
				boolean result = buck.addEntry(newEntry);
				assertTrue("bucket add failed on key "+key, result);
				length *= 2;
			}			
			buck.write();

			checkEntries(buck, numEntries, entries);
			NumericKeyHashBucket<VariableSizeHashEntry> newBuck = makeBucket();
			newBuck.read();
			Iterator<VariableSizeHashEntry> iter = newBuck.iterator();
			while (iter.hasNext()) { /* remove half the entries */
				iter.next();
				iter.remove();
				iter.next();
			}
			newBuck.write();
			newBuck = makeBucket();
			newBuck.read();
			countOversize(2);
		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception:"+e.getMessage());
		}
	}

	private void countOversize(int expectedCount) {
		Iterator<Long> oversizeIter;
		int oversizeEntryCount;
		oversizeIter = oversizeEntryManager.iterator();
		oversizeEntryCount = 0;
		while (oversizeIter.hasNext()) {
			++oversizeEntryCount;
			oversizeIter.next();
		}
		assertEquals("wrong number of oversize entries", expectedCount, oversizeEntryCount);
	}

	@Test
	public void testModifyEntries() {

		try {
			final int numEntries = 16;
			HashMap<Integer, VariableSizeHashEntry> entries= new HashMap<Integer, VariableSizeHashEntry>(numEntries);
			populateBucket(numEntries, entries);
			checkEntries(buck, numEntries, entries);
			buck.write();
			{
				NumericKeyHashBucket<VariableSizeHashEntry> tempBuck = makeBucket();
				tempBuck.read();
				checkEntries(tempBuck, numEntries, entries);
				VariableSizeHashEntry e = tempBuck.getEntry(3);
				byte[] dat = e.getData();
				dat[0] = (byte) 0x55;
				entries.put(3, e);
				e = tempBuck.getEntry(13);
				dat = e.getData();
				dat[0] = (byte) 0xaa;
				entries.put(13, e);
				tempBuck.setDirty(true);
				tempBuck.write();
				tempBuck.read();
				checkEntries(tempBuck, numEntries, entries);
			}		

		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception:"+e.getMessage());
		}
	}

	@Test
	public void testResizeEntries() {
		try {
			final int numEntries = 16;
			oversizeThreshold = 64;
			HashMap<Integer, VariableSizeHashEntry> entries= new HashMap<Integer, VariableSizeHashEntry>(numEntries);

			{
				NumericKeyHashBucket<VariableSizeHashEntry> tempBuck = makeBucket();


				int length = 2;
				for (int key = 1; key <= numEntries; key++ ) {
					VariableSizeHashEntry newEntry = createEntry(key, length);
					entries.put(key, newEntry);
					boolean result = tempBuck.addEntry(newEntry);
					assertTrue("bucket add failed on key "+key, result);
					length *= 2;
				}			
				tempBuck.write();
			}
			countOversize(numEntries - 5);

			{
				NumericKeyHashBucket<VariableSizeHashEntry> tempBuck = makeBucket();
				tempBuck.read();
				checkEntries(tempBuck, numEntries, entries);

				int length = 2 << numEntries;
				for (int key = 1; key <= numEntries; key++ ) {
					byte dat[] = new byte[length];
					for (int i = 0; i < length; ++i) {
						dat[i] = (byte) (key ^ i);
					}
					VariableSizeHashEntry newEntry = new MockVariableSizeHashEntry(key, dat);
					newEntry.setOversize((length > 128) || (key %2) == 1);
					entries.put(key, newEntry);
					boolean result = tempBuck.addEntry(newEntry);
					assertTrue("bucket add failed on key "+key, result);
					length /= 2;
				}			
				tempBuck.write();
			}
			NumericKeyHashBucket<VariableSizeHashEntry> newBuck = makeBucket();
			newBuck.read();
			checkEntries(newBuck, numEntries, entries);
		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception:"+e.getMessage());
		}
	}

	@Before
	public void setUp() throws Exception {
		workingDirectory = Utilities.makeTempTestDirectory();
		if (null == testFile) {
			testFile = Utilities.makeTestFileObject(workingDirectory, "variableHashFile");
		}
		backingStore = HashUtils.MakeHashFile(testFile);
		mgr = Utilities.makeFileSpaceManager(workingDirectory, getName()+"_mgr");
		oversizeEntryManager = new MockOverflowManager(mgr);
		buck = makeBucket();
		oversizeThreshold = -1;
	}

	@Override
	protected void tearDown() throws Exception {
		if (null != backingStore) {
			backingStore.close();
		}
		Utilities.destroyFileSpaceManager(mgr);
		Utilities.deleteWorkingDirectory();
	}

	private MockVariableSizeEntryBucket makeBucket() {
		return new MockVariableSizeEntryBucket(backingStore, 0, oversizeEntryManager);
	}

	private int populateBucket(final int numEntries,
			HashMap<Integer, VariableSizeHashEntry> entries)
					throws DatabaseException, IOException {
		int entryCount = 0;
		int expectedOccupancy = 2;
		for (int key = 1; key <= numEntries; key++ ) {
			int length = 32;
			VariableSizeHashEntry newEntry = createEntry(key, length);
			entries.put(key, newEntry);
			boolean result = buck.addEntry(newEntry);
			expectedOccupancy += 4 + 2 + length;
			if (expectedOccupancy <= NumericKeyHashBucket.getBucketSize()) {
				++entryCount;
				assertTrue("bucket add failed on key "+key, result);
			} else {
				assertFalse("overflow not detected on key "+key, result);
				break;
			}
		}
		return entryCount;
	}

	private VariableSizeHashEntry createEntry(int key, int length) throws IOException {
		VariableSizeHashEntry newEntry = makeVariableSizeEntry(key, length);
		byte[] eData = newEntry.getData();
		byte cdata = (byte) Character.getNumericValue('a');
		int limit = Character.getNumericValue('z') + 1;
		for (int i = 0; i < length/2; ++i) {
			eData[2*i] = cdata;
			eData[2*i + 1] = (byte) key;
			cdata = (byte) ((cdata + 1) % limit);
		}
		return newEntry;
	}

	private void checkEntries(NumericKeyHashBucket<VariableSizeHashEntry> testBucket, int expectedCount, HashMap<Integer, VariableSizeHashEntry> entries) {
		int actualCount = 0;
		for (VariableSizeHashEntry e: testBucket) {
			assertNotNull("missing entry after "+actualCount+" entries", e);
			int key = e.getKey();
			VariableSizeHashEntry comparand = entries.get(Integer.valueOf(key));
			assertNotNull("key "+key+" missing", comparand);
			assertEquals(comparand, e);
			++actualCount ;
		}
		assertEquals("Wrong number of entries", expectedCount, actualCount);
	}

}

package org.lasalledebain.libris.hashfile;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.lasalledebain.libris.exception.DatabaseException;

public abstract class HashFile<EntryType extends HashEntry, 
BucketType extends HashBucket<EntryType>, FactoryType extends EntryFactory<EntryType>> {

	protected RandomAccessFile backingStore;
	int numBuckets;
	int bucketModulus = 0;
	private int bucketSize;
	static final int CACHESIZE=8;
	HashMap<Integer, BucketType> bucketCache;

	int bucketAge = 0;
	private HashBucketFactory<EntryType, BucketType, FactoryType> bucketFactory;
	FactoryType entryFact;
	private int expansionCount, bucketLoadCount, flushCount;

	public int getBucketLoadCount() {
		return bucketLoadCount;
	}

	public void resetBucketLoadCount() {
		bucketLoadCount = 0;
	}

	public int getExpansionCount() {
		return expansionCount;
	}

	public void resetExpansionCount() {
		this.expansionCount = 0;
	}

	public HashFile(RandomAccessFile backingStore, HashBucketFactory<EntryType, BucketType, FactoryType> bFact) throws IOException {
		this.backingStore = backingStore;
		this.bucketFactory = bFact;
		expansionCount = bucketLoadCount = flushCount = 0;
		long fileLength = backingStore.length();
		bucketSize = NumericKeyHashBucket.getBucketSize();
		setNumBuckets((int) fileLength/bucketSize + 1);
		bucketCache = new HashMap<Integer, BucketType>();
	}

	public int getFlushCount() {
		return flushCount;
	}

	/**
	 * @param backingStore
	 * @throws IOException accessing backing store
	 */
	public HashFile(RandomAccessFile backingStore, HashBucketFactory<EntryType, BucketType, FactoryType> bFact, EntryFactory<EntryType> eFact) throws IOException {
		this(backingStore, bFact);
	}

	/**
	 * @param fileLength
	 */
	protected void setNumBuckets(int num) {
		numBuckets = num;
		if (0 == bucketModulus) {
			bucketModulus = 1;
		}
		if (bucketModulus != (long) numBuckets) { 
			if (bucketModulus < numBuckets) { 
				while (2*bucketModulus < numBuckets) {
					bucketModulus *= 2;
				}
			} else while (bucketModulus > numBuckets) {
				bucketModulus /= 2;
			}
		}
	}

	public int getNumBuckets() {
		return numBuckets;
	}

	/**
	 * @param bucketNum
	 * @return
	 * @throws IOException 
	 * @throws DatabaseException 
	 */
	protected BucketType getBucket(int bucketNum) throws IOException, DatabaseException {
		BucketType buck = bucketCache.get(bucketNum);
		++bucketAge;
		if (bucketAge < 0) {
			clear();
			bucketAge = 0;
		}
		if (null == buck) {
			if (bucketCache.size() >= CACHESIZE) {
				flush(true);
			}
			buck = createBucket(bucketNum);
			buck.read();
			bucketLoadCount++;
			bucketCache.put(bucketNum, buck);
		}
		buck.setAge(bucketAge);
		return buck;
	}

	protected BucketType createBucket(int bucketNum) {
		return bucketFactory.createBucket(backingStore, bucketNum, entryFact);
	}
	
	protected abstract int findHomeBucket(long key);
	
	public static int hash(long key) {
		int hash = Math.abs((int) ((key ^ 0x5DEECE66DL) & ((1L << 48) - 1)));
		return hash;
	}

	protected int hashToBucketNumber(long key) {
		int homeBucket = Math.abs((int) key) % (2*bucketModulus);
		if (homeBucket >= numBuckets) {
			homeBucket -= bucketModulus;
		}
		return homeBucket;
	}

	public void clear() throws IOException, DatabaseException {
		for (HashBucket<EntryType> b: bucketCache.values()) {
			b.write();
		}
		bucketCache.clear();
	}

	private void flush(boolean removeOldest) throws IOException, DatabaseException {
		++flushCount;
		HashBucket<EntryType> oldestBucket = null;
		for (HashBucket<EntryType> b: bucketCache.values()) {
			if ((null == oldestBucket) || (b.getAge() < oldestBucket.getAge())) {
				oldestBucket = b;
			}
			b.write();
		}
		if (removeOldest) {
			bucketCache.remove(oldestBucket);
		}
	}

	public void flush() throws IOException, DatabaseException {
		flush(false);
	}

	public int getNumEntries() throws DatabaseException {
		int sumSizes = 0;
		try {
			long fileLength = backingStore.length();
			if (fileLength < 4) {
				return 0;
			}
			for (int i=0; i < numBuckets; ++i) {
				HashBucket<EntryType> buck = createBucket(i);
				sumSizes += buck.getNumEntries();
			}
		} catch (IOException e) {
			throw new DatabaseException(e);
		}
		return sumSizes;
	}
	
	public boolean resize(int requestedBuckets) throws DatabaseException {
		if (requestedBuckets < numBuckets) {
			return false;
		} else {
			int numEntries = getNumEntries();
			setNumBuckets(requestedBuckets);
			if (numEntries > 0) try {
				flush();
				for (int i = 0; i < numBuckets; ++i) {
					HashBucket<EntryType> buck = getBucket(i);
					if (buck.getNumEntries() > 0) {
						ArrayList<EntryType> entries = new ArrayList<EntryType>();
						for (EntryType e: buck) {
							entries.add(e);
						}
						buck.clear();
						for (EntryType e: entries) {
							addEntry(e);
						}
					}
				}
				clear();
			} catch (IOException e) {		
				throw new DatabaseException(e);
			}
		}
		return true;
	}

	protected void expandAndRehash(BucketType oldOverflowBucket) throws IOException, DatabaseException {
		ArrayList<EntryType> splitEntries = new ArrayList<EntryType>();
		ArrayList<EntryType> oldOverflowNativeEntries = new ArrayList<EntryType>();
		ArrayList<EntryType> newOverflowEntries = new ArrayList<EntryType>();
		int oldOverflowBucketNum = numBuckets-1;
		final int splitBucketNum = (2*bucketModulus == numBuckets)? 0: (numBuckets - bucketModulus);
		setNumBuckets(numBuckets+1);
		for (EntryType entry: oldOverflowBucket) {
			int homeBucket = findHomeBucket(entry);
			if (homeBucket == oldOverflowBucketNum) {
				oldOverflowNativeEntries.add(entry);
			} else if (homeBucket == splitBucketNum) {
				splitEntries.add(entry);
			} else {
				newOverflowEntries.add(entry);
			}
		}

		expansionCount++;
		oldOverflowBucket.clear();
		for (EntryType e: oldOverflowNativeEntries) {
			oldOverflowBucket.addEntry(e);
		}

		BucketType splitBucket = getBucket(splitBucketNum);
		for (EntryType entry: splitBucket) {
			int homeBucket = findHomeBucket(entry);
			if (homeBucket == splitBucketNum) {
				splitEntries.add(entry);
			} else {
				newOverflowEntries.add(entry);
			}
		}
		splitBucket.clear();
		for (EntryType e: splitEntries) {
			if (!splitBucket.addEntry(e)) {
				newOverflowEntries.addAll(splitEntries.subList(splitEntries.indexOf(e), splitEntries.size()));
				break;
			}
		}

		int lastIndex = -1;
		if (newOverflowEntries.size() > 0) {
			BucketType newOverflowBucket = getBucket(numBuckets-1);
			for (EntryType e: newOverflowEntries) {
				if (!newOverflowBucket.addEntry(e)) {
					lastIndex = newOverflowEntries.indexOf(e);
					break;
				}
			}

			if (lastIndex >= 0) { /* overflow bucket overflowed */
				List<EntryType> remainder = newOverflowEntries.subList(lastIndex, newOverflowEntries.size());
				expandAndRehash(newOverflowBucket);
				for (EntryType e: remainder) {
					addEntry(e);
				}
			}
		}
	}

	protected abstract int findHomeBucket(EntryType entry);

	public void addEntry(EntryType entry) throws IOException, DatabaseException {
		int bucketNum = findHomeBucket(entry);
		BucketType homeBucket = getBucket(bucketNum);
		if (!homeBucket.addEntry(entry)) {
			BucketType overflowBucket = getBucket(numBuckets-1);
			if (!overflowBucket.addEntry(entry)) {
				expandAndRehash(overflowBucket);
				homeBucket = overflowBucket = null;
				addEntry(entry);
			}
		}
	}
}

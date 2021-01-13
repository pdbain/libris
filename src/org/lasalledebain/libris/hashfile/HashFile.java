package org.lasalledebain.libris.hashfile;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import org.lasalledebain.libris.exception.DatabaseError;
import org.lasalledebain.libris.exception.DatabaseException;

public abstract class HashFile<EntryType extends HashEntry, BucketType extends HashBucket<EntryType>> {

	protected RandomAccessFile backingStore;
	int numBuckets;
	int bucketAge;
	int bucketModulus;
	private int bucketSize;
	static final int CACHESIZE=1000;
	LinkedHashMap<Integer, BucketType> bucketCache;

	private int expansionCount, bucketLoadCount, flushCount;

	public HashFile(RandomAccessFile backingStore) throws IOException {
		bucketAge = 0;
		bucketModulus = 0;
		this.backingStore = backingStore;
		expansionCount = bucketLoadCount = flushCount = 0;
		long fileLength = backingStore.length();
		bucketSize = NumericKeyHashBucket.getBucketSize();
		setNumBuckets((int) fileLength/bucketSize + 1);
		bucketCache = new BucketCacheMap(100, 0.75f);
	}

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

	public int getFlushCount() {
		return flushCount;
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
			bucketLoadCount++;
			buck = createBucket(bucketNum);
			buck.read();
			bucketCache.put(bucketNum, buck);
		}
		buck.setAge(bucketAge);
		return buck;
	}

	protected abstract  BucketType createBucket(int bucketNum);

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

	public void flush() throws IOException, DatabaseException {
		++flushCount;
		for (Entry<Integer, BucketType> e: bucketCache.entrySet()) {
			if (e.getValue().dirty)
				e.getValue().write();
		}
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
	
	@SuppressWarnings("serial")
	class BucketCacheMap extends LinkedHashMap<Integer, BucketType> {

		public BucketCacheMap(int initialCapacity, float loadFactor) {
			super(initialCapacity, loadFactor, true);
		}

		@Override
		protected boolean removeEldestEntry(Entry<Integer, BucketType> eldest) {
			boolean result = false;
			if (size() > CACHESIZE) {
				final BucketType eldestValue = eldest.getValue();
				if (eldestValue.dirty) {
					try {
						eldestValue.write();
					} catch (DatabaseException e) {
						throw new DatabaseError("Error writing hash bucket", e);
					}
				}
				result = true;
			}
			return result;
		}

	}
}

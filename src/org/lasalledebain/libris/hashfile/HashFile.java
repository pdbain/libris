package org.lasalledebain.libris.hashfile;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;

import org.lasalledebain.libris.exception.DatabaseException;

public abstract class HashFile<EntryType extends HashEntry, BucketType extends HashBucket<EntryType>> {

	protected RandomAccessFile backingStore;
	int numBuckets;
	int bucketModulus = 0;
	private int bucketSize;
	static final int CACHESIZE=8;
	HashMap<Integer, BucketType> bucketCache;

	int bucketAge = 0;
	private HashBucketFactory<EntryType, BucketType> bucketFactory;
	EntryFactory<EntryType> entryFact;

	public HashFile(RandomAccessFile backingStore, HashBucketFactory<EntryType, BucketType> bFact) throws IOException {
		this.backingStore = backingStore;
		this.bucketFactory = bFact;
		long fileLength = backingStore.length();
		bucketSize = NumericKeyHashBucket.getBucketSize();
		setNumBuckets((int) fileLength/bucketSize + 1);
		bucketCache = new HashMap<Integer, BucketType>();
	}

	/**
	 * @param backingStore
	 * @throws IOException accessing backing store
	 */
	public HashFile(RandomAccessFile backingStore, HashBucketFactory<EntryType, BucketType> bFact, EntryFactory<EntryType> eFact) throws IOException {
		this(backingStore, bFact);
	}

	public abstract void addEntry(EntryType entry) throws IOException, DatabaseException;

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

	protected abstract void expandAndRehash(HashBucket<EntryType> oldOverflowBucket) throws IOException, DatabaseException;
	
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
			buck = bucketFactory.createBucket(backingStore, bucketNum, entryFact);
			buck.read();
			bucketCache.put(bucketNum, buck);
		}
		buck.setAge(bucketAge);
		return buck;
	}
	
	protected abstract int findHomeBucket(long key);
	
	public static int hash(long key) {
		int hash = Math.abs((int) ((key ^ 0x5DEECE66DL) & ((1L << 48) - 1)));
		return hash;
	}

	public void clear() throws IOException, DatabaseException {
		for (HashBucket<EntryType> b: bucketCache.values()) {
			b.write();
		}
		bucketCache.clear();
	}

	private void flush(boolean removeOldest) throws IOException, DatabaseException {
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
				HashBucket<EntryType> buck = bucketFactory.createBucket(backingStore, i, entryFact);
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

	public abstract EntryType getEntry(int recordId) throws IOException, DatabaseException;
}

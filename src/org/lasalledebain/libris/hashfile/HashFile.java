package org.lasalledebain.libris.hashfile;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.lasalledebain.libris.exception.DatabaseException;

public class HashFile<T extends HashEntry> {

	protected RandomAccessFile backingStore;
	int numBuckets;
	int bucketModulus = 0;
	EntryFactory<T> entryFact;
	private int bucketSize;
	static final int CACHESIZE=8;
	HashMap<Integer, HashBucket<T>> bucketCache;
	int bucketAge = 0;
	private HashBucketFactory<T> bucketFactory;

	/**
	 * @param backingStore
	 * @throws IOException accessing backing store
	 */
	public HashFile(RandomAccessFile backingStore, HashBucketFactory<T> bFact, EntryFactory<T> eFact) throws IOException {
		this.backingStore = backingStore;
		this.entryFact = eFact;
		this.bucketFactory = bFact;
		long fileLength = backingStore.length();
		bucketSize = HashBucket.getBucketSize();
		setNumBuckets((int) fileLength/bucketSize + 1);
		bucketCache = new HashMap<Integer, HashBucket<T>>();
	}

	public void addEntry(T entry) throws IOException, DatabaseException {
		int key = entry.getKey();
		int bucketNum = findHomeBucket(key);
		HashBucket<T> homeBucket = getBucket(bucketNum);
		if (!homeBucket.addElement(entry)) {
			HashBucket<T> overflowBucket = getBucket(numBuckets-1);
			if (!overflowBucket.addElement(entry)) {
				expandAndRehash(overflowBucket);
				homeBucket = overflowBucket = null;
				addEntry(entry);
			}
		}
	}

	public T getEntry(int key) throws IOException, DatabaseException {
		int bucketNum = findHomeBucket(key);
		HashBucket<T> homeBucket = getBucket(bucketNum);
		T foundEntry;
		if (null != (foundEntry = homeBucket.findEntry(key))) {
			return foundEntry;
		} else {
			HashBucket<T> overflowBucket = getBucket(numBuckets-1);
			if (null != (foundEntry = overflowBucket.findEntry(key))) {
				return foundEntry;
			}
		}
		return null;
	}

	/**
	 * @param fileLength
	 */
	private void setNumBuckets(int num) {
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

	private void expandAndRehash(HashBucket<T> oldOverflowBucket) throws IOException, DatabaseException {
		ArrayList<T> splitEntries = new ArrayList<T>();
		ArrayList<T> oldOverflowNativeEntries = new ArrayList<T>();
		ArrayList<T> newOverflowEntries = new ArrayList<T>();
		int oldOverflowBucketNum = numBuckets-1;
		int splitBucketNum = (2*bucketModulus == numBuckets)? 0: (numBuckets - bucketModulus);
		setNumBuckets(numBuckets+1);
		
		for (T entry: oldOverflowBucket) {
			int homeBucket = findHomeBucket(entry.getKey());
			if (homeBucket == oldOverflowBucketNum) {
				oldOverflowNativeEntries.add(entry);
			} else if (homeBucket == splitBucketNum) {
				splitEntries.add(entry);
			} else {
				newOverflowEntries.add(entry);
			}
		}
		oldOverflowBucket.clear();
		for (T e: oldOverflowNativeEntries) {
			oldOverflowBucket.addElement(e);
		}
		
		HashBucket <T> splitBucket = getBucket(splitBucketNum);
		for (T entry: splitBucket) {
			int homeBucket = findHomeBucket(entry.getKey());
			if (homeBucket == splitBucketNum) {
				splitEntries.add(entry);
			} else {
				newOverflowEntries.add(entry);
			}
		}
		splitBucket.clear();
		for (T e: splitEntries) {
			if (!splitBucket.addElement(e)) {
				newOverflowEntries.addAll(splitEntries.subList(splitEntries.indexOf(e), splitEntries.size()));
				break;
			}
		}
		
		int lastIndex = -1;
		if (newOverflowEntries.size() > 0) {
			HashBucket<T> newOverflowBucket = getBucket(numBuckets-1);
			for (T e: newOverflowEntries) {
				if (!newOverflowBucket.addElement(e)) {
					lastIndex = newOverflowEntries.indexOf(e);
					break;
				}
			}
			
			if (lastIndex >= 0) { /* overflow bucket overflowed */
				List<T> remainder = newOverflowEntries.subList(lastIndex, newOverflowEntries.size());
				expandAndRehash(newOverflowBucket);
				for (T e: remainder) {
					addEntry(e);
				}
			}
		}
	}

	/**
	 * @param bucketNum
	 * @return
	 * @throws IOException 
	 * @throws DatabaseException 
	 */
	private HashBucket<T> getBucket(int bucketNum) throws IOException, DatabaseException {
		HashBucket<T> buck = bucketCache.get(bucketNum);
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
	
	private int findHomeBucket(long key) {
		int hashedKey = hash(key);
		int homeBucket = (int) hashedKey % (2*bucketModulus);
		if (homeBucket >= numBuckets) {
			homeBucket -= bucketModulus;
		}
		return homeBucket;
	}

	public static int hash(long key) {
		int hash = Math.abs((int) ((key ^ 0x5DEECE66DL) & ((1L << 48) - 1)));
		return hash;
	}

	public void clear() throws IOException, DatabaseException {
		for (HashBucket<T> b: bucketCache.values()) {
			b.write();
		}
		bucketCache.clear();
	}

	private void flush(boolean removeOldest) throws IOException, DatabaseException {
		HashBucket<T> oldestBucket = null;
		for (HashBucket<T> b: bucketCache.values()) {
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
				HashBucket<T> buck = bucketFactory.createBucket(backingStore, i, entryFact);
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
			setNumBuckets(requestedBuckets);
			try {
				flush();
				for (int i = 0; i < numBuckets; ++i) {
					HashBucket<T> buck = getBucket(i);
					ArrayList<T> entries = new ArrayList<T>();
					for (T e: buck) {
						entries.add(e);
					}
					buck.clear();
					for (T e: entries) {
						addEntry(e);
					}
				}
				clear();
			} catch (IOException e) {		
				throw new DatabaseException(e);
			}
		}
		return true;
	}
}

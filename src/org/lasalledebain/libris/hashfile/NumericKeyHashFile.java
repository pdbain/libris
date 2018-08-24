package org.lasalledebain.libris.hashfile;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.lasalledebain.libris.exception.DatabaseException;

public class NumericKeyHashFile<EntryType extends NumericKeyHashEntry, BucketType extends NumericKeyHashBucket<EntryType>> extends HashFile<EntryType, BucketType> {

	HashMap<Integer, NumericKeyHashBucket<EntryType>> bucketCache;
	public NumericKeyHashFile(RandomAccessFile backingStore, HashBucketFactory<EntryType, BucketType> bFact, NumericKeyEntryFactory<EntryType> eFact)
			throws IOException {
		super(backingStore, bFact);
		this.entryFact = eFact;
		bucketCache = new HashMap<Integer, NumericKeyHashBucket<EntryType>>();
	}
	
	public EntryType getEntry(int key) throws IOException, DatabaseException {
		int bucketNum = findHomeBucket(key);
		BucketType homeBucket = getBucket(bucketNum);
		EntryType foundEntry = homeBucket.getEntry(key);
		if (null != foundEntry) {
			return foundEntry;
		} else {
			NumericKeyHashBucket<EntryType> overflowBucket = getBucket(numBuckets-1);
			foundEntry = overflowBucket.getEntry(key);
			if (null != foundEntry) {
				return foundEntry;
			}
		}
		return null;
	}

	protected void expandAndRehash(HashBucket<EntryType> oldOverflowBucket) throws IOException, DatabaseException {
		ArrayList<EntryType> splitEntries = new ArrayList<EntryType>();
		ArrayList<EntryType> oldOverflowNativeEntries = new ArrayList<EntryType>();
		ArrayList<EntryType> newOverflowEntries = new ArrayList<EntryType>();
		int oldOverflowBucketNum = numBuckets-1;
		int splitBucketNum = (2*bucketModulus == numBuckets)? 0: (numBuckets - bucketModulus);
		setNumBuckets(numBuckets+1);

		for (EntryType entry: oldOverflowBucket) {
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
		for (EntryType e: oldOverflowNativeEntries) {
			oldOverflowBucket.addEntry(e);
		}

		NumericKeyHashBucket <EntryType> splitBucket = getBucket(splitBucketNum);
		for (EntryType entry: splitBucket) {
			int homeBucket = findHomeBucket(entry.getKey());
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
			NumericKeyHashBucket<EntryType> newOverflowBucket = getBucket(numBuckets-1);
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

	protected int findHomeBucket(long key) {
		int hashedKey = hash(key);
		int homeBucket = (int) hashedKey % (2*bucketModulus);
		if (homeBucket >= numBuckets) {
			homeBucket -= bucketModulus;
		}
		return homeBucket;
	}

	public void addEntry(EntryType entry) throws IOException, DatabaseException {
	int key = entry.getKey();
	int bucketNum = findHomeBucket(key);
	NumericKeyHashBucket<EntryType> homeBucket = getBucket(bucketNum);
	if (!homeBucket.addEntry(entry)) {
		NumericKeyHashBucket<EntryType> overflowBucket = getBucket(numBuckets-1);
		if (!overflowBucket.addEntry(entry)) {
			expandAndRehash(overflowBucket);
			homeBucket = overflowBucket = null;
			addEntry(entry);
		}
	}
}


}

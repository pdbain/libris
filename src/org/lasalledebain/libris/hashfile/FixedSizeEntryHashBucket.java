package org.lasalledebain.libris.hashfile;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Iterator;
import java.util.TreeMap;

import org.lasalledebain.libris.exception.DatabaseException;

@SuppressWarnings("unchecked")
public class FixedSizeEntryHashBucket <T extends HashEntry<?>> extends HashBucket {

	protected TreeMap<Integer, T> entries;
	private FixedSizeEntryHashBucket(RandomAccessFile backingStore,
			int bucketNum, EntryFactory fact) {
		super(backingStore, bucketNum, fact);
		entries = new TreeMap<Integer, T>();

		occupancy = 4;
	}

	public static HashBucketFactory getFactory() {
		return new FixedSizeEntryHashBucketFactory();
	}
	private static class FixedSizeEntryHashBucketFactory implements HashBucketFactory {

		@Override
		public HashBucket<HashEntry> createBucket(RandomAccessFile backingStore,
				int bucketNum, EntryFactory fact) {
			return new FixedSizeEntryHashBucket(backingStore, bucketNum, fact);
		}

	}
	
	public T findEntry(int key) {
		return entries.get(key);
	}

	@Override
	protected void addToBucket(int key, HashEntry newEntry) {
		entries.put(key, (T) newEntry);		
	}
	
	@Override
	public Iterator<T> iterator() {
		return entries.values().iterator();
	}


	public void clear() {
		if (null != entries) {
			entries.clear();
		}
		super.clear();
	}

	public void write() throws DatabaseException {
		if (!dirty) {
			return;
		}
		try {
			backingStore.seek(filePosition);
			if (null != entries) {
				int numEntries = entries.size();
				backingStore.writeInt(numEntries);
				for (T e: entries.values()) {
					e.writeData(backingStore);
				}
			} else {
				backingStore.writeInt(0);		
			}
		} catch (IOException e) {
			throw new DatabaseException("Error writing hash entry", e);
		}

		dirty = false;
	}


}

package org.lasalledebain.libris.hashfile;

import java.io.DataInput;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Iterator;
import java.util.TreeMap;

import org.lasalledebain.libris.exception.DatabaseException;

@SuppressWarnings({ })
public class FixedSizeEntryHashBucket <EntryType extends FixedSizeHashEntry> extends NumericKeyHashBucket<EntryType> {

	protected TreeMap<Integer, EntryType> entries;
	protected EntryFactory<EntryType> entryFact;
	public FixedSizeEntryHashBucket(RandomAccessFile backingStore, int bucketNum) {
		super(backingStore, bucketNum);
		entries = new TreeMap<Integer, EntryType>();
		occupancy = 4;
	}

	
	public EntryType getEntry(int key) {
		EntryType result = entries.get(key);
		return result;
	}

	@Override
	protected void addToBucket(int key, EntryType newEntry) {
		entries.put(key, (EntryType) newEntry);		
	}
	
	@Override
	protected EntryType removeFromBucket(int key) {
		return entries.remove(key);
	}

	@Override
	public Iterator<EntryType> iterator() {
		return entries.values().iterator();
	}


	public void clear() {
		if (null != entries) {
			entries.clear();
		}
		occupancy = 4;
		dirty = true;		
	}

	public void read() throws IOException, DatabaseException {
		clear();
		long fileSize = backingStore.length();
		if (filePosition >= fileSize) {
			return;
		}
		backingStore.seek(filePosition);
		int numEntries = backingStore.readInt();
		for (int i = 0; i < numEntries; ++i) {
			EntryType newEntry = makeEntry(backingStore);
			addEntry(newEntry);
		}
		dirty = false;
	}

	protected EntryType makeEntry(DataInput backingStore) throws IOException {
		return entryFact.makeEntry(backingStore);
	}
	
	@Override
	protected int getNumEntriesImpl() {
		return entries.size();
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
				for (EntryType e: entries.values()) {
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

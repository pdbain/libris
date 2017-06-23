package org.lasalledebain.libris.hashfile;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeMap;

import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.indexes.BucketOverflowFileManager;

@SuppressWarnings("unchecked")
public class VariableSizeEntryHashBucket <T extends VariableSizeHashEntry> extends HashBucket {

	static final int MAX_VARIABLE_HASH_ENTRY=256;
	/**
	 * Bucket format:
	 * - number of entries: 2 bytes
	 * - sorted list of int keys (4 bytes each), size specified by number of entries
	 * - offset to the last byte of each entry (2 bytes each), same number and order of keys, from the start of the first entry
	 * If entry is longer than MAX_VARIABLE_HASH_ENTRY, offset is negated.  The entry is a 8-byte offset in the overflow file
	 */
	private BucketOverflowFileManager overflowManager;
	public VariableSizeEntryHashBucket(RandomAccessFile backingStore,
			int bucketNum, BucketOverflowFileManager overflowManager,
			EntryFactory<T> eFact) {
		super(backingStore, bucketNum);
		occupancy = 2;
		this.overflowManager = overflowManager;
		entries = new TreeMap<Integer, EntryInfo>();
		lastEntry = null;
		entryFact = eFact;
	}
	protected TreeMap<Integer, EntryInfo> entries;
	EntryInfo lastEntry;
	private ByteBuffer bucketEntryData;
	private ArrayList<EntryInfo> victimList;
	protected EntryFactory<T> entryFact;

	@Override
	protected void addToBucket(int key, HashEntry newEntry) {
		EntryInfo oldEi = entries.remove(new Integer(key));
		if ((null != oldEi) && oldEi.getEntry().isOversize()) {
			addOversizeVictim(oldEi);
		}
		EntryInfo newEi = new EntryInfo(key, (T) newEntry);
		entries.put(key, newEi);	
	}

	@Override
	public void write() throws DatabaseException {
		if (!dirty) {
			return;
		}
		int numEntries = entries.size();

		ByteArrayOutputStream ids = new ByteArrayOutputStream(numEntries*4);
		ByteArrayOutputStream offsets = new ByteArrayOutputStream(numEntries*2);
		ByteArrayOutputStream values = new ByteArrayOutputStream(BUCKET_SIZE);
		DataOutputStream idStream = new DataOutputStream(ids);
		DataOutputStream offsetStream = new DataOutputStream(offsets);
		DataOutputStream valueStream = new DataOutputStream(values);
		int offset = 0;
		if (null != victimList) {
			for (EntryInfo ei: victimList) {
				long pos = ei.getOversizeFilePosition();
				if (pos >= 0) {
					overflowManager.remove(pos);
				}
				ei.setOversizeFilePosition(-1);
			}
		}
		try {
			for (EntryInfo ei: entries.values()) {
				T entry = (T) ei.getEntry();
				offset += entry.getEntryLength();
				idStream.writeInt(entry.getKey());
				if (entry.isOversize()) {
					offsetStream.writeShort(-offset);
					long oldPosition = ei.getOversizeFilePosition();
					long position = -1;
					if (-1 != oldPosition) {
						 position = overflowManager.update(oldPosition, entry.getData());
					} else {
						position = overflowManager.put(entry.getData());
					}
					valueStream.writeLong(position);
					ei.setOversizeFilePosition(position);
				} else {
					long oldPosition = ei.getOversizeFilePosition();
					if (-1 != oldPosition) {
						 overflowManager.remove(oldPosition);
					}
					offsetStream.writeShort(offset);
					valueStream.write(entry.getData(), 0, entry.getDataLength());
					ei.setOversizeFilePosition(-1);
				}
			}
			backingStore.seek(filePosition);
			backingStore.writeShort(numEntries);
			backingStore.write(ids.toByteArray(), 0, ids.size());
			backingStore.write(offsets.toByteArray(), 0, offsets.size());
			backingStore.write(values.toByteArray(), 0, values.size());
		} catch (IOException e) {
			throw new DatabaseException("Error writing hashfile ", e);
		}
		overflowManager.flush();
	}
	
	public void read() throws IOException, DatabaseException {
		clear();
		byte[] bucketBuffer = new byte[BUCKET_SIZE];
		long fileSize = backingStore.length();
		if (filePosition >= fileSize) {
			return;
		}
		backingStore.seek(filePosition);
		int nRead = 0;
		int startPos = 0;
		int spaceRemaining = bucketBuffer.length;
		int totalRead = 0;
		do {
			nRead = backingStore.read(bucketBuffer, startPos, spaceRemaining);
			if (nRead > 0) {
				startPos += nRead;
				spaceRemaining -= nRead;
				totalRead += nRead;
			}
		} while (nRead > 0);

		ByteBuffer sizeBuff =  ByteBuffer.wrap(bucketBuffer);
		short numEntries = sizeBuff.getShort();
		int  idList[] =  new int[numEntries];
		
		for (int i = 0; i < numEntries; ++i) {
			idList[i] = sizeBuff.getInt();
		}
		short lastOffset = 0;
		int dataStart = 2 + (4 * numEntries) + (2 * numEntries);
		bucketEntryData = ByteBuffer.wrap(bucketBuffer, dataStart, totalRead - dataStart);
		for (int i = 0; i < numEntries; ++i) {
			short end = sizeBuff.getShort();
			boolean overflow = false;
			if (end < 0) {
				overflow = true;
				end*= -1;
			}
			EntryInfo ei = new EntryInfo(idList[i], end - lastOffset, overflow);
			entries.put(idList[i], ei);
			lastOffset = end;
		}
		dirty = false;
	}
	
	protected void addOversizeVictim(EntryInfo victim) {
		if (null == victimList) {
			victimList = new ArrayList<EntryInfo>(1);
		}
		victimList.add(victim);
	}

	public static HashBucketFactory getFactory(BucketOverflowFileManager overflowManager) {
		return new VariableSizeBucketHashBucketFactory(overflowManager);
	}
	
	private static class VariableSizeBucketHashBucketFactory implements HashBucketFactory {
		BucketOverflowFileManager overflowManager;
		public VariableSizeBucketHashBucketFactory(
				BucketOverflowFileManager overflowManager) {
			super();
			this.overflowManager = overflowManager;
		}
		@Override
		public HashBucket<HashEntry> createBucket(RandomAccessFile backingStore,
				int bucketNum, EntryFactory fact) {
			return new VariableSizeEntryHashBucket(backingStore, bucketNum, overflowManager, fact);
		}		
	}

	@Override
	public Iterator<T> iterator() {
		return new EntryIterator();
	}

	@Override
	public
	T getEntry(int key) {

		EntryInfo ei = entries.get(key);
		if (null != ei) {
			return ei.getEntry();
		} else {
			return null;
		}
	}

	public void clear() {
		occupancy = 4;
		if (null != entries) {
			entries.clear();
		}
		dirty = true;
	}

	@Override
	protected int getNumEntriesImpl() {
		return entries.size();
	}

	protected class EntryInfo {
		int id;
		T entry;
		private long oversizeFilePosition;
		public long getOversizeFilePosition() {
			return oversizeFilePosition;
		}

		public void setOversizeFilePosition(long oversizeFilePosition) {
			this.oversizeFilePosition = oversizeFilePosition;
		}

		public EntryInfo(int key, T newEntry) {
			id = key;
			entry = newEntry;
			oversizeFilePosition = -1;
		}

		EntryInfo(int entryId, int length, boolean oversize) throws DatabaseException {
			id = entryId;
			entry = entryFact.makeEntry();
			entry.setKey(id);
			if (oversize) {
				long position = bucketEntryData.getLong();
				oversizeFilePosition = position;
				byte[] dat = overflowManager.get(position);
				entry.setData(dat);
			} else {
				oversizeFilePosition = -1;
				entry.readData(bucketEntryData, length);
			}
			entry.setOversize(oversize);
		}

		public int getId() {
			return id;
		}
		
		public void setId(int id) {
			this.id = id;
		}

		public T getEntry() {
			return entry;
		}
		public void setEntry(T he) {
			entry = he;
		}
	}
	
	protected class EntryIterator implements Iterator<T>  {
		
		private Iterator<Integer> ki;
		private Integer currentKey;
		private EntryInfo currentEntryInfo;

		public EntryIterator() {
			ki = entries.keySet().iterator();
		}
		@Override
		public boolean hasNext() {
			return ki.hasNext();
		}

		@Override
		public T next() {
			currentKey = ki.next();
			if (null != currentKey) {
				currentEntryInfo = entries.get(currentKey);
				T currentEntry = (T) currentEntryInfo.getEntry();
				return currentEntry;
			}
			return null;
		}

		@Override
		public void remove() {
			if (null != currentKey) {
				EntryInfo victim = entries.get(currentKey);
				if (victim.getOversizeFilePosition() >= 0) {
					addOversizeVictim(victim);
				}
			}
			ki.remove();
			dirty = true;
		}
		
	}
}

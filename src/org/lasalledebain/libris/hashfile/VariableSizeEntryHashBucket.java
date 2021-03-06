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

public abstract class VariableSizeEntryHashBucket <EntryType extends VariableSizeHashEntry> extends NumericKeyHashBucket<EntryType> {

	static final int MAX_VARIABLE_HASH_ENTRY=256;
	/**
	 * Bucket format:
	 * - number of entries: 2 bytes
	 * - sorted list of int keys (4 bytes each), size specified by number of entries
	 * - offset to the last byte of each entry (2 bytes each), same number and order of keys, from the start of the first entry
	 * If entry is longer than MAX_VARIABLE_HASH_ENTRY, offset is negated.  The entry is a 8-byte offset in the overflow file
	 */
	protected TreeMap<Integer, EntryInfo> entries;
	EntryInfo lastEntry;
	private ByteBuffer bucketEntryData;
	private ArrayList<EntryInfo> victimList;
	private BucketOverflowFileManager overflowManager;

	public VariableSizeEntryHashBucket(RandomAccessFile backingStore,
			int bucketNum, BucketOverflowFileManager overflowManager) {
		super(backingStore, bucketNum);
		occupancy = 2;
		this.overflowManager = overflowManager;
		entries = new TreeMap<Integer, EntryInfo>();
		lastEntry = null;
	}

	protected abstract EntryType makeEntry(int entryId, byte[] dat);

	protected abstract EntryType makeEntry(int entryId, ByteBuffer dat, int length);

	@Override
	protected void addToBucket(int key, EntryType newEntry) {
		EntryInfo oldEi = entries.remove(key);
		if ((null != oldEi) && oldEi.getEntry().isOversize()) {
			addOversizeVictim(oldEi);
		}
		EntryInfo newEi = new EntryInfo(key, newEntry);
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
				EntryType entry = (EntryType) ei.getEntry();
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

	@Override
	public Iterator<EntryType> iterator() {
		return new EntryIterator();
	}

	@Override
	public
	EntryType getEntry(int key) {

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

	@Override
	protected EntryType removeFromBucket(int key) {
		return entries.remove(key).getEntry();
	}
	
	protected class EntryInfo {
		int id;
		protected final EntryType entry;
		private long oversizeFilePosition;
		public long getOversizeFilePosition() {
			return oversizeFilePosition;
		}

		public void setOversizeFilePosition(long oversizeFilePosition) {
			this.oversizeFilePosition = oversizeFilePosition;
		}

		public EntryInfo(int key, EntryType newEntry) {
			id = key;
			entry = newEntry;
			oversizeFilePosition = -1;
		}

		EntryInfo(int entryId, int length, boolean oversize) throws DatabaseException {
			id = entryId;
			if (oversize) {
				long position = bucketEntryData.getLong();
				oversizeFilePosition = position;
				byte[] dat = overflowManager.get(position);
				entry = makeEntry(entryId, dat);
			} else {
				oversizeFilePosition = -1;
				entry = makeEntry(entryId, bucketEntryData, length);
			}
			entry.setOversize(oversize);
		}

		public int getId() {
			return id;
		}

		public void setId(int id) {
			this.id = id;
		}

		public EntryType getEntry() {
			return entry;
		}
	}

	protected class EntryIterator implements Iterator<EntryType>  {

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
		public EntryType next() {
			currentKey = ki.next();
			if (null != currentKey) {
				currentEntryInfo = entries.get(currentKey);
				EntryType currentEntry = (EntryType) currentEntryInfo.getEntry();
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

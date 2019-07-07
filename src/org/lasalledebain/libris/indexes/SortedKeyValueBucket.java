package org.lasalledebain.libris.indexes;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.lasalledebain.libris.FileAccessManager;
import org.lasalledebain.libris.LibrisConstants;
import org.lasalledebain.libris.exception.InputException;

public abstract class SortedKeyValueBucket<T extends KeyValueTuple> implements Iterable<T>, KeyValueIndex<T> {
	TreeMap<String, T> tuples;
	static final int BUCKET_CAPACITY = 4096;
	protected int occupancy;
	private boolean dirty;
	private long filePosition;
	private FileAccessManager dataFileManager;

	public SortedKeyValueBucket() {
		occupancy = LibrisConstants.SHORT_LEN; 
		tuples = new TreeMap<String, T>();
	}

	/**
	 * Represents one bucket's worth of tuples.
	 * File format:
	 * 2 bytes number of tuples (max 32767)
	 * tuples:
	 * 	- key
	 * 	- data
	 * Data are stored in sorted order.
	 * @param source backing store for the bucket
	 * @throws IOException
	 */
	public SortedKeyValueBucket(DataInput source) throws IOException {
		this();
		short numTuples = source.readShort();
		for (int i = 0; i < numTuples; ++i) {
			T result = readTuple(source);
			tuples.put(result.key, result);
			occupancy += result.entrySize();
		}
		dirty = false;
	}
	
	KeyLongTuple getDescriptor() throws InputException {
		return new KeyLongTuple(getFirstKey(), filePosition);
	}
	
	public void write(DataOutput op) throws IOException {
		op.writeShort(tuples.size());
		for (KeyValueTuple t: tuples.values()) {
			t.write(op);
		}
		dirty = false;
	}

	/* (non-Javadoc)
	 * @see org.lasalledebain.libris.indexes.KeyValueIndex#addElement(T)
	 */
	public boolean addElement(T newTuple) throws InputException {
		int newSize = occupancy + newTuple.entrySize();
		if (newSize > BUCKET_CAPACITY) {
			return false;
		}
		occupancy = newSize;
		String key = newTuple.key;
		if (null != tuples.get(key)) {
			throw new InputException("Duplicate key "+newTuple.key);
		} else {
			tuples.put(key, newTuple);
			dirty = true;
		}
		return true;
	}

	void forceAddElement(T newTuple) {
		tuples.put(newTuple.key, newTuple);
	}
	/* (non-Javadoc)
	 * @see org.lasalledebain.libris.indexes.KeyValueIndex#removeElement(T)
	 */
	public void removeElement(String key) throws InputException {
		T victim = tuples.get(key);
		if (null ==victim) {
			throw new InputException("key not found"+victim.key);
		}
		occupancy = (occupancy - victim.entrySize());
		tuples.remove(key);
	}

	protected abstract  T readTuple(DataInput source) throws IOException;

	public T getFirstTuple() {
		return (tuples.size() == 0) ? null: tuples.firstEntry().getValue();
	}
	
	public String getFirstKey() {
		return (tuples.size() == 0) ? null: tuples.firstKey();
	}

	/* (non-Javadoc)
	 * @see org.lasalledebain.libris.indexes.KeyValueIndex#getByName(java.lang.String)
	 */
	public T getByName(String key) throws InputException {
		return tuples.get(key);
	}

	@Override
	public Iterator<T> iterator() {
		return tuples.values().iterator();
	}

	public PrefixIterator<T> iterator(String prefix) throws InputException {
		Iterator<T> valueIterator = tuples.tailMap(prefix, true).values().iterator();
		PrefixIterator<T> prefixIterator = new PrefixIterator<T>(valueIterator, prefix);
		return prefixIterator;
	}
	
	/* (non-Javadoc)
	 * @see org.lasalledebain.libris.indexes.KeyValueIndex#findPredecessor(java.lang.String)
	 */
	public T getPredecessor(String prefix, boolean inclusive) throws InputException {
		Entry<String, T> result = inclusive? tuples.floorEntry(prefix): tuples.lowerEntry(prefix);
		return (null == result)? null: result.getValue();
	}

	/* (non-Javadoc)
	 * @see org.lasalledebain.libris.indexes.KeyValueIndex#findSuccessor(java.lang.String)
	 */
	public T getSuccessor(String prefix) throws InputException {
		Entry<String, T> result = tuples.higherEntry(prefix);
		return (null == result)? null: result.getValue();
	}
	
	class PrefixIterator<TupleType extends KeyValueTuple> implements Iterator<T> {
		String prefix;
		Iterator<T> masterIterator;
		T nextElement;
		public PrefixIterator(Iterator<T> masterIterator, String prefix) {
			this.prefix = prefix;
			this.masterIterator = masterIterator;
			getNext();
		}

		private void getNext() {
			nextElement = null;
			while ((null == nextElement) && masterIterator.hasNext()) {
				nextElement = masterIterator.next();
				if (!nextElement.getKey().startsWith(prefix)) {
					nextElement = null;
				}
			}
		}

		@Override
		public boolean hasNext() {
			return (null != nextElement);
		}

		@Override
		public T next() {
			T temp = nextElement;
			getNext();
			return temp;
		}

		@Override
		public void remove() {
			return;
		}

	}

	public int size() {
		return tuples.size();
	}

	public void setDataFileManager(FileAccessManager mgr) {
		dataFileManager = mgr;
	}

	public long getFilePosition() {
		return filePosition;
	}

	public void setFilePosition(long filePosition) {
		this.filePosition = filePosition;
	}

	public boolean isDirty() {
		return dirty;
	}

	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}
	
	public  void flush() throws InputException {
		if (dirty && (null != dataFileManager)) {
			FileOutputStream opStream;
			try {
				dataFileManager.createIfNonexistent();
				opStream = dataFileManager.getOpStream();
				try {
					write(new DataOutputStream(opStream));
				} finally {
					dataFileManager.releaseOpStream();
				}
			} catch (IOException e) {
				throw new InputException(e);
			}
		}
	}
	
	public void close() {
		if (null != dataFileManager) {
			dataFileManager.close();
		}
	}
}

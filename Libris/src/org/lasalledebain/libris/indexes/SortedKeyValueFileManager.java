package org.lasalledebain.libris.indexes;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.logging.Level;

import org.lasalledebain.libris.FileAccessManager;
import org.lasalledebain.libris.LibrisDatabase;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.InternalError;


/**
 * File format:
 * Series of fixed-size buckets.
 * Each bucket contains a text key (null terminated) followed by data (size determined by concrete class)
 *
 */
public class SortedKeyValueFileManager <T extends KeyValueTuple> implements Iterable<T>, KeyValueIndex<T> {
	private RandomAccessFile dataFile;
	private KeyValueIndex<KeyLongTuple> bucketIndex;
	private SortedKeyValueBucketFactory<T> bucketFactory;
	private static int BUCKET_CACHE_SIZE = Integer.getInteger("org.lasalledebain.libris.indexes.bucketcachesize", 32);
	private BucketCache cache;
	int indexLevel;
	private String managerName;

	public String getManagerName() {
		return managerName;
	}

	public void setManagerName(String managerName) {
		this.managerName = managerName;
	}

	public SortedKeyValueFileManager(List<FileAccessManager> indexFiles, SortedKeyValueBucketFactory<T> bucketFactory) 
	throws InputException {
		dataManager = indexFiles.get(0);
		if (null == dataManager) {
			throw new InternalError("Wrong number of files for SortedKeyValueFileManager");
		}
		try {
			dataFile = dataManager.getReadWriteRandomAccessFile();
			indexLevel = indexFiles.size() - 1;
			if (indexLevel == 1) {
				FileAccessManager indexManager = indexFiles.get(1);
				
				SortedKeyLongBucket longBucket;
				if (indexManager.getLength() > 0) {
					FileInputStream ipStream = indexManager.getIpStream();
					DataInputStream dataIp = new DataInputStream(ipStream);
					longBucket = new SortedKeyLongBucket(dataIp);
					indexManager.releaseIpStream(ipStream);
					
				} else{
					longBucket = new SortedKeyLongBucket();
				}
				longBucket.setDataFileManager(indexManager);
				bucketIndex = longBucket;
			} else {
				SortedKeyValueBucketFactory<KeyLongTuple> indexBucketFactory = SortedKeyLongBucket.getFactory();
				SortedKeyValueFileManager<KeyLongTuple> mgr = 
					new SortedKeyValueFileManager<KeyLongTuple>(indexFiles.subList(1, indexFiles.size()), indexBucketFactory);
				mgr.managerName = "index_"+indexLevel;
				bucketIndex = mgr;
			}
			this.bucketFactory = bucketFactory;
			cache = new BucketCache(BUCKET_CACHE_SIZE);
		} catch (FileNotFoundException e) {
			throw new InputException(e);
		} catch (IOException e) {
			throw new InputException(e);
		}
	}

	FileAccessManager dataManager;

	public T getByName(String key) throws InputException {
		T result = null;
		KeyLongTuple bucketDescriptor = bucketIndex.getPredecessor(key, true);
		if (null != bucketDescriptor) {
			KeyValueIndex<T> bucket = cache.getBucket(bucketDescriptor.key);
			result = bucket.getByName(key);
		}
		return result;
	}

	@Override
	public T getPredecessor(String key, boolean inclusive) throws InputException {
		T result = null;
		KeyLongTuple bucketDescriptor = bucketIndex.getPredecessor(key, inclusive);
		if (null != bucketDescriptor) {
			KeyValueIndex<T> bucket = cache.getBucket(bucketDescriptor);
			result = bucket.getPredecessor(key, inclusive);
		
		}
		return result;
	}

	@Override
	public T getSuccessor(String key) throws InputException {
		T result = null;
		KeyLongTuple bucketDescriptor = bucketIndex.getPredecessor(key, true);
		if (null != bucketDescriptor) {
			KeyValueIndex<T> bucket = cache.getBucket(bucketDescriptor);
			result = bucket.getSuccessor(key);
			if (null == result) {
				KeyLongTuple nextDescriptor = bucketIndex.getSuccessor(bucketDescriptor.key);
				if (null != nextDescriptor) {
					bucket = cache.getBucket(nextDescriptor);
					result = bucket.getSuccessor(key);
				}
			}
		}
		return result;
	}

	public boolean addElement(T newTuple) throws InputException {
		KeyLongTuple bucketDescriptor = bucketIndex.getPredecessor(newTuple.key, true);
		if (null == bucketDescriptor) {
			bucketDescriptor = bucketIndex.getSuccessor(newTuple.key);
			if (null == bucketDescriptor) {
				newBucket(newTuple);
			} else {
				SortedKeyValueBucket<T> victim = removeBucket(bucketDescriptor.key);
				if (victim.addElement(newTuple)) {
					addBucket(victim); /* add with new key */
				} else {
					splitBucket(newTuple, victim);
				}
			}
		} else {
			addTupleToBucket(newTuple, cache.getBucket(bucketDescriptor.key));
		}
		return true;
	}

	private void addTupleToBucket(T newTuple, SortedKeyValueBucket<T> bucket) throws InputException {
		boolean success;
		success = bucket.addElement(newTuple);
		if (!success) {
			KeyLongTuple descriptor = bucket.getDescriptor();
			removeBucket(descriptor.key);
			splitBucket(newTuple, bucket);
		}
	}

	private void splitBucket(T newTuple, SortedKeyValueBucket<T> oldBucket) throws InputException {
		oldBucket.forceAddElement(newTuple);
	
		int size = oldBucket.size();
		Iterator<T> tupleIterator = oldBucket.iterator();
		T nextTuple = tupleIterator.next();
		SortedKeyValueBucket<T> firstBucket = newBucket(nextTuple, oldBucket.getFilePosition());
		for (int i = 1; i <= size/2; ++i) {
			nextTuple = tupleIterator.next();
			if (!firstBucket.addElement(nextTuple)) {
				throw new InternalError("Error splitting bucket");
			}
		}
		nextTuple = tupleIterator.next();
		SortedKeyValueBucket<T> secondBucket = newBucket(nextTuple);
		while (tupleIterator.hasNext()) {
			nextTuple = tupleIterator.next();
			if (!secondBucket.addElement(nextTuple)) {
				throw new InternalError("Error splitting bucket");
			}
		}
	}

	private SortedKeyValueBucket<T> newBucket(T initialTuple) throws InputException {
		long currentLength;
		try {
			currentLength = dataFile.length();
			long newLength = currentLength+SortedKeyValueBucket.BUCKET_CAPACITY;
			dataFile.setLength(newLength);
			return newBucket(initialTuple, currentLength);
		} catch (IOException e) {
			throw new InputException("Error accessing key-value data file "+dataManager.getPath(), e);
		}
	}

	private SortedKeyValueBucket<T> newBucket(T initialTuple, long filePosition) throws InputException {
		SortedKeyValueBucket<T> result = bucketFactory.makeBucket(initialTuple);
		result.setFilePosition(filePosition);
		addBucket(result);
		return result;
	}

	private void addBucket(SortedKeyValueBucket<T> result) throws InputException {
		KeyLongTuple descriptor = result.getDescriptor();
		if (!bucketIndex.addElement(descriptor)) {
			throw new InternalError("Cannot add "+result.getDescriptor()+" to bucket index");
		}
		cache.addBucket(result);
	}

	private SortedKeyValueBucket<T> removeBucket(String key) throws InputException {
		SortedKeyValueBucket<T> victim = cache.removeBucket(key);
		bucketIndex.removeElement(key);
		return victim;
	}

	@Override
	public void removeElement(String key) throws InputException {
		KeyLongTuple descriptor = bucketIndex.getPredecessor(key, true);
		SortedKeyValueBucket<T> bucket = cache.getBucket(descriptor.key);
		if (null == bucket) {
			throw new InputException("key "+key+" not found in index");
		} else {
			bucket.removeElement(key);
		}
	}

	public  void flush() throws InputException {
		cache.flush();
		bucketIndex.flush();
	}

	public void close() throws InputException {
		flush();
		bucketIndex.close();
		dataFile = null;
		dataManager.close();
	}

	@Override
	public Iterator<T> iterator() {
		return new TupleIterator();
	}

	public Iterator<T> iterator(String prefix) throws InputException {
		return new TupleIterator(prefix);
	}

	private class TupleIterator implements Iterator<T> {

		private Iterator<KeyLongTuple> bucketIterator;
		private String prefix;
		private Iterator<T> tupleIterator;

		public TupleIterator() {
			bucketIterator = bucketIndex.iterator();
			tupleIterator = null;
		}

		public TupleIterator(String prefix) throws InputException {
			tupleIterator = null;
			this.prefix = prefix;
			bucketIterator = bucketIndex.iterator(prefix);
		}

		@Override
		public boolean hasNext() {
			boolean result = false;
			if (null != tupleIterator) {
				result = tupleIterator.hasNext();
			} else {
				tupleIterator = null;
			}
			while (!result && bucketIterator.hasNext()) {
				KeyLongTuple bucketDescriptor = bucketIterator.next();
				try {
					SortedKeyValueBucket<T> currentBucket = cache.getBucket(bucketDescriptor.key);
					tupleIterator = (null == prefix)? currentBucket.iterator(): currentBucket.iterator(prefix);
					result = tupleIterator.hasNext();
				} catch (Exception e) {
					LibrisDatabase.librisLogger.log(Level.SEVERE, "error reading key-value file", e);
					break;
				}
			}
			return result;
		}

		@Override
		public T next() {
			T result = null;
			if (hasNext()) {
				result = tupleIterator.next();
			}
			return result;
		}

		@Override
		public void remove() {return;}

	}
	private class BucketCache {
		int cacheSize;
		LinkedHashMap<String, SortedKeyValueBucket<T>> cacheList;

		public BucketCache(int cacheSize) {
			this.cacheSize = cacheSize;
			cacheList = new LinkedHashMap<String, SortedKeyValueBucket<T>>(cacheSize/4);
		}

		public KeyValueIndex<T> getBucket(KeyLongTuple bucketDescriptor) throws InputException {
			return getBucket(bucketDescriptor.key);
		}

		public SortedKeyValueBucket<T> getBucket(String key) throws InputException {
			SortedKeyValueBucket<T> result = cacheList.get(key);
			if (null == result) {
				KeyLongTuple bucketDescriptor = bucketIndex.getByName(key);
				if (null == bucketDescriptor) {
					throw new InternalError("Key "+key+" not in index");
				}
				long position = bucketDescriptor.value;
				try {
					dataFile.seek(position);
					result = bucketFactory.makeBucket(dataFile);
					result.setFilePosition(position);
				} catch (IOException e) {
					throw new InputException("Error accessing key-value data file "+dataManager.getPath(), e);
				}
				addBucket(result);
			} else {
				cache.refreshBucket(result);
			}
			return result;
		}

		public void addBucket(SortedKeyValueBucket<T> buck) throws InputException {
			String key = buck.getFirstKey();

			if (cacheList.size() > cacheSize) {
				String oldestKey = cacheList.keySet().iterator().next();
				KeyLongTuple bucketDescriptor = bucketIndex.getByName(oldestKey);
				if (null == bucketDescriptor) {
					throw new InternalError("Key "+key+" not in index");
				}
				long position = bucketDescriptor.value;
				try {
					SortedKeyValueBucket<T> victim = cacheList.remove(oldestKey);
					if (victim.isDirty()) {
						dataFile.seek(position);
						victim.write(dataFile);
					}
				} catch (IOException e) {
					throw new InputException("Error accessing key-value data file "+dataManager.getPath(), e);
				}
			}
			if (null == key) {
				throw new InputException("Null key for bucket in "+managerName);
			}
			if (null == buck) {
				throw new InputException("Null bucket key="+key+" for bucket in "+managerName);
			}
			cacheList.put(key, buck);
		}

		public SortedKeyValueBucket<T> removeBucket(String key) throws InputException {
			SortedKeyValueBucket<T> victim = cacheList.remove(key);
			return victim;
		}

		void flush() throws InputException {
			if (cacheList.size() > 0) {
				for (String victimKey: cacheList.keySet()) {
					try {
						SortedKeyValueBucket<T> victim = cacheList.get(victimKey);
						if (victim.isDirty()) {
							long position = victim.getFilePosition();
							dataFile.seek(position);
							victim.write(dataFile);
						}
					} catch (IOException e) {
						throw new InputException("Error accessing key-value data file "+dataManager.getPath(), e);
					}
				}
			}
		}
		
		public void refreshBucket(SortedKeyValueBucket<T> buck) {
			if (cacheList.size() > 1) {
				String firstKey = buck.getFirstKey();
				if (cacheList.containsKey(firstKey)) {
					SortedKeyValueBucket<T> element = cacheList.remove(firstKey);
					cacheList.put(firstKey, element);
				}
			}
		}
	}
}
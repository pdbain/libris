package org.lasalledebain.libris.indexes;

import java.io.DataInput;
import java.io.IOException;

import org.lasalledebain.libris.exception.InputException;

public class SortedKeyLongBucket extends SortedKeyValueBucket<KeyLongTuple> {

	public SortedKeyLongBucket(DataInput dataIp) throws IOException {
		super(dataIp);
	}
	public SortedKeyLongBucket() {
		super();
	}
	@Override
	protected KeyLongTuple readTuple(DataInput source) throws IOException {
		KeyLongTuple tuple = new KeyLongTuple(source);
		return tuple;
	}

	private static Factory myFactory = new Factory();
	static SortedKeyValueBucketFactory<KeyLongTuple> getFactory() {
		return myFactory;
	}
	
	static class Factory implements SortedKeyValueBucketFactory<KeyLongTuple> {

		@Override
		public SortedKeyValueBucket<KeyLongTuple> makeBucket(KeyLongTuple initialTuple) throws InputException {
			SortedKeyLongBucket result = new SortedKeyLongBucket();
			result.addElement(initialTuple);
			return result;
		}

		@Override
		public SortedKeyValueBucket<KeyLongTuple> makeBucket(DataInput source) throws IOException {
			SortedKeyLongBucket result = new SortedKeyLongBucket(source);
			return result;
		}

		@Override
		public SortedKeyValueBucket<KeyLongTuple> makeBucket()
				throws InputException {
			SortedKeyLongBucket result = new SortedKeyLongBucket();
			return result;
		}		
	}
}

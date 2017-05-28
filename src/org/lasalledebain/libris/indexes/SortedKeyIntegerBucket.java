package org.lasalledebain.libris.indexes;

import java.io.DataInput;
import java.io.IOException;

import org.lasalledebain.libris.exception.InputException;


public class SortedKeyIntegerBucket extends SortedKeyValueBucket {

	public SortedKeyIntegerBucket(DataInput dataIp) throws IOException {
		super(dataIp);
	}
	public SortedKeyIntegerBucket() {
		super();
	}
	@Override
	protected KeyValueTuple readTuple(DataInput source) throws IOException {
		KeyIntegerTuple tuple = new KeyIntegerTuple(source);
		return tuple;
	}
	
	private static Factory myFactory = new Factory();
	public static SortedKeyValueBucketFactory<KeyIntegerTuple> getFactory() {
		return myFactory;
	}
	
	static class Factory implements SortedKeyValueBucketFactory<KeyIntegerTuple> {

		@Override
		public SortedKeyValueBucket<KeyIntegerTuple> makeBucket(KeyIntegerTuple initialTuple) throws InputException {
			SortedKeyIntegerBucket result = new SortedKeyIntegerBucket();
			result.addElement(initialTuple);
			return result;
		}

		@Override
		public SortedKeyValueBucket<KeyIntegerTuple> makeBucket(DataInput source) throws IOException {
			SortedKeyIntegerBucket result = new SortedKeyIntegerBucket(source);
			return result;
		}

		@Override
		public SortedKeyValueBucket<KeyIntegerTuple> makeBucket()
				throws InputException {
			SortedKeyIntegerBucket result = new SortedKeyIntegerBucket();
			return result;
		}		
	}
}

package org.lasalledebain.libris.indexes;

import java.io.DataInput;
import java.io.IOException;

import org.lasalledebain.libris.exception.InputException;

public interface SortedKeyValueBucketFactory<T extends KeyValueTuple> {
	
	SortedKeyValueBucket<T> makeBucket(T initialTuple) throws InputException;
	SortedKeyValueBucket<T> makeBucket() throws InputException;
	SortedKeyValueBucket<T> makeBucket(DataInput source) throws IOException;
}

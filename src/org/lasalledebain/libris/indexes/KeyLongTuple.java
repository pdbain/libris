package org.lasalledebain.libris.indexes;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.lasalledebain.libris.LibrisConstants;
import org.lasalledebain.libris.exception.InputException;

public class KeyLongTuple extends KeyValueTuple {

	long value;
	public KeyLongTuple(String key, long value) throws InputException {
		super(key);
		this.value = value;
	}
	
	public KeyLongTuple(DataInput inStream) throws IOException {	
		super(inStream);
		value = inStream.readLong();
	}
	@Override
	int entrySize() {
		return getKeyLen()+LibrisConstants.LONG_LEN;
	}

	@Override
	protected void write(DataOutput outStream) throws IOException {
		writeKey(outStream);
		outStream.writeLong(value);
	}

	@Override
	public boolean equals(Object comparand) {
		boolean result = false;
		if (KeyLongTuple.class.isInstance(comparand)) {
			KeyLongTuple other = (KeyLongTuple) comparand;
			result = (other.value == value) && super.keyEquals(other);	
		}
		return result;
	}

	@Override
	public String toString() {
		return key+":"+Long.toString(value);
	}

}

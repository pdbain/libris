package org.lasalledebain.libris.indexes;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.lasalledebain.libris.LibrisConstants;
import org.lasalledebain.libris.exception.InputException;

public class KeyIntegerTuple extends KeyValueTuple {

	int value;
	public KeyIntegerTuple(String key, int value) throws InputException {
		super(key);
		this.value = value;
	}
	
	public KeyIntegerTuple(DataInput inStream) throws IOException {	
		super(inStream);
		value = inStream.readInt();
	}
	@Override
	int entrySize() {
		return getKeyLen()+LibrisConstants.INT_LEN;
	}

	@Override
	protected void write(DataOutput outStream) throws IOException {
		writeKey(outStream);
		outStream.writeInt(value);
	}

	@Override
	public boolean equals(Object comparand) {
		boolean result = false;
		if (KeyIntegerTuple.class.isInstance(comparand)) {
			KeyIntegerTuple other = (KeyIntegerTuple) comparand;
			result = (other.value == value) && super.keyEquals(other);	
		}
		return result;
	}

	@Override
	public String toString() {
		return key+":"+Integer.toString(value);
	}

	public int getValue() {
		return value;
	}
}

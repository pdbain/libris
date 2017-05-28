package org.lasalledebain.libris.indexes;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.charset.Charset;

import org.lasalledebain.libris.LibrisConstants;
import org.lasalledebain.libris.exception.InputException;

/**
 * Represents a string key/binary data value pair.
 * @param <T>
 *
 */
public abstract class KeyValueTuple implements Comparable<KeyValueTuple>{
	static final Charset UTF8 = Charset.forName("UTF-8");
	protected final String key;
	public String getKey() {
		return key;
	}
	private int keyLen = 0;
	
	protected int getKeyLen() {
		if (0 == keyLen) {
			keyLen = key.getBytes(UTF8).length + LibrisConstants.SHORT_LEN;			
		}
		return keyLen;
	}

	protected KeyValueTuple(String key) throws InputException {
		if (key.length() > LibrisConstants.KEY_MAX_LENGTH) {
			throw new InputException("key "+key+" too long");
		}
		this.key = key;
	}
	
	protected KeyValueTuple(DataInput inStream) throws IOException {
		key = inStream.readUTF();
	}
	
	protected abstract void write(DataOutput outStream) throws IOException;
	
	protected void writeKey(DataOutput outStream) throws IOException {
		outStream.writeUTF(key);
	}
	
	abstract int entrySize ();
	
	boolean keyStartsWith(String prefix) {
		return key.startsWith(prefix);
	}
	
	@Override
	public int compareTo(KeyValueTuple comparand) {
		return key.compareTo(comparand.key);
	}

	protected boolean keyEquals(KeyValueTuple comparand) {
		return comparand.key.equals(key);
	}
}

package org.lasalledebain.hashtable;

import java.io.DataInput;
import java.nio.ByteBuffer;

public class MockVariableSizeEntryFactory {

	private int oversizeThreshold;
	int length;
	byte testData = 42;
	/**
	 * @param currentKey
	 * @param length
	 */
	public MockVariableSizeEntryFactory(int length) {
		this();
		this.length = length;
	}

	public MockVariableSizeEntryFactory() {
		length = -1;
		oversizeThreshold = -1;
	}

	public int getEntrySize() {
		return length+4;
	}

	public MockVariableSizeHashEntry makeEntry(int currentKey) {
		testData += currentKey;
		MockVariableSizeHashEntry result = new MockVariableSizeHashEntry(currentKey, length, testData);
		result.setOversize((oversizeThreshold > 0) && (length >= oversizeThreshold));
		return result;
	}

	public MockVariableSizeHashEntry makeEntry(int key, byte[] dat) {
		return new MockVariableSizeHashEntry(key, dat);
	}

	public MockVariableSizeHashEntry makeEntry(int key, ByteBuffer src, int len) {
		return new MockVariableSizeHashEntry(key, src, len);

	}

	public MockVariableSizeHashEntry makeEntry(DataInput backingStore) {
		throw new UnsupportedOperationException();
	}

}

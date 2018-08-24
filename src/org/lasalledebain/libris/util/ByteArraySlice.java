package org.lasalledebain.libris.util;

import java.nio.ByteBuffer;

public class ByteArraySlice {
	public byte[] getBaseArray() {
		return baseArray;
	}

	public void setBaseArray(byte[] baseArray) {
		this.baseArray = baseArray;
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	byte[] baseArray;
	int offset;
	int length;

	public ByteArraySlice(byte[] baseArray, int offset, int length) {
		super();
		this.baseArray = baseArray;
		this.offset = offset;
		this.length = length;
	}

	public int getInt(int intOffset) {
		ByteBuffer intBuffer =  ByteBuffer.wrap(baseArray, offset + intOffset, 4);
		return intBuffer.getInt();
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}
}

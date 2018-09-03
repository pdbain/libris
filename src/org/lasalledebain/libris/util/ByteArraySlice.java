package org.lasalledebain.libris.util;

import java.nio.ByteBuffer;

public class ByteArraySlice {
	byte[] baseArray;
	int offset;
	int length;
	boolean hasHash;
	int hashValue;


	public ByteArraySlice(byte[] baseArray, int offset, int length) {
		this.baseArray = baseArray;
		this.offset = offset;
		this.length = length;
	}

	public ByteArraySlice(byte[] baseArray) {
		this.baseArray = baseArray;
		this.offset = 0;
		this.length = baseArray.length;
	}

	public ByteArraySlice(String s) {
		this.baseArray = s.getBytes();
		this.offset = 0;
		this.length = baseArray.length;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object that) {
		if (!that.getClass().equals(this.getClass())) {
			return false;
		} else {
			ByteArraySlice thatSlice = (ByteArraySlice) that;
			if (length != thatSlice.length) {
				return false;
			} else if (hasHash && thatSlice.hasHash 
					&& (hashValue != thatSlice.hashValue)) {
				return false;
			} else {
				for (int i = 0; i < length; ++ i) {
					if (baseArray[offset + i] != thatSlice.baseArray[thatSlice.offset + i]) {
						return false;
					}
				}
				return true;
			}
		}
	}

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
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		if (!hasHash) {
			hashValue = Murmur3.hash32(baseArray, offset, length, Murmur3.DEFAULT_SEED);
			hasHash = true;
		}
		return hashValue;
	}

	@Override
	public String toString() {
		return new String(baseArray, offset, length);
	}
}

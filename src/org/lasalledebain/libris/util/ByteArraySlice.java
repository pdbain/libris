package org.lasalledebain.libris.util;

import java.nio.ByteBuffer;

public class ByteArraySlice {
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
			} else {
				for (int i = 0; i < length; ++ i) {
					if (baseArray[offset + 1] != thatSlice.baseArray[thatSlice.offset + 1]) {
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

	byte[] baseArray;
	int offset;
	int length;

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

package org.lasalledebain.libris.hashfile;

import org.lasalledebain.libris.util.ByteArraySlice;
import org.lasalledebain.libris.util.Murmur3;

public abstract class StringKeyHashEntry  implements HashEntry {
	protected ByteArraySlice keyBytes;
	/**
	 * @return the keyBytes
	 */
	public ByteArraySlice getKey() {
		return keyBytes;
	}
}

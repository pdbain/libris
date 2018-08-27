package org.lasalledebain.libris.hashfile;

import org.lasalledebain.libris.util.ByteArraySlice;
import org.lasalledebain.libris.util.Murmur3;

public abstract class StringKeyHashEntry  implements HashEntry {
	protected ByteArraySlice keyBytes;
	boolean hasHash;
	int hashValue;

	protected StringKeyHashEntry() {
		hasHash = false;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		if (!hasHash) {
			hashValue = Murmur3.hash32(keyBytes.getBaseArray(), keyBytes.getOffset(), keyBytes.getLength(), Murmur3.DEFAULT_SEED);
			hasHash = true;
		}
		return hashValue;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		StringKeyHashEntry other = (StringKeyHashEntry) obj;
		if (keyBytes == null) {
			if (other.keyBytes != null)
				return false;
		} else if (hasHash && other.hasHash 
				&& (hashValue != other.hashValue)) {
			return false;
		} else if (!keyBytes.equals(other.keyBytes)) {
			return false;
		}
		return true;
	}

}

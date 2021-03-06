package org.lasalledebain.libris.hashfile;

import java.io.DataOutput;
import java.io.IOException;

public interface HashEntry {
	void writeData(DataOutput backingStore) throws IOException;
	/**
	 * @return Size of the hash entry, including overhead in bucket
	 */
	int getTotalLength();
	
	/**
	 * @return Bucket overhead for this entry
	 */
	int getOverheadLength();
	/**
	 * This is the size of the overflow manager reference if the entry is oversize.
	 * Otherwise this is the size of the data itself.
	 * @return Size of entry, less overhead
	 */
	int getEntryLength();
	
	/**
	 * @return true if the entry should go in an overflow file
	 */
	boolean isOversize();
	
	void setOversize(boolean oversize);

	/**
	 * If the data is not oversize, this is the same as getOverheadLength()
	 * @return Size of the data.
	 */
	int getDataLength();

}

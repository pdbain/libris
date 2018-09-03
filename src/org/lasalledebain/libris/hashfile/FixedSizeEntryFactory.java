package org.lasalledebain.libris.hashfile;

import java.io.DataInput;
import java.io.IOException;

public interface FixedSizeEntryFactory<T extends FixedSizeHashEntry> extends NumericKeyEntryFactory<T> {
	public int getEntrySize();
	public T makeEntry(DataInput src) throws IOException;

}

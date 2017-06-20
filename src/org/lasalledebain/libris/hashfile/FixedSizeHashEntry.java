package org.lasalledebain.libris.hashfile;

import java.io.DataInput;
import java.io.IOException;

public interface FixedSizeHashEntry extends HashEntry {

	void readData(DataInput backingStore) throws IOException;

}

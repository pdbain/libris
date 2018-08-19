package org.lasalledebain.libris.hashfile;

import java.io.IOException;

public interface VariableSizeHashEntry extends NumericKeyHashEntry {
	static final int MAX_VARIABLE_HASH_ENTRY=256;
	static final int OVERSIZE_HASH_ENTRY_LENGTH = 8;
	byte[] getData() throws IOException;
	int getOverheadLength();
	int getEntryLength();
}

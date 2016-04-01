package org.lasalledebain.libris.hashfile;


@SuppressWarnings("unchecked")
public interface VariableSizeHashEntry extends HashEntry, Comparable<HashEntry> {
	static final int MAX_VARIABLE_HASH_ENTRY=256;
	static final int OVERSIZE_HASH_ENTRY_LENGTH = 8;
	byte[] getData();
}

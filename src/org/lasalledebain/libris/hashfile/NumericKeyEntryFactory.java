package org.lasalledebain.libris.hashfile;

import java.io.DataInput;
import java.io.IOException;
import java.nio.ByteBuffer;

public interface NumericKeyEntryFactory <EntryType extends NumericKeyHashEntry> extends EntryFactory<EntryType> {
	public EntryType makeEntry(int key, byte dat[]);
	public EntryType makeEntry(int key, ByteBuffer src, int len);
	public EntryType makeEntry(DataInput backingStore) throws IOException;
}

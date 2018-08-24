package org.lasalledebain.libris.hashfile;

import java.io.DataInput;
import java.io.IOException;
import java.nio.ByteBuffer;

public interface EntryFactory<EntryType extends HashEntry> {

	EntryType makeEntry(int entryId, byte[] dat);

	EntryType makeEntry(int entryId, ByteBuffer bucketEntryData, int length);

	EntryType makeEntry(DataInput backingStore) throws IOException;

}

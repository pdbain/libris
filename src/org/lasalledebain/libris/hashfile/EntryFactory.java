package org.lasalledebain.libris.hashfile;

import java.io.DataInput;
import java.io.IOException;
import java.nio.ByteBuffer;

public interface EntryFactory <T extends HashEntry> {
	public T makeEntry(int key, byte dat[]);
	public T makeEntry(int key, ByteBuffer src, int len);
	public T makeEntry(DataInput backingStore) throws IOException;
}

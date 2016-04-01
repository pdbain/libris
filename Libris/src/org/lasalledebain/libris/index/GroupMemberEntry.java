package org.lasalledebain.libris.index;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.lasalledebain.libris.hashfile.HashEntry;
import org.lasalledebain.libris.hashfile.VariableSizeEntryFactory;
import org.lasalledebain.libris.hashfile.VariableSizeHashEntry;

@SuppressWarnings("unchecked")
public class GroupMemberEntry implements VariableSizeHashEntry {

	int recordId;
	private boolean oversize;
	private int length;
	private final int OVERHEAD = 4 + 2;
	private Group group;
	byte data[];
	public GroupMemberEntry(Group grp) {
		this.group = grp;
		this.recordId = grp.getId().getRecordNumber();
		this.length = (grp.getNumAffiliates() + grp.getNumChildren()) * 4;
		oversize = length > MAX_VARIABLE_HASH_ENTRY;
	}

	@Override
	public int getKey() {
		return recordId;
	}

	@Override
	public byte[] getData() {
		return data;
	}

	@Override
	public void setKey(int newKey) {
		recordId = newKey;
	}

	@Override
	public int getTotalLength() {
		return getEntryLength() + getOverheadLength();
	}
	
	@Override
	public int getEntryLength() {
		return oversize? 4: length;
	}

	@Override
	public int getOverheadLength() {
		return OVERHEAD;
	}

	@Override
	public boolean isOversize() {
		return oversize;
	}

	@Override
	public void readData(DataInput backingStore) throws IOException {
		throw new IOException("not implemented");
	}

	@Override
	public void writeData(DataOutput backingStore) throws IOException {
		throw new IOException("not implemented");
		}
	@Override
	public int compareTo(HashEntry arg0) {
		int otherKey = arg0.getKey();
		int myKey = getKey();
		return (otherKey == myKey)? 0: ((otherKey < myKey)? -1: 1);
	}

	@Override
	public Integer getIntegerKey() {
		return new Integer(recordId);
	}

	@Override
	public void readData(ByteBuffer buff, int length) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getDataLength() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setData(byte[] dat) {
		// TODO Auto-generated method stub
		
	}
	
	static class GroupMemberEntryFactory implements VariableSizeEntryFactory<GroupMemberEntry> {

		@Override
		public GroupMemberEntry makeEntry(int length) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public int getEntrySize() {
			return 0;
		}

		@Override
		public HashEntry makeEntry() {
			return makeEntry(0);
		}
		
	}

	@Override
	public void setOversize(boolean oversize) {
		// TODO Auto-generated method stub
		
	}
}

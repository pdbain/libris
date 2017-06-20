package org.lasalledebain.libris.index;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.lasalledebain.libris.hashfile.HashEntry;

/**
 * Entry comprises:
 * 4 bytes child count (nChildren)
 * 4 * nChildren bytes record IDs in sorted order
 * Remainder  is 4 byte affiliates
 *
 */
public class AffiliateListEntry extends AbstractVariableSizeHashEntry {

	int nChildren;
	int children[];
	int affiliates[];
	private int nAffiliates;
	
	@Override
	public void readData(ByteBuffer buff, int length) {
		nChildren = buff.getInt();
		children = new int[nChildren];
		for (int i = 0; i < nChildren; i++) {
			children[i] = buff.getInt();
		}
		nAffiliates = (length/4) - 1 - nChildren;
		affiliates = new int[nAffiliates];
		for (int i = 0; i < nAffiliates; i++) {
			affiliates[i] = buff.getInt();
		}
	}


	@Override
	public void writeData(DataOutput backingStore) throws IOException {
		backingStore.writeInt(nChildren);
		for (int i = 0; i < nChildren; i++) {
			backingStore.writeInt(children[i]);
		}
		for (int i = 0; i < nAffiliates; i++) {
			backingStore.writeInt(affiliates[i]);
		}
	}

	@Override
	public int getTotalLength() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isOversize() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getDataLength() {
		return 4 * ( + nChildren + nAffiliates);
	}

	@Override
	public void setData(byte[] dat) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int compareTo(Object comparand) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public byte[] getData() {
		// TODO Auto-generated method stub
		return null;
	}


}

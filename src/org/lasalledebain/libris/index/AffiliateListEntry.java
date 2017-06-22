package org.lasalledebain.libris.index;

import java.io.DataOutput;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.lasalledebain.libris.hashfile.HashEntry;
import org.lasalledebain.libris.hashfile.VariableSizeEntryFactory;

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
	public int getDataLength() {
		return 4 * (1 + nChildren + nAffiliates);
	}

	public class AffiliateListEntryFactory implements VariableSizeEntryFactory<AffiliateListEntry> {

		@Override
		public HashEntry makeEntry() {
			return new AffiliateListEntry();
		}

		@Override
		public int getEntrySize() {
			return 0;
		}

		@Override
		public AffiliateListEntry makeEntry(int length) {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
}

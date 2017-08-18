package org.lasalledebain.libris.index;

import java.io.DataOutput;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import org.lasalledebain.libris.hashfile.VariableSizeEntryFactory;

/**
 * Entry comprises:
 * 4 bytes child count (nChildren)
 * 4 * nChildren bytes record IDs in sorted order
 * Remainder  is 4 byte affiliates
 * Maximum size is 255 children+affiliates 
 */
public class AffiliateListEntry extends AbstractVariableSizeHashEntry {

	@Override
	public boolean isOversize() {
		
		return (children.length + affiliates.length) > oversizeThreashold;
	}

	int children[];
	int affiliates[];
	static int oversizeThreashold = 255;
	static final int[] emptyList = new int[0];  
	
	public AffiliateListEntry() {
		children = affiliates = emptyList;
	}
	
	public static int getOversizeThreshold() {
		return oversizeThreashold;
	}

	public static void setOversizeThreshold(int oversizeThreashold) {
		AffiliateListEntry.oversizeThreashold = oversizeThreashold;
	}

	@Override
	public void readData(ByteBuffer buff, int length) {
		int nChildren = buff.getInt();
		children = new int[nChildren];
		for (int i = 0; i < nChildren; i++) {
			children[i] = buff.getInt();
		}
		int nAffiliates = (length/4) - 1 - nChildren;
		affiliates = new int[nAffiliates];
		for (int i = 0; i < nAffiliates; i++) {
			affiliates[i] = buff.getInt();
		}
	}

	@Override
	public void writeData(DataOutput backingStore) throws IOException {
		int nChildren = children.length;
		backingStore.writeInt(nChildren );
		for (int i = 0; i < nChildren; i++) {
			backingStore.writeInt(children[i]);
		}
		int nAffiliates = affiliates.length;
		for (int i = 0; i < nAffiliates ; i++) {
			backingStore.writeInt(affiliates[i]);
		}
	}
	
	public void addChild(int newChild) {
		children = Arrays.copyOf(children, children.length+1);
		children[children.length-1] = newChild;
		Arrays.sort(children);
	}
	

	public void addAffiliate(int newAffiliate) {
		affiliates = Arrays.copyOf(affiliates, affiliates.length+1);
		affiliates[affiliates.length-1] = newAffiliate;
		Arrays.sort(affiliates);
	}

	public void addAffiliates(int addenda[], int occupancy, boolean addChildren) {
		int[] original = addChildren? children: affiliates;
		int[] newAffiliates = Arrays.copyOf(original, original.length+occupancy);
		System.arraycopy(addenda, 0, newAffiliates, original.length, occupancy);
		Arrays.sort(newAffiliates);
		if (addChildren) {
			children = newAffiliates;
		} else {
			affiliates = newAffiliates;
		}
	}

	@Override
	public int getDataLength() {
		return 4 * (1 + children.length + affiliates.length);
	}

	public static AffiliateListEntryFactory getFactory() {
		return new AffiliateListEntryFactory();
	}
	
	public static class AffiliateListEntryFactory implements VariableSizeEntryFactory<AffiliateListEntry> {

		@Override
		public AffiliateListEntry makeEntry() {
			AffiliateListEntry entry = new AffiliateListEntry();
			return entry;
		}

		public AffiliateListEntry makeEntry(int key) {
			AffiliateListEntry entry = new AffiliateListEntry();
			entry.setKey(key);
			return entry;
		}

	}

	public int[] getChildren() {
		return children;
	}

	public int[] getAffiliates() {
		return affiliates;
	}
}

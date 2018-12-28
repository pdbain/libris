package org.lasalledebain.libris.index;

import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;

import org.lasalledebain.libris.hashfile.NumericKeyHashEntry;

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

	private final int children[];
	private final int affiliates[];
	static int oversizeThreashold = 255;
	public static final int[] emptyList = new int[0];  
	
	public AffiliateListEntry(int key) {
		super(key);
		children = affiliates = emptyList;
	}
	
	public AffiliateListEntry(int key, int newChildren[], int newAffiliates[]) {
		super(key);
		children = Arrays.copyOf(newChildren, newChildren.length);
		affiliates = Arrays.copyOf(newAffiliates, newAffiliates.length);;
	}
	
	public AffiliateListEntry(int key, int affiliate, boolean addChild) {
		super(key);
		if (addChild) {
			children = new int[1];
			children[0] = affiliate;
			affiliates = emptyList;
		} else {
			affiliates = new int[1];
			affiliates[0] = affiliate;
			children = emptyList;
		}
	}
	
	public AffiliateListEntry(AffiliateListEntry original, int newAffiliate, boolean addChild) {
		super(original.getKey());
		if (addChild) {
			if (Arrays.binarySearch(original.children, newAffiliate) < 0) {
				children = Arrays.copyOf(original.children, original.children.length + 1);
				children[children.length-1] = newAffiliate;
				Arrays.sort(children);
			} else {
				children = original.children;				
			}
			affiliates = original.affiliates;
		} else {
			if (Arrays.binarySearch(original.affiliates, newAffiliate) < 0) {
				affiliates = Arrays.copyOf(original.affiliates, original.affiliates.length + 1);
				affiliates[affiliates.length-1] = newAffiliate;
				Arrays.sort(affiliates);
			} else {
				affiliates = original.affiliates;				
			}
			children = original.children;
		}
	}
		
	public AffiliateListEntry(AffiliateListEntry original, int newAffiliates[], boolean addChild) {
		super(original.getKey());
		if (addChild) {
			children = Arrays.copyOf(original.children, original.children.length + newAffiliates.length);
			System.arraycopy(newAffiliates, 0, children, original.children.length, newAffiliates.length);
			affiliates = original.affiliates;
		} else {
			affiliates = Arrays.copyOf(original.affiliates, original.affiliates.length + newAffiliates.length);
			System.arraycopy(newAffiliates, 0, affiliates, original.affiliates.length, newAffiliates.length);
			children = original.children;
		}
	}
		
	public static int getOversizeThreshold() {
		return oversizeThreashold;
	}

	public static void setOversizeThreshold(int oversizeThreashold) {
		AffiliateListEntry.oversizeThreashold = oversizeThreashold;
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
	
	@Override
	public int getDataLength() {
		return 4 * (1 + children.length + affiliates.length);
	}

	public int[] getChildren() {
		return children;
	}

	public int[] getAffiliates() {
		return affiliates;
	}

	@Override
	public int compareTo(NumericKeyHashEntry comparand) {
		if (this == comparand) {
			return 0;
		} else if (this.getClass().isInstance(comparand)) {
			return Integer.compare(((AbstractNumericKeyHashEntry)comparand).key, key);
		} else {
			return -1;
		}
	}
}

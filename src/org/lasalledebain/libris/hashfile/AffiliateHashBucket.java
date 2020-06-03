package org.lasalledebain.libris.hashfile;

import java.io.DataInput;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.Arrays;

import org.lasalledebain.libris.index.AffiliateListEntry;
import org.lasalledebain.libris.indexes.BucketOverflowFileManager;

public class AffiliateHashBucket extends VariableSizeEntryHashBucket<AffiliateListEntry> {

	public AffiliateHashBucket(RandomAccessFile backingStore, int bucketNum, BucketOverflowFileManager overflowMgr,
			EntryFactory<AffiliateListEntry> eFact) {
		super(backingStore, bucketNum, overflowMgr);
	}

	public AffiliateHashBucket(RandomAccessFile backingStore, int bucketNum, BucketOverflowFileManager overflowMgr) {
		super(backingStore, bucketNum, overflowMgr);
	}

	@Override
	protected AffiliateListEntry makeEntry(int key, byte[] dat) {
		ByteBuffer src = ByteBuffer.wrap(dat, 0, dat.length);
		int nChildren = src.getInt();
		int[] tempChildren = new int[nChildren];
		for (int i = 0; i < nChildren; i++) {
			tempChildren[i] = src.getInt();
		}
		Arrays.sort(tempChildren);
		int nAffiliates = (dat.length/4) - 1 - nChildren;
		int[] tempAffiliates = new int[nAffiliates];
		for (int i = 0; i < nAffiliates; i++) {
			tempAffiliates[i] = src.getInt();
		}
		Arrays.sort(tempAffiliates);
		return new AffiliateListEntry(key, tempChildren, tempAffiliates);
	}

	@Override
	protected AffiliateListEntry makeEntry(int key, ByteBuffer src, int length) {
		int nChildren = src.getInt();
		int[] tempChildren = new int[nChildren];
		for (int i = 0; i < nChildren; i++) {
			tempChildren[i] = src.getInt();
		}
		Arrays.sort(tempChildren);
		int nAffiliates = (length/4) - 1 - nChildren;
		int[] tempAffiliates = new int[nAffiliates];
		for (int i = 0; i < nAffiliates; i++) {
			tempAffiliates[i] = src.getInt();
		}
		Arrays.sort(tempAffiliates);
		return new AffiliateListEntry(key, tempChildren, tempAffiliates);
	}

	/* Reads the key, number of children, and the list of children.
	 * @see org.lasalledebain.libris.hashfile.EntryFactory#makeEntry(java.io.DataInput)
	 */
	public AffiliateListEntry makeEntry(DataInput src) throws IOException {
		int key = src.readInt();
		int nChildren = src.readInt();
		int[] tempChildren = new int[nChildren];
		for (int i = 0; i < nChildren; i++) {
			tempChildren[i] = src.readInt();
		}
		Arrays.sort(tempChildren);
		
		return new AffiliateListEntry(key, tempChildren, AffiliateListEntry.emptyList);
	}

	public AffiliateListEntry makeEntry(int key) {
		AffiliateListEntry entry = new AffiliateListEntry(key);
		return entry;
	}

	public AffiliateListEntry makeEntry(int parent, int affiliate, boolean addChild) {
		return new AffiliateListEntry(parent, affiliate, addChild);
	}

	public AffiliateListEntry makeEntry(AffiliateListEntry original, int newAffiliate, boolean addChild) {
		return new AffiliateListEntry(original, newAffiliate, addChild);
	}

}

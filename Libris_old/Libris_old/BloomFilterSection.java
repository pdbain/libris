package Libris;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;

import Libris.LibrisException.ErrorIds;

/*
 * Created on Dec 9, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

/**
 * This class implements keyword searching using a Bloom filter  There is one filter for 
 * each level of resolution of the filter.  Level 1 filters have a bit mask for each record,
 * level n have a bit mask for BRANCHFACTOR**n records.  
 * Only one bit mask exists in a given BloomFilterSection at any given time.
 */

public class BloomFilterSection extends BloomFilter{
	private static final int NULLRECORD = -1;
	/**
	 *  resident bit mask
	 */
	long [] bitMask;
	private ArrayList<long[]> queryMasks;
	private ArrayList<Integer> queryResult;
	int nWords;
	int maskSize;
	int baseRecordNum;
	File maskFile;
	public BloomFilterSection() {
		
	}
	public BloomFilterSection (LibrisDatabase database, int level) {
		this.level = level;
		maskFile = database.fileIndex.getMaskFile(level);
		maskSize =  BASICSIZE;  // bits per mask
		RECORDSPERMASK = 1;
		queryMasks = new ArrayList<long[]>(1);
		queryResult = new ArrayList<Integer>();
		for (int i = 1; i < level; ++i) {
			RECORDSPERMASK *= BRANCHFACTOR;
			maskSize *= BRANCHFACTOR;
		}
		nWords = maskSize/wSize;
		baseRecordNum = NULLRECORD;
		if (level > 1)
				subFilter = new BloomFilterSection(database, level - 1);
		else
				subFilter = null;
	}
	
	public void addKeywords(long[] bitMask, String[] keywords) {
		/**
		 * Adds keywords to the given bit mask
		 */
		int wordNum; long bitPos;
		for (String k: keywords) {
			int [] hashList = hashKeyword(k);
			for (int i: hashList) {
				wordNum = (i % maskSize)/wSize;
				bitPos = 1l << (i%wSize);
				bitMask[wordNum] |= bitPos;
			}
		}
	}

	protected void addKeywords(String[] keywords) {
		/**
		 * Adds keywords to the resident bit mask
		 */
		addKeywords(bitMask, keywords);
		if (subFilter != null)
			subFilter.addKeywords(keywords);
	}
	
	private long[] makeBitmask(String[] keywords) {
		long [] newBitMask = makeBitmask();
		addKeywords(newBitMask, keywords);
		return(newBitMask);
	}

	private long[] makeBitmask() {
		long [] newBitMask = new long[nWords];
		return(newBitMask);
	}

	public void updateBitmask(int recNum) {
		/**
		 *  Create a new bitmask if it does not exist.  If it does
		 * and recnum is in the bitmask's range, do nothing.  If the 
		 * bitmask exists but new record is outside, write the bitmask to the file
		 * and clear it.
		 */
		if ((bitMask == null) || !((recNum >= baseRecordNum) &&
			(recNum < (baseRecordNum + RECORDSPERMASK)))){
				if ((bitMask != null) && (baseRecordNum != NULLRECORD)) {
					writeBitMask();
				}
			if (bitMask != null) {
				for (int i = 0; i < nWords; ++i) {
					bitMask[i] = 0;
				}
			} else {
				bitMask = new long[nWords];
			}
			baseRecordNum = recNum - (recNum % RECORDSPERMASK);
		}
		if (subFilter != null) 
			subFilter.updateBitmask(recNum);
	}

	// These aren't real serialization methods since they don't have
	// the type information
	
	protected void makeMaskFile() {
		try {
			maskFileHandle = new RandomAccessFile(maskFile, "rw");
		} catch(IOException e) {
			database.error("Could not open "
					+maskFile.toString()+":"+e.toString());
		}
		if (subFilter != null) 
			subFilter.makeMaskFile();
	}
	protected void writeBitMask() {
		long filePos = getFilePos(baseRecordNum);
		for (int i = 0; i < nWords; ++i) {
			/* the bit mask shows which keywords AREN'T in the record */
			bitMask[i] = ~bitMask[i];
		}
		try {
			maskFileHandle.seek(filePos);
			for (long w: bitMask) {
				maskFileHandle.writeLong(w);
			}
		} catch (IOException e) {
			database.error("Could not open "
				+maskFile.toString()+":"+e.toString());
		}
		if (subFilter != null) 
			subFilter.writeBitMask();
	}
	private long getFilePos(int recordNum) {
		return (recordNum/RECORDSPERMASK)*maskSize/bitsPerByte;
	}
	private void readObject(int recordNum)
    throws IOException {
		maskFileHandle.seek(getFilePos(recordNum));
		if (bitMask == null) {
			bitMask = new long[nWords];
		}
		for (int i=0; i < nWords; ++i) {
			bitMask[i] = maskFileHandle.readLong();
		}
	}

	/**
	 * 
	 * @param query 2-d array, AND the words in each row, OR the rows
	 * @param result Initally null, gets matching records added
	 * @return
	 */
	/**
	 * @param databaseSize
	 * @throws LibrisException 
	 */
	
	public void clearQuery() throws LibrisException {
		queryMasks.clear();
		queryResult.clear();
		if (maskFileHandle == null) try {
			maskFileHandle = new RandomAccessFile(maskFile, "r");
			if (level > 1)
				subFilter.clearQuery();
		} catch(IOException e) {
			throw(new LibrisException(ErrorIds.ERR_NO_INDEX_FILE, maskFile.toString()+":"+e.toString()));
		}
		try {
			maskFileHandle.seek(0);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void addQueryKeywords (String keywords[]) throws LibrisException {
		if (maskFileHandle == null) try {
			maskFileHandle = new RandomAccessFile(maskFile, "r");
		} catch(IOException e) {
			throw(new LibrisException(ErrorIds.ERR_NO_INDEX_FILE, maskFile.toString()+":"+e.toString()));
		}
		queryMasks.add(makeBitmask(keywords));
		if (level > 1)
			subFilter.addQueryKeywords(keywords);
	}
	public ArrayList <Integer> runQuery (int startRecord, int endRecord) {
		ArrayList <Integer> result = new ArrayList<Integer>();
		for (int r=startRecord; r <= endRecord; r += RECORDSPERMASK) {
			try {
				readObject(r);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Iterator<long[]> i = queryMasks.iterator();
			boolean found = false;
			while (i.hasNext() && !found) {
				long[] qM = i.next();
				if (compareMask(qM)) {
					if (level > 1) {
						int subEnd = r+(RECORDSPERMASK)-1;
						if (subEnd > endRecord) subEnd = endRecord;
						result.addAll(subFilter.runQuery(r, subEnd));
					} else {
						result.add(r);
					}
					found = true;
				}
			}
		}
		return(result);
		
	}
	private boolean compareMask(long[] qm) {
		// The qm contains ones in the  bits for symbol sequences which the record must contain
		// the bitMask contains ones in the bit positions for symbol sequences which
		// the record does not contain
		for (int i = 0; i < nWords; ++i) {
			if ((qm[i] & bitMask[i]) != 0)
				return false;
		}
		return true;
	}
	/**
	 Level in the hierarchy. Level 0 represents records.  Level 1 represents groups of records,
	 level 2 groups of groups, etc.
	 */
	private int level;
	private int hash(int num) {
		return((num * 16807)%2147483647);
	}
	private int [] hashKeyword(String keyword) {
		int i;
		int l = keyword.length();
		int  currHash = 0;
		int[] hashValues = new int[l];
		char c;
		for (i = 0; i < l; ++i) {
			c = keyword.charAt(i);	
			currHash = hash(Character.getNumericValue(Character.toLowerCase(c))+currHash);
			if (currHash < 0) currHash += Integer.MAX_VALUE;
			hashValues[i] = currHash;
		}
		return hashValues;
	}
	
	public static int getLevels(int recCount) {
		int nRecords = recCount;
		int nLevels = 1;
		while (nRecords > BRANCHFACTOR) {
			nRecords /= BRANCHFACTOR;
			++nLevels;
		}
		return(nLevels);
	}
}

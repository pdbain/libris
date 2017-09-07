package Libris;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

public class BloomFilter {

	/*
	 * Container for the keyword search filter.  The subfilters contain the actual
	 * Bloom filter bit masks.
	 */
	protected static final int BRANCHFACTOR = 4;
	protected static final int BASICSIZE = 1024;
	public static final int wSize = 64; // bits
	public static final int bitsPerByte = 8;
	private int nRecords;
	public int RECORDSPERMASK;
	protected RandomAccessFile maskFileHandle = null;
	private DatabaseFileRecords reader;
	protected LibrisDatabase database;
	protected BloomFilterSection subFilter;

	public BloomFilter() {
		
	}
	public BloomFilter(LibrisDatabase database, int recCount) {
		this.database = database;
		nRecords = recCount;
		int nLevels = BloomFilterSection.getLevels(recCount);
		subFilter = new BloomFilterSection(database, nLevels);
	}

	protected boolean readDatabase() throws LibrisException {
		reader = new DatabaseFileRecords(database.getCurrentDBFile(), database);
		LibrisRecord r;
		String [] k;
		
		subFilter.makeMaskFile();
		
		for (int i = 0; i < nRecords; ++i) {
			r = reader.readRecord(i);
			k = r.getKeywords();
			addKeywords(i, k);
		}
		writeBitMask();
		return true;
	}

	private void addKeywords(int recNum, String[] keywords) {
		subFilter.updateBitmask(recNum);
		subFilter.addKeywords(keywords);
	}

	private void writeBitMask() {
		subFilter.writeBitMask();
	}

	public void clearQuery() throws LibrisException {
		// TODO Auto-generated method stub
		
	}

	public void addQueryKeywords(String[] k) throws LibrisException {
		// TODO Auto-generated method stub
		
	}

	public ArrayList<Integer> runQuery(int i, int j) {
		// TODO Auto-generated method stub
		return null;
	}

}
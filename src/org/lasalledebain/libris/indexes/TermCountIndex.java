package org.lasalledebain.libris.indexes;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Objects;
import java.util.Optional;

import org.lasalledebain.libris.FileAccessManager;
import org.lasalledebain.libris.exception.DatabaseError;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.hashfile.TermCountHashFile;
import org.lasalledebain.libris.index.TermCountEntry;
import org.lasalledebain.libris.ui.Messages;
import org.lasalledebain.libris.util.LibrisStemmer;
import org.lasalledebain.libris.util.Reporter;

public class TermCountIndex {
	private final FileAccessManager backingStoreFileMgr;
	private final RandomAccessFile backingStore;
	private final TermCountHashFile termHashFile;
	private final boolean readOnly;
	long numTerms;
	long numUniqueTerms;

	public TermCountIndex(FileAccessManager backingStoreFileMgr, boolean readOnly) throws DatabaseException, IOException {
		this( backingStoreFileMgr, Optional.empty(), readOnly);
	}
	
	public TermCountIndex(FileAccessManager backingStoreFileMgr, Optional<Integer> initialNumBuckets, boolean readOnly) throws DatabaseException, IOException {
		this.readOnly = readOnly;
		this.backingStoreFileMgr = backingStoreFileMgr;
		try {
			backingStore = readOnly?  backingStoreFileMgr.getReadOnlyRandomAccessFile(): 
				backingStoreFileMgr.getReadWriteRandomAccessFile();
			termHashFile = new TermCountHashFile(backingStore);
			if (initialNumBuckets.isPresent()) {
				termHashFile.resize(initialNumBuckets.get());
			}
		} catch (FileNotFoundException e) {
			throw new DatabaseException(Messages.getString("AffiliateList.0")+backingStoreFileMgr.getPath(), e); //$NON-NLS-1$
		}
		numTerms = numUniqueTerms = 0;
		termHashFile.resetExpansionCount();
	}

	public void flush() throws DatabaseException {
		if (!readOnly && (null != termHashFile)) {
			try {
				termHashFile.flush();
			} catch (IOException e) {
				throw new DatabaseException(e);
			}
		}
	}

	public void close() throws DatabaseException {
		flush();
		try {
			backingStoreFileMgr.releaseRaFile(backingStore);
		} catch (IOException e) {
			throw new DatabaseException(e);
		}
	}


	public void incrementTermCount(String term, boolean normalize) {

		String normalizedTerm = normalize(term, normalize);
		TermCountEntry entry;
		++numTerms;
		try {
			entry = termHashFile.getEntry(normalizedTerm);
			if (Objects.isNull(entry)) {
				entry = new TermCountEntry(normalizedTerm, 1);
				termHashFile.addEntry(entry);
				++numUniqueTerms;
			} else {
				entry.incrementCount();
			}
		} catch (IOException | DatabaseException e) {
			throw new DatabaseError("error adding "+term, e);
		}
	}

	public void setTermCount(String term, boolean normalize, int termCount) throws DatabaseException {

		String normalizedTerm = normalize(term, normalize);
		TermCountEntry entry;
		++termCount;
		try {
			entry = termHashFile.getEntry(normalizedTerm);
			if (Objects.isNull(entry)) {
				entry = new TermCountEntry(normalizedTerm, termCount);
				termHashFile.addEntry(entry);
				++numUniqueTerms;
			} else {
				entry.setTermCount(termCount);
			}
		} catch (IOException e) {
			throw new DatabaseException(e);
		}
	}

	public void resetNumTerms() {
		this.numTerms = 0;
	}

	public void resetNumUniqueTerms() {
		this.numUniqueTerms = 0;
	}

	private static String normalize(String term, boolean normalize) {
		String normalizedTerm;
		if (normalize) {
			LibrisStemmer s = new LibrisStemmer(term, true);
			s.stem();
			normalizedTerm = s.toString();
		} else {
			normalizedTerm = term;
		}
		return normalizedTerm;
	}

	public int getTermCount(String term, boolean normalize) throws DatabaseException {
		try {
			String normalizedTerm = normalize(term, normalize);
			TermCountEntry entry = termHashFile.getEntry(normalizedTerm);
			if (Objects.isNull(entry)) {
				return 0;
			} else {
				return entry.getTermCount();
			}
		} catch (IOException e) {
			throw new DatabaseException(e);
		}
	}

	public void generateReport(Reporter rpt) {
		rpt.reportValue(Reporter.INDEXING_NUM_TERMS, numTerms);
		rpt.reportValue(Reporter.INDEXING_NUM_UNIQUE_TERMS, numUniqueTerms);
		rpt.reportValue(Reporter.INDEXING_TERMS_BUCKETS_NUM, termHashFile.getNumBuckets());
		rpt.reportValue(Reporter.INDEXING_TERMS_BUCKETS_EXPANSION, termHashFile.getExpansionCount());
		rpt.reportValue(Reporter.INDEXING_TERMS_BUCKETS_FLUSHES, termHashFile.getFlushCount());
		rpt.reportValue(Reporter.INDEXING_TERMS_BUCKETS_LOADS, termHashFile.getBucketLoadCount());
	}
}

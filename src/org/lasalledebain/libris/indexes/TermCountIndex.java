package org.lasalledebain.libris.indexes;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Objects;

import org.lasalledebain.libris.FileAccessManager;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.hashfile.TermCountHashFile;
import org.lasalledebain.libris.index.TermCountEntry;
import org.lasalledebain.libris.ui.Messages;
import org.lasalledebain.libris.util.LibrisStemmer;

public class TermCountIndex {
	private final FileAccessManager backingStoreFileMgr;
	private final RandomAccessFile backingStore;
	private final TermCountHashFile termHashFile;
	private final boolean readOnly;

	public TermCountIndex(FileAccessManager backingStoreFileMgr, boolean readOnly) throws DatabaseException, IOException {
		this.readOnly = readOnly;
		this.backingStoreFileMgr = backingStoreFileMgr;
		try {
			backingStore = readOnly?  backingStoreFileMgr.getReadOnlyRandomAccessFile(): 
				backingStoreFileMgr.getReadWriteRandomAccessFile();
			termHashFile = new TermCountHashFile(backingStore);
		} catch (FileNotFoundException e) {
			throw new DatabaseException(Messages.getString("AffiliateList.0")+backingStoreFileMgr.getPath(), e); //$NON-NLS-1$
		}
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


	public void incrementTermCount(String term, boolean normalize) throws DatabaseException {

		String normalizedTerm = normalize(term, normalize);
		TermCountEntry entry;
		try {
			entry = termHashFile.getEntry(normalizedTerm);
			if (Objects.isNull(entry)) {
				entry = new TermCountEntry(normalizedTerm, 1);
				termHashFile.addEntry(entry);
			} else {
				entry.incrementCount();
			}
		} catch (IOException e) {
			throw new DatabaseException(e);
		}
	}

	public void setTermCount(String term, boolean normalize, int termCount) throws DatabaseException {

		String normalizedTerm = normalize(term, normalize);
		TermCountEntry entry;
		try {
			entry = termHashFile.getEntry(normalizedTerm);
			if (Objects.isNull(entry)) {
				entry = new TermCountEntry(normalizedTerm, termCount);
				termHashFile.addEntry(entry);
			} else {
				entry.setTermCount(termCount);
			}
		} catch (IOException e) {
			throw new DatabaseException(e);
		}
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
}

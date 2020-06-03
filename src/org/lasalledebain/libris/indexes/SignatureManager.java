package org.lasalledebain.libris.indexes;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.lasalledebain.libris.FileAccessManager;
import org.lasalledebain.libris.FileManager;
import org.lasalledebain.libris.GenericDatabase;
import org.lasalledebain.libris.LibrisConstants;
import org.lasalledebain.libris.exception.DatabaseError;
import org.lasalledebain.libris.exception.UserErrorException;
import org.lasalledebain.libris.util.StringUtils;

public class SignatureManager implements LibrisConstants {
	private GenericDatabase<?> database;
	private BloomFilterSectionEditor signatureEditors[];
	private int sigLevels;
	private FileAccessManager signatureFileManagers[];
	private RandomAccessFile sigQueryFiles[];

	public SignatureManager(GenericDatabase<?> database) {
		super();
		this.database = database;
		sigLevels = 0;
	}
	
	public SignatureFilteredIdList makeSignatureFilteredIdIterator(Collection<String> terms) throws UserErrorException, IOException {
		SignatureFilteredIdList mainKfri = null;
		for (int level = 0; level < sigLevels; ++level) {
			mainKfri = new SignatureFilteredIdList(sigQueryFiles[level], level, StringUtils.wordStreamToHashStream(StreamSupport.stream(terms.spliterator(), false)), mainKfri);
		}
		return mainKfri;
	}

	public void open(boolean readOnly) throws FileNotFoundException {
		sigLevels = database.getMetadata().getSignatureLevels();
		initializeFiles();
		initializeEditors(readOnly);
	}
	
	public void flush() {
		for (BloomFilterSectionEditor e: signatureEditors) {
			try {
				e.store();
			} catch (IOException e1) {
				throw new DatabaseError("Error writing Bloom filter file", e1);
			}
		}
	}
	
	public void close() {
		flush();
		for (FileAccessManager f: signatureFileManagers) {
			f.close();
		}
	}
	
	public void createFiles(IndexConfiguration config) throws FileNotFoundException {
		Optional<Integer> sigLevelsTemp = config.getSignatureLevels();
		if (sigLevelsTemp.isPresent()) {
			sigLevels = sigLevelsTemp.get();
		} else {
			sigLevels = calculateSignatureLevels(database.getLastRecordId());
		}
		initializeFiles();
		for (int i = 0; i < sigLevels; ++i) {
			FileAccessManager m = signatureFileManagers[i];
			try {
				m.createNewFile();
			} catch (IOException e) {
				throw new DatabaseError("Error creating "+m.getPath(), e);
			}		
		}
		initializeEditors(false);
	}

	private void initializeFiles() {
		signatureFileManagers = new FileAccessManager[sigLevels];
		FileManager fileMgr = database.getFileMgr();
		for (int sigLevel = 0; sigLevel < sigLevels; ++sigLevel) {
			final FileAccessManager sigFileMgr = fileMgr.getAuxiliaryFileMgr(SIGNATURE_FILENAME_ROOT+sigLevel);
			signatureFileManagers[sigLevel] = sigFileMgr;
		}
	}

	private void initializeEditors(boolean readOnly) throws FileNotFoundException {
		signatureEditors = new BloomFilterSectionEditor[sigLevels];
		sigQueryFiles = new RandomAccessFile[sigLevels];
		for (int sigLevel = 0; sigLevel < sigLevels; ++sigLevel) {
			RandomAccessFile sigFile = readOnly?
					signatureFileManagers[sigLevel].getReadOnlyRandomAccessFile() :
						signatureFileManagers[sigLevel].getReadWriteRandomAccessFile();
					sigQueryFiles[sigLevel] = sigFile;
					signatureEditors[sigLevel] = new BloomFilterSectionEditor(sigFile, sigLevel);
		}
	}

	public void addKeywords(final int rId, final Stream<String> keywords) throws IOException {
		for (BloomFilterSectionEditor b:signatureEditors) {
			b.switchTo(rId);
		}
		StringUtils.wordStreamToHashStreams(keywords).forEach(hashStream -> hashStream.forEach(hash -> {for (BloomFilterSectionEditor b:signatureEditors) b.addHash(hash);}));
	}

	public static int calculateSignatureLevels(int numRecords) {
		int levels = 1;
		int numSigs = levels * BloomFilterSection.BRANCH_FACTOR;
		int numTopLevelSignatures = numRecords / BloomFilterSection.BRANCH_FACTOR;
		while (numSigs < numTopLevelSignatures) {
			++levels;
			numSigs *= BloomFilterSection.BRANCH_FACTOR;
		}
		return levels;
	}

	public boolean isIndexed() {
		FileManager fileMgr = database.getFileMgr();
		return fileMgr.getAuxiliaryFileMgr(SIGNATURE_FILENAME_ROOT+0).exists();
	}

	public int getSigLevels() {
		return sigLevels;
	}

	public void switchTo(int rId) throws IOException {
		for (BloomFilterSectionEditor b:signatureEditors) {
			b.switchTo(rId);
		}
	}
}

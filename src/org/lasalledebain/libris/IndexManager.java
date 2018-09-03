package org.lasalledebain.libris;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import org.lasalledebain.libris.exception.DatabaseError;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.InternalError;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.index.IndexField;
import org.lasalledebain.libris.indexes.AffiliateList;
import org.lasalledebain.libris.indexes.BloomFilterSection;
import org.lasalledebain.libris.indexes.BloomFilterSectionEditor;
import org.lasalledebain.libris.indexes.KeyIntegerTuple;
import org.lasalledebain.libris.indexes.LibrisRecordsFileManager;
import org.lasalledebain.libris.indexes.RecordKeywords;
import org.lasalledebain.libris.indexes.SortedKeyIntegerBucket;
import org.lasalledebain.libris.indexes.SortedKeyValueBucketFactory;
import org.lasalledebain.libris.indexes.SortedKeyValueFileManager;
import org.lasalledebain.libris.records.Records;
import org.lasalledebain.libris.xmlUtils.LibrisXMLConstants;

public class IndexManager implements LibrisConstants {

	private LibrisFileManager fileMgr;

	private Boolean indexed = null;
	@SuppressWarnings("unused")
	private LibrisMetadata metadata;
	private LibrisDatabase db;
	private SortedKeyValueFileManager<KeyIntegerTuple> namedRecordIndex;
	private AffiliateList affList[];

	private ArrayList<FileAccessManager> namedRecsFileMgrs;

	private int numGroups;

	private LibrisRecordsFileManager recordsFile;

	static final int NAMED_RECORDS_INDEX_LEVELS = Integer.getInteger("org.lasalledebain.libris.namedrecsindexlevels",
			2);

	/**
	 * @param librisDatabase
	 *            database metadata
	 * @param metadata
	 *            database file manager
	 * @param fileMgr
	 *            set true to prevent updates
	 * @throws DatabaseException
	 */
	public IndexManager(LibrisDatabase librisDatabase, LibrisMetadata metadata, LibrisFileManager fileMgr)
			throws DatabaseException {
		db = librisDatabase;
		this.fileMgr = fileMgr;
		this.metadata = metadata;
	}

	public boolean isIndexed() {
		if ((null == indexed) || (!indexed)) {
			indexed = fileMgr.checkAuxFiles();
		}
		return indexed;
	}

	public void buildIndexes(Records recs) throws LibrisException {
		int numGroups = db.getSchema().getNumGroups();
		FileAccessManager childTempFiles[] = new FileAccessManager[numGroups];
		FileAccessManager affiliateTempFiles[] = new FileAccessManager[numGroups];
		DataOutputStream childOpStreams[] = new DataOutputStream[numGroups];
		DataOutputStream affiliateOpStreams[] = new DataOutputStream[numGroups];
		int numChildren[] = new int[numGroups];
		int numAffiliates[] = new int[numGroups];
		LibrisMetadata metadata = db.getMetadata();
		int sigLevels = BloomFilterSection.calculateSignatureLevels(metadata.getLastRecordId());
		metadata.setSignatureLevels(sigLevels);
		FileAccessManager signatureFileManagers[] = new FileAccessManager[sigLevels];
		BloomFilterSectionEditor signatureEditors[] = new BloomFilterSectionEditor[sigLevels];
		try {
			for (int g = 0; g < numGroups; ++g) {
				childTempFiles[g] = fileMgr.getAuxiliaryFileMgr(TEMP_CHILD_FILE + g);
				childTempFiles[g].createNewFile();
				childOpStreams[g] = new DataOutputStream(childTempFiles[g].getOpStream());
				affiliateTempFiles[g] = fileMgr.getAuxiliaryFileMgr(TEMP_AFFILIATE_FILE + g);
				affiliateOpStreams[g] = new DataOutputStream(affiliateTempFiles[g].getOpStream());
				childTempFiles[g].setDeleteOnExit();
				affiliateTempFiles[g].setDeleteOnExit();
			}
			for (int sigLevel = 0; sigLevel < sigLevels; ++sigLevel) {
				final FileAccessManager sigFileMgr = db.getFileMgr().getAuxiliaryFileMgr(SIGNATURE_FILENAME_ROOT+sigLevel);
				signatureFileManagers[sigLevel] = sigFileMgr;
				RandomAccessFile raf = sigFileMgr.getReadWriteRandomAccessFile();
				signatureEditors[sigLevel] = new BloomFilterSectionEditor(raf, sigLevel);
			}
			RecordKeywords keywordList = RecordKeywords.createRecordKeywords(true, false);
			final IndexField[] indexFields = db.getSchema().getIndexFields(LibrisXMLConstants.XML_INDEX_NAME_KEYWORDS);
			for (Record r : recs) {
				String name = r.getName();
				int id = r.getRecordId();
				if (null != name) {
					KeyIntegerTuple newTuple = new KeyIntegerTuple(name, id);
					namedRecordIndex.addElement(newTuple);
				}
				if (r.hasAffiliations()) {
					for (int g = 0; g < numGroups; ++g) {
						if (r.hasAffiliations(g)) {
							++numChildren[g];
							int[] affs = r.getAffiliates(g);
							childOpStreams[g].writeInt(affs[0]);
							childOpStreams[g].writeInt(id);
							for (int i = 1; i < affs.length; ++i) {
								affiliateOpStreams[g].writeInt(id);
								affiliateOpStreams[g].writeInt(affs[i]);
								++numAffiliates[g];
							}
						}
					}

				}
				final int rId = r.getRecordId();
				r.getKeywords(indexFields, keywordList);
				for (BloomFilterSectionEditor b:signatureEditors) {
					b.switchTo(rId);
					b.addTerms(keywordList.getKeywords());
				}
			}
			for (int i = 0; i < sigLevels; ++i) {
				signatureEditors[i].store();
				signatureFileManagers[i].close();
			}
			for (int g = 0; g < numGroups; ++g) {
				childTempFiles[g].close();
				affiliateTempFiles[g].close();
				FileAccessManager hashTableFileMgr = fileMgr.getAuxiliaryFileMgr(AFFILIATES_FILENAME_HASHTABLE_ROOT + g);
				FileAccessManager overflowFileMgr = fileMgr.getAuxiliaryFileMgr(AFFILIATES_FILENAME_OVERFLOW_ROOT + g);
				AffiliateList l = new AffiliateList(hashTableFileMgr, overflowFileMgr, false);
				l.setSize(numChildren[g] + numAffiliates[g]);
				buildAffiliateIndex(l, childTempFiles[g], hashTableFileMgr, overflowFileMgr, numChildren[g], true);
				buildAffiliateIndex(l, affiliateTempFiles[g], hashTableFileMgr, overflowFileMgr, numAffiliates[g], false);
			}
			setIndexed(true);
		} catch (IOException e) {
			throw new InputException("Error rebuilding indexes", e);
		}
	}

	private void buildAffiliateIndex(AffiliateList l, FileAccessManager affiliateTempFMgr, 
			FileAccessManager hashTableFileMgr, FileAccessManager overflowFileMgr, int numAffiliates, boolean isChildren)
					throws DatabaseException {
		try {
			DataInputStream affiliateStream = new DataInputStream(affiliateTempFMgr.getIpStream());
			final int CACHE_SIZE = 16384;
			LinkedHashMap<Integer, RecordAffiliates> cache = new LinkedHashMap<>(CACHE_SIZE);
			for (int i = 0; i < numAffiliates; ++i) {
				int parent = affiliateStream.readInt();
				int aff = affiliateStream.readInt();
				RecordAffiliates ra = cache.get(parent);
				if (null == ra) {
					ra = new RecordAffiliates(aff);
					if (cache.size() >= CACHE_SIZE) {
						Entry<Integer, RecordAffiliates> victim = cache.entrySet().iterator().next();
						Integer key = victim.getKey();
						cache.remove(key);
						RecordAffiliates value = victim.getValue();
						l.addAffiliates(key, Arrays.copyOf(value.affiliates, value.occupancy), isChildren);
					}
					cache.put(parent, ra);
				} else {
					if (ra.addAffiliate(aff)) {
						cache.remove(parent);
						l.addAffiliates(parent, Arrays.copyOf(ra.affiliates, ra.occupancy), isChildren);
					}
				}
			}
			affiliateTempFMgr.delete();
			for (Entry<Integer, RecordAffiliates> victim: cache.entrySet()) {
				RecordAffiliates value = victim.getValue();
				l.addAffiliates(victim.getKey(), Arrays.copyOf(value.affiliates, value.occupancy), isChildren);
			}
			l.flush();
		} catch (IOException e) {
			throw new InternalError("Error building index for group ", e);
		}
	}

	public void open() throws DatabaseException {
		try {
			namedRecsFileMgrs = new ArrayList<FileAccessManager>(1 + NAMED_RECORDS_INDEX_LEVELS);

			namedRecsFileMgrs.add(fileMgr.getAuxiliaryFileMgr(LibrisFileManager.NAMEDRECORDS_FILENAME_ROOT + "data"));
			for (int i = 0; i < IndexManager.NAMED_RECORDS_INDEX_LEVELS; ++i) {
				namedRecsFileMgrs.add(
						fileMgr.getAuxiliaryFileMgr(LibrisFileManager.NAMEDRECORDS_FILENAME_ROOT + "index" + (i + 1)));
			}
			SortedKeyValueBucketFactory<KeyIntegerTuple> bucketFactory = SortedKeyIntegerBucket.getFactory();
			namedRecordIndex = new SortedKeyValueFileManager<KeyIntegerTuple>(namedRecsFileMgrs, bucketFactory);
			numGroups = db.getSchema().getNumGroups();
			affList = new AffiliateList[numGroups];
			boolean readOnly = db.isReadOnly();
			for (int i = 0; i < numGroups; ++i) {
				affList[i] = new AffiliateList(fileMgr.getAuxiliaryFileMgr(AFFILIATES_FILENAME_HASHTABLE_ROOT + i),
						fileMgr.getAuxiliaryFileMgr(AFFILIATES_FILENAME_OVERFLOW_ROOT), readOnly);
			}
		} catch (InputException e) {
			throw new DatabaseException("error opening namedRecordIndex", e);
		}
	}

	public void close() throws InputException, DatabaseException, IOException {
		flush();
		for (FileAccessManager m : namedRecsFileMgrs) {
			m.close();
		}
		recordsFile.close();
	}

	public void flush() throws InputException, DatabaseException {
		namedRecordIndex.flush();
		for (AffiliateList aff: affList) {
			aff.flush();
		}
	}

	public void setIndexed(boolean indexed) {
		this.indexed = indexed;
	}

	public SortedKeyValueFileManager<KeyIntegerTuple> getNamedRecordIndex() {
		return namedRecordIndex;
	}

	public LibrisRecordsFileManager getRecordsFileMgr() throws LibrisException {
		if (null == recordsFile) {
			recordsFile = new LibrisRecordsFileManager(db, db.isReadOnly(), db.getSchema(), fileMgr);
		}
		return recordsFile;
	}

	public void addChild(int groupNum, int parent, int child) throws DatabaseException {
		affList[groupNum].addChild(parent, child);
	}

	public void addAffiliate(int groupNum, int dest, int src) throws DatabaseException {
		affList[groupNum].addAffiliate(dest, src);
	}

	public AffiliateList getAffiliateList(int groupNum) {
		if (groupNum >= affList.length) {
			throw new DatabaseError("cannot access group " + groupNum);
		}
		return affList[groupNum];
	}

	private final static class RecordAffiliates {
		int affiliates[];
		int occupancy;
		final static int LIST_SIZE = 8;

		public RecordAffiliates(int affiliate) {
			affiliates = new int[LIST_SIZE];
			affiliates[0] = affiliate;
			occupancy = 1;
		}
		
		boolean addAffiliate(int aff) {
			affiliates[occupancy] = aff;
			++occupancy;
			return (LIST_SIZE == occupancy);
		}
	}

}

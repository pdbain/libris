package org.lasalledebain.libris;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.stream.Stream;

import org.lasalledebain.libris.exception.DatabaseError;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.exception.UserErrorException;
import org.lasalledebain.libris.index.IndexField;
import org.lasalledebain.libris.indexes.AffiliateList;
import org.lasalledebain.libris.indexes.IndexConfiguration;
import org.lasalledebain.libris.indexes.KeyIntegerTuple;
import org.lasalledebain.libris.indexes.LibrisRecordsFileManager;
import org.lasalledebain.libris.indexes.RecordKeywords;
import org.lasalledebain.libris.indexes.RecordPositions;
import org.lasalledebain.libris.indexes.SignatureFilteredIdList;
import org.lasalledebain.libris.indexes.SignatureManager;
import org.lasalledebain.libris.indexes.SortedKeyIntegerBucket;
import org.lasalledebain.libris.indexes.SortedKeyValueBucketFactory;
import org.lasalledebain.libris.indexes.SortedKeyValueFileManager;
import org.lasalledebain.libris.indexes.TermCountIndex;
import org.lasalledebain.libris.records.Records;
import org.lasalledebain.libris.util.Reporter;
import org.lasalledebain.libris.xmlUtils.LibrisXMLConstants;

public class IndexManager<RecordType extends Record> implements LibrisConstants {

	private GenericDatabase<RecordType> database;
	private FileManager fileMgr;

	private Boolean indexed = null;
	private SortedKeyValueFileManager<KeyIntegerTuple> namedRecordIndex;
	private AffiliateList<RecordType> affList[];

	private RecordPositions myRecordPositions;
	private ArrayList<FileAccessManager> namedRecsFileMgrs;
	private int numGroups;
	private LibrisRecordsFileManager<RecordType> recordsFile;
	private TermCountIndex termCounts;
	static final int NAMED_RECORDS_INDEX_LEVELS = Integer.getInteger("org.lasalledebain.libris.namedrecsindexlevels", 2);
	private final SignatureManager sigMgr;
	public SignatureManager getSignatureFilter() {
		return sigMgr;
	}

	private final FileAccessManager termCountFileMgr;

	private IndexField[] indexFields = null;
	
	/**
	 * @param theDatabase
	 *            database metadata
	 * @param metadata
	 *            database file manager
	 * @param fileMgr
	 *            set true to prevent updates
	 * @throws DatabaseException
	 */
	public IndexManager(GenericDatabase<RecordType> theDatabase, FileManager fileMgr)
			throws DatabaseException {
		database = theDatabase;
		this.fileMgr = fileMgr;

		namedRecsFileMgrs = new ArrayList<FileAccessManager>(1 + NAMED_RECORDS_INDEX_LEVELS);
		FileAccessManager auxFileMgr = fileMgr.getAuxiliaryFileMgr(LibrisConstants.NAMEDRECORDS_FILENAME_ROOT + "data");
		namedRecsFileMgrs.add(auxFileMgr);
		for (int i = 0; i < IndexManager.NAMED_RECORDS_INDEX_LEVELS; ++i) {
			namedRecsFileMgrs.add(
					fileMgr.getAuxiliaryFileMgr(LibrisConstants.NAMEDRECORDS_FILENAME_ROOT + "index" + (i + 1)));
		}

		sigMgr = new SignatureManager(database);

		termCountFileMgr = fileMgr.getAuxiliaryFileMgr(TERM_COUNT_FILENAME_ROOT);
	}

	public boolean isIndexed() {
		if ((null == indexed) || (!indexed)) {
			indexed = fileMgr.checkAuxFiles() &&
					sigMgr.isIndexed();
		}
		return indexed;
	}

	public void buildIndexes(IndexConfiguration config, Records<RecordType> recs) throws LibrisException {
		int numGroups = database.getSchema().getNumGroups();
		FileAccessManager childTempFiles[] = new FileAccessManager[numGroups];
		FileAccessManager affiliateTempFiles[] = new FileAccessManager[numGroups];
		DataOutputStream childOpStreams[] = new DataOutputStream[numGroups];
		DataOutputStream affiliateOpStreams[] = new DataOutputStream[numGroups];
		int numChildren[] = new int[numGroups];
		int numAffiliates[] = new int[numGroups];
		try {
			openInternal(false);
			sigMgr.createFiles(config);
			for (int g = 0; g < numGroups; ++g) {
				childTempFiles[g] = fileMgr.getAuxiliaryFileMgr(TEMP_CHILD_FILE + g);
				childTempFiles[g].createNewFile();
				childOpStreams[g] = new DataOutputStream(childTempFiles[g].getOpStream());
				affiliateTempFiles[g] = fileMgr.getAuxiliaryFileMgr(TEMP_AFFILIATE_FILE + g);
				affiliateOpStreams[g] = new DataOutputStream(affiliateTempFiles[g].getOpStream());
				childTempFiles[g].setDeleteOnExit();
				affiliateTempFiles[g].setDeleteOnExit();
			}

			termCounts = new TermCountIndex(termCountFileMgr, config.getTermcountBuckets(), false);

			RecordKeywords keywordList = RecordKeywords.createRecordKeywords(true, false);
			IndexField[] ixFields = indexFields;
			long keywordCount = 0;
			long recordCount = 0;
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
				++recordCount;
				keywordList.clear();
				r.getKeywords(ixFields, keywordList);
				keywordCount += keywordList.estimateSize();
				addKeywords(id, keywordList.wordStream());
				keywordList.wordStream().forEach(t -> termCounts.incrementTermCount(t));
			}
			config.getIndexingReporter().reportValue(Reporter.INDEXING_RECORDS_NUM_RECS, recordCount);
			config.getIndexingReporter().reportValue(Reporter.INDEXING_KEYWORD_COUNT, keywordCount);
			termCounts.generateReport(config.getIndexingReporter());

			for (int g = 0; g < numGroups; ++g) {
				childTempFiles[g].close();
				affiliateTempFiles[g].close();
				FileAccessManager hashTableFileMgr = fileMgr.getAuxiliaryFileMgr(AFFILIATES_FILENAME_HASHTABLE_ROOT + g);
				FileAccessManager overflowFileMgr = fileMgr.getAuxiliaryFileMgr(AFFILIATES_FILENAME_OVERFLOW_ROOT + g);
				AffiliateList<RecordType> l = new AffiliateList<RecordType>(hashTableFileMgr, overflowFileMgr, false);
				l.setSize(numChildren[g] + numAffiliates[g]);
				buildAffiliateIndex(l, childTempFiles[g], hashTableFileMgr, overflowFileMgr, numChildren[g], true);
				buildAffiliateIndex(l, affiliateTempFiles[g], hashTableFileMgr, overflowFileMgr, numAffiliates[g], false);
			}
			database.getMetadata().setSignatureLevels(sigMgr.getSigLevels());

			setIndexed(true);
			try (FileOutputStream reportFile = fileMgr.getUnmanagedOutputFile(INDEXING_REPORT_FILE)) {
				config.getIndexingReporter().writeReport(reportFile);
			};
		} catch (IOException e) {
			throw new InputException("Error rebuilding indexes", e);
		}
	}

	void addKeywords(final int rId, final Stream<String> keyWords) throws IOException {
		sigMgr.addKeywords(rId, keyWords);
	}

	public void addRecordKeywords(Record rec) {
		try {
			int rId = rec.getRecordId();
			sigMgr.switchTo(rId);
			RecordKeywords keywordList = RecordKeywords.createRecordKeywords(true, false);
			rec.getKeywords(indexFields, keywordList);
			addKeywords(rId, keywordList.wordStream());
			sigMgr.flush();
		} catch (InputException | IOException e) {
			throw new DatabaseError("Error adding record "+rec.getRecordId()+" to keyword index", e);
		}
	}

	void addNamedRecord(int id, String recordName) throws InputException, UserErrorException {
		if ((null != recordName) && !recordName.isEmpty()) {
			KeyIntegerTuple query = namedRecordIndex.getByName(recordName);
			if (null != query) {
				if (query.getValue() != id) {
					throw new UserErrorException("Duplicate record name "+recordName);
				}
			} else {
				namedRecordIndex.addElement(new KeyIntegerTuple(recordName, id));
			}
		}
	}

	 private void buildAffiliateIndex(AffiliateList<RecordType> l, FileAccessManager affiliateTempFMgr, 
			FileAccessManager hashTableFileMgr, FileAccessManager overflowFileMgr, int numAffiliates, boolean isChildren) throws DatabaseException {
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
			throw new DatabaseException("Error building index for group ", e);
		}
	}
	
	public SignatureFilteredIdList makeSignatureFilteredIdIterator(Collection<String> terms) throws UserErrorException, IOException {
		return sigMgr.makeSignatureFilteredIdIterator(terms);
	}

	public void open(boolean readOnly) throws DatabaseException {
		try {
			openInternal(readOnly);
			sigMgr.open(readOnly);
		} catch (InputException | IOException e) {
			throw new DatabaseException("error opening namedRecordIndex", e);
		}
	}

	private void openInternal(boolean readOnly) throws InputException, DatabaseException, IOException {
		SortedKeyValueBucketFactory<KeyIntegerTuple> bucketFactory = SortedKeyIntegerBucket.getFactory();
		namedRecordIndex = new SortedKeyValueFileManager<KeyIntegerTuple>(namedRecsFileMgrs, bucketFactory);
		numGroups = database.getSchema().getNumGroups();
		affList = new AffiliateList[numGroups];
		for (int i = 0; i < numGroups; ++i) {
			affList[i] = new AffiliateList<RecordType>(fileMgr.getAuxiliaryFileMgr(AFFILIATES_FILENAME_HASHTABLE_ROOT + i),
					fileMgr.getAuxiliaryFileMgr(AFFILIATES_FILENAME_OVERFLOW_ROOT), readOnly);
		}
		termCounts = new TermCountIndex(fileMgr.getAuxiliaryFileMgr(TERM_COUNT_FILENAME_ROOT), false);
		indexFields = database.getSchema().getIndexFields(LibrisXMLConstants.XML_INDEX_NAME_KEYWORDS);
	}

	public void close() throws InputException, DatabaseException, IOException {
		flush();
		sigMgr.close();
		for (FileAccessManager m : namedRecsFileMgrs) {
			m.close();
		}
		recordsFile.close();
	}

	public void flush() throws InputException, DatabaseException {
		namedRecordIndex.flush();
		termCounts.flush();
		for (AffiliateList<RecordType> aff: affList) {
			aff.flush();
		}
		sigMgr.flush();
	}

	public void setIndexed(boolean indexed) {
		this.indexed = indexed;
	}

	public SortedKeyValueFileManager<KeyIntegerTuple> getNamedRecordIndex() {
		return namedRecordIndex;
	}

	public LibrisRecordsFileManager<RecordType> getRecordsFileMgr() throws LibrisException {
		if (null == recordsFile) {
			recordsFile = new LibrisRecordsFileManager<RecordType>(database, 
					database.readOnly, 
					database.getSchema(), 
					fileMgr.getAuxiliaryFileMgr(LibrisConstants.RECORDS_FILENAME),
					database.getRecordPositions()
					);
		}
		return recordsFile;
	}

	public int getTermCount(String term) {
		return termCounts.getTermCount(term);
	}

	public void setTermCount(String term, boolean normalize, int termCount) throws DatabaseException {
		termCounts.setTermCount(term, termCount);
	}

	public void incrementTermCount(String term) {
		termCounts.incrementTermCount(term);
	}

	public void addChild(int groupNum, int parent, int child) throws DatabaseException {
		affList[groupNum].addChild(parent, child);
	}

	public void addAffiliate(int groupNum, int dest, int src) throws DatabaseException {
		affList[groupNum].addAffiliate(dest, src);
	}

	public AffiliateList<RecordType> getAffiliateList(int groupNum) {
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

	public synchronized RecordPositions getRecordPositions() throws DatabaseException {
		if (null == myRecordPositions) {
			FileAccessManager positionsFileManager = fileMgr.getAuxiliaryFileMgr(LibrisConstants.POSITION_FILENAME);
			myRecordPositions = new RecordPositions(positionsFileManager, database.readOnly);
		}
		return myRecordPositions;
	}

}

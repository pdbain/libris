package org.lasalledebain.libris;

import java.util.ArrayList;

import org.lasalledebain.libris.exception.DatabaseError;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.indexes.AffiliateList;
import org.lasalledebain.libris.indexes.KeyIntegerTuple;
import org.lasalledebain.libris.indexes.SortedKeyIntegerBucket;
import org.lasalledebain.libris.indexes.SortedKeyValueBucketFactory;
import org.lasalledebain.libris.indexes.SortedKeyValueFileManager;
import org.lasalledebain.libris.records.Records;

public class IndexManager implements LibrisConstants {

	private LibrisFileManager fileMgr;

	private Boolean indexed=null;
	@SuppressWarnings("unused")
	private LibrisMetadata metadata;
	private LibrisDatabase db;
	private SortedKeyValueFileManager<KeyIntegerTuple> namedRecordIndex;
	private AffiliateList affList[];

	private ArrayList<FileAccessManager> namedRecsFileMgrs;

	private int numGroups;

	static final int NAMED_RECORDS_INDEX_LEVELS = Integer.getInteger("org.lasalledebain.libris.namedrecsindexlevels", 2);

	/**
	 * @param librisDatabase database metadata
	 * @param metadata database file manager
	 * @param fileMgr set true to prevent updates
	 * @throws DatabaseException
	 */
	public IndexManager(LibrisDatabase librisDatabase, LibrisMetadata metadata, LibrisFileManager fileMgr) throws DatabaseException {
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
		for (Record r: recs) {
			String name = r.getName();
			if (null != name) {
				int id = r.getRecordId();
				KeyIntegerTuple newTuple = new KeyIntegerTuple(name, id);
				namedRecordIndex.addElement(newTuple);
			}
		}
		setIndexed(true);
	}

	public void open() throws DatabaseException {
		try {
			namedRecsFileMgrs = new ArrayList<FileAccessManager>(1+NAMED_RECORDS_INDEX_LEVELS);

			namedRecsFileMgrs.add(fileMgr.getAuxiliaryFileMgr(LibrisFileManager.NAMEDRECORDS_FILENAME_ROOT+"data"));
			for (int i = 0; i < IndexManager.NAMED_RECORDS_INDEX_LEVELS; ++i) {
				namedRecsFileMgrs.add(fileMgr.getAuxiliaryFileMgr(LibrisFileManager.NAMEDRECORDS_FILENAME_ROOT+"index"+(i+1)));
			}
			SortedKeyValueBucketFactory<KeyIntegerTuple> bucketFactory = SortedKeyIntegerBucket.getFactory();
			namedRecordIndex = new SortedKeyValueFileManager<KeyIntegerTuple>(namedRecsFileMgrs, 
					bucketFactory);
			numGroups = db.getSchema().getNumGroups();
			affList = new AffiliateList[numGroups];
			boolean readOnly = db.isReadOnly();
			for (int i = 0; i < numGroups; ++i) {
				affList[i] = new AffiliateList(fileMgr.getAuxiliaryFileMgr(AFFILIATES_FILENAME_HASHTABLE_ROOT+i), 
						fileMgr.getAuxiliaryFileMgr(AFFILIATES_FILENAME_OVERFLOW_ROOT), readOnly);
			}
		} catch (InputException e) {
			throw new DatabaseException("error opening namedRecordIndex", e);
		}
	}

	public void close() throws InputException {
		flush();
		for (FileAccessManager m: namedRecsFileMgrs) {
			m.close();
		}
	}

	public void flush() throws InputException {
		namedRecordIndex.flush();
	}
	public void setIndexed(boolean indexed) {
		this.indexed = indexed;
	}

	public SortedKeyValueFileManager<KeyIntegerTuple> getNamedRecordIndex() {
		return namedRecordIndex;
	}

	public AffiliateList getAffiliateList(int groupNum) {
		if (groupNum >= affList.length) {
			throw new DatabaseError("cannot access group "+groupNum);
		}
		return affList[groupNum];
	}

}

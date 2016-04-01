package org.lasalledebain.libris;

import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.records.Records;

public class IndexManager {

	private LibrisFileManager fileMgr;

	private Boolean indexed=null;
	@SuppressWarnings("unused")
	private LibrisMetadata metadata;
	private RecordId lastId;
	private LibrisDatabase db;

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
		setIndexed(true);
	}

	public void setIndexed(boolean indexed) {
		this.indexed = indexed;
	}

	public synchronized void setLastRecordId(RecordId lastId) {
		this.lastId = lastId;
	}

}

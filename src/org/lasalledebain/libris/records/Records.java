package org.lasalledebain.libris.records;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import org.lasalledebain.libris.LibrisConstants;
import org.lasalledebain.libris.LibrisDatabase;
import org.lasalledebain.libris.LibrisFileManager;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.RecordList;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.indexes.LibrisRecordsFileManager;

public class Records implements Iterable<Record>{

	private LibrisRecordsFileManager recMgr;
	LibrisDatabase db;

	public Records(LibrisDatabase db, LibrisFileManager fileMgr) throws LibrisException {
		recMgr = db.getRecordsFileMgr();
		this.db = db;
	}

	public Iterable<Record> getNativeRecordsReader() {
		return recMgr;
	}

	public void importRecords(LibrisConstants.DatabaseFormat format, File[] importFiles) throws DatabaseException {
		throw new DatabaseException("not implemented");
	}

	public void putRecords(RecordList modifiedRecords) throws InputException, DatabaseException {
		for (Record r: modifiedRecords) {
			recMgr.putRecord(r);
		}
	}

	public void putRecord(Record rec) throws InputException, DatabaseException {
			recMgr.putRecord(rec);
	}

	public void flush() throws IOException, DatabaseException {
		recMgr.flush();
	}

	public Iterator<Record> iterator() {
		return recMgr.iterator();
	}

	public Iterator<Record> iterator(String prefix) {
		return db.getNamedRecords().iterator();
	}
}

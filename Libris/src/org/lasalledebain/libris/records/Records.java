package org.lasalledebain.libris.records;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import org.lasalledebain.libris.LibrisDatabase;
import org.lasalledebain.libris.LibrisFileManager;
import org.lasalledebain.libris.ModifiedRecordList;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.indexes.LibrisRecordsFileManager;

public class Records implements Iterable<Record>{

	private LibrisRecordsFileManager recMgr;

	public Records(LibrisDatabase db, LibrisFileManager fileMgr) throws LibrisException {
		recMgr = new LibrisRecordsFileManager(db, db.isReadOnly(), db.getSchema(), fileMgr);
	}

	public RecordsReader getNativeRecordsReader() {
		return recMgr;
	}

	public void importRecords(RecordsReader.DatabaseFormat format, File[] importFiles) throws DatabaseException {
		throw new DatabaseException("not implemented");
	}

	public void putRecords(ModifiedRecordList modifiedRecords) throws InputException, DatabaseException {
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

}

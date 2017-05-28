package org.lasalledebain.libris.records;

import org.lasalledebain.libris.Record;

public interface RecordsReader extends Iterable<Record> {
	public enum DatabaseFormat {DBFMT_XML, DBFMT_CSV, DBFMT_NATIVE};

}

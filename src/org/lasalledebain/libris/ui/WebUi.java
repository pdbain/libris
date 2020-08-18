package org.lasalledebain.libris.ui;

import org.lasalledebain.libris.NamedRecordList;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.LibrisException;

public class WebUi<RecordType extends Record> extends LibrisUi<RecordType> {

	@Override
	public void put(RecordType newRecord) throws DatabaseException {
		return; /* not implemented */
	}

	@Override
	public void setRecordName(NamedRecordList<RecordType> namedRecs) throws InputException {
		return; /* not implemented */
	}

	@Override
	public void setRecordArtifact() {
		return; /* not implemented */
	}


	@Override
	public String SelectSchemaFile(String schemaName) throws DatabaseException {
		return null; /* not implemented */
	}

	@Override
	public int confirm(String msg) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int confirmWithCancel(String msg) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected boolean checkAndCloseDatabase(boolean force) throws DatabaseException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void alert(String msg, Exception e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void alert(String msg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void displayRecord(int recordId) throws LibrisException {
		// TODO Auto-generated method stub
		
	}

}

package org.lasalledebain.libris.ui;

import org.lasalledebain.libris.DatabaseRecord;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.LibrisException;

public class WebUi extends AbstractUi<DatabaseRecord> {

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
	public boolean checkAndCloseDatabase(boolean force) throws DatabaseException {
		// TODO write checkAndCloseDatabase
		return false;
	}

	@Override
	public void alert(String msg, Throwable e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void alert(String msg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public DatabaseRecord displayRecord(int recordId) throws LibrisException {
		return null;
		// TODO Auto-generated method stub
		
	}


	@Override
	public WebUi getMainUi() {
		return this;
	}

}

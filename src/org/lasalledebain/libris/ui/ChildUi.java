package org.lasalledebain.libris.ui;

import java.io.File;

import org.lasalledebain.libris.DatabaseRecord;
import org.lasalledebain.libris.NamedRecordList;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.InputException;

public class ChildUi extends LibrisUiGeneric {
	LibrisUi parent;

	public ChildUi(File dbFile, boolean readOnly, LibrisUi parent) {
		super(dbFile, readOnly);
		this.parent = parent;
	}

	@Override
	public String promptAndReadReply(String prompt) throws DatabaseException {
		return parent.promptAndReadReply(prompt);
	}

	@Override
	public void put(Record newRecord) throws DatabaseException {
		return;
	}

	@Override
	public void setRecordName(NamedRecordList<DatabaseRecord> namedRecs) throws InputException {
		throw new InternalError(getClass().getName()+".setRecordArtifact() not implemented");
	}

	@Override
	public void setRecordArtifact() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int confirm(String msg) {
		return parent.confirm(msg);
	}

	@Override
	public int confirmWithCancel(String msg) {
		return parent.confirmWithCancel(msg);
	}

	@Override
	protected boolean checkAndCloseDatabase(boolean force) throws DatabaseException {
		return parent.closeDatabase(force);
	}

	@Override
	public String SelectSchemaFile(String schemaName) throws DatabaseException {
		return parent.SelectSchemaFile(schemaName);
	}

	@Override
	public void alert(String msg, Exception e) {
		parent.alert(msg, e);
	}

	@Override
	public void alert(String msg) {
		parent.alert(msg);
	}

}

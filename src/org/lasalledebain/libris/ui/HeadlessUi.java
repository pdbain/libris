package org.lasalledebain.libris.ui;

import static org.lasalledebain.libris.LibrisDatabase.librisLogger;

import java.awt.Dimension;
import java.io.File;
import java.util.logging.Level;

import org.lasalledebain.libris.NamedRecordList;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.RecordId;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.LibrisException;

public class HeadlessUi extends LibrisWindowedUi implements LibrisUi {
	private String schemaPath;
	private int confirmValue;
	private boolean accessible; 

	public HeadlessUi() {
		super();
	}

	public HeadlessUi(File dbFile) throws LibrisException {
		super(dbFile);
	}

	// TODO write headless UI
	// use logging interface for messages, store results
	@Override
	public String SelectSchemaFile(String schemaName) throws DatabaseException {
		return schemaPath;
	}

	public void setSchemaPath(String schemaPath) {
		this.schemaPath = schemaPath;
	}

	@Override
	public void alert(String msg, Exception e) {
		librisLogger.log(Level.WARNING, msg, e);
	}

	@Override
	public void alert(String msg) {
		librisLogger.log(Level.WARNING, msg);
	}

	@Override
	public int confirm(String msg) {
		return confirmValue;
	}

	@Override
	public int confirmWithCancel(String msg) {
		return confirmValue;
	}

	@Override
	public void displayRecord(int recordId) throws LibrisException {
		librisLogger.log(Level.FINE, RecordId.toString(recordId));
	}

	@Override
	public String promptAndReadReply(String prompt) throws DatabaseException {
		return null;
	}

	@Override
	public void recordsAccessible(boolean accessible) {
		this.accessible = accessible;
	}

	public void setReadOnly(boolean readOnly) {
		return;
	}

	public boolean isReadOnly() {
		return false;
	}

	public int getConfirmValue() {
		return confirmValue;
	}

	public void setConfirmValue(int confirmValue) {
		this.confirmValue = confirmValue;
	}

	public boolean isAccessible() {
		return accessible;
	}

	public void setAccessible(boolean accessible) {
		this.accessible = accessible;
	}

	@Override
	public void put(Record newRecord) throws DatabaseException {
		librisLogger.log(Level.FINE, newRecord.toString());
	}

	public void repaint() {
	}

	@Override
	public void setRecordName(NamedRecordList namedRecs) throws InputException {
		// TODO write setRecordName
		throw new InternalError(getClass().getName()+".setRecordName() not implemented");
	}

	@Override
	public boolean quit(boolean force) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void closeWindow(boolean allWindows) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void destroyWindow(boolean b) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Record newChildRecord(Record currentRecord, int groupNum) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Dimension getDisplayPanelSize() {
		// TODO Auto-generated method stub
		return null;
	}

}

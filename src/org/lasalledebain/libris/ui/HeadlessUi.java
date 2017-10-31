package org.lasalledebain.libris.ui;

import java.awt.Dimension;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

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

	public Logger getuiLogger() {
		return uiLogger;
	}

	public void setuiLogger(Logger uiLogger) {
		this.uiLogger = uiLogger;
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
		uiLogger.log(Level.WARNING, msg, e);
	}

	@Override
	public void alert(String msg) {
		uiLogger.log(Level.WARNING, msg);
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
		uiLogger.log(Level.FINE, RecordId.toString(recordId));
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
		uiLogger.log(Level.FINE, newRecord.toString());
	}

	public void repaint() {
	}

	@Override
	public void exit() {
		// TODO write headlessUI.exit()
		
	}
	@Override
	public void setRecordName(NamedRecordList namedRecs) throws InputException {
		// TODO write setRecordName
		throw new InternalError(getClass().getName()+".setRecordName() not implemented");
	}

	@Override
	public void close(boolean allWindows, boolean closeGui) {
		return;
	}

	@Override
	public Record newChildRecord(Record currentRecord, int groupNum) {
		return null;
	}

	@Override
	public Dimension getDisplayPanelSize() {
		return null;
	}
}

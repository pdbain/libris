package org.lasalledebain.libris.ui;

import java.awt.Dimension;
import java.io.File;
import java.util.logging.Level;

import org.lasalledebain.libris.NamedRecordList;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.RecordId;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.LibrisException;

import static org.lasalledebain.libris.LibrisDatabase.log;

public class HeadlessUi<RecordType extends Record> extends LibrisWindowedUi<RecordType> implements DatabaseUi<RecordType> {
	private String schemaPath;
	private int confirmValue;
	private boolean accessible; 

	public HeadlessUi(boolean readOnly) {
		super(readOnly);
	}

	public HeadlessUi(File theDatabaseFile, boolean readOnly) {
		super(false);
		setDatabaseFile(theDatabaseFile);
	}

	public HeadlessUi() {
		this(false);
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
		log(Level.SEVERE, msg, e);
	}

	@Override
	public void alert(String msg) {
		log(Level.WARNING, msg);
		System.err.println("Alert: "+msg);
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
		log(Level.FINE, RecordId.toString(recordId));
	}

	@Override
	public String promptAndReadReply(String prompt) throws DatabaseException {
		return null;
	}

	@Override
	public void recordsAccessible(boolean accessible) {
		this.accessible = accessible;
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
	log(Level.FINE, newRecord.toString());
	}

	public void repaint() {
	}

	@Override
	public void setRecordName(NamedRecordList namedRecs) throws InputException {
		throw new InternalError(getClass().getName()+".setRecordName() not implemented");
	}

	@Override
	public void setRecordArtifact() {
		throw new InternalError(getClass().getName()+".setRecordArtifact() not implemented");
	}

	@Override
	public boolean closeWindow(boolean allWindows) {
		return true;
	}

	@Override
	protected void destroyWindow(boolean b) {
		return;
		}

	@Override
	public Record newChildRecord(Record currentRecord, int groupNum) {
		throw new InternalError(getClass().getName()+".newChildRecord() not implemented");
	}

	@Override
	public Dimension getDisplayPanelSize() {
		throw new InternalError(getClass().getName()+".getDisplayPanelSize() not implemented");
	}

	public File getDatabaseFile() {
		return null;
	}

}

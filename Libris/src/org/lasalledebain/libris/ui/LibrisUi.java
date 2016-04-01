package org.lasalledebain.libris.ui;

import java.util.logging.Logger;

import org.lasalledebain.libris.LibrisDatabase;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.RecordId;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.LibrisException;

public interface LibrisUi {

	public LibrisDatabase openDatabase(LibrisParameters params);
	public LibrisDatabase openDatabase();
	public LibrisDatabase getDatabase();
	public void databaseOpened(LibrisDatabase librisDatabase) throws DatabaseException, DatabaseException;

	public void databaseClosed();

	public void close(boolean allWindows, boolean closeGui);
	public void exit();

	public void setTitle(String title);
	public void indicateModified(boolean isModified);
	public void setEditable(boolean editable) throws LibrisException;

	public abstract void displayRecord(RecordId recordId) throws LibrisException;

	public abstract void alert(String msg, Exception e);

	public abstract void alert(String msg);

	/**
	 * Print a message and get a yes/no/cancel response.
	 * Possible responses are:
	 * YES_OPTION
	 * NO_OPTION
	 * CANCEL_OPTION
	 * OK_OPTION
	 * CLOSED_OPTION
	 * @param msg message to print
	 * @return user response
	 */
	public abstract int confirm(String msg);
	public abstract int confirmWithCancel(String msg);

	public abstract String SelectSchemaFile(String schemaName) throws DatabaseException;

	public String promptAndReadReply(String prompt) throws DatabaseException;

	public abstract void pasteToField();

	void recordsAccessible(boolean accessible);

	public boolean isDatabaseSelected();
	
	public boolean isReadOnly();

	public void setReadOnly(boolean readOnly);

	public Logger getuiLogger();

	public abstract void put(Record newRecord) throws DatabaseException;

	public void addRecord(Record newRecord) throws DatabaseException;

	/**
	 * Create a new, empty, value for a field
	 */
	public abstract void newFieldValue();

	public abstract void removeFieldValue();

	public abstract void fieldSelected(boolean b);

	UiField getSelectedField();

	void setSelectedField(UiField selectedField);

	public abstract void arrangeValues();

	public void repaint();
	Record newRecord();
}

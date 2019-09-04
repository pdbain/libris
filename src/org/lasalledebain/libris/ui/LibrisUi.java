package org.lasalledebain.libris.ui;

import java.io.File;

import org.lasalledebain.libris.DatabaseRecord;
import org.lasalledebain.libris.GenericDatabase;
import org.lasalledebain.libris.LibrisDatabase;
import org.lasalledebain.libris.NamedRecordList;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.XmlSchema;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.indexes.LibrisIndexConfiguration;

public interface LibrisUi {

	public void setSchema(XmlSchema mySchema);
	public LibrisDatabase openDatabase() throws DatabaseException;
	public void saveDatabase();
	public boolean closeDatabase(boolean force) throws DatabaseException;
	public boolean quit(boolean force) throws DatabaseException;

	public void rebuildDatabase() throws LibrisException;
	public void rebuildDatabase(LibrisIndexConfiguration config) throws LibrisException;
	public boolean isDatabaseSelected();
	public boolean isDatabaseOpen();
	public boolean isDatabaseReadOnly();
	public GenericDatabase<DatabaseRecord> getDatabase();
	void setDatabaseFile(File dbFile);
	public abstract String SelectSchemaFile(String schemaName) throws DatabaseException;
	public void setTitle(String title);

	public abstract void pasteToField();

	void recordsAccessible(boolean accessible);

	Record newRecord();
	public abstract void displayRecord(int recordId) throws LibrisException;
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

	public void setRecordName(NamedRecordList<DatabaseRecord> namedRecs) throws InputException;
	
	public abstract void arrangeValues();

	public void repaint();
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
	public String promptAndReadReply(String prompt) throws DatabaseException;
}

package org.lasalledebain.libris.ui;

import java.io.File;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import org.lasalledebain.libris.DatabaseAttributes;
import org.lasalledebain.libris.Libris;
import org.lasalledebain.libris.LibrisConstants;
import org.lasalledebain.libris.LibrisDatabase;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.InternalError;
import org.lasalledebain.libris.exception.LibrisException;

public abstract class LibrisUiGeneric implements LibrisUi, LibrisConstants {
	
	@Override
	public Record newRecord() {
		return null;
	}
	@Override
	public LibrisDatabase getDatabase() {
		return currentDatabase;
	}


	private static final String NO_DATABASE_OPENED = "No database opened";

	public void setTitle(String title) {
		uiTitle = title;
	}
	@Override
	public void indicateModified(boolean isModified) {
		return;
	}

	@Override
	public void arrangeValues() {
		throw new InternalError("LibrisUiGeneric.arrangeValues unimplemented");
	}

	@Override
	public void addRecord(Record newRecord) throws DatabaseException {
		throw new InternalError("LibrisUiGeneric.addRecord unimplemented");
	}

	/**
	 * 
	 */
	protected static Preferences librisPrefs;
	protected static Object prefsSync = new Object();
	protected Logger uiLogger;
	boolean readOnly;
	private UiField selectedField;
	protected String uiTitle;
	protected LibrisDatabase currentDatabase;
	private File databaseFile;
	private File auxDirectory;

	public UiField getSelectedField() {
		return selectedField;
	}

	public LibrisUiGeneric(File dbFile, File auxDir,
			boolean readOnly) throws LibrisException {
		this();
		databaseFile = dbFile;
		auxDirectory = auxDir;
	}

	public LibrisUiGeneric() {
		uiLogger = Logger.getLogger(LibrisUi.class.getName());
		LibrisUiGeneric.setLoggingLevel(uiLogger);
		readOnly = false;
		fieldSelected(false);
		setSelectedField(null);
	}

	@Override
	public abstract String SelectSchemaFile(String schemaName) throws DatabaseException;

	@Override
	public abstract void alert(String msg, Exception e);

	@Override
	public abstract void alert(String msg);

	public void fatalError(Exception e, String msg) {
		alert(msg+e.getMessage());
		e.printStackTrace();
		System.exit(1);
	}

	public LibrisDatabase openDatabase() {
		if (null != currentDatabase) {
			alert("Cannot open "+databaseFile.getAbsolutePath()+" because "+currentDatabase.getDatabaseFile().getAbsolutePath()+" is open");
		}
		try {
			currentDatabase = new LibrisDatabase(databaseFile, auxDirectory, this, readOnly);
			if (!currentDatabase.isIndexed()) {
				alert("database "+databaseFile.getAbsolutePath()+" is not indexed.  Please re-index.");
				return null;
			}
			currentDatabase.open();
		} catch (Exception e) {
			alert("Error opening database", e);
			return currentDatabase;
		}
		getLibrisPrefs().put(LibrisDatabase.DATABASE_FILE, databaseFile.getAbsolutePath());
		return currentDatabase;
	}

	public void rebuildDatabase() throws LibrisException {
			LibrisDatabase indexDb = Libris.buildAndOpenDatabase(databaseFile);
			indexDb.close();
	}

	@Override
	public void databaseClosed() {
		currentDatabase = null;
		setTitle(NO_DATABASE_OPENED);
	}

	@Override
	public void displayRecord(int recordId) throws LibrisException {
		// TODO Auto-generated method stub

	}
	@Override
	public void pasteToField() {
		// TODO Auto-generated method stub

	}

	@Override
	public String promptAndReadReply(String prompt) throws DatabaseException {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isDatabaseSelected() {
		return (null != databaseFile);
	}

	 public void setAuxiliaryDirectory(File auxDir) {
		 auxDirectory = auxDir;
	}
	 public void setDatabaseFile(File dbFile) {
		 databaseFile = dbFile;
	}

	@Override
	public void recordsAccessible(boolean accessible) {
	}

	@Override
	public boolean isReadOnly() {
		return readOnly;
	}

	@Override
	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}

	@Override
	public void setEditable(boolean editable) throws LibrisException {
		// TODO Auto-generated method stub

	}

	@Override
	public void databaseOpened(LibrisDatabase db) throws DatabaseException {
		currentDatabase = db;
		String title = NO_DATABASE_OPENED;
		if (null != db) {
			DatabaseAttributes databaseAttributes = db.getAttributes();
			title = databaseAttributes.getDatabaseName();
		}
		setTitle(title);
	}

	public Logger getuiLogger() {
		return uiLogger;
	}

	public static Preferences getLibrisPrefs() {
		synchronized (prefsSync) {
			if (null == librisPrefs) {
				librisPrefs = Preferences.userRoot();
			}

		}
		return librisPrefs;
	}

	public Logger getUiLogger() {
		return uiLogger;
	}

	@Override
	public void newFieldValue() {
		return;
	}

	@Override
	public void removeFieldValue() {
		throw new InternalError("removeField not implemented");
	}

	@Override
	public void fieldSelected(boolean b) {
		return;
	}

	public void setSelectedField(UiField selectedField) {
		this.selectedField = selectedField;
	}

	public void repaint() {
	}

	public static void setLoggingLevel(Logger myLogger) {
		String logLevelString = System.getProperty(LIBRIS_LOGGING_LEVEL);
		if (null != logLevelString) {
			Level logLevel = Level.parse(logLevelString);
			myLogger.setLevel(logLevel);
			for (Handler handler : Logger.getLogger("").getHandlers()) {
				handler.setLevel(logLevel);
			}
		}
	}

}

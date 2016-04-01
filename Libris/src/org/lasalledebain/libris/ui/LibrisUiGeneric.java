package org.lasalledebain.libris.ui;

import java.io.File;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import javax.swing.JOptionPane;

import org.lasalledebain.libris.DatabaseAttributes;
import org.lasalledebain.libris.LibrisConstants;
import org.lasalledebain.libris.LibrisDatabase;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.RecordId;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.DatabaseNotIndexedException;
import org.lasalledebain.libris.exception.InternalError;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.exception.UserErrorException;

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
	private boolean readOnly;
	private UiField selectedField;
	protected LibrisParameters parameters;
	protected LibrisDatabase database;
	protected String uiTitle;
	private LibrisDatabase currentDatabase;

	public UiField getSelectedField() {
		return selectedField;
	}

	public LibrisUiGeneric() {
		uiLogger = Logger.getLogger(LibrisUi.class.getName());
		LibrisUiGeneric.setLoggingLevel(uiLogger);
		readOnly = false;
		fieldSelected(false);
		setSelectedField(null);
		parameters = null;
	}

	public LibrisUiGeneric(String[] args) {
		this();
		parameters = new LibrisParameters(args);
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

	public LibrisDatabase openDatabase(LibrisParameters params) {
		String dbFileName;
		File sf = params.getDatabaseFile();
		dbFileName = sf.getAbsolutePath();
		try {
			try {
				database = LibrisDatabase.open(new LibrisParameters(this, readOnly, sf));
			} catch (DatabaseNotIndexedException e) {
				int confirmResult = confirm("Database is not indexed. Rebuild now?");
				if (JOptionPane.YES_OPTION == confirmResult) {
					database = LibrisDatabase.rebuild(new LibrisParameters(this, sf));
					if (null == database) {
						fatalError(LibrisDatabase.openException, "cannot rebuild database");
					}
				} else {
					return database;
				}
				if (readOnly) {
					database = LibrisDatabase.open(new LibrisParameters(this, true, false, sf));							
				}
			} 
		} catch (Exception e) {
			alert("Error opening database", e);
			// TODO rebuild index
			return database;
		}
		if (null == database) {
			fatalError(LibrisDatabase.openException, "cannot open database");
		}
		database.setDatabaseFile(dbFileName);
		librisPrefs.put(LibrisDatabase.DATABASE_FILE, dbFileName);
		return database;
	}

	@Override
	public void close(boolean allWindows, boolean closeGui) {
		currentDatabase = null;
		uiLogger.log(Level.FINE, "UI close");
	}

	@Override
	public void databaseClosed() {
		currentDatabase = null;
		setTitle(NO_DATABASE_OPENED);
	}

	@Override
	public void displayRecord(RecordId recordId) throws LibrisException {
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

	@Override
	public void setSelectedField(UiField selectedField) {
		this.selectedField = selectedField;
	}

	protected boolean isParameterError() {
		UserErrorException paramException = parameters.getErrorException();
		if (null != paramException) {
			System.err.println(paramException.getMessage());
			return true;
		} else {
			return false;
		}
	}

	public void repaint() {
	}

	public boolean isDatabaseSelected() {
		return (null != parameters.getDatabaseFile());
	}

	public LibrisDatabase openDatabase() {
		currentDatabase = openDatabase(this.parameters);
		return currentDatabase;
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

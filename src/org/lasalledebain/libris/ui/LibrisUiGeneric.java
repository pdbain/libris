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
	
	private static final String NO_DATABASE_OPENED = "No database opened";
	/**
	 * 
	 */
	protected static Preferences librisPrefs;
	protected static Object prefsSync = new Object();
	private UiField selectedField;
	protected String uiTitle;
	protected LibrisDatabase currentDatabase;
	protected File databaseFile;
	protected File auxDirectory;

	public UiField getSelectedField() {
		return selectedField;
	}
	@Override
	public Record newRecord() {
		return null;
	}
	@Override
	public LibrisDatabase getDatabase() {
		return currentDatabase;
	}


	public void setTitle(String title) {
		uiTitle = title;
	}

	@Override
	public void arrangeValues() {
		throw new InternalError("LibrisUiGeneric.arrangeValues unimplemented");
	}

	@Override
	public void addRecord(Record newRecord) throws DatabaseException {
		throw new InternalError("LibrisUiGeneric.addRecord unimplemented");
	}

	public LibrisUiGeneric(File dbFile, File auxDir,
			boolean readOnly) throws LibrisException {
		this();
		databaseFile = dbFile;
		auxDirectory = auxDir;
	}

	public LibrisUiGeneric() {
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
			// TODO add option to open read-write
			currentDatabase = new LibrisDatabase(databaseFile, auxDirectory, this, false);
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
	public void databaseOpened(LibrisDatabase db) throws DatabaseException {
		currentDatabase = db;
		String title = NO_DATABASE_OPENED;
		if (null != db) {
			DatabaseAttributes databaseAttributes = db.getAttributes();
			title = databaseAttributes.getDatabaseName();
		}
		setTitle(title);
	}

	public static Preferences getLibrisPrefs() {
		synchronized (prefsSync) {
			if (null == librisPrefs) {
				librisPrefs = Preferences.userRoot();
			}

		}
		return librisPrefs;
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

	public abstract boolean isReadOnly();
	
	protected boolean isDatabaseModified() {
		return (null != currentDatabase) && currentDatabase.isModified();
	}
	public static String formatConciseStackTrace(Exception e, StringBuilder buff) {
		String emessage;
		emessage = e.getMessage();
		if (null != emessage) {
			buff.append(": "); buff.append(emessage);
		} else {
			buff.append(" at ");
			String sep = "";
			for (StackTraceElement t: e.getStackTrace()) {
				buff.append(sep);
				String className = t.getClassName();
				int lastDot = className.lastIndexOf('.');
				if (lastDot > 0) {
					buff.append(className.substring(lastDot + 1, className.length()));
				} else {
					buff.append(className);
				}
				buff.append(".");
				buff.append(t.getMethodName());
				buff.append("() line ");
				buff.append(t.getLineNumber());
				sep = "\n";
			}
		}
		return emessage;
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

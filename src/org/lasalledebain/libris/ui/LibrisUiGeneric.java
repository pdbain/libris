package org.lasalledebain.libris.ui;

import java.io.File;
import java.util.Objects;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import org.lasalledebain.libris.Libris;
import org.lasalledebain.libris.LibrisConstants;
import org.lasalledebain.libris.LibrisDatabase;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.InternalError;
import org.lasalledebain.libris.exception.LibrisException;

public abstract class LibrisUiGeneric implements LibrisUi, LibrisConstants {
	
	private static final String NO_DATABASE_OPENED = "No database opened";
	protected boolean databaseOpen;
	protected static Preferences librisPrefs;
	protected static Object prefsSync = new Object();
	private UiField selectedField;
	protected String uiTitle;
	protected LibrisDatabase currentDatabase;
	protected File databaseFile;
	protected File auxDirectory;

	public LibrisUiGeneric(File dbFile, File auxDir,
			boolean readOnly) throws LibrisException {
		this();
		databaseFile = dbFile;
		auxDirectory = auxDir;
	}
	public LibrisUiGeneric() {
		databaseOpen = false;
		fieldSelected(false);
		setSelectedField(null);
	}
	@Override
	public LibrisDatabase getDatabase() {
		return currentDatabase;
	}
	public LibrisDatabase openDatabase() throws DatabaseException {
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
			currentDatabase.openDatabase();
		} catch (Exception e) {
			alert("Error opening database", e);
			return currentDatabase;
		}
		getLibrisPrefs().put(LibrisDatabase.DATABASE_FILE, databaseFile.getAbsolutePath());
		return currentDatabase;
	}
	@Override
	public boolean closeDatabase(boolean force) {
		boolean result = false;
		if (Objects.nonNull(currentDatabase)) {
			result = currentDatabase.closeDatabase(force);
		}
		if (result) {
			currentDatabase = null;
			setTitle(NO_DATABASE_OPENED);
		}
		return result;
	}
	
	public boolean isDatabaseSelected() {
		return (null != databaseFile);
	}
	@Override
	public boolean isDatabaseOpen() {
		return databaseOpen;
	}
	protected boolean isDatabaseModified() {
		return (null != currentDatabase) && currentDatabase.isModified();
	}
	public void setDatabaseFile(File dbFile) {
		 databaseFile = dbFile;
	}
	public UiField getSelectedField() {
		return selectedField;
	}
	@Override
	public Record newRecord() {
		return null;
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

	public void rebuildDatabase() throws LibrisException {
		Libris.buildIndexes(databaseFile, new HeadlessUi());
	}

	@Override
	public void displayRecord(int recordId) throws LibrisException {
		// TODO implement or remove displayRecord

	}
	@Override
	public void pasteToField() {
		// TODO implement or remove pasteToField

	}

	@Override
	public String promptAndReadReply(String prompt) throws DatabaseException {
		// TODO implement or remove promptAndReadReply
		return null;
	}

	 @Override
	public void recordsAccessible(boolean accessible) {
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
	
	public static void cmdlineError(String msg) {
		System.err.println(msg);
		System.exit(1);
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

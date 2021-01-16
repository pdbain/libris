package org.lasalledebain.libris.ui;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static java.util.Objects.nonNull;

import java.io.File;
import java.util.Objects;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import org.lasalledebain.libris.DatabaseAttributes;
import org.lasalledebain.libris.DatabaseRecord;
import org.lasalledebain.libris.GenericDatabase;
import org.lasalledebain.libris.Libris;
import org.lasalledebain.libris.LibrisConstants;
import org.lasalledebain.libris.LibrisDatabase;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.XmlSchema;
import org.lasalledebain.libris.exception.DatabaseError;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.indexes.LibrisDatabaseConfiguration;

public abstract class LibrisUi<RecordType extends Record> implements DatabaseUi<RecordType>, LibrisConstants {
	
	private static final String NO_DATABASE_OPENED = "No database opened";
	protected static Preferences librisPrefs;
	protected static Object prefsSync = new Object();
	private UiField selectedField;
	protected String uiTitle;
	// TODO make this generic
	protected LibrisDatabase currentDatabase;
	protected File databaseFile;
	protected File auxiliaryDirectory;
	protected File artifactDirectory;
	private boolean readOnly;
	private int expectedWork, accomplishedWork;

	public int getAccomplishedWork() {
		return accomplishedWork;
	}
	public int addAccomplishedWork(int theWork) {
		accomplishedWork += theWork;
		return accomplishedWork;
	}
	public int getExpectedWork() {
		return expectedWork;
	}
	/**
	 * Set the total amount of anticipated work for progress monitoring
	 * and reset the accomplished work to zero.
	 * @param theTotal
	 */
	public void setExpectedWork(int theTotal) {
		expectedWork = theTotal;
		accomplishedWork = 0;
	}
	
	public LibrisUi(boolean readOnly) {
		this();
		this.readOnly = readOnly;
	}
	public LibrisUi() {
		fieldSelected(false);
		setSelectedField(null);
	}

	@Override
	public GenericDatabase<DatabaseRecord> getDatabase() {
		return currentDatabase;
	}

	public LibrisDatabase getLibrisDatabase() {
		return currentDatabase;
	}
	/**
	 * @return the uiTitle
	 */
	public String getUiTitle() {
		return uiTitle;
	}
	/**
	 * @param uiTitle the uiTitle to set
	 */
	public void setUiTitle(String uiTitle) {
		this.uiTitle = uiTitle;
	}
	/**
	 * @return the mySchema
	 */
	public XmlSchema getSchema() {
		XmlSchema result = null;
		if (nonNull(currentDatabase))
			result = currentDatabase.getSchema();
		return result;
	}

	/**
	 * @param openReadOnly the openReadOnly to set
	 */
	public void setOpenReadOnly(boolean openReadOnly) {
		readOnly = openReadOnly;
	}

	/**
	 * Convenience method for {@link #openDatabase(LibrisDatabaseConfiguration) with current database}
	 */
	public LibrisDatabase openDatabase() throws DatabaseException {
		return openDatabase(new LibrisDatabaseConfiguration(databaseFile, readOnly));
	}
	
	public LibrisDatabase openDatabase(LibrisDatabaseConfiguration config) throws DatabaseException {
		setDatabaseFile(config.getDatabaseFile());
		if (!isDatabaseSelected()) {
			throw new DatabaseException("Database file not set");
		}
		if (isDatabaseOpen()) {
			alert("Cannot open "+databaseFile.getAbsolutePath()+" because "+currentDatabase.getDatabaseFile().getAbsolutePath()+" is open");
		}
		try {
			currentDatabase = new LibrisDatabase(config, this);
			if (!currentDatabase.isIndexed()) {
				alert("database "+databaseFile.getAbsolutePath()+" is not indexed.  Please re-index.");
				return null;
			}
			currentDatabase.openDatabase();
			DatabaseAttributes databaseAttributes = currentDatabase.getDatabaseAttributes();
			setUiTitle(databaseAttributes.getDatabaseName());
			getLibrisPrefs().put(LibrisConstants.DATABASE_FILE, databaseFile.getAbsolutePath());
		} catch (Exception e) {
			alert("Error opening database", e);
			throw new DatabaseException(e);
		}
		return currentDatabase;
	}
	
	@Override
	public boolean closeDatabase(boolean force) throws DatabaseException {
		boolean result = true;
		if (Objects.nonNull(currentDatabase) && currentDatabase.isDatabaseOpen()) {
			result = checkAndCloseDatabase(force);
		}
		if (result) {
			currentDatabase = null;
			setUiTitle(NO_DATABASE_OPENED);
			recordsAccessible(false);
		}
		return result;
	}

	@Override
	public boolean quit(boolean force) throws DatabaseException {
		return closeDatabase(force);
	}

	public boolean isDatabaseSelected() {
		return (null != databaseFile);
	}
	
	@Override
	public boolean isDatabaseOpen() {
		return Objects.nonNull(currentDatabase) && currentDatabase.isDatabaseOpen();
	}
	
	public boolean isDatabaseModified() {
		return (null != currentDatabase) && currentDatabase.isModified();
	}
	public void setDatabaseFile(File dbFile) {
		 databaseFile = dbFile;
	}
	public UiField getSelectedField() {
		return selectedField;
	}
	@Override
	public RecordType newRecord() {
		return null;
	}

	@Override
	public void arrangeValues() {
		throw new DatabaseError("LibrisUiGeneric.arrangeValues unimplemented");
	}

	@Override
	public void addRecord(RecordType newRecord) throws DatabaseException{
		throw new DatabaseError("LibrisUiGeneric.addRecord unimplemented");
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

	public boolean rebuildDatabase() throws LibrisException {
		assertTrue("Database not opened", isDatabaseOpen());
		assertFalse("Database has unsaved changes", isDatabaseModified());
		currentDatabase.exportDatabaseXml();
		closeDatabase(false);
		return Libris.buildIndexes(databaseFile, this);
	}

	public boolean rebuildDatabase(LibrisDatabaseConfiguration config) throws LibrisException {
		closeDatabase(false);
		return Libris.buildIndexes(config, this);
	}

	@Override
	public abstract void displayRecord(int recordId) throws LibrisException;
	
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
		throw new DatabaseError("removeField not implemented");
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

	public boolean isDatabaseReadOnly() {
		return Objects.nonNull(currentDatabase)? currentDatabase.isDatabaseReadOnly(): false;
	}

	
	public static void cmdlineError(String msg) {
		System.err.println(msg);
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
	@Override
	public void saveDatabase() {
		currentDatabase.save();
	}
	public void sendChooseDatabase() {
		alert("No database selected");
	}
	@Override
	public boolean start() {
		return true;
	}
	@Override
	public boolean stop() {
		return true;
	}
}

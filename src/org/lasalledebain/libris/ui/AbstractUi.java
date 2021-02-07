package org.lasalledebain.libris.ui;

import static java.util.Objects.nonNull;
import static org.junit.Assert.assertFalse;

import java.io.File;
import java.util.Objects;

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
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.indexes.LibrisDatabaseConfiguration;

public abstract class AbstractUi implements DatabaseUi, LibrisConstants {
	
	@Override
	public void setRecordName() throws InputException {
		alert("Operation not available");
	}
	
	private static final String NO_DATABASE_OPENED = "No database opened";
	private UiField selectedField;
	protected String uiTitle;
	// TODO make this generic
	protected LibrisDatabase currentDatabase;
	protected File databaseFile;
	protected File auxiliaryDirectory;
	protected File artifactDirectory;
	private boolean readOnly;
	private int expectedWork, accomplishedWork;

	public AbstractUi(boolean readOnly) {
		this();
		this.readOnly = readOnly;
	}
	public AbstractUi() {
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
			alert("Database file not set");
			return null;
		}
		if (isDatabaseOpen()) {
			alert("Cannot open "+databaseFile.getAbsolutePath()+" because "+currentDatabase.getDatabaseFile().getAbsolutePath()+" is open");
			return null;
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
			Libris.getLibrisPrefs().put(LibrisConstants.DATABASE_FILE, databaseFile.getAbsolutePath());
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

	/**
	 * Exit the user interface and if possible
	 */
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
	public void arrangeValues() {
		throw new DatabaseError("LibrisUiGeneric.arrangeValues unimplemented");
	}

	@Override
	public abstract String SelectSchemaFile(String schemaName) throws DatabaseException;

	@Override
	public abstract void alert(String msg, Throwable e);

	@Override
	public abstract void alert(String msg);

	public void fatalError(Exception e, String msg) {
		alert(msg+e.getMessage());
		e.printStackTrace();
		System.exit(1);
	}

	public boolean buildDatabase(LibrisDatabaseConfiguration config) throws LibrisException {
		assertFalse("Cannot build database while database is open", isDatabaseOpen());
		if (config.isReadOnly()) {
			this.alert("Cannot build indexes if read-only set");
			return false;
		}
		try (LibrisDatabase db = new LibrisDatabase(config, this)) {
			if (!db.isDatabaseReserved()) {
				boolean buildResult = db.buildDatabase();
				if (!buildResult) {
					return false;
				}
				return db.closeDatabase(true);
			} else {
				return false;
			}
		} catch (Error e) {
			this.alert("Error rebuilding database", e);
			return false;
		}
	}

	@Override
	public  boolean buildDatabase(File databaseFile) throws LibrisException {
		LibrisDatabaseConfiguration config = new LibrisDatabaseConfiguration(databaseFile);
		return buildDatabase(config);
	}
	
	@Override
	// TODO displayRecord return record
	public abstract Record displayRecord(int recordId) throws LibrisException;
	
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

	
	@Override
	public void saveDatabase() {
		currentDatabase.save();
	}
	
	public void sendChooseDatabase() {
		alert("No database selected");
	}

	@Override
	public int getAccomplishedWork() {
		return accomplishedWork;
	}
	
	@Override
	public int addAccomplishedWork(int theWork) {
		accomplishedWork += theWork;
		return accomplishedWork;
	}
	
	@Override
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
	@Override
	public boolean start() {
		return true;
	}
	@Override
	public boolean stop() {
		return true;
	}
}

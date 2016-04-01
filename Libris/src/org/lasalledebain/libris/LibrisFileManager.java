package org.lasalledebain.libris;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.concurrent.locks.ReentrantLock;

import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.InternalError;
import org.lasalledebain.libris.exception.LibrisException;

public class LibrisFileManager {	
	private static final String AUX_DIRECTORY_NAME = ".libris_auxfiles";
	private static final String POSITION_FILENAME = "positions";
	private static final String PROPERTIES_FILENAME = "properties";
	private static final String RECORDS_FILENAME = "records";
	private static final String JOURNAL_FILENAME = "journal";
	/**
	 * XML representation of the schema and database records
	 */
	private File databaseFile;
	/**
	 * Native representation of database records.
	 */
	private File auxDirectory;
	private FileAccessManager propertiesFileMgr;
	private FileAccessManager recordsFileMgr;
	private FileAccessManager positionFileMgr;
	private FileAccessManager schemaAccessMgr;
	
	private final HashSet<FileAccessManager> activeManagers;
	private final HashSet<FileAccessManager> expendableFiles;
	ReentrantLock mgrLock;
	boolean locked;

	/**
	 * XML representation of database records added
	 */
	private FileAccessManager journalAccessMgr;
	private FileAccessManager databaseAccessMgr;

	public LibrisFileManager(File dbFile, File dbDir) {
		File myDbDir = dbDir;
		activeManagers = new HashSet<FileAccessManager>();
		expendableFiles = new HashSet<FileAccessManager>();
		if (null == dbDir) {
			myDbDir =dbFile.getParentFile();
		}
		open(dbFile, myDbDir);
		mgrLock  = new ReentrantLock();
		locked = false;
	}

	public void setDatabaseFile(String databaseFileName) {
		setDatabaseFile(new File(databaseFileName));
	}

	public boolean checkAuxFiles() {
		if ((null == auxDirectory) || !auxDirectory.exists()) {
			return false;
		}
		for (FileAccessManager f:expendableFiles) {
			if ((null == f) || !f.exists()) {
				return false;
			}
		}
		return true;
	}
	/**
	 * Create the auxiliary directory and initialize the files
	 * @param replace if true, destroy any existing directory and files
	 * @throws LibrisException
	 */
	public void createAuxFiles(boolean replace) throws LibrisException {
		if (replace && (null != auxDirectory) && (auxDirectory.exists())) {
			for (File f: auxDirectory.listFiles()) {
				f.delete();
			}
		}
		try {
			createAuxDirectory();
			for (FileAccessManager f:expendableFiles) {
				if (f.exists()) {
					f.delete();
				}
				f.createNewFile();
			}
		} catch (IOException e) {
			throw new DatabaseException("error creating auxiliary files", e);
		}
	}

	private void createAuxDirectory() throws IOException {
		if (!auxDirectory.exists()) {
			auxDirectory.mkdir();
		} else if (false && !auxDirectory.isDirectory()) {
			if (!auxDirectory.delete()) {
				throw new IOException("cannot delete "+auxDirectory.getAbsolutePath());
			}
			createAuxDirectory();
		}
	}

	public String getDatabaseFilePath() {
		return (databaseFile == null)? "<no database file given>": databaseFile.getPath();
	}

	public String getDatabaseDirectory() {
		return (databaseFile == null)? "<no database file given>": databaseFile.getParent();
	}

	public void open(File dbFile, File dbDir) {
		setDatabaseFile(dbFile);
		try {
			setAuxiliaryFiles(dbDir);
		} catch (DatabaseException e) {
			LibrisException.saveException(e);
		}
	}

	public void close() {
		checkLock();
		databaseFile = null;
		auxDirectory = null;
		for (FileAccessManager f: activeManagers) {
			f.close();
		}
	}
	/**
	 * @param databaseDir base directory into which to put the auxiliary files directory
	 * @throws DatabaseException 
	 */
	public synchronized void setAuxiliaryFiles(File databaseDir) throws DatabaseException {
		checkLock();
		if (null == databaseFile) {
			throw new DatabaseException("Database file not set");
		}
		String directoryName = databaseFile.getName();
		int suffixPosition = directoryName.lastIndexOf(".xml");
		if (suffixPosition > 0) {
			directoryName = directoryName.substring(0, suffixPosition);
		}
		auxDirectory = new File(databaseDir, AUX_DIRECTORY_NAME+'_'+directoryName);

		positionFileMgr = new FileAccessManager(auxDirectory, POSITION_FILENAME);
		activeManagers.add(positionFileMgr);
		expendableFiles.add(positionFileMgr);

		recordsFileMgr = new FileAccessManager(auxDirectory, RECORDS_FILENAME);
		activeManagers.add(recordsFileMgr);
		expendableFiles.add(recordsFileMgr);

		journalAccessMgr = new FileAccessManager(new File(auxDirectory, JOURNAL_FILENAME));
		activeManagers.add(journalAccessMgr);
		
		databaseAccessMgr = new FileAccessManager(databaseFile);
		activeManagers.add(databaseAccessMgr);
		
		propertiesFileMgr = new FileAccessManager(new File(auxDirectory, PROPERTIES_FILENAME));
		activeManagers.add(propertiesFileMgr);
		expendableFiles.add(propertiesFileMgr);
	}
	
	public synchronized FileAccessManager makeAccessManager(File managedFile) {
		FileAccessManager mgr = new FileAccessManager(managedFile);
		activeManagers.add(mgr);
		return mgr;
	}
	
	public synchronized void releaseAccessManager(FileAccessManager mgr) {
		checkLock();
		mgr.close();
		activeManagers.remove(mgr);
	}
	/**
	 * @return File object for the database source (XML) file
	 */
	public synchronized FileAccessManager getDatabaseFileMgr() {
		checkLock();
		return databaseAccessMgr;
	}
	
	public synchronized File getDatabaseFile() {
		checkLock();
		return databaseFile;
	}
	
	public synchronized FileAccessManager getPropertiesFileMgr() {
		checkLock();
		return propertiesFileMgr;
	}

	/**
	 * Set the database source (XML) file
	 * @param dbFile
	 */
	public synchronized void setDatabaseFile(File dbFile) {
		checkLock();
		databaseFile = dbFile;
	}
	
	/**
	 * @return File object for index of record positions in the native record store.
	 */
	public synchronized FileAccessManager getPositionFileMgr() {
		checkLock();
		return positionFileMgr;
	}

	public synchronized File getAuxDirectory() {
		checkLock();
		return auxDirectory;
	}
	public synchronized void setAuxDirectory(File auxDirectory) {
		checkLock();
		this.auxDirectory = auxDirectory;
	}

	public synchronized FileAccessManager getSchemaAccessMgr() {
		checkLock();
		return schemaAccessMgr;
	}

	/**
	 * Create a manager for the schema file
	 * @return true if the file exists and is non-empty
	 * @throws DatabaseException if the schema has already been set
	 */
	public synchronized boolean setSchemaAccessMgr(File schemaFile) throws DatabaseException {
		checkLock();
		if (null != schemaAccessMgr) {
			throw new DatabaseException("schema file already set");
		}
		schemaAccessMgr = new FileAccessManager(schemaFile);
		activeManagers.add(schemaAccessMgr);
		return schemaFile.exists() && (schemaFile.length() > 0);
	}

	public synchronized FileAccessManager getRecordsFileMgr() {
		checkLock();
		return recordsFileMgr;
	}
	/**
	 * @return File object for journal (XML) file containing updates to the database source file
	 * @throws DatabaseException 
	 */
	public synchronized FileAccessManager getJournalFileMgr() {
		checkLock();
		return journalAccessMgr;
	}

	private void checkLock() throws InternalError {
		if (locked && !mgrLock.isHeldByCurrentThread()) {
			throw new InternalError("file manager locked by other thread");
		}
	}
	public boolean fileSet() {
		return (null != databaseFile);
	}
	
	public synchronized boolean lock() {
		boolean result = mgrLock.tryLock();
		if (result) {
			locked = true;
			try {
				for (FileAccessManager m: activeManagers) {
					if (m.hasFilesOpen()) {
						mgrLock.unlock();
						result = false;
						break;
					}
				}
			} catch (Throwable t) {
				mgrLock.unlock();
				result = false;
			}
		}
		return result;	
	}
	
	public synchronized void unlock() {
		if (locked && mgrLock.isHeldByCurrentThread()) {
			locked = false;
			mgrLock.unlock();
		}
	}
	
}

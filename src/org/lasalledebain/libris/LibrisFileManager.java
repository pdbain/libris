package org.lasalledebain.libris;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;

import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.InternalError;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.exception.UserErrorException;

public class LibrisFileManager implements LibrisConstants {	
	/**
	 * XML representation of the schema and database records
	 */
	private File databaseFile;
	/**
	 * Location of indexes, native copy of records, etc.
	 */
	private File auxDirectory;
	

	private String[] coreAuxFiles = {
					PROPERTIES_FILENAME,
					POSITION_FILENAME,
					RECORDS_FILENAME, 
					LOCK_FILENAME
	};
	private FileAccessManager schemaAccessMgr;

	private final HashSet<FileAccessManager> expendableFiles;
	private final HashMap<String, FileAccessManager> activeManagers;
	ReentrantLock mgrLock;
	boolean locked;

	/**
	 * XML representation of database records added
	 */
	private FileAccessManager journalAccessMgr;
	private FileAccessManager databaseAccessMgr;
	private File lockFile;
	private FileOutputStream dbLockFile;
	private FileLock dbLock;
	private boolean databaseReserved;

	public LibrisFileManager(File dbFile, File auxDir) throws UserErrorException {
		activeManagers = new HashMap<String, FileAccessManager>();
		expendableFiles = new HashSet<FileAccessManager>();
		databaseFile = dbFile;
		if (!databaseFile.exists()) {
			throw new UserErrorException("database file "+dbFile+" dos not exist");
		}
		if (null == auxDir) {
			File databaseDir = databaseFile.getParentFile();
			String directoryName = databaseFile.getName();
			int suffixPosition = directoryName.lastIndexOf(".xml");
			if (suffixPosition > 0) {
				directoryName = directoryName.substring(0, suffixPosition);
			}

			auxDirectory = new File(databaseDir, AUX_DIRECTORY_NAME+'_'+directoryName);
		}
		lockFile = new File(auxDirectory, LOCK_FILENAME);
		open(auxDirectory);
		mgrLock  = new ReentrantLock();
		locked = false;
	}

	public void setDatabaseFile(String databaseFileName) {
		setDatabaseFile(new File(databaseFileName));
	}
	
	// FIXME put database lock in aux dir
	public synchronized boolean  reserveDatabase() {
		try {
			lockFile.createNewFile();
			dbLockFile = new FileOutputStream(lockFile);
			dbLock = dbLockFile.getChannel().tryLock();
			if (null != dbLock) {
				String dateString = "Reserved "+DateFormat.getDateInstance().format(new Date());
				dbLockFile.write(dateString.getBytes());
				databaseReserved = true;
				return true;
			} else {
				dbLockFile.close();
			}
		} catch (IOException e) {
			LibrisDatabase.librisLogger.log(Level.SEVERE, "Error creating lock file", e);
		}
		return false;
	}
	
	public boolean isDatabaseReserved() {
		return databaseReserved;
	}

	public synchronized void freeDatabase() {
		if (null != dbLock) {
			try {
				dbLock.release();
				RandomAccessFile lckFile = new RandomAccessFile(lockFile, "rw");
				lckFile.setLength(0);
				lckFile.close();
				databaseReserved = false;
			} catch (IOException e) {
				LibrisDatabase.librisLogger.log(Level.SEVERE, "Error unlocking file", e);
			}
		}
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
		} else if (!auxDirectory.isDirectory()) {
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

	public void open(File auxDir) {
		try {
			setAuxiliaryFiles(auxDir);
		} catch (DatabaseException e) {
			LibrisException.saveException(e);
		}
	}

	public void close() {
		checkLock();
		databaseFile = null;
		auxDirectory = null;
		activeManagers.values().forEach(f->f.close());
	}
	/**
	 * @param databaseDir base directory into which to put the auxiliary files directory
	 * @throws DatabaseException 
	 */
	public synchronized void setAuxiliaryFiles(File databaseDir) throws DatabaseException {
		checkLock();
		if (!fileSet()) {
			throw new DatabaseException("Database file not set");
		}
		journalAccessMgr = new FileAccessManager(new File(auxDirectory, JOURNAL_FILENAME));
		activeManagers.put(JOURNAL_FILENAME, journalAccessMgr);
		
		databaseAccessMgr = new FileAccessManager(databaseFile);
		activeManagers.put(LibrisConstants.DATABASE_NAME, databaseAccessMgr);
		
		for (String m: coreAuxFiles) {
			makeExpendableAccessManager(m);
		}
	}

	public FileAccessManager makeExpendableAccessManager(String fileName) {
		FileAccessManager mgr = new FileAccessManager(auxDirectory, fileName);
		activeManagers.put(fileName, mgr);
		expendableFiles.add(mgr);
		return mgr;
	}
	
	public synchronized FileAccessManager makeAccessManager(String mgrName, File managedFile) {
		FileAccessManager mgr = new FileAccessManager(managedFile);
		activeManagers.put(mgrName, mgr);
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
	
	public synchronized FileAccessManager getAuxiliaryFileMgr(String auxFileName) {
		checkLock();
		FileAccessManager mgr = activeManagers.get(auxFileName);
		if (null == mgr) {
			mgr = makeExpendableAccessManager(auxFileName);
		}
		return mgr;
	}

	/**
	 * Set the database source (XML) file
	 * @param dbFile
	 */
	public synchronized void setDatabaseFile(File dbFile) {
		checkLock();
		databaseFile = dbFile;
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
		activeManagers.put(SCHEMA_NAME, schemaAccessMgr);
		return schemaFile.exists() && (schemaFile.length() > 0);
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
				for (FileAccessManager m: activeManagers.values()) {
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

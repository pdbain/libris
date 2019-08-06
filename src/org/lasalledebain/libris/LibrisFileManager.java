package org.lasalledebain.libris;

import static org.lasalledebain.libris.LibrisConstants.JOURNAL_FILENAME;
import static org.lasalledebain.libris.LibrisConstants.LOCK_FILENAME;
import static org.lasalledebain.libris.LibrisConstants.POSITION_FILENAME;
import static org.lasalledebain.libris.LibrisConstants.PROPERTIES_FILENAME;
import static org.lasalledebain.libris.LibrisConstants.RECORDS_FILENAME;

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

import org.lasalledebain.libris.exception.Assertion;
import org.lasalledebain.libris.exception.DatabaseError;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.LibrisException;

public class LibrisFileManager {	
	private FileAccessManager schemaAccessMgr;
	ReentrantLock mgrLock;
	boolean locked;

	/**
	 * XML representation of database records added
	 */
	private FileAccessManager journalAccessMgr;
	private File lockFile;
	private FileOutputStream dbLockFile;
	private FileLock dbLock;
	private boolean databaseReserved;

	public LibrisFileManager(File auxDir) {
		auxDirectory = auxDir;
		activeManagers = new HashMap<String, FileAccessManager>();
		expendableFiles = new HashSet<FileAccessManager>();
		lockFile = new File(auxDirectory, LOCK_FILENAME);
		open(auxDirectory);
		mgrLock  = new ReentrantLock();
		locked = false;
	}
	/**
	 * Location of indexes, native copy of records, etc.
	 */
	protected final File auxDirectory;
	protected final HashSet<FileAccessManager> expendableFiles;
	protected final HashMap<String, FileAccessManager> activeManagers;
	protected static final String[] coreAuxFiles = {
						PROPERTIES_FILENAME,
						POSITION_FILENAME,
						RECORDS_FILENAME, 
						LOCK_FILENAME
		};

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
			LibrisDatabase.log(Level.SEVERE, "Error creating lock file", e);
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
				LibrisDatabase.log(Level.SEVERE, "Error unlocking file", e);
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
			for (FileAccessManager f: expendableFiles) {
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

	public void open(File auxDir) {
		try {
			setAuxiliaryFiles(auxDir);
		} catch (DatabaseException e) {
			LibrisException.saveException(e);
		}
	}

	public void close() {
		checkLock();
		activeManagers.values().forEach(f->f.close());
	}
	/**
	 * @param databaseDir base directory into which to put the auxiliary files directory
	 * @throws DatabaseException 
	 */
	private synchronized void setAuxiliaryFiles(File databaseDir) throws DatabaseException {
		checkLock();
		journalAccessMgr = new FileAccessManager(new File(auxDirectory, JOURNAL_FILENAME));
		activeManagers.put(JOURNAL_FILENAME, journalAccessMgr);
		
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
	
	public synchronized FileAccessManager getAuxiliaryFileMgr(String auxFileName) {
		checkLock();
		FileAccessManager mgr = activeManagers.get(auxFileName);
		if (null == mgr) {
			mgr = makeExpendableAccessManager(auxFileName);
		}
		return mgr;
	}
	
	public FileOutputStream getUnmanagedOutputFile(String fileName) throws IOException {
		return new FileOutputStream(new File(auxDirectory, fileName));
	}

	public synchronized File getAuxDirectory() {
		checkLock();
		return auxDirectory;
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
		activeManagers.put(LibrisConstants.SCHEMA_NAME, schemaAccessMgr);
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

	void checkLock() throws DatabaseError {
		Assertion.assertTrueError("file manager locked by other thread", !locked || mgrLock.isHeldByCurrentThread());
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

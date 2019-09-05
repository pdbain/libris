package org.lasalledebain.libris;

import static org.lasalledebain.libris.LibrisConstants.LOCK_FILENAME;
import static org.lasalledebain.libris.LibrisConstants.POSITION_FILENAME;
import static org.lasalledebain.libris.LibrisConstants.PROPERTIES_FILENAME;
import static org.lasalledebain.libris.LibrisConstants.RECORDS_FILENAME;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.locks.ReentrantLock;

import org.lasalledebain.libris.exception.Assertion;
import org.lasalledebain.libris.exception.DatabaseError;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.LibrisException;

public class FileManager {
	/**
	 * Location of indexes, native copy of records, etc.
	 */
	protected final File auxDirectory;
	protected final HashSet<FileAccessManager> expendableFiles;
	protected final HashMap<String, FileAccessManager> activeManagers;
	protected final ReentrantLock mgrLock;
	boolean locked;
	protected static final String[] coreAuxFiles = { PROPERTIES_FILENAME, POSITION_FILENAME, RECORDS_FILENAME,
			LOCK_FILENAME };

	public FileManager(File auxDir) {
		auxDirectory = auxDir;
		activeManagers = new HashMap<String, FileAccessManager>();
		expendableFiles = new HashSet<FileAccessManager>();
		mgrLock = new ReentrantLock();
		locked = false;
		
		for (String m : coreAuxFiles) {
			makeExpendableAccessManager(m);
		}
	}

	public synchronized FileAccessManager makeAccessManager(String mgrName, File managedFile) {
		FileAccessManager mgr = new FileAccessManager(managedFile);
		activeManagers.put(mgrName, mgr);
		return mgr;
	}

	public synchronized FileAccessManager makeAuxiliaryFileAccessManager(String name) {
		FileAccessManager mgr = new FileAccessManager(new File(auxDirectory, name));
		activeManagers.put(name, mgr);
		return mgr;
	}

	public FileAccessManager makeExpendableAccessManager(String fileName) {
		FileAccessManager mgr = new FileAccessManager(auxDirectory, fileName);
		activeManagers.put(fileName, mgr);
		expendableFiles.add(mgr);
		return mgr;
	}

	public synchronized void releaseAccessManager(FileAccessManager mgr) {
		checkLock();
		mgr.close();
		activeManagers.remove(mgr.getManagerName());
	}

	void checkLock() throws DatabaseError {
		Assertion.assertTrueError("file manager locked by other thread", !locked || mgrLock.isHeldByCurrentThread());
	}

	/**
	 * Create the auxiliary directory and initialize the files
	 * 
	 * @param replace if true, destroy any existing directory and files
	 * @throws LibrisException
	 */
	public void createAuxFiles(boolean replace) throws LibrisException {
		if (replace && (null != auxDirectory) && (auxDirectory.exists())) {
			for (File f : auxDirectory.listFiles()) {
				f.delete();
			}
		}
		try {
			createAuxDirectory();
			for (FileAccessManager f : expendableFiles) {
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
				throw new IOException("cannot delete " + auxDirectory.getAbsolutePath());
			}
			createAuxDirectory();
		}
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

	public boolean checkAuxFiles() {
		boolean result = true;
		if ((null == auxDirectory) || !auxDirectory.exists()) {
			result = false;
		}
		for (FileAccessManager f : expendableFiles) {
			if ((null == f) || !f.exists()) {
				result = false;
			}
		}
		return result;
	}

	public synchronized boolean lock() {
		boolean result = mgrLock.tryLock();
		if (result) {
			locked = true;
			try {
				for (FileAccessManager m : activeManagers.values()) {
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

	public void close() {
		checkLock();
		activeManagers.values().forEach(f -> f.close());
	}

}

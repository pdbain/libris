package org.lasalledebain.libris;

import static org.lasalledebain.libris.LibrisConstants.LOCK_FILENAME;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;
import java.text.DateFormat;
import java.util.Date;
import java.util.logging.Level;

public class LibrisFileManager extends FileManager {
	private FileAccessManager schemaAccessMgr;

	private File lockFile;
	private FileOutputStream dbLockFile;
	private FileLock dbLock;
	private boolean databaseReserved;

	public LibrisFileManager(File auxDir) {
		super(auxDir);
		lockFile = new File(auxDirectory, LOCK_FILENAME);
		checkLock();
	}

	public synchronized boolean reserveDatabase() {
		try {
			lockFile.createNewFile();
			dbLockFile = new FileOutputStream(lockFile);
			dbLock = dbLockFile.getChannel().tryLock();
			if (null != dbLock) {
				String dateString = "Reserved " + DateFormat.getDateInstance().format(new Date());
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

}

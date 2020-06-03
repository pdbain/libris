package org.lasalledebain.libris;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;
import java.text.DateFormat;
import java.util.Date;
import java.util.logging.Level;

import org.lasalledebain.libris.exception.DatabaseException;

class ReservationManager {

	private FileAccessManager lockFile;
	private FileLock dbLock;
	private boolean databaseReserved;

	public ReservationManager(FileAccessManager lockFile) {
		this.lockFile = lockFile;
	}

	public synchronized boolean reserveDatabase() throws DatabaseException {
		LibrisDatabase.log(Level.INFO, " > lock database lock file " + lockFile.getPath());
		if (databaseReserved) {
			throw new DatabaseException("Database already reserved");
		}
		try {
			lockFile.createNewFile();
			FileOutputStream lockFileStream = lockFile.getOpStream();
			dbLock = lockFileStream.getChannel().tryLock();
			if (null != dbLock) {
				String dateString = "Reserved " + DateFormat.getDateInstance().format(new Date());
				lockFileStream.write(dateString.getBytes());
				databaseReserved = true;
				return true;
			} else {
				lockFileStream.close();
			}
		} catch (IOException e) {
			LibrisDatabase.log(Level.SEVERE, "Error creating lock file", e);
		}
		return false;
	}

	public boolean isDatabaseReserved() {
		return databaseReserved;
	}

	public synchronized void freeDatabase() throws DatabaseException {
		LibrisDatabase.log(Level.INFO, "< free database lock file " + lockFile.getPath());
		if (!databaseReserved) {
			throw new DatabaseException("Database not reserved");
		}
		if (null != dbLock) {
			try {
				dbLock.release();
				RandomAccessFile lckFile = lockFile.getReadWriteRandomAccessFile();
				lckFile.setLength(0);
				lckFile.close();
				databaseReserved = false;
			} catch (IOException e) {
				LibrisDatabase.log(Level.SEVERE, "Error unlocking file", e);
			}
		}
	}

}
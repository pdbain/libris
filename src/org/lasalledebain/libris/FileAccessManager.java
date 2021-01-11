/**
 * 
 */
package org.lasalledebain.libris;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.HashSet;

public class FileAccessManager {
	
	String managerName;
	File myFile;
	HashSet<InputStream> ipStreams;
	HashSet<RandomAccessFile> raRoFiles;
	FileOutputStream opStream;
	RandomAccessFile raRwFile;
	private boolean readOnly;
	private boolean readWrite;
	private boolean deleteOnExit;

	public FileAccessManager(File managedFile) {
		readOnly = false;
		readWrite = false;
		this.myFile = managedFile;
		ipStreams = new HashSet<InputStream>();
		raRoFiles = new HashSet<RandomAccessFile>();
		deleteOnExit = false;
	}

	public FileAccessManager(File directoryPath, String filename) {
		this(new File(directoryPath, filename));
	}

	public void setDeleteOnExit() {
		this.deleteOnExit = true;
		myFile.deleteOnExit();
	}

	public File getFile() {
		return myFile;
	}

	public synchronized void setFile(File managedFile) {
		close();
		myFile = managedFile;
		myFile.deleteOnExit();
	}

	public String getManagerName() {
		return managerName;
	}

	public void setManagerName(String managerName) {
		this.managerName = managerName;
	}

	public synchronized boolean createIfNonexistent() throws IOException {
		boolean newFile = false;
		if (!myFile.exists()) {
			myFile.createNewFile();
			newFile = true;
		}
		return newFile;
	}

	public synchronized FileInputStream getIpStream() throws FileNotFoundException {
		FileInputStream tempIpStream = new FileInputStream(myFile);
		ipStreams.add(tempIpStream);
		return tempIpStream;
	}

	public synchronized void releaseIpStream(InputStream tempIpStream) throws IOException {
		if (null != tempIpStream) {
			tempIpStream.close();
			ipStreams.remove(tempIpStream);
		}
	}

	public synchronized FileOutputStream getOpStream() throws FileNotFoundException {
		if (null == opStream) {
			opStream = new FileOutputStream(myFile);
		}
		return opStream;
	}

	public synchronized void releaseOpStream() throws IOException {
		if (null != opStream) {
			opStream.close();
			opStream = null;
		}
	}

	public synchronized RandomAccessFile getReadOnlyRandomAccessFile() throws FileNotFoundException {
		RandomAccessFile raRoFile = new RandomAccessFile(myFile, "r");
		raRoFiles.add(raRoFile);
		return raRoFile;
	}

	public synchronized RandomAccessFile getReadWriteRandomAccessFile() throws FileNotFoundException {
		if (null == raRwFile) {
			raRwFile = new RandomAccessFile(myFile, "rw");
		}
		return raRwFile;
	}

	public synchronized void releaseRaRoFile(RandomAccessFile raRoFile) throws IOException {
		if (null != raRoFile) {
			raRoFile.close();
			raRoFiles.remove(raRoFile);
		}
	}

	public synchronized void releaseRaRwFile() throws IOException {
		if (null != raRwFile) {
			raRwFile.close();
		}
		raRwFile = null;
	}

	public synchronized void close() {
		try {
			for (InputStream s: ipStreams) {
				s.close();
			}
			ipStreams.clear();
			for (RandomAccessFile r: raRoFiles) {
				r.close();
			}
			raRoFiles.removeAll(raRoFiles);
			releaseOpStream();
			releaseRaRwFile();
		} catch (IOException e) {}
	}

	public synchronized boolean isReadWrite() {
		return readWrite;
	}
	
	public synchronized boolean hasFilesOpen() {
		return readOnlyFilesOpen() || readWriteFilesOpen();
	}

	public synchronized boolean setReadWrite(boolean value) {
		if (value && readOnlyFilesOpen()) {
			return false;
		} else {
			readWrite = value;
			return true;
		}
	}

	public synchronized boolean readOnlyFilesOpen() {
		return (ipStreams.size() > 0) || (raRoFiles.size() > 0);
	}

	public synchronized boolean isReadOnly() {
		return readOnly;
	}
	
	public synchronized boolean setReadOnly (boolean value) {
		if (value && readWriteFilesOpen()) {
			return false;
		} else {
			readOnly = value;
			return true;
		}
	}

	public synchronized boolean readWriteFilesOpen() {
		return (null != opStream) || (null != raRwFile);
	}
	
	public synchronized boolean filesOpen() {
		return (readOnlyFilesOpen() || readWriteFilesOpen());
	}
	
	public long getLength() {
		return myFile.length();
	}

	public String getPath() {
		return myFile.getPath();
	}

	public boolean compareFile(File comparand) {
		return myFile.equals(comparand);
	}

	public boolean exists() {
		return myFile.exists();
	}

	public synchronized void delete() {
		close();
		myFile.delete();
	}

	public synchronized void createNewFile() throws IOException {
		myFile.createNewFile();
	}

	public synchronized void releaseRaFile(RandomAccessFile raFile) throws IOException {
		if (raFile == raRwFile) {
			releaseRaRwFile();
		} else {
			releaseRaRoFile(raFile);
		}
		
	}

	public synchronized File renameToBackup(boolean overwrite) throws FileNotFoundException {
		File backupFile = new File(myFile.getAbsolutePath()+".bak");
		if (backupFile.exists()) {
			if (!overwrite) {
				return null;
			} else {
				backupFile.delete();
			}
		} else {
			myFile.renameTo(backupFile);
		}
		return backupFile;
	}
}
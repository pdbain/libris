package org.lasalledebain.libris.indexes;

import java.io.File;

import org.lasalledebain.libris.exception.DatabaseException;

public class LibrisDatabaseConfiguration extends DatabaseConfiguration {
	private File myDatabaseFile;
	private File repositoryDirectory;
	private File auxiliaryDirectory;
	private boolean loadMetadata;
	private boolean readOnly;

	private LibrisDatabaseConfiguration() {
		super();
		readOnly = false;
		auxiliaryDirectory = null;
		myDatabaseFile = null;
		repositoryDirectory = null;
		loadMetadata = true;
	}

	public LibrisDatabaseConfiguration(File theDatabaseFile) throws DatabaseException {
		this();
		myDatabaseFile = theDatabaseFile;
	}

	public LibrisDatabaseConfiguration(File theDatabaseFile, boolean readOnly) {
		this();
		setReadOnly(readOnly);
		myDatabaseFile = theDatabaseFile;
	}

	public void setDatabaseFile(File databaseFile) {
		this.myDatabaseFile = databaseFile;
	}

	public void setAuxiliaryDirectory(File auxiliaryDirectory) {
		this.auxiliaryDirectory = auxiliaryDirectory;
	}

	public File getAuxiliaryDirectory() {
		return auxiliaryDirectory;
	}

	public boolean isLoadMetadata() {
		return loadMetadata;
	}

	public void setLoadMetadata(boolean loadMetadata) {
		this.loadMetadata = loadMetadata;
	}

	public File getDatabaseFile() {
		return myDatabaseFile;
	}

	public File getRepositoryDirectory() {
		return repositoryDirectory;
	}

	public void setRepositoryDirectory(File repositoryDir) {
		repositoryDirectory = repositoryDir;
	}

	public boolean isReadOnly() {
		return readOnly;
	}

	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}
}

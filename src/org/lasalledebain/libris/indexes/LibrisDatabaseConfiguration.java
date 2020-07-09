package org.lasalledebain.libris.indexes;

import java.io.File;

import org.lasalledebain.libris.Schema;
import org.lasalledebain.libris.exception.DatabaseException;

public class LibrisDatabaseConfiguration extends DatabaseConfiguration {
	private File myDatabaseFile;
	private File artifactDirectory;
	private File auxiliaryDirectory;
	private boolean loadMetadata;
	Schema databaseSchema;
	private boolean readOnly;

	private LibrisDatabaseConfiguration() {
		super();
		readOnly = false;
		auxiliaryDirectory = null;
		myDatabaseFile = null;
		databaseSchema = null;
		artifactDirectory = null;
		loadMetadata = true;
	}

	public LibrisDatabaseConfiguration(File theDatabaseFile) throws DatabaseException {
		this();
		myDatabaseFile = theDatabaseFile;
	}

	public LibrisDatabaseConfiguration(File theDatabaseFile, boolean readOnly, Schema schem) {
		this();
		setReadOnly(readOnly);
		myDatabaseFile = theDatabaseFile;
		databaseSchema = schem;
	}

	public void setDatabaseFile(File databaseFile) {
		this.myDatabaseFile = databaseFile;
	}

	public Schema getDatabaseSchema() {
		return databaseSchema;
	}

	public void setDatabaseSchema(Schema databaseSchema) {
		this.databaseSchema = databaseSchema;
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

	public File getArtifactDirectory() {
		return artifactDirectory;
	}

	public void setArtifactDirectory(File artifactDirectory) {
		this.artifactDirectory = artifactDirectory;
	}

	public boolean isReadOnly() {
		return readOnly;
	}

	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}
}

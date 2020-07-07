package org.lasalledebain.libris.indexes;

import java.io.File;

import org.lasalledebain.libris.FileManager;
import org.lasalledebain.libris.Schema;
import org.lasalledebain.libris.exception.DatabaseException;

public class LibrisDatabaseConfiguration extends DatabaseConfiguration {
	private File databaseFile;
	private File artifactDirectory;
	private File auxiliaryDirectory;
	boolean loadMetadata;
	Schema databaseSchema;

	public LibrisDatabaseConfiguration(File databaseFile) throws DatabaseException {
		this();
		this.databaseFile = databaseFile;
		File artifactDirectoryFile = FileManager.getDefautlArtifactsDirectory(databaseFile);
		artifactDirectory = artifactDirectoryFile;
	}

	private LibrisDatabaseConfiguration() {
		super();
		databaseFile = null;
		databaseSchema = null;
		artifactDirectory = null;
		loadMetadata = true;
	}

	public LibrisDatabaseConfiguration(File theDatabaseFile, boolean readOnly, Schema schem) {
		super();
		setReadOnly(readOnly);
		databaseFile = theDatabaseFile;
		databaseSchema = schem;
	}

	public void setDatabaseFile(File databaseFile) {
		this.databaseFile = databaseFile;
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
		return databaseFile;
	}

	public File getArtifactDirectory() {
		return artifactDirectory;
	}

	public void setArtifactDirectory(File artifactDirectory) {
		this.artifactDirectory = artifactDirectory;
	}
}

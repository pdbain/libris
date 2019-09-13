package org.lasalledebain.libris.indexes;

import java.io.File;

import org.lasalledebain.libris.LibrisDatabase;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.ui.HeadlessUi;
import org.lasalledebain.libris.ui.LibrisUi;

public class LibrisIndexConfiguration extends IndexConfiguration {
	private File databaseFile;
	private File artifactDirectory;
	boolean loadMetadata;
	public LibrisIndexConfiguration(LibrisUi theUi) {
		super(theUi);
	}

	public LibrisIndexConfiguration(File databaseFile, LibrisUi databaseUi) throws DatabaseException {
		this(databaseUi);
		loadMetadata = true;
		this.databaseFile = databaseFile;
		File artifactDirectoryFile = getDefautlArtifactsDirectory(databaseFile);
		artifactDirectory = artifactDirectoryFile;
	}

	public static File getDefautlArtifactsDirectory(File databaseFile) throws DatabaseException {
		File artifactDirectoryFile =LibrisDatabase.getDatabaseAuxDirectory(databaseFile, "artifacts");
		return artifactDirectoryFile;
	}

	public static String foo(File databaseFile) {
		return databaseFile.getName() + "_artifacts";
	}

	public LibrisIndexConfiguration(File databaseFile) {
		this(new HeadlessUi(databaseFile, false));
		this.databaseFile = databaseFile;
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

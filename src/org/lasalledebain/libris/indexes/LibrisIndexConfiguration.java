package org.lasalledebain.libris.indexes;

import java.io.File;

import org.lasalledebain.libris.ui.HeadlessUi;
import org.lasalledebain.libris.ui.LibrisUi;

public class LibrisIndexConfiguration extends IndexConfiguration {
	private File databaseFile;
	private File artifactDirectory;
	boolean loadMetadata;
	public LibrisIndexConfiguration(LibrisUi theUi) {
		super(theUi);
	}

	public LibrisIndexConfiguration(File databaseFile, LibrisUi databaseUi) {
		this(databaseUi);
		loadMetadata = true;
		this.databaseFile = databaseFile;
		File parentDirectory = databaseFile.getParentFile();
		String artifactDirectoryName = databaseFile.getName() + "_artifacts";
		artifactDirectory = new File(parentDirectory, artifactDirectoryName);
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

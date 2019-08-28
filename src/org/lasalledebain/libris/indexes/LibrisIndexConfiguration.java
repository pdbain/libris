package org.lasalledebain.libris.indexes;

import java.io.File;

import org.lasalledebain.libris.ui.HeadlessUi;
import org.lasalledebain.libris.ui.LibrisUi;

public class LibrisIndexConfiguration extends IndexConfiguration {
	private File databaseFile;
	boolean loadMetadata;
	public LibrisIndexConfiguration(LibrisUi theUi) {
		super(theUi);
	}

	public LibrisIndexConfiguration(File databaseFile, LibrisUi databaseUi) {
		this(databaseUi);
		loadMetadata = true;
		this.databaseFile = databaseFile;
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
}

package org.lasalledebain.libris.indexes;

import java.io.File;
import java.util.Objects;
import java.util.function.Supplier;

import org.lasalledebain.libris.ui.HeadlessUi;
import org.lasalledebain.libris.ui.LibrisUi;

public class IndexConfiguration {
	private final File databaseFile;
	int signatureLevels;
	LibrisUi databaseUi;
	boolean loadMetadata;

	public boolean isLoadMetadata() {
		return loadMetadata;
	}

	public void setLoadMetadata(boolean loadMetadata) {
		this.loadMetadata = loadMetadata;
	}

	public IndexConfiguration(File databaseFile, LibrisUi databaseUi) {
		this(databaseFile);
		this.databaseUi = databaseUi;
		loadMetadata = true;
	}
	
	public IndexConfiguration(File databaseFile) {
		this.databaseFile = databaseFile;
		signatureLevels = 0;
	}

	public LibrisUi getDatabaseUi() {
		if (Objects.isNull(databaseUi)) {
			databaseUi = new HeadlessUi(databaseFile, false);

		}
		return databaseUi;
	}

	public void setDatabaseUi(LibrisUi databaseUi) {
		this.databaseUi = databaseUi;
	}

	public int getSignatureLevels() {
		return signatureLevels;
	}

	public void setSignatureLevels(int signatureLevels) {
		this.signatureLevels = signatureLevels;
	}

	public File getDatabaseFile() {
		return databaseFile;
	}
}

package org.lasalledebain.libris.indexes;

import java.io.File;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;

import org.lasalledebain.libris.ui.HeadlessUi;
import org.lasalledebain.libris.ui.LibrisUi;
import org.lasalledebain.libris.util.Reporter;

public class IndexConfiguration {
	public static String SIGNATURE_LEVELS = "signature.levels";
	public static String TERMCOUNT_BUCKETS = "termcount.buckets";
	private final File databaseFile;
	final Properties config;
	LibrisUi databaseUi;
	boolean loadMetadata;
	public final Reporter indexingReporter;
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
		config = new Properties();
		indexingReporter = new Reporter();
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

	public Optional<Integer> getAttribute(final String attribute) {
		if (config.containsKey(attribute)) {
			final Optional<Integer> value = Optional.of(Integer.parseInt(config.getProperty(attribute)));
			return value;
		} else {
			return Optional.empty();
		}
	}

	public void setAttribute(String attributeName, int value) {
		config.setProperty(attributeName, Integer.toString(value));
	}

	public File getDatabaseFile() {
		return databaseFile;
	}
}

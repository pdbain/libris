package org.lasalledebain.libris.indexes;

import java.util.Optional;
import java.util.Properties;

import org.lasalledebain.libris.ui.LibrisUi;
import org.lasalledebain.libris.util.Reporter;

public class IndexConfiguration {

	public static String SIGNATURE_LEVELS = "signature.levels";
	public static String TERMCOUNT_BUCKETS = "termcount.buckets";
	protected final LibrisUi myUi;
	protected final Reporter indexingReporter;
	protected final Properties config;

	public IndexConfiguration(LibrisUi theUi) {
		myUi = theUi;
		indexingReporter = new Reporter();
		config = new Properties();
	}

	public LibrisUi getDatabaseUi() {
		return myUi;
	}

	public Reporter getIndexingReporter() {
		return indexingReporter;
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

}

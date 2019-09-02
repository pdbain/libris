package org.lasalledebain.libris.indexes;

import java.util.Optional;
import java.util.Properties;

import org.lasalledebain.libris.ui.LibrisUi;
import org.lasalledebain.libris.util.Reporter;

public class IndexConfiguration {

	protected final LibrisUi myUi;
	protected final Reporter indexingReporter;
	protected final Properties config;
	private Optional<Integer> signatureLevels;
	private Optional<Integer> termcountBuckets;

	public IndexConfiguration(LibrisUi theUi) {
		myUi = theUi;
		indexingReporter = new Reporter();
		config = new Properties();
		signatureLevels = Optional.empty();
		termcountBuckets = Optional.empty();
	}

	public LibrisUi getDatabaseUi() {
		return myUi;
	}

	public Reporter getIndexingReporter() {
		return indexingReporter;
	}

	public Optional<Integer> getSignatureLevels() {
		return signatureLevels;
	}

	public void setSignatureLevels(int signatureLevels) {
		this.signatureLevels = Optional.of(signatureLevels);
	}

	public Optional<Integer> getTermcountBuckets() {
		return termcountBuckets;
	}

	public void setTermcountBuckets(int termcountBuckets) {
		this.termcountBuckets = Optional.of(termcountBuckets);
	}

}

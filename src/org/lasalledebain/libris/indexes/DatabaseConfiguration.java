package org.lasalledebain.libris.indexes;

import java.util.Optional;

import org.lasalledebain.libris.ui.DatabaseUi;
import org.lasalledebain.libris.util.Reporter;

public class DatabaseConfiguration {

	protected final Reporter indexingReporter;
	private Optional<Integer> signatureLevels;
	private Optional<Integer> termcountBuckets;
	private boolean readOnly;

	public DatabaseConfiguration() {
		indexingReporter = new Reporter();
		signatureLevels = Optional.empty();
		termcountBuckets = Optional.empty();
		readOnly = false;
	}

	public boolean isReadOnly() {
		return readOnly;
	}

	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
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

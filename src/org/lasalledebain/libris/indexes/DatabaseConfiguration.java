package org.lasalledebain.libris.indexes;

import java.util.Optional;

import org.lasalledebain.libris.util.Reporter;

public class DatabaseConfiguration {

	protected final Reporter indexingReporter;
	private Optional<Integer> signatureLevels;
	private Optional<Integer> termcountBuckets;
	public DatabaseConfiguration() {
		indexingReporter = new Reporter();
		signatureLevels = Optional.empty();
		termcountBuckets = Optional.empty();
	}

	public Reporter getIndexingReporter() {
		return indexingReporter;
	}

	public Optional<Integer> getSignatureLevels() {
		return signatureLevels;
	}

	public void setSignatureLevels(int signatureLevels) {
		this.signatureLevels = Optional.of(signatureLevels);
		indexingReporter.reportValue(Reporter.INDEXING_SIGNATURE_LEVELS_CONFIGURATION, signatureLevels);
	}

	public Optional<Integer> getTermcountBuckets() {
		return termcountBuckets;
	}

	public void setTermcountBuckets(int termcountBuckets) {
		this.termcountBuckets = Optional.of(termcountBuckets);
		indexingReporter.reportValue(Reporter.INDEXING_TERMCOUNT_BUCKETS_CONFIGURATION, termcountBuckets);
	}

}

package org.lasalledebain.libris.util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.lasalledebain.libris.LibrisMetadata;

public class Reporter {

	public static final String INDEXING_SIGNATURE_LEVELS_CONFIGURATION = "signature.levels.configuration",
			INDEXING_TERMCOUNT_BUCKETS_CONFIGURATION = "termcount.buckets.configuration";
	public static final String INDEXING_SIGNATURE_LEVELS_ACTUAL = "signature.levels.actual",
			INDEXING_TERMCOUNT_BUCKETS_ACTUAL = "termcount.buckets.actual";
	public static final String INDEXING_RECORDS_NUM_RECS = "records.num",
			INDEXING_KEYWORD_COUNT = "keyword.count";
	public static final String INDEXING_NUM_TERMS = "terms.num.total",
			INDEXING_NUM_UNIQUE_TERMS = "terms.num.unique", INDEXING_TERMS_BUCKETS_NUM = "terms.buckets.num",
			INDEXING_TERMS_BUCKETS_EXPANSION = "terms.buckets.expansion.num", INDEXING_TERMS_BUCKETS_LOADS = "terms.buckets.load.num",
			INDEXING_TERMS_BUCKETS_FLUSHES = "terms.buckets.flush.num";
	public static final String INDEXING_RECPOS_BUCKETS_FLUSHES = "recordpositions.buckets.flush.num",
			INDEXING_RECPOS_BUCKETS_LOADS = "recordpositions.buckets.load.num",
			INDEXING_RECPOS_BUCKETS_EXPANSION = "recordpositions.buckets.expansion.num";
	
	final Properties indexingReport;
	public Reporter() {
		indexingReport = new Properties();
	}

	public void reportValue(String key, long value) {
		indexingReport.setProperty(key, Long.toString(value));
	}

	public void writeReport(FileOutputStream reportFile) throws IOException {
		indexingReport.store(reportFile, "Indexing report "+LibrisMetadata.getCurrentDateAndTimeString());
	}
}

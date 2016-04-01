package org.lasalledebain.libris;

public interface LibrisConstants {

	public static final String BOOLEAN_FALSE_STRING = "false";
	public static final String BOOLEAN_TRUE_STRING = "true";
	public static final String PROPERTY_LAST_RECORD_ID = "libris.database.lastrecordid";
	public static final String PROPERTY_RECORD_COUNT = "libris.database.recordcount";
	public static final String PROPERTY_DATABASE_BRANCH = "libris.database.databasebranch";
	public static final String PROPERTY_DATABASE_LAST_CHILD = "libris.database.databaselastchild";
	public static final String PROPERTY_LAST_OPENED = "libris.database.lastopened";
	public static final String PROPERTY_LAST_SAVED = "libris.database.lastsaved";
	public static final String FILENAME_JOURNAL_SUFFIX = "lbrj";
	public static final String FILENAME_NATIVE_RECORDS_SUFFIX = "lbrn";
	public static final String FILENAME_XML_FILES_SUFFIX = "libr";
	public static int NULL_RECORD_ID = 0;
	public static int DATABASE_ROOT_BRANCH_ID = 0;
	public static String LIBRIS_LOGGING_LEVEL = "libris.logging.level";

	/**
	 * describe how the database is being used: GUI, command line, batch
	 *
	 */
	public enum DatabaseUsageMode {
		USAGE_BATCH, USAGE_CMDLINE, USAGE_GUI
	}

	public static final short NULL_FIELD_NUM = -1;
	public static final short ENUM_VALUE_OUT_OF_RANGE = -1;
}

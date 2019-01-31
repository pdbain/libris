package org.lasalledebain.libris;

public interface LibrisConstants {

	public static final int LONG_LEN = 8;
	public static final int INT_LEN = 4;
	public static final int SHORT_LEN = 2;
	public static final int KEY_MAX_LENGTH = 255;

	public static final String BOOLEAN_FALSE_STRING = "false";
	public static final String BOOLEAN_TRUE_STRING = "true";
	public static final String PROPERTY_LAST_RECORD_ID = "libris.database.lastrecordid";
	public static final String PROPERTY_RECORD_COUNT = "libris.database.recordcount";
	public static final String PROPERTY_LAST_OPENED = "libris.database.lastopened";
	public static final String PROPERTY_LAST_SAVED = "libris.database.lastsaved";
	public static final String PROPERTY_SIGNATURE_LEVELS = "libris.database.signaturelevels";
	public static final String FILENAME_JOURNAL_SUFFIX = "lbrj";
	public static final String FILENAME_NATIVE_RECORDS_SUFFIX = "lbrn";
	public static final String FILENAME_XML_FILES_SUFFIX = "libr";
	public static final String FILENAME_INSTANCE_INCREMENT_FILES_SUFFIX = "libi";
	public static final int NULL_RECORD_ID = 0;
	public static final int MAX_RECORD_ID = Integer.MAX_VALUE;
	public static String LIBRIS_LOGGING_LEVEL = "libris.logging.level";

	enum DatabaseFormat {DBFMT_XML, DBFMT_CSV, DBFMT_NATIVE}

	public static final short NULL_FIELD_NUM = -1;
	public static final short ENUM_VALUE_OUT_OF_RANGE = -1;
	public static final byte RECORD_HAS_GROUPS = 1 << 0;
	public static final byte RECORD_HAS_NAME = 1 << 2;
	
	String DATABASE_NAME = "DATABASE";
	int NULL_GROUP = -1;
	public String YMD_TIME_TZ = LibrisConstants.YMD_TIME+" z";
	String HH_MM_SS = "HH:mm:ss";
	String YMD_DASHSEP = "yyyy-MM-dd";
	String YMD = "yyyyMMdd";
	String YMD_TIME = YMD_DASHSEP+" "+HH_MM_SS;
	String DATABASE_OR_RECORD_ARE_READ_ONLY = "Database or record are read-only";
	String COULD_NOT_OPEN_SCHEMA_FILE = "could not open schema file "; //$NON-NLS-1$
	int MINIMUM_TERM_LENGTH = 2;
	int[] emptyIntList = new int[0];
	String DATABASE_FILE = "DATABASE_FILE"; //$NON-NLS-1$
	String REPO_DB = "repo_db";
	public static final String SCHEMA_NAME = "SCHEMA";
	public static final String AUX_DIRECTORY_NAME = ".libris_auxfiles";
	public static final String POSITION_FILENAME = "positions";
	public static final String PROPERTIES_FILENAME = "properties";
	public static final String RECORDS_FILENAME = "records";
	public static final String JOURNAL_FILENAME = "journal";
	public static final String LOCK_FILENAME = "databaseLock";
	public static final String NAMEDRECORDS_FILENAME_ROOT = "namedrecs_";
	public static final String SIGNATURE_FILENAME_ROOT = "signature_";
	public static final String AFFILIATES_FILENAME_ROOT = "affiliates_";
	public static final String AFFILIATES_FILENAME_HASHTABLE_ROOT = AFFILIATES_FILENAME_ROOT+"hashstable_";
	public static final String AFFILIATES_FILENAME_OVERFLOW_ROOT = AFFILIATES_FILENAME_ROOT+"overflow_";
	public static final String KEYWORDS_FILTER_FILENAME_ROOT = "keywords_filter_";
	public static final String TERM_COUNT_FILENAME_ROOT = "termcounts";
	public static final String INDEXING_REPORT_FILE = "indexReport.txt";
	public static final String IMPORT_REPORT_FILE = "importReport.txt";
	static final String TEMP_ROOT = "_TEMP_";
	static final String TEMP_CHILD_FILE = TEMP_ROOT+"child_";
	static final String TEMP_AFFILIATE_FILE = TEMP_ROOT+"affiliate_";
	public static final String CSV_FILE = "CSV_FILE";
	public static final int MAX_BLOOM_FILTER_LEVELS = 6;
}

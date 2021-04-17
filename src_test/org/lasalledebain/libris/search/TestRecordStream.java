package org.lasalledebain.libris.search;

import static org.lasalledebain.libris.Field.FieldType.T_FIELD_PAIR;
import static org.lasalledebain.libris.Field.FieldType.T_FIELD_STRING;
import static org.lasalledebain.libris.Field.FieldType.T_FIELD_TEXT;
import static org.lasalledebain.libris.util.Utilities.info;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.lasalledebain.libris.DatabaseRecord;
import org.lasalledebain.libris.Field.FieldType;
import org.lasalledebain.libris.LibrisDatabase;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.records.RecordStreamFilter;
import org.lasalledebain.libris.util.Utilities;

import junit.framework.TestCase;

public class TestRecordStream extends TestCase {

	private File workingDirectory;

	@Before
	public void setUp() throws Exception {
		info("Starting "+getName());
		workingDirectory = Utilities.makeTempTestDirectory();
	}

	@After
	public void tearDown() throws Exception {
		info("Ending "+getName());
		Utilities.deleteTestDatabaseFiles();
	}

	@Test
	public void testFilterSanity() throws FileNotFoundException, IOException, DatabaseException {
		try (LibrisDatabase db = getDatabase(Utilities.KEYWORD_DATABASE4_XML)) {
			Stream<DatabaseRecord> databaseRecords = db.getRecords().asStream();
			RecordStreamFilter<DatabaseRecord> filt = new RecordStreamFilter<DatabaseRecord>(r -> r.getRecordId() > 2);
			Stream<DatabaseRecord> filteredStream = filt.processStream(databaseRecords);
			List<DatabaseRecord> result = filteredStream.collect(Collectors.toList());
			assertEquals("Wrong number of records returned", 2, result.size());
		}
	}

	@Test
	public void testKeywordFilter() throws FileNotFoundException, IOException, DatabaseException {
		try (LibrisDatabase db = getDatabase(Utilities.EXAMPLE_DATABASE1_FILE)) {
			Stream<DatabaseRecord> databaseRecords = db.getRecords().asStream();
			Set<FieldType> searchFieldTypes = EnumSet.of(T_FIELD_STRING, T_FIELD_TEXT, T_FIELD_PAIR);
			int[] selectedFields = db.getSchema().getFieldsOfTypes(searchFieldTypes);
			TextFilter<DatabaseRecord> theFilter = new TextFilter<DatabaseRecord>(RecordFilter.MATCH_TYPE.MATCH_PREFIX, false, selectedFields, new String[] {"java", "virtual"});
			RecordStreamFilter<DatabaseRecord> filt = new RecordStreamFilter<DatabaseRecord>(theFilter);
			Stream<DatabaseRecord> filteredStream = filt.processStream(databaseRecords);
			List<DatabaseRecord> result = filteredStream.collect(Collectors.toList());
			result.forEach(r -> {
				String recordText = r.toString().toLowerCase(); 
				assertTrue("Missing Java", recordText.contains("java"));
				assertTrue("Missing virtual", recordText.contains("virtual"));});
		}
	}

	private LibrisDatabase getDatabase(String dbFile) throws FileNotFoundException, IOException {
		LibrisDatabase db = Utilities.buildTestDatabase(workingDirectory, dbFile);
		return db;
	}

}

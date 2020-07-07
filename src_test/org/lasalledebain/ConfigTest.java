package org.lasalledebain;

import static java.util.Objects.isNull;
import static org.lasalledebain.Utilities.testLogger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.lasalledebain.libris.DatabaseRecord;
import org.lasalledebain.libris.GenericDatabase;
import org.lasalledebain.libris.Libris;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.indexes.LibrisDatabaseConfiguration;
import org.lasalledebain.libris.ui.HeadlessUi;
import org.lasalledebain.libris.ui.LibrisUi;
import org.lasalledebain.libris.ui.LibrisUiGeneric;

import junit.framework.TestCase;

public class ConfigTest extends TestCase {

	private File workingDirectory;
	GenericDatabase<DatabaseRecord> currentDb;

	@Before
	public void setUp() throws Exception {
		testLogger.log(Level.INFO,"running "+getName());
		workingDirectory = Utilities.makeTempTestDirectory();
	}

	@After
	public void tearDown() throws Exception {
		assertFalse(isNull(currentDb) || currentDb.isDatabaseOpen());
		Utilities.deleteRecursively(workingDirectory);
	}

	@Test
	public void testReadOnly() throws FileNotFoundException, IOException, LibrisException {
		File testDatabaseFileCopy = Utilities.copyTestDatabaseFile(Utilities.TEST_DATABASE_WITH_REPO, workingDirectory);
		LibrisDatabaseConfiguration config = new LibrisDatabaseConfiguration(testDatabaseFileCopy);
		LibrisUiGeneric ui = new HeadlessUi();
		ui.rebuildDatabase(config);
		currentDb = ui.openDatabase(config);
		DatabaseRecord rec = currentDb.newRecord();
		assertNotNull("Record not created in read-write mode", rec);
		currentDb.closeDatabase(true);
		
		ui = new HeadlessUi();
// TODO		Libris.openDatabase(databaseFile, ui)
	}

}

package org.lasalledebain;

import static java.util.Objects.isNull;
import static org.lasalledebain.libris.util.Utilities.testLogger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.lasalledebain.libris.DatabaseRecord;
import org.lasalledebain.libris.LibrisConstants;
import org.lasalledebain.libris.LibrisDatabase;
import org.lasalledebain.libris.exception.DatabaseError;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.indexes.LibrisDatabaseConfiguration;
import org.lasalledebain.libris.ui.HeadlessUi;
import org.lasalledebain.libris.ui.AbstractUi;
import org.lasalledebain.libris.util.Utilities;

import junit.framework.TestCase;

@SuppressWarnings("rawtypes")
public class ConfigTest extends TestCase {

	private File workingDirectory;
	LibrisDatabase currentDb;

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
		AbstractUi ui = new HeadlessUi();
		ui.rebuildDatabase(config);
		currentDb = ui.openDatabase(config);
		DatabaseRecord rec = currentDb.newRecord();
		assertNotNull("Record not created in read-write mode", rec);
		currentDb.closeDatabase(true);
		try {
			rec = currentDb.getRecord(2);
			assertNull("Record retrieved from closed database", rec);
		} catch (DatabaseError e) {/* ignore */}

		try {
			rec = currentDb.newRecord();
			assertNull("Record created in closed database", rec);
		} catch (DatabaseError e) {/* ignore */}

		ui = new HeadlessUi();
		config.setReadOnly(true);
		ui.openDatabase(config);
		currentDb = ui.getLibrisDatabase();
		try {
			rec = currentDb.newRecord();
			assertNull("Record created in read-only mode", rec);
		} catch (DatabaseError e) {/* ignore */}
		currentDb.closeDatabase(true);
	}

	@Test
	public void testAuxDir() throws FileNotFoundException, IOException, LibrisException {
		File dbDir = new File(workingDirectory, "database");
		assertTrue("Cannot create database directory", dbDir.mkdir());
		File auxDir = new File(workingDirectory, "aux");
		assertTrue("Cannot create aux directory", auxDir.mkdir());
		File testDatabaseFileCopy = Utilities.copyTestDatabaseFile(Utilities.TEST_DATABASE_WITH_REPO, dbDir);
		LibrisDatabaseConfiguration config = new LibrisDatabaseConfiguration(testDatabaseFileCopy);
		config.setAuxiliaryDirectory(auxDir);
		AbstractUi ui = new HeadlessUi();
		ui.rebuildDatabase(config);
		currentDb = ui.openDatabase(config);
		DatabaseRecord rec = currentDb.newRecord();
		rec = currentDb.getRecord(3);
		assertNotNull("Record not created in read-write mode", rec);
		assertEquals("Wrong number of files in working directory", 2, workingDirectory.list().length);
		assertEquals("Wrong number of files in database directory", 2, dbDir.list().length);
		assertTrue("Wrong number of files in auxiliary directory", auxDir.list().length > 5);
		currentDb.closeDatabase(true);
	}
	@Test
	public void testAuxArtDir() throws FileNotFoundException, IOException, LibrisException {
		File dbDir = new File(workingDirectory, "database");
		File myArtifact = Utilities.copyTestDatabaseFile(Utilities.EXAMPLE_ARTIFACT_PDF, workingDirectory);
		assertTrue("Cannot create database directory", dbDir.mkdir());
		File auxDir = new File(workingDirectory, "aux");
		assertTrue("Cannot create aux directory", auxDir.mkdir());
		File artDir = new File(workingDirectory, "art");
		assertTrue("Cannot create aux directory", artDir.mkdir());
		File testDatabaseFileCopy = Utilities.copyTestDatabaseFile(Utilities.TEST_DATABASE_WITH_REPO, dbDir);
		
		LibrisDatabaseConfiguration config = new LibrisDatabaseConfiguration(testDatabaseFileCopy);
		config.setAuxiliaryDirectory(auxDir);
		config.setRepositoryDirectory(artDir);
		AbstractUi ui = new HeadlessUi();
		ui.rebuildDatabase(config);
		currentDb = ui.openDatabase(config);
		DatabaseRecord rec = currentDb.newRecord();
		rec = currentDb.getRecord(3);
		rec.setEditable(true);
		currentDb.addArtifact(rec, myArtifact);
		currentDb.putRecord(rec);
		currentDb.save();
		assertEquals("Wrong number of files in working directory", 4, workingDirectory.list().length);
		assertEquals("Wrong number of files in database directory", 1, dbDir.list().length);
		boolean found = false;
		File repoDir = new File(artDir, LibrisConstants.ARTIFACTS_REPOSITORY_DIRECTORY);
		for (String f: repoDir.list()) {
			if (f.startsWith("r_1")) {
				found = true;
				break;
			}
		}
		assertTrue("Artifact file not found in artifact directory", found);
		assertTrue("Wrong number of files in auxiliary directory", auxDir.list().length > 5);
		currentDb.closeDatabase(true);
	}
}

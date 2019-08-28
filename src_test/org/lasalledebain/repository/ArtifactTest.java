package org.lasalledebain.repository;

import static org.lasalledebain.Utilities.testLogger;

import java.io.File;
import java.net.URI;
import java.util.Objects;
import java.util.logging.Level;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.lasalledebain.Utilities;
import org.lasalledebain.libris.ArtifactDatabase;
import org.lasalledebain.libris.ArtifactParameters;
import org.lasalledebain.libris.ArtifactRecord;
import org.lasalledebain.libris.LibrisConstants;
import org.lasalledebain.libris.LibrisFileManager;
import org.lasalledebain.libris.Repository;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.indexes.IndexConfiguration;
import org.lasalledebain.libris.ui.HeadlessUi;
import org.lasalledebain.libris.ui.LibrisUi;

import junit.framework.TestCase;

public class ArtifactTest extends TestCase {

	private File workingDirectory;
	private static final String RECORD = "record_";

	@Before
	public void setUp() throws Exception {
		testLogger.log(Level.INFO,"running "+getName());
		workingDirectory = Utilities.makeTempTestDirectory();
	}

	@After
	public void tearDown() throws Exception {
		Utilities.deleteRecursively(workingDirectory);
	}

	@Test
	public void testCreateDatabase() throws LibrisException {
		final int numArtifacts = 16;
		LibrisUi myUi = new HeadlessUi(null, false);
		ArtifactDatabase db = new ArtifactDatabase(myUi, workingDirectory);
		db.initialize();
		db.openDatabase();
		ArtifactParameters testArtifacts[] = new ArtifactParameters[numArtifacts];
		for (int i = 0; i < numArtifacts; ++i) {
			final int expectedId = i + 1;
			final String fName = "test_artifact_"+expectedId;
			File testFile = new File(workingDirectory, fName);
			testFile.deleteOnExit();
			URI originalUri = testFile.toURI();
			ArtifactParameters params = new ArtifactParameters(originalUri);
			testArtifacts[i] = params;
			params.setComments("comments_"+expectedId);
			params.setDate("date_"+expectedId);
			params.setDoi("doi_"+expectedId);
			params.setKeywords("keywords_"+expectedId);
			params.setRecordName(RECORD+expectedId);
			params.setTitle("title_"+expectedId);
			ArtifactRecord rec = db.newRecord(params);
			db.putRecord(rec);
			assertEquals(expectedId, rec.getRecordId());
		}
		for (int i = 0; i < numArtifacts; ++i) {
			final int expectedId = i + 1;
			ArtifactParameters actual = db.getArtifactInfo(expectedId);
			assertEquals(testArtifacts[i], actual);
		}
		db.save();
		db = new ArtifactDatabase(myUi, workingDirectory);
		db.openDatabase();
		for (int i = 0; i < numArtifacts; ++i) {
			final int expectedId = i + 1;
			ArtifactParameters actual = db.getArtifactInfo(expectedId);
			assertEquals(testArtifacts[i], actual);
		}
	}

	@Test
	public void testImportExportRecords() {
		fail("not implemented");
	}
	@Test
	public void testGroups() throws LibrisException {
		final int numArtifacts = 16;
		LibrisUi myUi = new HeadlessUi(null, false);
		ArtifactDatabase db = new ArtifactDatabase(myUi, workingDirectory);
		db.initialize();
		db.openDatabase();
		String groupId = db.getSchema().getGroupId(0);
		ArtifactParameters testArtifacts[] = new ArtifactParameters[numArtifacts];
		for (int i = 0; i < numArtifacts; ++i) {
			final int expectedId = i + 1;
			final String fName = "test_artifact_"+expectedId;
			File testFile = new File(workingDirectory, fName);
			testFile.deleteOnExit();
			URI originalUri = testFile.toURI();
			ArtifactParameters params = new ArtifactParameters(originalUri);
			testArtifacts[i] = params;
			params.setRecordName(RECORD+expectedId);
			params.setTitle("title_"+expectedId);
			ArtifactRecord rec = db.newRecord(params);
			if (expectedId > 1) {
				ArtifactRecord parent = db.getRecord(expectedId/2);
				if (Objects.nonNull(parent.getName()) && ((expectedId %2) == 0)) {
					rec.setParent(groupId, parent);
				} else {
					rec.setParent(0, parent.getRecordId());
				}
				assertEquals("wrong parent for "+expectedId, expectedId/2, rec.getParent(0));
				params.setRecordParentName(parent.getName());
				params.setParentId(parent.getRecordId());
			}
			db.putRecord(rec);
			assertEquals(expectedId, rec.getRecordId());
		}
		for (int i = 0; i < numArtifacts; ++i) {
			final int expectedId = i + 1;
			ArtifactParameters actual = db.getArtifactInfo(expectedId);
			assertEquals(testArtifacts[i], actual);
		}
		db.save();
		db = new ArtifactDatabase(myUi, workingDirectory);
		db.openDatabase();
		for (int i = 0; i < numArtifacts; ++i) {
			final int expectedId = i + 1;
			ArtifactRecord actual = db.getRecord(expectedId);
			int numAff = actual.getNumAffiliatesAndParent(0);
			if (1 == expectedId) {
				assertEquals("Wrong affiliate list size for record 1", 0, numAff);
				assertEquals("wrong parent for "+expectedId, 0, actual.getParent(0));
			} else {
				assertEquals("Wrong affiliate list size for record "+expectedId, 1, numAff);
				assertEquals("wrong parent for "+expectedId, expectedId/2, actual.getParent(0));
		
			}
		}
	}
}

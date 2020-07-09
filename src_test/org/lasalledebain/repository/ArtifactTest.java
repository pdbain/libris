package org.lasalledebain.repository;

import static org.lasalledebain.Utilities.testLogger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.util.Objects;
import java.util.logging.Level;

import javax.xml.stream.XMLStreamException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.lasalledebain.Utilities;
import org.lasalledebain.libris.ArtifactDatabase;
import org.lasalledebain.libris.ArtifactParameters;
import org.lasalledebain.libris.ArtifactRecord;
import org.lasalledebain.libris.FilteredRecordList;
import org.lasalledebain.libris.GenericDatabase;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.indexes.DatabaseConfiguration;
import org.lasalledebain.libris.search.RecordFilter.MATCH_TYPE;
import org.lasalledebain.libris.ui.HeadlessUi;
import org.lasalledebain.libris.ui.DatabaseUi;
import org.lasalledebain.libris.xmlUtils.ElementManager;
import org.lasalledebain.libris.xmlUtils.ElementWriter;
import org.lasalledebain.libris.xmlUtils.LibrisXMLConstants;
import org.lasalledebain.libris.xmlUtils.XmlShapes;

import junit.framework.TestCase;

public class ArtifactTest extends TestCase {

	private static final String EVEN_KEYWORD = "even";
	private static final String TITLE_PREFIX = "title_";
	private File workingDirectory;
	private static final String RECORD = "record_";

	@Before
	public void setUp() throws Exception {
		testLogger.log(Level.INFO, "running " + getName());
		workingDirectory = Utilities.makeTempTestDirectory();
	}

	@After
	public void tearDown() throws Exception {
		Utilities.deleteRecursively(workingDirectory);
	}

	@Test
	public void testCreateDatabase() throws LibrisException {
		final int numArtifacts = 16;
		DatabaseUi myUi = new HeadlessUi(null, false);
		ArtifactDatabase db = new ArtifactDatabase(myUi, workingDirectory);
		db.initialize();
		db.openDatabase();
		ArtifactParameters testArtifacts[] = new ArtifactParameters[numArtifacts];
		for (int i = 0; i < numArtifacts; ++i) {
			final int expectedId = i + 1;
			final String fName = "test_artifact_" + expectedId;
			File testFile = new File(workingDirectory, fName);
			testFile.deleteOnExit();
			URI originalUri = testFile.toURI();
			ArtifactParameters params = new ArtifactParameters(originalUri);
			testArtifacts[i] = params;
			params.setComments("comments_" + expectedId);
			params.setDate("date_" + expectedId);
			params.setDoi("doi_" + expectedId);
			params.setKeywords("keywords_" + expectedId);
			params.setRecordName(RECORD + expectedId);
			params.setTitle(TITLE_PREFIX + expectedId);
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
	public void testImportExportRecords() throws LibrisException, IOException, XMLStreamException {
		final int numArtifacts = 16;
		DatabaseUi myUi = new HeadlessUi(null, false);
		ArtifactDatabase db = new ArtifactDatabase(myUi, workingDirectory);
		db.initialize();
		db.openDatabase();
		String groupId = db.getSchema().getGroupId(0);
		ArtifactParameters testArtifacts[] = new ArtifactParameters[numArtifacts];
		for (int i = 0; i < numArtifacts; ++i) {
			final int expectedId = i + 1;
			final String fName = "test_artifact_" + expectedId;
			File testFile = new File(workingDirectory, fName);
			testFile.deleteOnExit();
			URI originalUri = testFile.toURI();
			ArtifactParameters params = new ArtifactParameters(originalUri);
			testArtifacts[i] = params;
			params.setRecordName(RECORD + expectedId);
			params.setTitle(TITLE_PREFIX + expectedId);
			ArtifactRecord rec = db.newRecord(params);
			if (expectedId > 1) {
				ArtifactRecord parent = db.getRecord(expectedId / 2);
				if (Objects.nonNull(parent.getName()) && ((expectedId % 2) == 0)) {
					rec.setParent(groupId, parent);
				} else {
					rec.setParent(0, parent.getRecordId());
				}
				params.setRecordParentName(parent.getName());
				params.setParentId(parent.getRecordId());
			}
			db.putRecord(rec);
		}
		db.save();
		File exportFile = new File(workingDirectory, "database.xml");
		try (FileOutputStream eStream = new FileOutputStream(exportFile)) {
			ElementWriter eWriter = ElementWriter.eventWriterFactory(eStream);
			db.toXml(eWriter);
			db = new ArtifactDatabase(myUi, workingDirectory);
			db.initialize();
			
		}
		try (FileReader reader = new FileReader(exportFile)) {
			ElementManager mgr = GenericDatabase.getXmlFactory().makeElementManager(reader, exportFile.getAbsolutePath(),
					LibrisXMLConstants.XML_ARTIFACTS_TAG, new XmlShapes(XmlShapes.SHAPE_LIST.ARTIFACTS_SHAPES));
			db.fromXml(mgr);
			db.openDatabase();
			for (int i = 0; i < numArtifacts; ++i) {
				final int expectedId = i + 1;
				ArtifactRecord actual = db.getRecord(expectedId);
				int numAff = actual.getNumAffiliatesAndParent(0);
				if (1 == expectedId) {
					assertEquals("Wrong affiliate list size for record 1", 0, numAff);
					assertEquals("wrong parent for " + expectedId, 0, actual.getParent(0));
				} else {
					assertEquals("Wrong affiliate list size for record " + expectedId, 1, numAff);
					assertEquals("wrong parent for " + expectedId, expectedId / 2, actual.getParent(0));

				}
			}
		}
	}

	@Test
	public void testIndex() throws LibrisException, IOException, XMLStreamException {
		final int numArtifacts = 16;
		DatabaseUi myUi = new HeadlessUi(null, false);
		ArtifactDatabase db = new ArtifactDatabase(myUi, workingDirectory);
		db.initialize();
		db.openDatabase();
		ArtifactParameters testArtifacts[] = new ArtifactParameters[numArtifacts];
		for (int i = 0; i < numArtifacts; ++i) {
			final int expectedId = i + 1;
			final String fName = "test_artifact_" + expectedId;
			File testFile = new File(workingDirectory, fName);
			testFile.deleteOnExit();
			URI originalUri = testFile.toURI();
			ArtifactParameters params = new ArtifactParameters(originalUri);
			testArtifacts[i] = params;
			params.setRecordName(RECORD + expectedId);
			params.setTitle(TITLE_PREFIX + expectedId);
			if (0 == (expectedId % 2)) {
				params.setKeywords(EVEN_KEYWORD);
			}
			ArtifactRecord rec = db.newRecord(params);
			db.putRecord(rec);
		}
		db.save();
		File exportFile = new File(workingDirectory, "database.xml");
		try (FileOutputStream eStream = new FileOutputStream(exportFile)) {
			ElementWriter eWriter = ElementWriter.eventWriterFactory(eStream);
			db.toXml(eWriter);
			db = new ArtifactDatabase(myUi, workingDirectory);
			db.initialize();
			
		}
		try (FileReader reader = new FileReader(exportFile)) {
			ElementManager mgr = GenericDatabase.getXmlFactory().makeElementManager(reader, exportFile.getAbsolutePath(),
					LibrisXMLConstants.XML_ARTIFACTS_TAG, new XmlShapes(XmlShapes.SHAPE_LIST.ARTIFACTS_SHAPES));
			db.fromXml(mgr);
			DatabaseConfiguration config = new DatabaseConfiguration();
			config.setSignatureLevels(2);
			db.buildIndexes(config);
			db.openDatabase();
			FilteredRecordList<ArtifactRecord> filteredList = db.makeKeywordFilteredRecordList(MATCH_TYPE.MATCH_EXACT, true, new int[] {ArtifactDatabase.TITLE_FIELD}, TITLE_PREFIX+3);
			int recordCount = 0;
			for (ArtifactRecord rec: filteredList) {
				++recordCount;
				assertEquals("wrong record returned", 3, rec.getRecordId());
			}
			assertEquals("wrong number of records returned", 1, recordCount);
			filteredList = db.makeKeywordFilteredRecordList(MATCH_TYPE.MATCH_EXACT, true, new int[] {ArtifactDatabase.KEYWORDS_FIELD}, 
					EVEN_KEYWORD);
			recordCount = 0;
			for (ArtifactRecord rec: filteredList) {
				++recordCount;
				assertTrue("wrong record " + rec.getRecordId() + " returned", 0 == (rec.getRecordId() % 2));
			}
			assertEquals("wrong number of records returned for Keywords", numArtifacts/2, recordCount);
		}
	}

	@Test
	public void testGroups() throws LibrisException {
		final int numArtifacts = 16;
		DatabaseUi myUi = new HeadlessUi(null, false);
		ArtifactDatabase db = new ArtifactDatabase(myUi, workingDirectory);
		db.initialize();
		db.openDatabase();
		String groupId = db.getSchema().getGroupId(0);
		ArtifactParameters testArtifacts[] = new ArtifactParameters[numArtifacts];
		for (int i = 0; i < numArtifacts; ++i) {
			final int expectedId = i + 1;
			final String fName = "test_artifact_" + expectedId;
			File testFile = new File(workingDirectory, fName);
			testFile.deleteOnExit();
			URI originalUri = testFile.toURI();
			ArtifactParameters params = new ArtifactParameters(originalUri);
			testArtifacts[i] = params;
			params.setRecordName(RECORD + expectedId);
			params.setTitle(TITLE_PREFIX + expectedId);
			ArtifactRecord rec = db.newRecord(params);
			if (expectedId > 1) {
				ArtifactRecord parent = db.getRecord(expectedId / 2);
				if (Objects.nonNull(parent.getName()) && ((expectedId % 2) == 0)) {
					rec.setParent(groupId, parent);
				} else {
					rec.setParent(0, parent.getRecordId());
				}
				assertEquals("wrong parent for " + expectedId, expectedId / 2, rec.getParent(0));
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
				assertEquals("wrong parent for " + expectedId, 0, actual.getParent(0));
			} else {
				assertEquals("Wrong affiliate list size for record " + expectedId, 1, numAff);
				assertEquals("wrong parent for " + expectedId, expectedId / 2, actual.getParent(0));

			}
		}
	}
}

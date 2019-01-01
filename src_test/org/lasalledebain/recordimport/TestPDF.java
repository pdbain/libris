package org.lasalledebain.recordimport;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.lasalledebain.Utilities;
import org.lasalledebain.libris.LibrisDatabase;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.Repository;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.records.PdfRecordImporter;

import junit.framework.TestCase;

public class TestPDF extends TestCase {

	private LibrisDatabase db;
	private File workdir;
	private File repoFile;
	private File repoRoot;
	private Repository repo;

	@Before
	public void setUp() throws Exception {
		workdir = Utilities.makeTempTestDirectory();
		db = Utilities.buildTestDatabase(Utilities.KEYWORD_DATABASE4_XML);
		repoFile = new File(workdir, "test_repo");
		repoFile.delete();
		repoFile.deleteOnExit();
		repoRoot = new File(workdir, "root");
		assertTrue("could not create database", Repository.initialize(repoFile));
		repo = Repository.open(repoFile, repoRoot, false);
	}

	@After
	public void tearDown() throws Exception {
		assertTrue("Could not close database", db.closeDatabase(false));
		db = null;
		Utilities.deleteRecursively(workdir);
		Utilities.deleteTestDatabaseFiles();
	}

	@Test
	public void testImportDocument() {
		try {
			short keywordField = db.getSchema().getFieldNum("ID_keywords");
			short abstractField = db.getSchema().getFieldNum("ID_text");
			PdfRecordImporter importer = new PdfRecordImporter(db, repo,keywordField, abstractField);
			Record rec = db.newRecord();
			File testPdf = Utilities.getTestDatabase(Utilities.EXAMPLE_ARTIFACT_PDF);
			importer.importDocument(testPdf.toURI(), t -> 1, rec);
			String keywordsText = rec.getField(keywordField).getValuesAsString();
			String abstractText = rec.getField(abstractField).getValuesAsString();
			for (String s: new String[] {"now", "is", "the", "time"}) {
				assertTrue("Missing keyword "+s, keywordsText.contains(s));
			}
			assertTrue("Abstract malformed: "+abstractText, 
					abstractText.contains("Now is the time for all good men to come to the aid of the party."));
		} catch (LibrisException | IOException e) {
			fail("unexpected exception"+e.getMessage());
		}
	}
	@Test
	public void testImportLargeDocument() {
		try {
			short keywordField = db.getSchema().getFieldNum("ID_keywords");
			short abstractField = db.getSchema().getFieldNum("ID_text");
			PdfRecordImporter importer = new PdfRecordImporter(db, repo,keywordField, abstractField);
			Record rec = db.newRecord();
			File testPdf = Utilities.getTestDatabase(Utilities.EXAMPLE_LARGE_PDF);
			importer.importDocument(testPdf.toURI(), t -> 1, rec);
			String keywordsText = rec.getField(keywordField).getValuesAsString();
			String abstractText = rec.getField(abstractField).getValuesAsString();
			assertTrue("Abstract malformed: "+abstractText, 
					abstractText.contains("a number of problems arise which have\n"
							+ "not been adequately dealt with: the semantics of nested monitor calls; the various ways of\n"
							+ "defining the meaning of WAIT; priority scheduling; handling of timeouts, aborts and other\n"
							+ "exceptional conditions"
							));
		} catch (LibrisException | IOException e) {
			fail("unexpected exception"+e.getMessage());
		}
	}
}

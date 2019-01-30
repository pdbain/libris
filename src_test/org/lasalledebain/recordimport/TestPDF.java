package org.lasalledebain.recordimport;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.lasalledebain.Utilities;
import org.lasalledebain.libris.LibrisDatabase;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.Repository;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.records.PdfRecordImporter;
import org.lasalledebain.libris.ui.LibrisUi;
import org.lasalledebain.libris.util.ZipUtils;

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
		repoFile = new File(workdir, "test_repo");
		repoFile.delete();
		repoFile.deleteOnExit();
		repoRoot = new File(workdir, "root");
		assertTrue("could not create database", Repository.initialize(repoFile));
		repo = Repository.open(repoFile, repoRoot, false);
		db = null;
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
			db = Utilities.buildTestDatabase(Utilities.KEYWORD_DATABASE4_XML);
			short keywordField = db.getSchema().getFieldNum("ID_keywords");
			short abstractField = db.getSchema().getFieldNum("ID_text");
			PdfRecordImporter importer = new PdfRecordImporter(db, repo,keywordField, abstractField);
			Record rec = db.newRecord();
			File testPdf = Utilities.getTestDatabase(Utilities.EXAMPLE_ARTIFACT_PDF);
			importer.importDocument(testPdf.toURI(), t -> 1, rec);
			String keywordsText = rec.getField(keywordField).getValuesAsString();
			String abstractText = rec.getField(abstractField).getValuesAsString();
			for (String s: new String[] {"now", "the", "time"}) {
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
			db = Utilities.buildTestDatabase(Utilities.KEYWORD_DATABASE4_XML);
			short keywordField = db.getSchema().getFieldNum("ID_keywords");
			short abstractField = db.getSchema().getFieldNum("ID_text");
			PdfRecordImporter importer = new PdfRecordImporter(db, repo,keywordField, abstractField);
			Record rec = db.newRecord();
			File testPdf = Utilities.getTestDatabase(Utilities.EXAMPLE_LARGE_PDF);
			importer.importDocument(testPdf.toURI(), t -> 1, rec);
			String keywordsText = rec.getField(keywordField).getValuesAsString();
			String abstractText = rec.getField(abstractField).getValuesAsString();
			final String expectedAbstractContents = "a number of problems arise which have "
					+ "not been adequately dealt with: the semantics of nested monitor calls; the various ways of "
					+ "defining the meaning of WAIT; priority scheduling; handling of timeouts, aborts and other "
					+ "exceptional conditions";
			assertTrue("Expect abstract contains:\n"+abstractText+"\nactual:\n"+abstractText, 
					abstractText.contains(expectedAbstractContents
							));
		} catch (LibrisException | IOException e) {
			fail("unexpected exception"+e.getMessage());
		}
	}
	@Test
	public void testImportMultipleDocuments() throws IOException {
		try {
			db = Utilities.buildTestDatabase(Utilities.KEYWORD_DATABASE1_XML);
	        Map<String, String> env = new HashMap<>(); 
	        env.put("create", "true");
	        LibrisUi ui = db.getUi();
			short keywordField = db.getSchema().getFieldNum("ID_keywords");
			short abstractField = db.getSchema().getFieldNum("ID_text");
			PdfRecordImporter importer = new PdfRecordImporter(db, repo,keywordField, abstractField);
			Record parentRec = db.newRecord();
			db.put(parentRec);
			parentRec.setName("Parent_record");
			URI exampleDocs = URI.create("jar:file:"+(Utilities.getTestDatabase(Utilities.EXAMPLE_DOCS_ZIP).getAbsolutePath()));
			File docDir = new File(workdir, "docs");
			ZipUtils.unzipZipFile(exampleDocs, docDir);
			final List<URI> fileUris = Arrays.stream(docDir.listFiles()).map(f -> f.toURI()).collect(Collectors.toList());
			importer.importPDFDocuments(fileUris, parentRec);
			ui.saveDatabase();
			File dumpFile = new File(workdir, "dump.libr");
			db.exportDatabaseXml(new FileOutputStream(dumpFile), true, true, false);
			ui.closeDatabase(false);
			
		} catch (LibrisException e) {
			fail("unexpected exception"+e.getMessage());
		}
	}

}

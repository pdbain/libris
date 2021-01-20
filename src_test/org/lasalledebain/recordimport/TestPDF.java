package org.lasalledebain.recordimport;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.lasalledebain.libris.DatabaseRecord;
import org.lasalledebain.libris.LibrisDatabase;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.Repository;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.records.PdfRecordImporter;
import org.lasalledebain.libris.ui.DatabaseUi;
import org.lasalledebain.libris.ui.HeadlessUi;
import org.lasalledebain.libris.util.Utilities;
import org.lasalledebain.libris.util.ZipUtils;

import junit.framework.TestCase;

public class TestPDF extends TestCase {

	private LibrisDatabase db;
	private File workingDirectory;
	private File repoFile;
	private File repoRoot;
	private Repository repo;
	static {
		URL props = PdfRecordImporter.class.getClassLoader().getResource("commons-logging.properties");
		if (Objects.nonNull(props)) {
			String path = props.getPath();
			System.setProperty("java.util.logging.config.file", path);
		}
	}

	@Before
	public void setUp() throws Exception {
		
		workingDirectory = Utilities.makeTempTestDirectory();
		repoRoot = new File(workingDirectory, "repo_root");
		assertTrue("Could not create "+repoRoot.getPath(), repoRoot.mkdir());
		final File repoDbFile = Repository.initialize(repoRoot);
		assertTrue("could not create database", null != repoDbFile);
		HeadlessUi<Record> ui = new HeadlessUi<Record>(repoFile, false);
		repo = Repository.open(ui, repoRoot);
		db = null;
	}

	@After
	public void tearDown() throws Exception {
		assertTrue("Could not close database", db.closeDatabase(false));
		db = null;
		Utilities.deleteRecursively(workingDirectory);
		Utilities.deleteTestDatabaseFiles();
	}

	@Test
	public void testImportDocument() {
		try {
			db = Utilities.buildTestDatabase(workingDirectory, Utilities.KEYWORD_DATABASE4_XML);
			int keywordField = db.getSchema().getFieldNum("ID_keywords");
			int abstractField = db.getSchema().getFieldNum("ID_text");
			PdfRecordImporter importer = new PdfRecordImporter(db, repo,keywordField, abstractField);
			DatabaseRecord rec = db.newRecord();
			File testPdf = Utilities.copyTestDatabaseFile(Utilities.EXAMPLE_ARTIFACT_PDF, workingDirectory);
			importer.importDocument(testPdf.toURI(), t -> 1, rec);
			String keywordsText = rec.getField(keywordField).getValuesAsString();
			String abstractText = rec.getField(abstractField).getValuesAsString();
			for (String s: new String[] {"now", "the", "time"}) {
				assertTrue("Missing keyword "+s, keywordsText.contains(s));
			}
			assertTrue("Abstract malformed: "+abstractText, 
					abstractText.contains("Now is the time for all good men to come to the aid of the party."));
		} catch (LibrisException | IOException e) {
			e.printStackTrace();
			fail("unexpected exception"+e.getMessage());
		}
	}
	@Test
	public void testImportLargeDocument() {
		try {
			db = Utilities.buildTestDatabase(workingDirectory, Utilities.KEYWORD_DATABASE4_XML);
			int keywordField = db.getSchema().getFieldNum("ID_keywords");
			int abstractField = db.getSchema().getFieldNum("ID_text");
			PdfRecordImporter importer = new PdfRecordImporter(db, repo,keywordField, abstractField);
			DatabaseRecord rec = db.newRecord();
			File testPdf = Utilities.copyTestDatabaseFile(Utilities.EXAMPLE_LARGE_PDF, workingDirectory);
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
			e.printStackTrace();
			fail("unexpected exception"+e.getMessage());
		}
	}
	@Test
	public void testImportMultipleDocuments() throws IOException {
		try {
			db = Utilities.buildTestDatabase(workingDirectory, Utilities.KEYWORD_DATABASE1_XML);
	        Map<String, String> env = new HashMap<>(); 
	        env.put("create", "true");
	        DatabaseUi<?> ui = db.getUi();
			int keywordField = db.getSchema().getFieldNum("ID_keywords");
			int abstractField = db.getSchema().getFieldNum("ID_text");
			PdfRecordImporter importer = new PdfRecordImporter(db, repo,keywordField, abstractField);
			DatabaseRecord parentRec = db.newRecord();
			db.putRecord(parentRec);
			parentRec.setName("Parent_record");
			URI exampleDocs = URI.create("jar:file:"+(Utilities.copyTestDatabaseFile(Utilities.EXAMPLE_DOCS_ZIP, workingDirectory).getAbsolutePath()));
			File docDir = new File(workingDirectory, "docs");
			ZipUtils.unzipZipFile(exampleDocs, docDir);
 			final List<URI> fileUris = Arrays.stream(docDir.listFiles()).map(f -> f.toURI()).collect(Collectors.toList());
			importer.importPDFDocuments(fileUris, parentRec);
			ui.saveDatabase();
			File dumpFile = new File(workingDirectory, "dump.libr");
			db.exportDatabaseXml(new FileOutputStream(dumpFile), true, true, false);
			
		} catch (LibrisException e) {
			e.printStackTrace();
			fail("unexpected exception"+e.getMessage());
		}
	}

}

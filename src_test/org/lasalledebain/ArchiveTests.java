package org.lasalledebain;

import static org.lasalledebain.libris.util.Utilities.KEYWORD_DATABASE1_XML;
import static org.lasalledebain.libris.util.Utilities.testLogger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.stream.Stream;

import org.junit.Test;
import org.lasalledebain.libris.DatabaseArchive;
import org.lasalledebain.libris.DatabaseRecord;
import org.lasalledebain.libris.GenericDatabase;
import org.lasalledebain.libris.LibrisDatabase;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.ui.DatabaseUi;
import org.lasalledebain.libris.util.Utilities;
import org.lasalledebain.libris.util.ZipUtils;

import junit.framework.TestCase;

public class ArchiveTests extends TestCase {

	private File workingDirectory;

	@Override
	protected void setUp() throws Exception {
		Utilities.deleteWorkingDirectory();
		testLogger.log(Level.INFO,getClass().getName()+" running "+getName());
		workingDirectory = Utilities.makeTempTestDirectory();
	}

	@Override
	protected void tearDown() throws Exception {
		Utilities.deleteWorkingDirectory();
	}

	@Test
	public void testArchive() throws FileNotFoundException, IOException {
		final File docDir = getArtifacts();
		Stream<Path> fileList = Files.walk(docDir.toPath()).filter(Files::isRegularFile);
		File archiveFile = new File(workingDirectory, "test.tar");
		archiveFile.createNewFile();
		try (DatabaseArchive archWriter  = new DatabaseArchive(archiveFile, null)) {
			archWriter.addFilesToArchive(fileList, docDir);
		}
		final File extractDir = new File(workingDirectory, "extract");
		DatabaseArchive.getFilesFromArchive(archiveFile, extractDir);

		Files.walk(docDir.toPath()).filter(Files::isRegularFile)
		.forEach(f -> {String foo = docDir.toPath().relativize(f).toString();
		File extractedFile = new File(extractDir, foo);
		assertTrue("compared "+ f.toString()+" to "+extractedFile.getPath(), extractedFile
				.exists());
		});
	}

	@Test
	public void testAddToArchive() throws FileNotFoundException, IOException {
		final File docDir = getArtifacts();
		File archiveFile = new File(workingDirectory, "test.tar");
		archiveFile.delete();
		archiveFile.createNewFile();
		Stream<Path> fileList = Files.walk(docDir.toPath()).filter(Files::isRegularFile).limit(10);
		try (DatabaseArchive archWriter  = new DatabaseArchive(archiveFile, null)) {
			archWriter.addFilesToArchive(fileList, docDir);
			fileList = Files.walk(docDir.toPath()).filter(Files::isRegularFile).skip(10);
			archWriter.addFilesToArchive(fileList, docDir);
		}
		final File extractDir = new File(workingDirectory, "extract");
		DatabaseArchive.getFilesFromArchive(archiveFile, extractDir);

		Files.walk(docDir.toPath()).filter(Files::isRegularFile)
		.forEach(f -> {String foo = docDir.toPath().relativize(f).toString();
		File extractedFile = new File(extractDir, foo);
		assertTrue("compared "+ f.toString()+" to "+extractedFile.getPath(), extractedFile
				.exists());
		});
	}

	@Test
	public void testAddDirectoryToArchive() throws FileNotFoundException, IOException {
		final File sourceDir = getArtifacts();
		File archiveFile = new File(workingDirectory, "test.tar");
		archiveFile.createNewFile();
		try (DatabaseArchive archWriter  = new DatabaseArchive(archiveFile, null)) {
			archWriter.addDirectoryToArchive(sourceDir, workingDirectory);
		}
		final File extractDir = new File(workingDirectory, "extract");

		DatabaseArchive.getFilesFromArchive(archiveFile, extractDir);

		Files.walk(sourceDir.toPath()).filter(Files::isRegularFile)
		.forEach(f -> {
			File extractedFile = new File(extractDir, workingDirectory.toPath().relativize(f).toString());
			assertTrue("compared "+ f.toString()+" to "+extractedFile.getPath(), extractedFile
					.exists());
		});
	}

	@Test
	public void testArchiveWithArtifacts() throws IOException, LibrisException {
		DatabaseUi<DatabaseRecord> ui;
		final File docDir = new File(workingDirectory, "docs");
		try (LibrisDatabase db = Utilities.buildTestDatabase(workingDirectory, KEYWORD_DATABASE1_XML)) {
			ui = db.getUi();
			db.addDocumentRepository(null);		
			ZipUtils.unzipZipFile(URI.create("jar:file:"+(Utilities.copyTestDatabaseFile(Utilities.EXAMPLE_DOCS_ZIP, workingDirectory).getAbsolutePath())), docDir);
			int recordCount = 1;
			int authFieldNum = db.getSchema().getFieldNum("ID_auth");
			int textFieldNum = db.getSchema().getFieldNum("ID_text");
			for (File f: docDir.listFiles()) {
				DatabaseRecord r = db.newRecord();
				r.addFieldValue(authFieldNum, "Author "+recordCount++);
				r.addFieldValue(textFieldNum, "Document "+f.getName());
				db.putRecord(r);
				db.addArtifact(r, f);
			}
			db.save();
			db.save();
			assertTrue("rebuild failed", ui.rebuildDatabase());
		}
		File databaseExport = new File(workingDirectory, "database_archive.tar");
		try	(LibrisDatabase db = ui.openDatabase()) {
			db.archiveDatabaseAndArtifacts(databaseExport, true, true);

			File extractDir = new File(workingDirectory, "extract");
			DatabaseArchive.getFilesFromArchive(databaseExport, extractDir);
			File extractedDatabase = new File(extractDir, KEYWORD_DATABASE1_XML);
			assertTrue("original database not open", db.isDatabaseOpen());
			try (LibrisDatabase recoveredDb = Utilities.buildAndOpenDatabase(extractedDatabase)) {
				assertTrue("recovered database not open", recoveredDb.isDatabaseOpen());
				assertEquals("recovered database does not match",  db, recoveredDb);
				File[] originalFiles = docDir.listFiles();
				for (int recordCount = 1; recordCount <= originalFiles.length; ++recordCount) {
					File recoveredArtifact = recoveredDb.getArtifactFileForRecord(recordCount);
					File sourceArtifact = originalFiles[recordCount-1];
					byte[] f1 = Files.readAllBytes(sourceArtifact.toPath());
					byte[] f2 = Files.readAllBytes(recoveredArtifact.toPath());
					assertTrue("Artifact files differ for record "+recordCount, Arrays.equals(f1, f2));
				}
			}
		}
	}

	@Test
	public void testArchiveWithManyArtifacts() throws IOException, LibrisException {
		final int documentCount = 500;
		DatabaseUi<DatabaseRecord> ui;
		final File sourceDir = new File(workingDirectory, "docs");
		final String sourceDirPath = sourceDir.getPath();
		try (LibrisDatabase db = Utilities.buildTestDatabase(workingDirectory, KEYWORD_DATABASE1_XML)) {
			db.addDocumentRepository(null);		
			ui = db.getUi();
			Utilities.deleteRecursively(sourceDir);
			sourceDir.mkdir();
			int recordCount;
			int authFieldNum = db.getSchema().getFieldNum("ID_auth");
			int textFieldNum = db.getSchema().getFieldNum("ID_text");
			for (recordCount = 1; recordCount <= documentCount; ++recordCount) {
				String fileText = artifactNumberToFilename(recordCount);
				Path outFile = Path.of(sourceDirPath, fileText);
				Files.writeString(outFile, fileText);
				DatabaseRecord r = db.newRecord();
				r.addFieldValue(authFieldNum, "Author "+recordCount);
				r.addFieldValue(textFieldNum, "Document "+outFile.getFileName());
				db.putRecord(r);
				db.addArtifact(r,outFile.toFile());
			}
			db.save();
			assertTrue("rebuild failed", ui.rebuildDatabase());
		}
		File databaseExport = new File(workingDirectory, "database_archive.tar");
		try	(LibrisDatabase db = ui.openDatabase()) {
			db.archiveDatabaseAndArtifacts(databaseExport, true, true);

			File extractDir = new File(workingDirectory, "extract");
			DatabaseArchive.getFilesFromArchive(databaseExport, extractDir);
			File extractedDatabase = new File(extractDir, KEYWORD_DATABASE1_XML);
			assertTrue("original database not open", db.isDatabaseOpen());
			try (LibrisDatabase recoveredDb = Utilities.buildAndOpenDatabase(extractedDatabase)) {
				assertTrue("recovered database not open", recoveredDb.isDatabaseOpen());
				assertEquals("recovered database does not match",  db, recoveredDb);
				for (int recordCount = 1; recordCount <= documentCount; ++recordCount) {
					File sourceArtifact = new File(sourceDir, artifactNumberToFilename(recordCount));
					File dbArtifact = new File(sourceDir, artifactNumberToFilename(recordCount));
					File recoveredArtifact = recoveredDb.getArtifactFileForRecord(recordCount);
					byte[] f1 = Files.readAllBytes(sourceArtifact.toPath());
					byte[] f2 = Files.readAllBytes(recoveredArtifact.toPath());
					assertTrue("Artifact files differ from source for record "+recordCount, Arrays.equals(f1, f2));
					f1 = Files.readAllBytes(dbArtifact.toPath());
					f2 = Files.readAllBytes(recoveredArtifact.toPath());
					assertTrue("Artifact files differ from original database for record "+recordCount, Arrays.equals(f1, f2));
				}
			}
		}
	}

	protected String artifactNumberToFilename(int recordCount) {
		String fileText = "file_"+recordCount+".txt";
		return fileText;
	}

	@Test
	public void testArchiveWithoutArtifacts() throws IOException, LibrisException {
		try (LibrisDatabase db = Utilities.buildTestDatabase(workingDirectory, KEYWORD_DATABASE1_XML)) {
			db.addDocumentRepository(null);		
			final File docDir = new File(workingDirectory, "docs");
			Utilities.deleteRecursively(docDir);
			docDir.mkdir();
			int recordCount = 0;
			int authFieldNum = db.getSchema().getFieldNum("ID_auth");
			int textFieldNum = db.getSchema().getFieldNum("ID_text");
			for (File f: docDir.listFiles()) {
				DatabaseRecord r = db.newRecord();
				r.addFieldValue(authFieldNum, "Author "+recordCount++);
				r.addFieldValue(textFieldNum, "Document "+f.getName());
				db.putRecord(r);
			}
			db.save();
			File databaseExport = new File(workingDirectory, "database_archive.tar");
			db.archiveDatabaseAndArtifacts(databaseExport, true, false);

			File extractDir = new File(workingDirectory, "extract");
			DatabaseArchive.getFilesFromArchive(databaseExport, extractDir);
			File extractedDatabase = new File(extractDir, KEYWORD_DATABASE1_XML);
			try (GenericDatabase<DatabaseRecord> recoveredDb = Utilities.buildAndOpenDatabase(extractedDatabase)) {
				assertEquals("recovered database does not match",  db, recoveredDb);
			}
		}
	}

	protected File getArtifacts() throws FileNotFoundException, IOException {
		URI exampleDocs = URI.create("jar:file:"+(Utilities.copyTestDatabaseFile(Utilities.EXAMPLE_DOCS_ZIP, workingDirectory).getAbsolutePath()));
		final File docDir = new File(workingDirectory, "docs");
		ZipUtils.unzipZipFile(exampleDocs, docDir);
		return docDir;
	}

}

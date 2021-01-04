package org.lasalledebain;

import static org.lasalledebain.Utilities.testLogger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;
import org.lasalledebain.libris.Archive;
import org.lasalledebain.libris.DatabaseRecord;
import org.lasalledebain.libris.LibrisDatabase;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.records.PdfRecordImporter;
import org.lasalledebain.libris.ui.DatabaseUi;
import org.lasalledebain.libris.util.ZipUtils;

import junit.framework.TestCase;

public class ArchiveTests extends TestCase {

	private File workingDirectory;

	@Override
	protected void setUp() throws Exception {
		Utilities.deleteWorkingDirectory();
		testLogger.log(Level.INFO,"running "+getName());
		workingDirectory = Utilities.makeTempTestDirectory();
	}

	@Override
	protected void tearDown() throws Exception {
		Utilities.deleteWorkingDirectory();
	}

	@Test
	public void testArchive() throws FileNotFoundException, IOException {
		URI exampleDocs = URI.create("jar:file:"+(Utilities.copyTestDatabaseFile(Utilities.EXAMPLE_DOCS_ZIP, workingDirectory).getAbsolutePath()));
		final File docDir = new File(workingDirectory, "docs");
		final Path docPath = docDir.toPath();
		ZipUtils.unzipZipFile(exampleDocs, docDir);
		Stream<Path> fileList = Files.walk(docDir.toPath()).filter(Files::isRegularFile);
		File archiveFile = new File(workingDirectory, "test.tar");
		archiveFile.createNewFile();
		Archive.addFilesToArchive(archiveFile, fileList, docDir);
		final File extractDir = new File(workingDirectory, "extract");
		Archive.getFilesFromArchive(archiveFile, extractDir);
		
		Files.walk(docDir.toPath()).filter(Files::isRegularFile)
		.forEach(f -> {String foo = docPath.relativize(f).toString();
		File extractedFile = new File(extractDir, foo);
		assertTrue("compared "+ f.toString()+" to "+extractedFile.getPath(), extractedFile
				.exists());
		});
	}
	
	@Test
	public void testArchiveWithArtifacts() throws IOException {
		try (LibrisDatabase db = Utilities.buildTestDatabase(workingDirectory, Utilities.KEYWORD_DATABASE1_XML)) {
			db.addDocumentRepository(null);		
			final File docDir = new File(workingDirectory, "docs");
			final Path docPath = docDir.toPath();
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
			File databaseExport = new File(workingDirectory, "database_archive.tar");
			db.exportDatabaseXml(databaseExport);
		} catch (LibrisException e) {
			e.printStackTrace();
			fail("unexpected exception"+e.getMessage());
		}
	}

}

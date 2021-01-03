package org.lasalledebain;

import static org.lasalledebain.Utilities.testLogger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.stream.Stream;

import org.junit.Test;
import org.lasalledebain.libris.Archive;
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
}

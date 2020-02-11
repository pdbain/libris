package org.lasalledebain.repository;

import static org.lasalledebain.Utilities.testLogger;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import org.lasalledebain.Utilities;
import org.lasalledebain.libris.ArtifactManager;
import org.lasalledebain.libris.ArtifactParameters;
import org.lasalledebain.libris.DatabaseRecord;
import org.lasalledebain.libris.FileManager;
import org.lasalledebain.libris.Libris;
import org.lasalledebain.libris.LibrisDatabase;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.ui.HeadlessUi;
import org.lasalledebain.libris.ui.LibrisUi;

import junit.framework.TestCase;

public class ArtifactManagerTest  extends TestCase {

	private static final String RECORD = "record_";
	private File workingDirectory;
	private File repoRoot;
	private FileManager fileMgr;

	@Override
	protected void setUp() throws Exception {
		workingDirectory = Utilities.makeTempTestDirectory();
		repoRoot = new File(workingDirectory, "root");
		fileMgr = new FileManager(new File("auxiliary"));
	}

	public void testRepoSanity() throws LibrisException, XMLStreamException, IOException, FactoryConfigurationError{
		ArtifactManager mgr = createManager();
		File testArtifact = new File(workingDirectory, "test_artifact");
		testArtifact.deleteOnExit();
		URI originalUri = testArtifact.toURI();
		int id = mgr.putArtifactInfo(originalUri);
		File newUri = mgr.getArtifactSourceFile(id);
		assertEquals(originalUri, newUri.toURI());
	}

	public void testRepoArtifactInfo() throws LibrisException, XMLStreamException, IOException, FactoryConfigurationError{
		final int numArtifacts = 16;
		ArtifactManager repo = createManager();
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
			params.setRecordParentName(generateParentName(expectedId));
			params.setParentId(generateParentId(expectedId));
			int id = repo.putArtifactInfo(params);
			assertEquals(expectedId, id);
		}
		for (int i = 0; i < numArtifacts; ++i) {
			final int expectedId = i + 1;
			ArtifactParameters actual = repo.getArtifactInfo(expectedId);
			assertEquals(testArtifacts[i], actual);
		}
	}
	
	public void testImportFile(){
		importAndCheckFiles(10);
	}

	public void testDatabaseWithDocumentRepo() throws FileNotFoundException, IOException, LibrisException {
		File repoRoot = new File(workingDirectory, "TestRoot");
		repoRoot.mkdir();
		File testDatabaseFileCopy = Utilities.copyTestDatabaseFile(Utilities.DATABASE_WITH_GROUPS_AND_RECORDS_XML, workingDirectory);
		LibrisDatabase db = Libris.buildAndOpenDatabase(testDatabaseFileCopy);
		db.addDocumentRepository(repoRoot); // use default
		File testPdfDirectory = new File(Utilities.getTestDataDirectory(), Utilities.EXAMPLE_FILES);
		String[] testFileNames = filterTestFilenames(testPdfDirectory);
		assertTrue("wrong number of test files", testFileNames.length >= 4);
		for (int i = 1; i <= 4; ++i) {
			File artifactSourceFile = new File(testPdfDirectory, testFileNames[i - 1]);
			db.addArtifact(i, artifactSourceFile);
		}
		db.save();
		assertTrue("Repository missing", repoRoot.exists());
		File[] rootDirs = repoRoot.listFiles();
		assertTrue("Repository first-level directory missing", (rootDirs.length == 1) && rootDirs[0].isDirectory());
		File[] artifactFiles = rootDirs[0].listFiles();
		Arrays.sort(artifactFiles, (f, g) -> f.getName().compareTo(g.getName()));
		for (int i = 0; i < 4; ++i) {
			String artifactSourceFileName = testFileNames[i];
			String artifactName = artifactFiles[i].getName();
			assertTrue("Wrong artifact returned", artifactName.contains(artifactSourceFileName));
		}
		checkRecordArtifacts(db);
		File copyDbXml = new File (workingDirectory, "database_copy.xml");
		copyDbXml.deleteOnExit();
		FileOutputStream copyStream = new FileOutputStream(copyDbXml);
		testLogger.log(Level.INFO,getName()+": copy database to"+copyDbXml);
		 
		db.exportDatabaseXml(copyStream);
		LibrisDatabase db2 = Libris.buildAndOpenDatabase(copyDbXml);
		checkRecordArtifacts(db2);
		
		for (Record r: db.getRecords()) {
			int recId = r.getRecordId();
			Record actualRec = db2.getRecord(recId);
			assertEquals("Mismatch on record "+recId, r, actualRec);
		}
		
		for (int i = 1; i <= 4; ++i) {
			String artifactSourceFileName = testFileNames[i - 1];
			File artifact = db2.getArtifactFileForRecord(i);
			assertTrue("Wrong artifact returned", artifact.getName().contains(artifactSourceFileName));
		}
	}

	public String[] filterTestFilenames(File testPdfDirectory) {
		int readPointer = 0;
		int writePointer = 0;
		String[] testFileNames = testPdfDirectory.list();
		while (readPointer < testFileNames.length) {
			while ((readPointer < testFileNames.length) && !testFileNames[readPointer].endsWith("pdf")) {
				++readPointer;
			}
			if (readPointer < testFileNames.length) {
				testFileNames[writePointer] = testFileNames[readPointer];
			}
			++readPointer;
		}
		return testFileNames;
	}

	public void testDatabaseWithDefaultDocumentRepo() throws FileNotFoundException, IOException, LibrisException {
		File repoRoot = null;
		File testDatabaseFileCopy = Utilities.copyTestDatabaseFile(Utilities.DATABASE_WITH_GROUPS_AND_RECORDS_XML, workingDirectory);
		LibrisDatabase db = Libris.buildAndOpenDatabase(testDatabaseFileCopy);
		db.addDocumentRepository(repoRoot); // use default
		File testPdfDirectory = new File(Utilities.getTestDataDirectory(), Utilities.EXAMPLE_FILES);
		String[] testFileNames = filterTestFilenames(testPdfDirectory);
		assertTrue("wrong number of test files", testFileNames.length >= 4);
		for (int i = 1; i <= 4; ++i) {
			File artifactSourceFile = new File(testPdfDirectory, testFileNames[i - 1]);
			db.addArtifact(i, artifactSourceFile);
		}
		db.save();
		checkRecordArtifacts(db);
		File copyDbXml = new File (workingDirectory, "database_copy.xml");
		copyDbXml.deleteOnExit();
		FileOutputStream copyStream = new FileOutputStream(copyDbXml);
		testLogger.log(Level.INFO,getName()+": copy database to"+copyDbXml);
		 
		db.exportDatabaseXml(copyStream);
		LibrisDatabase db2 = Libris.buildAndOpenDatabase(copyDbXml);
		checkRecordArtifacts(db2);
		
		for (Record r: db.getRecords()) {
			int recId = r.getRecordId();
			Record actualRec = db2.getRecord(recId);
			assertEquals("Mismatch on record "+recId, r, actualRec);
		}
		
		for (int i = 1; i <= 4; ++i) {
			String artifactSourceFileName = testFileNames[i - 1];
			File artifact = db2.getArtifactFileForRecord(i);
			assertTrue("Wrong artifact returned", artifact.getName().contains(artifactSourceFileName));
		}
	}

	public void testPutGet() throws FileNotFoundException, IOException, LibrisException {
		File testDatabaseFileCopy = Utilities.copyTestDatabaseFile(Utilities.TEST_DATABASE_WITH_REPO, workingDirectory);
		LibrisDatabase db = Libris.buildAndOpenDatabase(testDatabaseFileCopy);
		File testPdfDirectory = new File(Utilities.getTestDataDirectory(), Utilities.EXAMPLE_FILES);
		String[] testFileNames = filterTestFilenames(testPdfDirectory);
		assertTrue("wrong number of test files", testFileNames.length >= 4);
		for (int i = 1; i <= 4; ++i) {
			File artifactSourceFile = new File(testPdfDirectory, testFileNames[i - 1]);
			DatabaseRecord rec = db.getRecord(i);
			db.addArtifact(rec, artifactSourceFile);
			db.putRecord(rec);
		}
		for (int i = 1; i <= 4; ++i) {
			DatabaseRecord rec = db.getRecord(i);
			int artifactId = rec.getArtifactId();
			assertEquals("wrong artifactId", i, artifactId);
		}
	}

	private void checkRecordArtifacts(LibrisDatabase db) throws InputException, DatabaseException {
		for (int i = 1; i <= 4; ++i) {
			File result = db.getArtifactFileForRecord(i);
			assertNotNull("No artifact file for record "+i, result);
			assertTrue("Artifact file "+result.getAbsolutePath()+" does not exist", result.exists());
		}
	}


	protected void importAndCheckFiles(final int numFiles) {
		File originalFiles = new File(workingDirectory, "originals");
		originalFiles.mkdir();
		repoRoot.mkdir();
		String originalRoot = originalFiles.getAbsolutePath();
		try {
			ArtifactManager repo = createManager();
			ArrayList<File> archiveFiles = new ArrayList<>(11);
			archiveFiles.add(null);
			for (int i = 1; i <= numFiles; ++i) {
				byte[] fileContent = makeTestData(i);
				Path testFile = Paths.get(originalRoot, "f"+i);
				Files.write(testFile, fileContent);
				repo.importFile(testFile.toFile());
			}
			checkDirectorySize(repoRoot);
			for (int i = 1; i <= numFiles; ++i) {
				Path testFile = Paths.get(originalRoot, "f"+i);
				File sourceFile = repo.getArtifactSourceFile(i);
				assertEquals("source files do not match",  testFile.toFile(), sourceFile);
				Files.delete(testFile);
				byte[] expectedContent = makeTestData(i);
				File archiveFile = repo.getArtifactArchiveFile(i);
				ByteArrayOutputStream actualBuffer = new ByteArrayOutputStream();
				Files.copy(archiveFile.toPath(), actualBuffer);
				byte[] actualBytes = actualBuffer.toByteArray();
				assertTrue("wrong data in "+archiveFile.getAbsolutePath(), Arrays.equals(actualBytes, expectedContent));
			}
		} catch (IOException | LibrisException | XMLStreamException | FactoryConfigurationError e) {
			e.printStackTrace();
			fail("unexpected exception");
		}
	}

	private void checkDirectorySize(File root) {
		File[] children = root.listFiles();
		assertTrue("Too many files in "+root.getAbsolutePath(), children.length <= 100);
		for (File f: children) {
			if (f.isDirectory()) {
				checkDirectorySize(f);
			}
		}
	}

	public void testImportStress(){
		importAndCheckFiles(1000);
	}

	protected byte[] makeTestData(int i) {
		return ("teststring_"+i).getBytes();
	}


	private String generateParentName(int expectedId) {
		String result = "";
		if (expectedId > 1) {
			int expectedParentId = generateParentId(expectedId);
			result = RECORD+expectedParentId;
		}
		return result;
	}

	private int generateParentId(int expectedId) {
		int expectedParentId = expectedId/2;
		return expectedParentId;
	}

	private ArtifactManager createManager()
			throws LibrisException, XMLStreamException, IOException, FactoryConfigurationError {
		LibrisUi ui = new HeadlessUi();
		ArtifactManager dut = new ArtifactManager(ui, repoRoot, fileMgr);
		boolean initializeStatus = dut.initialize(false);
		assertTrue("Could not initialize artifact manager", initializeStatus);
		dut.open();
		return dut;
	}
	@Override
	protected void tearDown() throws Exception {
		Utilities.deleteRecursively(workingDirectory);
	}

}

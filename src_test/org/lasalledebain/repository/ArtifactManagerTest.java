package org.lasalledebain.repository;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import org.lasalledebain.Utilities;
import org.lasalledebain.libris.ArtifactManager;
import org.lasalledebain.libris.ArtifactParameters;
import org.lasalledebain.libris.FileManager;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.ui.HeadlessUi;
import org.lasalledebain.libris.ui.LibrisUi;

import junit.framework.TestCase;

public class ArtifactManagerTest  extends TestCase {

	private static final String RECORD = "record_";
	private File workdir;
	private File repoRoot;
	private FileManager fileMgr;

	@Override
	protected void setUp() throws Exception {
		workdir = Utilities.makeTempTestDirectory();
		repoRoot = new File(workdir, "root");
		fileMgr = new FileManager(new File("auxiliary"));
	}

	public void testRepoSanity() throws LibrisException, XMLStreamException, IOException, FactoryConfigurationError{
		ArtifactManager mgr = createManager();
		File testArtifact = new File(workdir, "test_artifact");
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
			File testFile = new File(workdir, fName);
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

	protected void importAndCheckFiles(final int numFiles) {
		File originalFiles = new File(workdir, "originals");
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
		Utilities.deleteRecursively(workdir);
	}

}

package org.lasalledebain.repository;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import org.lasalledebain.Utilities;
import org.lasalledebain.libris.Repository;
import org.lasalledebain.libris.Repository.ArtifactParameters;
import org.lasalledebain.libris.exception.LibrisException;

import junit.framework.TestCase;

public class RepositoryTest  extends TestCase {

	private static final String RECORD = "record_";
	private File workdir;
	private File dbFile;

	@Override
	protected void setUp() throws Exception {
		workdir = Utilities.getTempTestDirectory();
		dbFile = new File(workdir, "test_repo");
		dbFile.delete();
		dbFile.deleteOnExit();
	}

	public void testRepoSanity(){
		try {
			Repository repo = createDatabase();
			File testArtifact = new File(workdir, "test_artifact");
			testArtifact.deleteOnExit();
			URI originalUri = testArtifact.toURI();
			int id = repo.putArtifactInfo(originalUri);
			File newUri = repo.getArtifact(id);
			assertEquals(originalUri, newUri.toURI());
		} catch (LibrisException | XMLStreamException | IOException | URISyntaxException e) {
			e.printStackTrace();
			fail("unexpected error");
		}
	}

	public void testRepoArtifactInfo(){
		try {
			final int numArtifacts = 16;
			Repository repo = createDatabase();
			ArtifactParameters testArtifacts[] = new ArtifactParameters[numArtifacts];
			for (int i = 0; i < numArtifacts; ++i) {
				final int expectedId = i + 1;
				final String fName = "test_artifact_"+expectedId;
				File testFile = new File(workdir, fName);
				testFile.deleteOnExit();
				URI originalUri = testFile.toURI();
				ArtifactParameters params = new ArtifactParameters(originalUri);
				testArtifacts[i] = params;
				params.comments = "comments_"+expectedId;
				params.date = "date_"+expectedId;
				params.doi = "doi_"+expectedId;
				params.keywords = "keywords_"+expectedId;
				params.recordName = RECORD+expectedId;
				params.title = "title_"+expectedId;
				params.recordParentName = generateParentName(expectedId);
				int id = repo.putArtifactInfo(params);
				assertEquals(expectedId, id);
			}
			for (int i = 0; i < numArtifacts; ++i) {
				final int expectedId = i + 1;
				ArtifactParameters actual = repo.getArtifactInfo(expectedId);
				assertEquals(testArtifacts[i], actual);
			}
		} catch (LibrisException | XMLStreamException | IOException | URISyntaxException e) {
			e.printStackTrace();
			fail("unexpected error");
		}
	}
	
	public void testImportFile(){
		File originalFiles = new File(workdir, "originals");
		originalFiles.mkdir();
		File repoRoot = new File(workdir, "root");
		repoRoot.mkdir();
		String originalRoot = originalFiles.getAbsolutePath();
		try {
			Repository repo = createDatabase();
			ArrayList<URI> uris = new ArrayList<>();
			for (int i = 1; i < 10; ++i) {
				byte[] fileContent = makeTestData(i);
				Path testFile = Paths.get(originalRoot, "f"+i);
				Files.write(testFile, fileContent);
				URI theUri = repo.copyFileToRepo(testFile.toFile(), repoRoot, i);
				uris.set(i, theUri);
			}
			for (int i = 1; i < 10; ++i) {
				byte[] expectedContent = makeTestData(i);
				URI theUri = uris.get(i);
				InputStream actual = Files.newInputStream(Paths.get(theUri));
				for (int j = 0; j < expectedContent.length; ++j) {
					int b = actual.read();
				}
			}
		} catch (IOException | LibrisException | XMLStreamException | FactoryConfigurationError e) {
			e.printStackTrace();
			fail("unexpected exception");
		}
	}

	protected byte[] makeTestData(int i) {
		return ("teststring_"+i).getBytes();
	}


	private String generateParentName(int expectedId) {
		String result = "";
		if (expectedId > 1) {
			result = RECORD+(expectedId/2);
		}
		return result;
	}

	private Repository createDatabase()
			throws LibrisException, XMLStreamException, IOException, FactoryConfigurationError {
		assertTrue("could not create database", Repository.initialize(dbFile));
		Repository repo = Repository.open(dbFile, false);
		return repo;
	}
	@Override
	protected void tearDown() throws Exception {
		Utilities.deleteRecursively(workdir);
	}

}

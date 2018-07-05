package org.lasalledebain.repository;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.stream.XMLStreamException;

import org.lasalledebain.Utilities;
import org.lasalledebain.libris.Repository;
import org.lasalledebain.libris.exception.LibrisException;

import junit.framework.TestCase;

public class RepositoryTest  extends TestCase {

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
		assertTrue("could not create database", Repository.initialize(dbFile));
		File testArtifact = new File(workdir, "test_artifact");
		Repository repo = Repository.open(dbFile, false);
		testArtifact.deleteOnExit();
		URI originalUri = testArtifact.toURI();
		int id = repo.putArtifact(originalUri);
		File newUri = repo.getArtifact(id);
		assertEquals(originalUri, newUri.toURI());
	} catch (LibrisException | XMLStreamException | IOException | URISyntaxException e) {
		e.printStackTrace();
		fail("unexpected error");
	}
}
	@Override
	protected void tearDown() throws Exception {
		Utilities.deleteRecursively(workdir);
	}

}

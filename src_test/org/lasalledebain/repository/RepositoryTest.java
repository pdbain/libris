package org.lasalledebain.repository;

import java.io.File;

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
		dbFile.deleteOnExit();
	}

public void TestRepoSanity(){
	try {
		Repository repo = Repository.Initialize(dbFile);
	} catch (LibrisException e) {
		e.printStackTrace();
		fail("unexpected error");
	}
}
	@Override
	protected void tearDown() throws Exception {
		Utilities.deleteRecursively(workdir);
	}

}

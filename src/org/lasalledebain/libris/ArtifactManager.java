package org.lasalledebain.libris;

import java.io.File;

import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.ui.Dialogue;
import org.lasalledebain.libris.ui.LibrisUi;

public class ArtifactManager {
private final FileManager myFileMgr;
private final File artifactDirectory;
private final LibrisUi myUi;
private ArtifactDatabase myDb;
private ReservationManager reservationMgr;
public ArtifactManager(LibrisUi theUi, File artifactDirectory, FileManager theFileMgr) throws DatabaseException {
	this.artifactDirectory = artifactDirectory;
	myFileMgr = theFileMgr;
	myUi = theUi;
	myDb = new ArtifactDatabase(theUi, theFileMgr);
}

public void initialize(boolean force) throws LibrisException {
	if (artifactDirectory.exists()) {
		if (force || (Dialogue.YES_OPTION == myUi.confirm("Delete directory " + artifactDirectory.getAbsolutePath()))) {
			artifactDirectory.delete();
		}
	}
	if (!artifactDirectory.mkdir()) {
		throw new DatabaseException("Cannot create " + artifactDirectory.getAbsolutePath());
	}
	myDb.initialize();
}

public void open() throws LibrisException {
	reservationMgr = new ReservationManager(myFileMgr.makeAuxiliaryFileAccessManager(LibrisConstants.LOCK_FILENAME));
	reservationMgr.reserveDatabase();
	myDb.openDatabase();
}

public void close() {
//	myDb.
}
}

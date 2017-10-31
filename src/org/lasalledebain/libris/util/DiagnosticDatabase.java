package org.lasalledebain.libris.util;

import java.io.File;
import java.util.HashMap;

import org.lasalledebain.libris.LibrisDatabase;
import org.lasalledebain.libris.LibrisMetadata;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.ui.HeadlessUi;

public class DiagnosticDatabase extends LibrisDatabase {

	public DiagnosticDatabase(File databaseFile) throws LibrisException, DatabaseException {
		super(databaseFile, null, new HeadlessUi(), false);
		getFileMgr().createAuxFiles(true);
		HashMap<String, String> attrs = new HashMap<String, String>();
		attrs.put(XML_DATABASE_NAME_ATTR, "unknown");
		attrs.put(XML_DATABASE_SCHEMA_NAME_ATTR, "unknown");
		attrs.put(XML_SCHEMA_VERSION_ATTR, "unknown");
		metadata = new LibrisMetadata(this);
		metadata.setLastRecordId(1);
	}
}

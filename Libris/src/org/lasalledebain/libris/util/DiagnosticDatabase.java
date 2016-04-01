package org.lasalledebain.libris.util;

import java.io.File;
import java.util.HashMap;

import org.lasalledebain.libris.LibrisDatabase;
import org.lasalledebain.libris.LibrisMetadata;
import org.lasalledebain.libris.RecordId;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.ui.HeadlessUi;
import org.lasalledebain.libris.ui.LibrisParameters;

public class DiagnosticDatabase extends LibrisDatabase {

	public DiagnosticDatabase(File databaseFile) throws LibrisException, DatabaseException {
		super(new LibrisParameters(new HeadlessUi(), false, false, databaseFile));
		HashMap<String, String> attrs = new HashMap<String, String>();
		attrs.put(XML_DATABASE_NAME_ATTR, "unknown");
		attrs.put(XML_SCHEMA_NAME_ATTR, "unknown");
		attrs.put(XML_SCHEMA_VERSION_ATTR, "unknown");
		metadata = new LibrisMetadata(this);
		metadata.setLastRecordId(new RecordId(1));
	}
}

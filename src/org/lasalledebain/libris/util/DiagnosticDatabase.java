package org.lasalledebain.libris.util;

import java.io.File;
import java.util.HashMap;

import org.lasalledebain.libris.DatabaseAttributes;
import org.lasalledebain.libris.LibrisDatabase;
import org.lasalledebain.libris.LibrisDatabaseParameter;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.ui.HeadlessUi;
import org.lasalledebain.libris.xmlUtils.ElementManager;

public class DiagnosticDatabase extends LibrisDatabase {

	public DiagnosticDatabase(File databaseFile) throws LibrisException, DatabaseException {
		super(new LibrisDatabaseParameter(new HeadlessUi(), databaseFile));
		getFileMgr().createAuxFiles(true);
		HashMap<String, String> attrs = new HashMap<String, String>();
		attrs.put(XML_DATABASE_NAME_ATTR, "unknown");
		attrs.put(XML_DATABASE_SCHEMA_NAME_ATTR, "unknown");
		attrs.put(XML_SCHEMA_VERSION_ATTR, "unknown");
		xmlAttributes = new DatabaseAttributes(this, attrs);
	}

	/* (non-Javadoc)
	 * @see org.lasalledebain.libris.LibrisDatabase#fromXml(org.lasalledebain.libris.xmlUtils.ElementManager)
	 */
	@Override
	public void fromXml(ElementManager librisMgr) throws LibrisException {
		super.fromXml(librisMgr);
		metadata.setLastRecordId(1);
	}
}

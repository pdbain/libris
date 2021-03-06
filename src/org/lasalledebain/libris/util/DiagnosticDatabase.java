package org.lasalledebain.libris.util;

import java.io.File;

import org.lasalledebain.libris.DatabaseAttributes;
import org.lasalledebain.libris.LibrisDatabase;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.indexes.LibrisDatabaseConfiguration;
import org.lasalledebain.libris.ui.HeadlessUi;
import org.lasalledebain.libris.xmlUtils.ElementManager;

public class DiagnosticDatabase extends LibrisDatabase {

	public DiagnosticDatabase(File databaseFile) throws LibrisException, DatabaseException {
		super(new LibrisDatabaseConfiguration(databaseFile, false), new HeadlessUi(databaseFile, false));
		getFileMgr().createAuxFiles(true);
		DatabaseAttributes attrs = new DatabaseAttributes();
		attrs.setAttribute(XML_DATABASE_NAME_ATTR, "unknown");
		attrs.setAttribute(XML_DATABASE_SCHEMA_NAME_ATTR, "unknown");
		attrs.setAttribute(XML_SCHEMA_VERSION_ATTR, "unknown");
		dbAttributes = new DatabaseAttributes(attrs);
	}

	/* (non-Javadoc)
	 * @see org.lasalledebain.libris.LibrisDatabase#fromXml(org.lasalledebain.libris.xmlUtils.ElementManager)
	 */
	@Override
	public void fromXml(ElementManager librisMgr) throws LibrisException {
		super.fromXml(librisMgr);
		databaseMetadata.setLastRecordId(1);
	}
}

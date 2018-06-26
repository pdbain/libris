package org.lasalledebain.libris;

import java.io.File;
import java.io.StringReader;
import java.net.URI;
import java.util.HashMap;
import java.util.Objects;

import javax.xml.stream.FactoryConfigurationError;

import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.DatabaseNotIndexedException;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.ui.HeadlessUi;
import org.lasalledebain.libris.xmlUtils.ElementReader;
import org.lasalledebain.libris.xmlUtils.LibrisXMLConstants;
import org.lasalledebain.libris.xmlUtils.LibrisXmlFactory;

public class Repository extends Libris {
private static final String ID_SOURCE = "ID_source";

private static final String ID_TITLE = "ID_title";

private static final String schemaDefinition = "	<schema>"+
"<schema>"+
"<groupdefs>"+
"				<groupdef id=\"GRP_pubinfo\" structure=\"flat\"/>"+
"</groupdefs>"+
"<fielddefs>"+
"	<fielddef id=\""
+ ID_TITLE
+ "\" title=\"title\"/>"+
"	<fielddef id=\""
+ ID_SOURCE
+ "\" datatype=\"location\" />"+
"	<fielddef id=\"ID_date\" datatype=\"string\" />"+
"	<fielddef id=\"ID_comments\" datatype=\"string\" />"+
"	<fielddef id=\"ID_keywords\" datatype=\"string\" />"+
"</fielddefs>"+
"<indexdefs>"+
"</indexdefs>"+
"</schema>";

	LibrisDatabase database;

	public Repository(LibrisDatabase db) {
		database = db;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	public static Repository Initialize(File databaseFile) throws LibrisException {
		HeadlessUi theUi = new HeadlessUi();
		LibrisDatabaseParameter params = new LibrisDatabaseParameter(theUi);
		 LibrisDatabase result = new LibrisDatabase(params);

		result.getFileMgr().createAuxFiles(true);
		HashMap<String, String> attrs = new HashMap<String, String>();
		attrs.put(LibrisXMLConstants.XML_DATABASE_NAME_ATTR, "unknown");
		attrs.put(LibrisXMLConstants.XML_DATABASE_SCHEMA_NAME_ATTR, "unknown");
		attrs.put(LibrisXMLConstants.XML_SCHEMA_VERSION_ATTR, "unknown");
		result.setAttributes(new DatabaseAttributes(result, attrs));
		return new Repository(result);
	
	}
	public static Repository Open(File databaseFile, boolean readOnly) throws FactoryConfigurationError, LibrisException {
		HeadlessUi ui = new HeadlessUi(databaseFile, readOnly);
		XmlSchema mySchema = new XmlSchema(LibrisDatabase.xmlFactory.makeReader(new StringReader(schemaDefinition), "<internal>"));
		ui.setSchema(mySchema);
		Repository result = new Repository(ui.openDatabase());
		return result;
	}

	File getArtifact(int artifactId) {
		// TODO write getArtifact
		return null;
		
	}
	
	ArtifactParameters getArtifactInfo(int artifactId) {
		// TODO write getArtifact
		return null;
		
	}
	
	int putArtifact(ArtifactParameters params) throws InputException {
		Record rec = database.newRecord();
		rec.addFieldValue(ID_SOURCE, params.location.toString());
		if (!params.recordName.isEmpty()) {
			rec.setName(params.recordName);
		}
		if (!params.recordParent.isEmpty()) {
			Record parent = database.getRecord(params.recordParent);
			if (Objects.isNull(parent)) {
				throw new InputException("Cannot locate record "+params.recordParent);
			}
			rec.setParent(0, parent.getRecordId());
		}
		return 0;	
	}
	
	int putArtifact(URI location) throws InputException {
		return putArtifact(new ArtifactParameters(location));	
	}
	
	static class ArtifactParameters {
		private ArtifactParameters(URI location) {
			super();
			this.location = location;
			recordName = "";
			recordParent = "";
		}
		URI location;
		String date;
		String title;
		String comments;
		String keywords;
		String recordParent;
		String recordName;
	}
}

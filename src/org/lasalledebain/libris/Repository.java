package org.lasalledebain.libris;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.util.HashMap;
import java.util.Objects;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.ui.HeadlessUi;
import org.lasalledebain.libris.xmlUtils.ElementReader;
import org.lasalledebain.libris.xmlUtils.LibrisXMLConstants;

public class Repository extends Libris {
private static final String ID_SOURCE = "ID_source";

private static final String ID_TITLE = "ID_title";

private static final String schemaDefinition = 
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

	public static Repository initialize(File databaseFile) throws LibrisException, XMLStreamException, IOException {
		HeadlessUi theUi = new HeadlessUi();
		if (!LibrisDatabase.newDatabase(theUi, databaseFile, "Repository")) {
			return null;
		}
		LibrisDatabaseParameter params = new LibrisDatabaseParameter(theUi, databaseFile);
		LibrisDatabase result = new LibrisDatabase(params);

		result.getFileMgr().createAuxFiles(true);
		return new Repository(result);

	}
	public static Repository open(File databaseFile, boolean readOnly) throws FactoryConfigurationError, LibrisException {
		HeadlessUi ui = new HeadlessUi(databaseFile, readOnly);
		final ElementReader xmlRdr = LibrisDatabase.xmlFactory.makeReader(new StringReader(schemaDefinition), "<internal>");
		XmlSchema mySchema = new XmlSchema(xmlRdr);
		ui.setSchema(mySchema);
		Repository result = new Repository(ui.openDatabase());
		return result;
	}

	public File getArtifact(int artifactId) {
		// TODO write getArtifact
		return null;
		
	}
	
	public ArtifactParameters getArtifactInfo(int artifactId) {
		// TODO write getArtifact
		return null;
		
	}
	
	public int putArtifact(ArtifactParameters params) throws InputException {
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
	
	public int putArtifact(URI location) throws InputException {
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

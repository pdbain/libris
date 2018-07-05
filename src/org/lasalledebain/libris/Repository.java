package org.lasalledebain.libris;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Objects;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.ui.HeadlessUi;
import org.lasalledebain.libris.ui.Layouts;
import org.lasalledebain.libris.xmlUtils.ElementReader;
import org.lasalledebain.libris.xmlUtils.LibrisXMLConstants;

import com.sun.media.jfxmediaimpl.MarkerStateListener;

import static org.lasalledebain.libris.Field.FieldType.T_FIELD_STRING;
import static org.lasalledebain.libris.Field.FieldType.T_FIELD_LOCATION;

public class Repository extends Libris {

private static final String REPOSITORY = "Repository";

public static final String ID_DATE = "ID_date";
public static final String ID_DOI = "ID_doi";

public static final String ID_KEYWORDS = "ID_keywords";

private static final String ID_SOURCE = "ID_source";

private static final String ID_TITLE = "ID_title";

LibrisDatabase database;
static public final int TITLE_FIELD = 0;
public final int SOURCE_FIELD = TITLE_FIELD+1;
public final int DOI_FIELD = SOURCE_FIELD+1;
public final int DATE_FIELD = DOI_FIELD+1;
public final int KEYWORDS_FIELD = DATE_FIELD+1;

	private static final DynamicSchema mySchema = makeSchema();

	public Repository(LibrisDatabase db) {
		database = db;
	}

	private static DynamicSchema makeSchema() {
		DynamicSchema theSchema = new DynamicSchema();
		 theSchema.addField(new FieldTemplate(theSchema, ID_TITLE, "", T_FIELD_STRING));
		 theSchema.addField(new FieldTemplate(theSchema, ID_SOURCE, "", T_FIELD_LOCATION));
		 theSchema.addField(new FieldTemplate(theSchema, ID_DOI, "", T_FIELD_STRING));
		 theSchema.addField(new FieldTemplate(theSchema, ID_DATE, "", T_FIELD_STRING));
		 theSchema.addField(new FieldTemplate(theSchema, ID_KEYWORDS, "", T_FIELD_STRING));
		return theSchema;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	public static boolean initialize(File databaseFile) throws LibrisException, XMLStreamException, IOException {
		HeadlessUi theUi = new HeadlessUi();
		LibrisDatabaseParameter params = new LibrisDatabaseParameter(theUi, databaseFile);
		params.databaseSchema = mySchema;
		params.schemaName = REPOSITORY;
		Layouts theLayouts = new Layouts(mySchema);
		MetadataHolder metadata = new MetadataHolder(mySchema, theLayouts);
		return LibrisDatabase.newDatabase(params, metadata);
	}

	public static Repository open(File databaseFile, boolean readOnly) throws FactoryConfigurationError, LibrisException {
		HeadlessUi ui = new HeadlessUi(databaseFile, readOnly);
		Repository result = new Repository(ui.openDatabase());
		return result;
	}

	public File getArtifact(int artifactId) throws InputException, URISyntaxException {
		final Record record = database.getRecord(artifactId);
		String uriString = record.getFieldValue(SOURCE_FIELD).getMainValueAsString();
		File result = new File(new URI(uriString));
		return result;
		
	}
	
	public ArtifactParameters getArtifactInfo(int artifactId) throws InputException, URISyntaxException {
		final Record record = database.getRecord(artifactId);
		String uriString = record.getFieldValue(SOURCE_FIELD).getMainValueAsString();
		ArtifactParameters result = new ArtifactParameters(new URI(uriString));
		result.date = record.getFieldValue(ID_DATE).getMainValueAsString();
		result.doi = record.getFieldValue(ID_DOI).getMainValueAsString();
		result.keywords = record.getFieldValue(ID_KEYWORDS).getMainValueAsString();
		result.title = record.getFieldValue(ID_TITLE).getMainValueAsString();
		return null;
		
	}
	
	public int putArtifact(ArtifactParameters params) throws LibrisException {
		Record rec = database.newRecord();
		rec.addFieldValue(ID_DATE, params.date);
		rec.addFieldValue(ID_DOI, params.doi);
		rec.addFieldValue(ID_KEYWORDS, params.keywords);
		rec.addFieldValue(ID_TITLE, params.title);
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
		int id = database.put(rec);
		return id;	
	}
	
	public int putArtifact(URI location) throws LibrisException {
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
		String doi;
		String recordParent;
		String recordName;
	}
}

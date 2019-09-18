package org.lasalledebain.libris;

import static org.lasalledebain.libris.exception.Assertion.assertEquals;
import static org.lasalledebain.libris.exception.Assertion.assertNotNull;
import static org.lasalledebain.libris.exception.Assertion.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import org.lasalledebain.libris.exception.Assertion;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.DatabaseNotIndexedException;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.exception.OutputException;
import org.lasalledebain.libris.exception.UserErrorException;
import org.lasalledebain.libris.exception.XmlException;
import org.lasalledebain.libris.indexes.AffiliateList;
import org.lasalledebain.libris.indexes.GroupManager;
import org.lasalledebain.libris.indexes.KeyIntegerTuple;
import org.lasalledebain.libris.indexes.LibrisIndexConfiguration;
import org.lasalledebain.libris.indexes.SortedKeyValueFileManager;
import org.lasalledebain.libris.records.DelimitedTextRecordsReader;
import org.lasalledebain.libris.records.Records;
import org.lasalledebain.libris.records.XmlRecordsReader;
import org.lasalledebain.libris.ui.Layouts;
import org.lasalledebain.libris.ui.LibrisUi;
import org.lasalledebain.libris.ui.Messages;
import org.lasalledebain.libris.xmlUtils.ElementManager;
import org.lasalledebain.libris.xmlUtils.ElementWriter;
import org.lasalledebain.libris.xmlUtils.LibrisAttributes;
import org.lasalledebain.libris.xmlUtils.LibrisXMLConstants;
import org.lasalledebain.libris.xmlUtils.XmlShapes;
import org.lasalledebain.libris.xmlUtils.XmlShapes.SHAPE_LIST;

public class LibrisDatabase extends GenericDatabase<DatabaseRecord> implements LibrisXMLConstants, LibrisConstants, XMLElement {
	/**
	 * XML representation of the schema and database records
	 */
	private File myDatabaseFile;
	private Schema mySchema;
	public  LibrisException rebuildException;
	protected ArtifactManager documentRepository;
	private RecordTemplate mainRecordTemplate;
	protected LibrisDatabaseMetadata databaseMetadata;
	private static final Logger librisLogger = setupLogger();

	private static Logger setupLogger() {
		Logger myLogger = Logger.getLogger(LibrisDatabase.class.getName());
		myLogger.setLevel(Level.SEVERE);
		return myLogger;
	}
	protected DatabaseAttributes xmlAttributes;
	private FileAccessManager databaseFileMgr;
	private FileAccessManager schemaFileMgr;
	private final ReservationManager reservationMgr;
	
	public LibrisDatabase(File databaseFile, boolean readOnly, LibrisUi ui) throws LibrisException  {
		this(databaseFile, readOnly, ui, null);
	}
	
	public LibrisDatabase(File theDatabaseFile, boolean readOnly, LibrisUi ui, Schema schem) throws LibrisException  {
		super(ui,new FileManager(getDatabaseAuxDirectory(theDatabaseFile, DATABASE_AUX_DIRECTORY_NAME)));
		FileAccessManager lockFileManager = fileMgr.makeAuxiliaryFileAccessManager(LibrisConstants.LOCK_FILENAME);
		reservationMgr = new ReservationManager(lockFileManager);
		myDatabaseFile = theDatabaseFile;
		databaseMetadata = new LibrisDatabaseMetadata(this);
		isModified = false;
		dbOpen = false;
		this.readOnly = readOnly;
		mySchema = schem;
		groupMgr = new GroupManager<DatabaseRecord>(this);
		documentRepository = null;
	}

	public void openDatabase() throws LibrisException {
		if (dbOpen) {
			throw new DatabaseException("Database already open");
		}
		if (!reserveDatabase()) {
			throw new UserErrorException("database is in use");
		}
		if (Objects.nonNull(mySchema)) {
			loadDatabaseInfo(false);
		} else {
			loadDatabaseInfo(true);
		}
		openDatabaseImpl();
		if (hasDocumentRepository()) {
			documentRepository.open();
		}
		dbOpen = true;
	}

	private void openDatabaseImpl() throws DatabaseNotIndexedException, DatabaseException, LibrisException {
		mainRecordTemplate = RecordTemplate.templateFactory(mySchema, new DatabaseRecordList(this));
		if (!isIndexed()) {
			throw new DatabaseNotIndexedException();
		} else {
			FileAccessManager propsMgr = fileMgr.getAuxiliaryFileMgr(LibrisConstants.PROPERTIES_FILENAME);
			synchronized (propsMgr) {
				FileInputStream ipFile = null;
				try {
					ipFile = propsMgr.getIpStream();
					databaseMetadata.readProperties(ipFile);
				} catch (IOException | LibrisException e) {
					propsMgr.delete();
					throw new DatabaseException("Exception reading properties file"+propsMgr.getPath(), e); //$NON-NLS-1$
				} finally {
					try {
						propsMgr.releaseIpStream(ipFile);
					} catch (IOException e) { /* empty */ }					
				}
			}
			indexMgr.open(readOnly);
			if (!databaseMetadata.isMetadataOkay()) {
				throw new DatabaseException("Error in metadata");
			}
			getDatabaseRecords();
			if (hasDocumentRepository()) {
				FileManager artifactFileMgr = new FileManager(getDatabaseAuxDirectory(myDatabaseFile, 
						REPOSITORY_AUX_DIRECTORY_NAME));
				documentRepository = new ArtifactManager(getUi(), databaseMetadata.getRepositoryRoot(), artifactFileMgr);
			}
		}
	}

	public boolean reserveDatabase() throws DatabaseException {
		return reservationMgr.reserveDatabase();
	}

	public boolean isDatabaseReserved() {
		return Objects.nonNull(reservationMgr) && reservationMgr.isDatabaseReserved();
	}

	/**
	 * @param force close without saving
	 * @return true if database is not modified or force is true.
	 * @throws DatabaseException  if database is not reserved
	 */
	public boolean closeDatabase(boolean force) throws DatabaseException {
		if (!isOkayToClose(force)) {
			return false;
		}
		if (!isDatabaseOpen() && force) {
			return true;
		}
		myDatabaseFile = null;
		if (isDatabaseReserved()) {
			reservationMgr.freeDatabase();
		}
		if (hasDocumentRepository()) {
			documentRepository.close(force);
		}
		databaseMetadata = null;
		mySchema = null;
		return super.closeDatabase(force);
	}

	private void loadDatabaseInfo(boolean doLoadMetadata) throws LibrisException {
		try {
			databaseFileMgr = fileMgr.makeAccessManager(LibrisConstants.DATABASE_NAME, myDatabaseFile);
			FileInputStream fileIpStream = databaseFileMgr.getIpStream();
			String databaseFilePath = databaseFileMgr.getPath();
			ElementManager librisMgr = makeLibrisElementManager(fileIpStream, databaseFilePath);
			fromXml(librisMgr);
			if (doLoadMetadata) {
				loadMetadata(librisMgr);
			}
			if (isLocked()) {
				readOnly = true;
			}
			librisMgr.closeFile();
			databaseFileMgr.releaseIpStream(fileIpStream);
		} catch (XmlException e) {	
			throw new DatabaseException("Error reading schema", e); //$NON-NLS-1$
		} catch (IOException e) {	
			throw new DatabaseException("Error closing schema file", e); //$NON-NLS-1$
		}
	}

	private void loadMetadata(ElementManager librisMgr)
			throws DatabaseException, XmlException, InputException, LibrisException {
		String nextElementId = librisMgr.getNextId();
		ElementManager metadataMgr;
		if (XML_METADATA_TAG.equals(nextElementId)) {
			metadataMgr = librisMgr.nextElement();
		} else {
			final String schemaLocation = xmlAttributes.getSchemaLocation();
			final String schemaName = xmlAttributes.getSchemaName();
			metadataMgr = makeMetadataMgr(schemaName, schemaLocation);
		}
		Assertion.assertNotNullInputException("could not open schema file", metadataMgr);
		databaseMetadata.fromXml(metadataMgr);
	}

	public static boolean newDatabase(File databaseFile, String schemaName, boolean readOnly, LibrisUi ui, LibrisMetadata metadata) 
			throws XMLStreamException, IOException, LibrisException {
		if (!databaseFile .createNewFile()) {
			ui.alert("Database file "+databaseFile.getAbsolutePath()+" already exisits");
			return false;
		}
		FileOutputStream databaseStream = new FileOutputStream(databaseFile);
		ElementWriter databaseWriter = ElementWriter.eventWriterFactory(databaseStream, 0);
		{
			LibrisAttributes attrs = new LibrisAttributes();
			attrs.setAttribute(XML_DATABASE_SCHEMA_NAME_ATTR, schemaName);
			attrs.setAttribute(XML_SCHEMA_VERSION_ATTR, Schema.currentVersion);
			attrs.setAttribute(XML_DATABASE_DATE_ATTR, LibrisMetadata.getCurrentDateAndTimeString());
			databaseWriter.writeStartElement(XML_LIBRIS_TAG, attrs, false);
		}
		metadata.toXml(databaseWriter);
		{
			LibrisAttributes recordsAttrs = new LibrisAttributes();
			databaseWriter.writeStartElement(XML_RECORDS_TAG, recordsAttrs, true);
		}
		databaseWriter.writeEndElement();
		databaseWriter.flush();
		databaseStream.close();

		return Libris.buildIndexes(databaseFile, ui);
	}

	boolean buildDatabase(LibrisIndexConfiguration config) throws LibrisException {
		boolean result = true;
		initialize();
		if (!reserveDatabase()) {
			result = false;
		} else {
			loadDatabaseInfo(config.isLoadMetadata());
			mainRecordTemplate = RecordTemplate.templateFactory(mySchema, new DatabaseRecordList(this));
			final File databaseFile = getDatabaseFile();
			Records<DatabaseRecord> recs = getDatabaseRecords();
			try {
				ElementManager librisMgr = this.makeLibrisElementManager(databaseFile);
				librisMgr.parseOpenTag();
				String nextElement = librisMgr.getNextId();
				if (XmlRecordsReader.XML_INSTANCE_TAG.equals(nextElement)) {
					ElementManager instanceMgr = librisMgr.nextElement();
					instanceMgr.flushElement();
					nextElement = librisMgr.getNextId();
				}
				ElementManager metadataMgr;
				if (XmlRecordsReader.XML_METADATA_TAG.equals(nextElement)) {
					metadataMgr = librisMgr.nextElement();
					metadataMgr.flushElement();
					nextElement = librisMgr.getNextId();	
				}
				ElementManager recordsMgr = librisMgr.nextElement();
				recs.fromXml(recordsMgr);
				buildIndexes(config);
				nextElement = librisMgr.getNextId();
				if (XmlRecordsReader.XML_ARTIFACTS_TAG.equals(nextElement)) {
					ElementManager artifactsMgr = librisMgr.nextElement();
					File artifactDirectory = config.getArtifactDirectory();
					LibrisUi databaseUi = config.getDatabaseUi();
					initializeDocumentRepository(databaseUi, artifactDirectory);
					documentRepository.fromXml(artifactsMgr);
					documentRepository.buildIndexes(config);
				}
				saveMetadata();
				librisMgr.closeFile();
				this.closeDatabaseSource();
			} catch (FactoryConfigurationError | FileNotFoundException e) {
				String msg = Messages.getString("XmlRecordsReader.4"); //$NON-NLS-1$
				LibrisDatabase.log(Level.SEVERE, msg, e); //$NON-NLS-1$
				throw new DatabaseException(msg); //$NON-NLS-1$
			}
			reservationMgr.freeDatabase();
		}
		return result;
	}

	private void initializeDocumentRepository(LibrisUi databaseUi, File artifactDirectory)
			throws DatabaseException, LibrisException {
		FileManager artifactFileMgr = new FileManager(getDatabaseAuxDirectory(myDatabaseFile, 
				REPOSITORY_AUX_DIRECTORY_NAME));
		documentRepository = new ArtifactManager(databaseUi, artifactDirectory, artifactFileMgr);
		documentRepository.initialize(true);
		databaseMetadata.hasDocumentRepository(true);
		databaseMetadata.setRepositoryRoot(artifactDirectory);
	}

	public String getElementTag() {
		return getXmlTag();
	}

	public String getXmlTag() {
		return XML_LIBRIS_TAG;
	}
	
	@Override
	public void fromXml(ElementManager librisMgr) throws LibrisException {
		HashMap<String, String> dbElementAttrs = librisMgr.parseOpenTag();
		LibrisAttributes attrs = new LibrisAttributes(dbElementAttrs);
		String dateString = attrs.get(XML_DATABASE_DATE_ATTR);
		if ((null == dateString) || dateString.isEmpty()) {
			databaseMetadata.setDatabaseDate(new Date());
		} else try {
			databaseMetadata.setDatabaseDate(LibrisMetadata.parseDateString(dateString));
		} catch (ParseException e) {
			throw new InputException("illegal date format: "+dateString, e);
		}
		
		String nextElement = librisMgr.getNextId();
		DatabaseInstance instanceInfo = null;
		if (XML_INSTANCE_TAG.equals(nextElement)) {
			ElementManager instanceMgr = librisMgr.nextElement();
			instanceInfo  = new DatabaseInstance();
			instanceInfo.fromXml(instanceMgr);
			databaseMetadata.setInstanceInfo(instanceInfo);
			nextElement = librisMgr.getNextId();
		}
		xmlAttributes = new DatabaseAttributes(dbElementAttrs);
		if (xmlAttributes.isLocked()) {
			readOnly = true;
		}
	}

	@Override
	public void toXml(ElementWriter outWriter) throws LibrisException {
		outWriter.writeStartElement(XML_LIBRIS_TAG, getAttributes(), false);
		databaseMetadata.toXml(outWriter);
		databaseRecords.toXml(outWriter);
		if (hasDocumentRepository()) {
			documentRepository.toXml(outWriter);
		}
		outWriter.writeEndElement(); /* database */	
		outWriter.flush();
	}

	private void toXml(ElementWriter outWriter, boolean includeMetadata,
			Iterable<DatabaseRecord> recordSource, boolean includeInstanceInfo) throws XmlException, LibrisException {
		outWriter.writeStartElement(XML_LIBRIS_TAG, getAttributes(), false);
		if (includeInstanceInfo) {
			DatabaseInstance databaseInstanceInfo = databaseMetadata.getInstanceInfo();
			if (Objects.isNull(databaseInstanceInfo)) {
				databaseInstanceInfo = new DatabaseInstance(databaseMetadata);
			}
			databaseInstanceInfo.toXml(outWriter);
		}
		if (includeMetadata) {
			databaseMetadata.toXml(outWriter, includeInstanceInfo);
		}
		if (null != recordSource) {
			LibrisAttributes recordsAttrs = new LibrisAttributes();
			outWriter.writeStartElement(XML_RECORDS_TAG, recordsAttrs, false);
			for (Record r: recordSource) {
				r.toXml(outWriter);
			}
			outWriter.writeEndElement(); /* records */
		}
		outWriter.writeEndElement(); /* database */	
		outWriter.flush();
	}

	public void exportDatabaseXml(OutputStream destination, boolean includeSchema, boolean includeRecords, boolean addInstanceInfo) throws LibrisException {
		ElementWriter outWriter;
		if (!isDatabaseOpen()) {
			throw new UserErrorException("exportDatabaseXml: database not open");
		}
	
		try {
			outWriter = ElementWriter.eventWriterFactory(destination);
		} catch (XMLStreamException e) {
			throw new OutputException(Messages.getString("LibrisDatabase.exception_export_xml"), e); //$NON-NLS-1$
		}
		toXml(outWriter, includeSchema, getRecordReader(), addInstanceInfo); 
	}

	public void exportDatabaseXml(OutputStream destination) throws LibrisException {
		ElementWriter outWriter;
		if (!isDatabaseOpen()) {
			throw new UserErrorException("exportDatabaseXml: database not open");
		}
	
		try {
			outWriter = ElementWriter.eventWriterFactory(destination);
		} catch (XMLStreamException e) {
			throw new OutputException(Messages.getString("LibrisDatabase.exception_export_xml"), e); //$NON-NLS-1$
		}
		toXml(outWriter); 
	}

	/**
	 * Read records from a database forked instance and merge them to the current database.
	 * @param instanceReader reader for the XML of the exported records
	 * @throws FileNotFoundException if the specified increment file is missing
	 * @throws LibrisException if an error is encountered
	 */
	public boolean importIncrement(File incrementFile) throws FileNotFoundException, LibrisException {
		ElementManager incrementManager = makeLibrisElementManager(incrementFile);
		HashMap<String, String> incElementAttrs = incrementManager.parseOpenTag();
		if (
				!assertEquals(ui, "increment databasename attribute does not match database", xmlAttributes.getDatabaseName(), incElementAttrs.get(XML_DATABASE_NAME_ATTR))
				|| !		assertEquals(ui, "increment databasename attribute does not match database", xmlAttributes.getSchemaName(), incElementAttrs.get(XML_DATABASE_SCHEMA_NAME_ATTR))
				) {
			return false;
		}
		String nextElement = incrementManager.getNextId();
		if (!assertEquals(ui, "Wrong initial element in increment file", XML_INSTANCE_TAG, nextElement)) {
			return false;
		};
		ElementManager instanceMgr = incrementManager.nextElement();
		DatabaseInstance instanceInfo = new DatabaseInstance();
		instanceInfo.fromXml(instanceMgr);
		if (
				!assertNotNull(ui, "instance forkdate",instanceInfo.getForkDate())
				|| !assertNotNull(ui, "instance joindate",instanceInfo.getJoinDate())
				) {
			return false;
		}
		int baseId = instanceInfo.getRecordIdBase();
		int lastRecordId = getLastRecordId();
		if (!assertTrue(ui, "increment base record ID > root database last ID", baseId <= lastRecordId)) {
			return false;
		}
		int idAdjustment = lastRecordId - baseId;
		nextElement = incrementManager.getNextId();
		if (!assertEquals(ui, "Wrong element after instance element in increment file", XML_RECORDS_TAG, nextElement)) {
			return false;
		};
		ElementManager recsMgr = incrementManager.nextElement();
		Iterable<DatabaseRecord> recordsReader = new XmlRecordsReader<DatabaseRecord>(this, recsMgr);
		for (DatabaseRecord newRec: recordsReader) {
			if (idAdjustment > 0) {
				newRec.offsetIds(baseId, idAdjustment);
			}
			putRecord(newRec);
		}
		return true;
	}

	public void closeDatabaseSource() {
		databaseFileMgr.close();
	}

	@Override
	public boolean equals(Object comparand) {
		try {
			LibrisDatabase otherDb = (LibrisDatabase) comparand;
			if (isModified || otherDb.isModified()) {
				log(Level.FINE, "one of the databases has unsaved changes"); //$NON-NLS-1$
				return false; 
			}
			if (!databaseMetadata.equals(otherDb.databaseMetadata)) {
				log(Level.FINE, "metadata do not match"); //$NON-NLS-1$
				return false;
			}
			if (!mySchema.equals(otherDb.mySchema)) {
				log(Level.FINE, "schema do not match"); //$NON-NLS-1$
				return false;
			}
			try {
				if (getLastRecordId() != otherDb.getLastRecordId()) {
					return false;
				}
				for (Record r: getDatabaseRecords()) {
					int rid = r.getRecordId();
					Record otherRecord = otherDb.getRecord(rid);
					if (!r.equals(otherRecord)) {
						return false;
					}
				}
			} catch (LibrisException e) {
				// TODO throw error
				log(Level.SEVERE, "error reading records", e); //$NON-NLS-1$
				return false;
			}
			return true;
		} catch (ClassCastException e) {
			log(Level.WARNING, "incompatible type in "+getClass().getName()+".equals()"); //$NON-NLS-1$ //$NON-NLS-2$
			return false;
		}
	}

	/**
	 * @param sourceFileMgr
	 * @return
	 * @throws InputException 
	 * @throws FileNotFoundException
	 * @throws SchemaException
	 */
	public static ElementManager makeLibrisElementManager(FileInputStream fileStream, String filePath) throws InputException {
		String initialElementName = LibrisXMLConstants.XML_LIBRIS_TAG;
		return makeDatabaseElementManager(fileStream, filePath, initialElementName);
	}

	public static ElementManager makeDatabaseElementManager(FileInputStream fileStream, String filePath,
			String initialElementName) throws InputException {
		try {
			InputStreamReader xmlFileReader = new InputStreamReader(fileStream);
			fileStream.getChannel().position(0);
			Reader rdr = xmlFileReader;
			ElementManager mgr = makeElementManager(rdr, initialElementName, filePath);
			return mgr;
		} catch (IOException e) {
			String msg = "error opening "+filePath; //$NON-NLS-1$
			librisLogger.log(Level.SEVERE, msg, e); //$NON-NLS-1$
			throw new InputException("error opening "+msg, e); //$NON-NLS-1$
		}
	}

	public static ElementManager makeElementManager(Reader reader, String initialElementName, String filePath) throws
	InputException {
		return getXmlFactory().makeElementManager(reader, 
				filePath, initialElementName, new XmlShapes(SHAPE_LIST.DATABASE_SHAPES));
	}

	public ElementManager makeLibrisElementManager(File source) throws InputException, FileNotFoundException {
		FileInputStream elementSource = new FileInputStream(source);
		return makeLibrisElementManager(elementSource, source.getPath());
	}

	protected ElementManager makeMetadataMgr(String schemaName, String schemaLocation) throws InputException {
		InputStreamReader metadataReader = null;
		ElementManager metadataMgr = null;
		try {
			if (null == schemaFileMgr) {
				if (null == schemaLocation) {
					File schemaFile = new File(schemaName+FILENAME_XML_FILES_SUFFIX);
					try {
						if (!setSchemaAccessMgr(fileMgr, schemaFile)) {
							schemaFile = new File(getUi().SelectSchemaFile(schemaName));
						}
						setSchemaAccessMgr(fileMgr, schemaFile);
					} catch (DatabaseException e) {
						throw new InputException(LibrisConstants.COULD_NOT_OPEN_SCHEMA_FILE+schemaFile.getPath(), e);
					}
				} else {
					URL metadataUrl = new URL(schemaLocation);
					if (metadataUrl.getProtocol().equalsIgnoreCase("file")) { //$NON-NLS-1$
						String metaPath = metadataUrl.getPath();
						File schemaFile = new File(metaPath);
						if (!schemaFile.isAbsolute()) {
							schemaFile = new File(getDatabaseDirectoryPath(), metaPath);
						}
						setSchemaAccessMgr(fileMgr, schemaFile);
					}
				}
			}
			metadataReader = new InputStreamReader(schemaFileMgr.getIpStream());
			ElementManager librisMgr = makeElementManager(metadataReader, XML_LIBRIS_TAG, schemaFileMgr.getPath());
			librisMgr.parseOpenTag();
			metadataMgr = librisMgr.nextElement();
			return metadataMgr;
		} catch (FileNotFoundException | DatabaseException | MalformedURLException e) {
			throw new InputException(LibrisConstants.COULD_NOT_OPEN_SCHEMA_FILE+schemaLocation, e);
		}
	}

	public synchronized boolean setSchemaAccessMgr(FileManager fileMgr, File schemaFile) throws DatabaseException {
		if (null != schemaFileMgr) {
			throw new DatabaseException("schema file already set");
		}
		schemaFileMgr = fileMgr.makeAuxiliaryFileAccessManager(LibrisConstants.SCHEMA_NAME);
		return schemaFile.exists() && (schemaFile.length() > 0);
	}

	public  File getDatabaseFile() {
		synchronized (fileMgr) {
			fileMgr.checkLock();
			return myDatabaseFile;
		}
	}
	

	public String getDatabaseDirectoryPath() throws DatabaseException {
		if (null == myDatabaseFile) {
			throw new DatabaseException("Database file not set");
		}
		return myDatabaseFile.getParent();
	}

	public static File getDatabaseAuxDirectory(File theDatabaseFile, String auxDirectoryName) throws DatabaseException {
		if (!theDatabaseFile.exists()) {
			throw new DatabaseException("Database file not set");
		}
		File parentDrectory = theDatabaseFile.getParentFile();
		String directoryName = theDatabaseFile.getName();
		int suffixPosition = directoryName.lastIndexOf(".xml");
		if (suffixPosition > 0) {
			directoryName = directoryName.substring(0, suffixPosition);
		}
		File auxDirectory = new File(parentDrectory, auxDirectoryName+'_'+directoryName);
		return auxDirectory;
	}

	// TODO test read-onlyness
	
	public LibrisMetadata getMetadata() {
		return databaseMetadata;
	}

	public void setSchema(Schema schem) {
		this.mySchema = schem;
	}

	@Override
	public Schema getSchema() {
		return mySchema;
	}

	/**
	 * Add a document (artifacts) repository to the database.
	 * @param repositoryRoot File representing the directory to hold artifacts, or null if the default location is to be used
	 * @throws LibrisException if the database has a repository already
	 */
	public void addDocumentRepository(File repositoryRoot) throws LibrisException {
		if (hasDocumentRepository()) {
			throw new DatabaseException("Document repository already set");
		}
		if (Objects.isNull(repositoryRoot)) {
			repositoryRoot = LibrisIndexConfiguration.getDefautlArtifactsDirectory(myDatabaseFile);
		}
		initializeDocumentRepository(ui, repositoryRoot);
		if (isDatabaseOpen()) {
			documentRepository.open();
		}
	}
	@Override
	public LibrisAttributes getAttributes() {
		return xmlAttributes;
	}

	public DatabaseAttributes getDatabaseAttributes() {
		return xmlAttributes;
	}

	public void setAttributes(DatabaseAttributes attrs) {
		xmlAttributes = attrs;
	}

	public Layouts getLayouts() {
		return databaseMetadata.getLayouts();
	}
	public void viewRecord(int recordId) {
		try {
			ui.displayRecord(recordId);
		} catch (LibrisException e) {
			ui.alert(Messages.getString("LibrisDatabase.error_display_record")+recordId, e); //$NON-NLS-1$
		}
	}
	
	@Override
	public synchronized DatabaseRecord newRecord() throws InputException {
		return mainRecordTemplate.makeRecord(true);
	}

	public void setTermCount(String term, boolean normalize, int termCount) throws DatabaseException {
		indexMgr.setTermCount(term, normalize, termCount);
	}

	public int getTermCount(String term) throws DatabaseException {
		return indexMgr.getTermCount(term);
	}

	public Function<String, Integer> getDocumentFrequencyFunction() {
		return t -> indexMgr.getTermCount(t);
	}

	/**
	 * Save a record in the list of modified records
	 * @param rec Record to enter
	 * @return ID of the new record
	 * @throws LibrisException 
	 */
	public int putRecord(DatabaseRecord rec) throws LibrisException {
		LibrisMetadata metaData = getMetadata();
		int id = genericPutRecord(metaData, rec);
		for (int g = 0; g < mySchema.getNumGroups(); ++g) {
			int[] affiliations = rec.getAffiliates(g);
			if (affiliations.length != 0) {
				if (affiliations[0] != NULL_RECORD_ID) {
					indexMgr.addChild(g, affiliations[0], id);
				}
				for (int i = 1; i < affiliations.length; ++i) {
					indexMgr.addAffiliate(g, affiliations[i], id);
				}
			}
		}
		log(Level.FINE, "LibrisDatabase.put "+rec.getRecordId()); //$NON-NLS-1$
		return id;
	}

	// TODO addArtifact(int recordId, parameters object)
	public void addArtifact(int recordId, File artifactSourceFile) throws LibrisException, IOException {
		DatabaseRecord rec = getRecord(recordId);
		if (Objects.isNull(rec)) {
			throw new DatabaseException("Record "+recordId+" not found");
		}
		if (!rec.isEditable()) {
			throw new DatabaseException("Record "+recordId+" is read-only");
		}
		if (!artifactSourceFile.exists()) {
			throw new DatabaseException("File "+artifactSourceFile.getAbsolutePath()+" not found");
		}
		if (!hasDocumentRepository()) {
			throw new DatabaseException("Database has no document repository");
		}
		ArtifactParameters params = new ArtifactParameters(artifactSourceFile.toURI());
		int artifactId = documentRepository.importFile(params);
		rec.setArtifactId(artifactId);
		putRecord(rec);
	}
	public void saveRecords(Iterable <Record> recList) throws LibrisException {

		if (isIndexed()) {
			saveMetadata();
		}
		setModified(false);
		Records<DatabaseRecord> recs = getDatabaseRecords();
		int id = NULL_RECORD_ID;
		int lastId = 0;
		LibrisMetadata metadata = getMetadata();
		for (Record rec: recList) {
			id = rec.getRecordId();
			if (RecordId.isNull(id)) {
				id = metadata.newRecordId();
				rec.setRecordId(id);
			}
			recs.putRecord(rec);
			lastId = Math.max(id, lastId);
		}
		if (!RecordId.isNull(id)) {
			metadata.setLastRecordId(lastId);
		}

		try {
			recs.flush();
		} catch (IOException e) {
			throw (new OutputException("Error saving records", e)); //$NON-NLS-1$
		}
	}

	public RecordList<DatabaseRecord> getRecords() {
		return new DatabaseRecordList(this);	
	}

	public int getModifiedRecordCount() {
		return databaseMetadata.getModifiedRecords();
	}

	public boolean isLocked() {
		return xmlAttributes.isLocked();
	}
	
	public boolean isFork() {
		DatabaseInstance instanceInfo = databaseMetadata.getInstanceInfo();
		return (null != instanceInfo);
	}

	public boolean hasDocumentRepository() {
		return databaseMetadata.hasDocumentRepository();
	}

	public void lockDatabase() {
		xmlAttributes.setLocked(true);
		readOnly = true;
	}

	@Override
	public boolean isRecordReadOnly(int recordId) {
		boolean result;
		if (readOnly) {
			result = true;
		} else {
			result = (recordId <= getMetadata().getRecordIdBase());
		}
		return result;
	}

	public static void log(Level severity, String msg, Throwable e) {
		librisLogger.log(severity, msg, e);
	}
	public static void logException(String msg, Throwable e) {
		librisLogger.log(Level.SEVERE, msg, e);
	}
	public static void log(Level severity, String msg) {
		librisLogger.log(severity, msg);
	}
	public static void log(Level severity, String msg, Object param) {
		librisLogger.log(severity, msg, param);
	}
	
	public static void setLogLevel(Level severity) {
		librisLogger.setLevel(severity);
	}

	public static Record[] importDelimitedTextFile(LibrisDatabase db, 
			File dataFile, String fids[], char separatorChar)
					throws FileNotFoundException, LibrisException {
		DelimitedTextRecordsReader importer = new DelimitedTextRecordsReader(db, separatorChar);
		importer.setDatafile(dataFile);
		importer.fieldIds = fids;
		Record[] recs = importer.importRecordsToDatabase();
		return recs;
	}

	public boolean exportIncrement(OutputStream destination) throws OutputException  {
		if (isModified()) {
			ui.alert("Cannot export an increment from a database which has unsaved changes");
			return false;
		}
		DatabaseInstance instanceInfo = databaseMetadata.getInstanceInfo();
		if (!isFork()) {
			ui.alert("Cannot export an increment from a database which is not a fork");
			return false;
		}
		lockDatabase();
		instanceInfo.doJoin();
		Iterable<DatabaseRecord> rangeIter = new RangeRecordIterable<DatabaseRecord>(this, instanceInfo.getRecordIdBase(), getLastRecordId());
		try {
			ElementWriter outWriter = ElementWriter.eventWriterFactory(destination);
			toXml(outWriter, false, rangeIter, true); 
		} catch (XMLStreamException | LibrisException e) {
			throw new OutputException(Messages.getString("LibrisDatabase.exception_export_xml"), e); //$NON-NLS-1$
		}
		return true;
	}

	public void exportFork(FileOutputStream instanceStream) throws LibrisException {
		if (isFork()) {
			throw new UserErrorException("Cannot fork from a database fork");			
		}
		exportDatabaseXml(instanceStream, true, true, true);

	}

	public GroupManager<DatabaseRecord> getGroupMgr() {
		return groupMgr;
	}

	public SortedKeyValueFileManager<KeyIntegerTuple> getNamedRecordIndex() {
		return indexMgr.getNamedRecordIndex();
	}

	@Override
	public RecordFactory<DatabaseRecord> getRecordFactory() {
		return mainRecordTemplate;
	}

	public Iterable<DatabaseRecord> getChildRecords(int parent, int groupNum, boolean allDescendents) {
		AffiliateList<DatabaseRecord> affList = indexMgr.getAffiliateList(groupNum);
		if (allDescendents) {
			return affList.getDescendents(parent, this.getRecords());
		} else {
			int[] result = affList.getChildren(parent);
			return new ArrayRecordIterator<DatabaseRecord>(getRecords(), result);
		}
	}

	public Iterable<DatabaseRecord> getAffiliateRecords(int parent, int groupNum) {
		AffiliateList<DatabaseRecord> affList = indexMgr.getAffiliateList(groupNum);
		int[] result = affList.getAffiliates(parent);
		return new ArrayRecordIterator<DatabaseRecord>(getRecords(), result);
	}

	public File getArtifactFile(int artifact) {
		if (hasDocumentRepository() && !RecordId.isNull(artifact)) {
			return documentRepository.getArtifactArchiveFile(artifact);
		} else {
			return null;
		}
	}

	public File getArtifactFileForRecord(int recordId) throws InputException, DatabaseException {
		DatabaseRecord rec = getRecord(recordId);
		if (Objects.isNull(rec)) {
			throw new DatabaseException("Record "+recordId+" not found");
		}
		File result = null;
		int artifactId = rec.getArtifactId();
		if (hasDocumentRepository() && !RecordId.isNull(artifactId)) {
			result =  documentRepository.getArtifactArchiveFile(artifactId);
		}
		return result;
	}

	public ArtifactParameters getArtifactInfo(int artifact) {
		if (hasDocumentRepository() && !RecordId.isNull(artifact)) {
			return documentRepository.getArtifactInfo(artifact);
		} else {
			return null;
		}
	}

	public void incrementTermCount(String term) {
		indexMgr.incrementTermCount(term);
	}
	
	public void incrementTermCounts(final Stream<String> terms) {
		terms.sorted().distinct()
		.forEach(term -> indexMgr.incrementTermCount(term));
	}
}



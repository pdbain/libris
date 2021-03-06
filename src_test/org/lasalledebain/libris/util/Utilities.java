package org.lasalledebain.libris.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import org.lasalledebain.libris.DatabaseArchive;
import org.lasalledebain.libris.DatabaseRecord;
import org.lasalledebain.libris.Field;
import org.lasalledebain.libris.Field.FieldType;
import org.lasalledebain.libris.FieldTemplate;
import org.lasalledebain.libris.FileAccessManager;
import org.lasalledebain.libris.GenericDatabase;
import org.lasalledebain.libris.LibrisDatabase;
import org.lasalledebain.libris.LibrisMetadata;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.RecordFactory;
import org.lasalledebain.libris.RecordId;
import org.lasalledebain.libris.RecordTemplate;
import org.lasalledebain.libris.Schema;
import org.lasalledebain.libris.XmlSchema;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.exception.RecordDataException;
import org.lasalledebain.libris.exception.XmlException;
import org.lasalledebain.libris.indexes.FileSpaceManager;
import org.lasalledebain.libris.indexes.KeyIntegerTuple;
import org.lasalledebain.libris.indexes.LibrisDatabaseConfiguration;
import org.lasalledebain.libris.indexes.LibrisJournalFileManager;
import org.lasalledebain.libris.ui.DatabaseUi;
import org.lasalledebain.libris.ui.HeadlessUi;
import org.lasalledebain.libris.ui.Layouts;
import org.lasalledebain.libris.ui.TestGUI;
import org.lasalledebain.libris.xmlUtils.ElementManager;
import org.lasalledebain.libris.xmlUtils.ElementWriter;
import org.lasalledebain.libris.xmlUtils.LibrisAttributes;
import org.lasalledebain.libris.xmlUtils.LibrisXMLConstants;
import org.lasalledebain.libris.xmlUtils.LibrisXmlFactory;
import org.lasalledebain.libris.xmlUtils.XmlShapes;
import org.lasalledebain.libris.xmlUtils.XmlShapes.SHAPE_LIST;

import junit.framework.TestCase;
public class Utilities<RecordType extends Record> extends TestCase implements LibrisXMLConstants {
	public static final String KEYWORD_DATABASE1_ARCHIVE = "KeywordDatabase1_archive.tar";
	public static final String HTML_TEST_DATABASE = "htmlTestDatabase1.libr";
	public static final String KEYWORD_DATABASE4_XML = "KeywordDatabase4.xml";
	public static final String KEYWORD_DATABASE1_XML = "KeywordDatabase1.xml";
	public static final String KEYWORD_DATABASE0_XML = "KeywordDatabase0.xml";
	public static final String LAYOUT_DECLARATIONS_XML_FILE = "layoutDeclarations.xml";
	public static final String TEST_RECORD1_XML_FILE = "TestRecord1.xml";
	public static final String TEST_RECORD2_XML_FILE = "TestRecord2.xml";
	public static final String TEST_RECORD_WITH_GROUPS_XML_FILE = "TestRecordWithGroups.xml";
	public static final String TEST_RECORD_WITH_MISSING_FIELDS = "TestRecordWithMissingFields.xml";
	public static final String TEST_RECORD_WITH_ENUM_XML_FILE = "TestRecordWithEnum.xml";
	public static final String TEST_DELIM_TEXT_FILE_1 = "testimport1.csv";
	public static final String TEST_DELIM_TEXT_FILE_WITH_FIELD_IDS = "testImportWithFieldIds.csv";
	public static final String TEST_RECORD3_XML_FILE = "TestRecord3.xml";
	public static final String TEST_SCHEMA_XML_FILE = "schema.xml";
	public static final String TEST_SCHEMA_ENUMDEFS_XML_FILE = "schemaWithEnumDefs.xml";
	public static final String LAYOUT_DECLARATIONS2_XML_FILE = "layoutDeclarations2.xml";
	public static final String TEST_SCHEMA2_XML_FILE = "schema2.xml";
	public static final String TEST_DB1_XML_FILE = "testDatabase1.xml";
	public final static String TEST_DB1_IMPORT_FILE ="csv_import.xml";
	public static final String TEST_DB4_XML_FILE = "testDatabase4.xml";
	public static final String TEST_DB4_NOMETADATA_FILE = "testDatabase4_nometadata.libr";
	public static final String TEST_DB4_METADATAONLY_FILE = "testDatabase4_metadataonly.libr";
	public static final String TEST_DB_WITH_DEFAULTS_XML_FILE = "DatabaseWithDefaults.xml";
	public static final String TEST_DB_WITH_GROUPS_XML_FILE = "DatabaseWithGroups.xml";
	public static final String EMPTY_DATABASE_FILE = "emptyDatabase1.xml";
	public static final String SCHEMA_WITH_GROUP_DEFS_XML = "schemaWithGroupDefs.xml";
	public static final String SCHEMA_WITH_DEFAULTS = "SchemaWithDefaults.xml";
	public static final String DATABASE_WITH_GROUPS_XML = "DatabaseWithGroups.xml";
	public static final String DATABASE_WITH_GROUPS_AND_RECORDS_XML = "DatabaseWithGroupsAndRecords.xml";
	public static final String EXAMPLE_DATABASE1_FILE = "example_database1.libr";
	public static final String TEST_DATABASE_WITH_REPO = "testDatabaseWithRepo.xml";
	public static final String TEST_DATABASE_UNORDERED_XML = "testDatabaseUnordered.xml";
	public static final String EXAMPLE_ARTIFACT_PDF = "example_artifact.pdf";
	public static final String EXAMPLE_LARGE_PDF = "Mesa.pdf";
	public static final String EXAMPLE_DOCS_ZIP = "example_docs.zip";
	public static final String EXAMPLE_FILES = "example_pdfs";

	public static RecordFactory<DatabaseRecord> makeRecordTemplate(String[] fieldNames,
			FieldType[] fts) throws DatabaseException, LibrisException {
		Schema s = new MockSchema();
		for (int i = 0; i < fts.length; ++i) {
			String id = fieldNames[i];
			FieldTemplate ft = createTemplate(id, fts[i]);
			s.addField(ft);
		}
		RecordFactory<DatabaseRecord> rt = RecordTemplate.templateFactory(s);
		return rt;
	}

	public static FieldTemplate createTemplate(String id,
			Field.FieldType ft) {
		return new FieldTemplate(new MockSchema(), id, "", ft);
	}

	public static File getTestDataDirectory() {
		trace(System.getProperty("user.dir"));
		File testDir = new File(System.getProperty("user.dir"), "test_data");
		return testDir;
	}

	private static File getTestFile(String testFile) {
		File testDir = getTestDataDirectory();
		return new File(testDir, testFile);
	}

	public static File makeTestDirectory() {
		File testDirectory = new File(System.getProperty("java.io.tmpdir"), "librisTest");
		if (!testDirectory.exists()) {
			testDirectory.mkdir();
		}
		return testDirectory;
	}

	public static final String RECIPE_DATABASE1_LIBR = "RecipeDatabase1.libr";
	public static final String TESTDATABASE3_XML = "Testdatabase3.xml";

	public Layouts loadLayoutsFromXml(Schema schem, File inputFile)
			throws FileNotFoundException, FactoryConfigurationError,
			XMLStreamException, LibrisException, DatabaseException {
		ElementManager mgr = makeElementManagerFromFile(inputFile, "layouts");
		Layouts myLayouts = new Layouts(schem);
		myLayouts.fromXml(mgr);
		return myLayouts;
	}

	public static DatabaseRecord loadRecordFromXml(File schemaFile, File recordFile)
			throws LibrisException, XMLStreamException,
			LibrisException, RecordDataException,
			FactoryConfigurationError, DatabaseException, FileNotFoundException {
		Schema s = XmlSchema.loadSchema(schemaFile);
		RecordFactory<DatabaseRecord> rt = RecordTemplate.templateFactory(s);
		ElementManager mgr = makeElementManagerFromFile(recordFile, "record");
		DatabaseRecord rec = rt.makeRecord(true);
		rec.fromXml(mgr);
		return rec;
	}

	public static Record loadRecordFromXml(File schemaFile, InputStream xmlStream, File sourceFile) throws 
	LibrisException, FileNotFoundException, XMLStreamException, FactoryConfigurationError {
		Schema s = XmlSchema.loadSchema(schemaFile);
		RecordFactory<DatabaseRecord> rt = RecordTemplate.templateFactory(s);
		String sourcePath = (null == sourceFile)? null : sourceFile.getPath();
		ElementManager mgr = makeElementManagerFromInputStream(xmlStream, sourcePath, LibrisXMLConstants.XML_RECORD_TAG);
		Record rec = rt.makeRecord(true);
		rec.fromXml(mgr);
		return rec;
	}

	public static ElementManager makeElementManagerFromFile(File recordFile,
			String elementNameString) throws XmlException  {
		FileInputStream xmlInputStream;
		try {
			xmlInputStream = new FileInputStream(recordFile);
			return makeElementManagerFromInputStream(xmlInputStream, recordFile.getPath(), elementNameString);
		} catch (FileNotFoundException e) {
			throw new XmlException("error parsing "+elementNameString, e);
		}
	}

	public static final String LAYOUT2 = "layout2";
	public static ElementManager makeElementManager(
			ByteArrayInputStream xmlInput, File sourceFile, String elementNameString) {
		String sourcePath = (null == sourceFile)? null : sourceFile.getPath();
		return makeElementManagerFromInputStream(xmlInput, sourcePath, elementNameString) ;
	}

	/**
	 * @param elementNameString
	 * @param xmlInputStream
	 * @return
	 */
	public static ElementManager makeElementManagerFromInputStream(
			InputStream xmlInputStream, String sourceFile, String elementNameString) {
		LibrisXmlFactory xmlFactory = new LibrisXmlFactory();
		InputStreamReader xmlInput = new InputStreamReader(xmlInputStream);
		try {
			return xmlFactory.makeElementManager(xmlInput, 
					sourceFile, elementNameString, new XmlShapes(SHAPE_LIST.DATABASE_SHAPES));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static ElementManager makeElementManagerFromReader(
			Reader xmlSourceReader, String sourceFile, String elementNameString) {
		LibrisXmlFactory xmlFactory = new LibrisXmlFactory();
		try {
			return xmlFactory.makeElementManager(xmlSourceReader, 
					sourceFile, elementNameString, new XmlShapes(SHAPE_LIST.DATABASE_SHAPES));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static boolean deleteRecursively(File root) {
		if (null == root) {
			return false;
		}
		if (root.isDirectory()) {
			for (File f: root.listFiles()) {
				deleteRecursively(f);
			} 
		}
		return root.delete();
	}
	/**
	 * @param originalFile
	 * @param copyFile
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static void copyFile(File originalFile, File copyFile)
			throws FileNotFoundException, IOException {
		if (copyFile.exists()) {
			copyFile.delete();
		}
		copyFile.createNewFile();
		copyFile.setWritable(true);
		try (final FileInputStream originalStream = new FileInputStream(originalFile);
				final FileOutputStream copyStream = new FileOutputStream(copyFile)){
			FileChannel original = originalStream.getChannel();
			FileChannel copy = copyStream.getChannel();
			copy.transferFrom(original, 0, original.size());
		}
	}

	public static File copyTestDatabaseFile(String testDbName, File targetDirectory)
			throws FileNotFoundException, IOException {
		File testDatabaseFile = getTestFile(testDbName);
		File testDatabaseFileCopy = new File(targetDirectory, testDatabaseFile.getName());
		copyFile(testDatabaseFile, testDatabaseFileCopy);
		return testDatabaseFileCopy;
	}

	public static void deleteTestDatabaseFiles() {
		deleteTestDatabaseFiles(TEST_DB1_XML_FILE);
	}

	public static void deleteWorkingDirectory() {
		File workDir = getTempTestDirectory();
		deleteRecursively(workDir);
	}

	public static void deleteTestDatabaseFiles(final String dbName) {
		File testDatabaseFile = getTestFile(dbName);
		File testDatabaseFileCopy = new File(System.getProperty("java.io.tmpdir"), testDatabaseFile.getName());
		testDatabaseFileCopy.delete();
		File auxFiles = new File(testDatabaseFileCopy.getParent(), ".libris_auxfiles_"+dbName);
		if (auxFiles.exists()) {
			for (File f: auxFiles.listFiles()) {
				f.delete();
			}
			auxFiles.delete();
		}
	}

	private static File getTempTestDirectory() {
		File tempTestDirectory = new File(System.getProperty("java.io.tmpdir"), "libristest");
		return tempTestDirectory;
	}

	public static File makeTempTestDirectory() {
		File tempTestDirectory = getTempTestDirectory();
		deleteRecursively(tempTestDirectory);
		assertTrue("Cannot create temporary test directory " + tempTestDirectory.getAbsolutePath(), tempTestDirectory.mkdir());
		return tempTestDirectory;
	}

	public static File makeTempTestDirectory(String directoryName) {
		File parent = makeTempTestDirectory();
		File testDir = new File(parent, directoryName);
		assertTrue("Cannot create temporary test directory " + testDir.getAbsolutePath(), testDir.mkdir());
		return testDir;
	}

	@Deprecated
	public static FileSpaceManager makeFileSpaceManager(String managerName) {
		File workDir = makeTempTestDirectory();
		if (null == workDir) {
			fail("could not create working directory ");
		}
		return makeFileSpaceManager(workDir, managerName);
	}

	public static FileSpaceManager makeFileSpaceManager(File workDir, String managerName) {
		File testFile = new File(workDir, managerName);
		if (!testFile.exists()) {
			try {
				testFile.createNewFile();
			} catch (IOException e) {
				fail("could not create "+managerName);
			}
		}
		FileSpaceManager mgr = null;
		try {
			mgr = new FileSpaceManager(testFile, false);
			mgr.reset();
		} catch (FileNotFoundException e) {
			fail("could not find "+managerName);
		} catch (LibrisException e) {
			e.printStackTrace();
			fail("error opening filespace manager "+e.getMessage());
		}
		return mgr;
	}

	public static void destroyFileSpaceManager(FileSpaceManager mgr) {
		try {
			mgr.close();
		} catch (DatabaseException e) {
			e.printStackTrace();
			fail("error closing filespace manager");
		}
		mgr.getFilePath().delete();
	}

	public static final String ALPHANUMS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
	public static final char[] alphanumChars = ALPHANUMS.toCharArray();
	public static KeyIntegerTuple makeRandomKeyIntegerTuple(Random r, String suffix) throws InputException {
		StringBuffer sb = new StringBuffer(128);
		int keySize = 0;
		while (keySize < 1) {
			keySize = Math.abs(r.nextInt()) % 64;
		}
		for (int i = 0; i < keySize; ++i) {
			int selector = Math.abs(r.nextInt()) % alphanumChars.length;
			sb.append(alphanumChars[selector]);
		}
		sb.append('_');
		sb.append(suffix);
		KeyIntegerTuple tup = new KeyIntegerTuple(sb.toString(), r.nextInt());
		return tup;
	}

	public static String makeRandomWord(Random r, int minLen, int maxLen) {
		int wordSize = minLen + r.nextInt(maxLen - minLen);
		StringBuilder b = new StringBuilder(wordSize);
		while (wordSize > 0) {
			b.append(alphanumChars[r.nextInt(alphanumChars.length)]);
			--wordSize;
		}
		return b.toString();
	}

	public static String getRecordIdString(Record recData) {
		return RecordId.toString(recData.getRecordId());
	}

	public static final Logger testLogger;
	static {
		testLogger = Logger.getLogger("org.lasalledebain.test");
		testLogger.setLevel(Boolean.getBoolean("org.lasalledebain.test.verbose")? Level.ALL: Level.INFO);
		System.setProperty("java.util.logging.SimpleFormatter.format",
	              "[%1$tF %1$tT] [%4$-7s] %5$s %n");
		}

	public static void trace(String msg) {
		testLogger.fine(msg);
	}

	public static void info(String msg) {
		testLogger.info(msg);
	}

	public static void warn(String msg) {
		testLogger.warning(msg);
	}

	public static LibrisDatabase buildTestDatabase(File workingDirectory, String databaseFileName)
			throws FileNotFoundException, IOException {
		File testDatabaseFileCopy = copyTestDatabaseFile(databaseFileName, workingDirectory);			
		LibrisDatabase db = null;
		try {
			db = Utilities.buildAndOpenDatabase(testDatabaseFileCopy);
			DatabaseUi ui = db.getUi();
			ui.closeDatabase(false);
			db = ui.openDatabase();
		} catch (LibrisException e) {
			e.printStackTrace();
			fail("Error rebuilding database: "+e.getMessage());
		}
		return db;
	}

	public static LibrisDatabase buildTestDatabase(LibrisDatabaseConfiguration config)
			throws FileNotFoundException, IOException {
		LibrisDatabase db = null;
		try {
			db = Utilities.buildAndOpenDatabase(config);
			DatabaseUi ui = db.getUi();
			ui.closeDatabase(false);
			db = ui.openDatabase();
		} catch (LibrisException e) {
			e.printStackTrace();
			fail("Error rebuilding database: "+e.getMessage());
		}
		return db;
	}

	public static int[] toIntList(Collection<Integer> list) {
		int[] result = new int[list.size()];
		int index = 0;
		for (Integer i: list) {
			result[index++] = i;
		}
		return result;
	}

	public static boolean compareIntLists(String dType, int[] expectedData, int[] actualData) {
		int sed[] = Arrays.copyOf(expectedData, expectedData.length);
		int sad[] = Arrays.copyOf(actualData, actualData.length);
		Arrays.sort(sed);
		Arrays.sort(sad);
		if (!Arrays.equals(sed, sad)) {
			System.err.print("\nExpected:\n"+dType+": ");
			for (int i: sed) System.err.print(i+" ");
			System.err.print("\nActual:\n"+dType+": ");
			for (int i: sad) System.err.print(i+" ");
			System.err.print("\n");
			return false;
		}
		return true;
	}

	public static boolean checkForDuplicates(String dType, int[] actualData) {
		int sad[] = Arrays.copyOf(actualData, actualData.length);
		Arrays.sort(sad);
		for (int i = 0; i < sad.length - 1; ++i) {
			if (sad[i] == sad[i+1]) {
				return false;
			}
		}
		return true;
	}

	public static final String LIBRIS_TEST_LOGGER = "org.lasalledebain.LibrisTest";
	public static Level defaultLoggingLevel = Level.WARNING;

	public static void checkRecords(GenericDatabase<DatabaseRecord> database, int lastId) {
		for (int i = 1; i <= lastId; ++i) {
			try {
				Record r = database.getRecord(i);
				assertNotNull("Cannot find record "+i, r);
				assertEquals("Wrong record ID",  i, r.getRecordId());
			} catch (InputException e) {
				e.printStackTrace();
				fail("unexpected exception "+e.getMessage());
			}
		}
	}

	/**
	 * @param dirName 
	 * 
	 */
	@Deprecated
	public static File makeTestFileObject(String dirName) {
		File workingDirectory = new File(makeTempTestDirectory(), dirName);
		deleteRecursively(workingDirectory);
		workingDirectory.mkdirs();
		File tf = new File(workingDirectory, "testIndexFile");
		tf.deleteOnExit();
		return tf;
	}

	public static File makeTestFileObject(File parentDirectory, String fileName) {
		File tf = new File(parentDirectory, fileName);
		tf.deleteOnExit();
		return tf;
	}

	public static LibrisJournalFileManager<DatabaseRecord> createLibrisJournalFileManager(
			GenericDatabase<DatabaseRecord> database, FileAccessManager journalFileMr) throws LibrisException {
		RecordTemplate recFactory = RecordTemplate.templateFactory(database.getSchema(), null);
		return new LibrisJournalFileManager<DatabaseRecord>(database, journalFileMr, recFactory);
	}

	public static TestGUI openGuiAndDatabase(String testName, File dbFile) throws DatabaseException {
		@SuppressWarnings("unused")
		TestGUI gui = null;
		try {
			gui = new TestGUI(dbFile);
		} catch (LibrisException e) {
			e.printStackTrace();
			fail("Error creating GUI: "+e.getMessage());
		}
		gui.openDatabase();
		gui.setUiTitle(testName);
		return gui;
	}

	public static TestGUI rebuildAndOpenDatabase(String workingName, File theWorkingDirectory,
			String databaseFileName) throws FileNotFoundException, IOException,
	DatabaseException {
		LibrisDatabase db = buildTestDatabase(theWorkingDirectory, databaseFileName);
		File dbFile = db.getDatabaseFile();
		assertTrue("Could not close database", db.closeDatabase(false));

		TestGUI gui = openGuiAndDatabase(workingName, dbFile);
		return gui;
	}

	public static void pause() {
		int duration = pauseDuration;
		pause(duration);
	}

	public static void pause(int duration) {
		try {
			if (duration < 0) {
				return;
			} else if (0 == duration) {
				System.out.println("Paused...");
				System.in.read();
			} else {
				Thread.sleep(duration);
			}
		} catch (Exception e) {}
	}

	public static int pauseDuration = Integer.getInteger("libris.test.pause", -1);

	public static void pause(String msg) {
		trace("Pause: "+msg);
		pause();
	}

	public static LibrisDatabase extractBuildAndOpenDatabase(File archiveFile) throws LibrisException, IOException {
		ArrayList<File> fileList = DatabaseArchive.getFilesFromArchive(archiveFile, archiveFile.getParentFile());
		assertTrue("Archive file is empty", fileList.size() > 0);
		File databaseFile = fileList.get(0);
		HeadlessUi ui = new HeadlessUi(databaseFile, false);
		ui.buildDatabase(databaseFile);
		LibrisDatabase result = ui.openDatabase();
		assertNotNull("Database not opened", result);
		return result;
	}

	public static LibrisDatabase buildAndOpenDatabase(File databaseFile) throws LibrisException {
		HeadlessUi ui = new HeadlessUi(databaseFile, false);
		assertTrue("Database not built", ui.buildDatabase(databaseFile));
	
		LibrisDatabase result = ui.openDatabase();
		assertNotNull("Database not opened", result);
		return result;
	}

	public static LibrisDatabase buildAndOpenDatabase(LibrisDatabaseConfiguration config) throws LibrisException {
		HeadlessUi ui = new HeadlessUi();
		ui.buildDatabase(config);
		LibrisDatabase result = ui.openDatabase(config);
		return result;
	}

	public static boolean newDatabase(File databaseFile, String schemaName, boolean readOnly, DatabaseUi ui, LibrisMetadata<DatabaseRecord> metadata) 
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
	
		return ui.buildDatabase(databaseFile);
	}

}

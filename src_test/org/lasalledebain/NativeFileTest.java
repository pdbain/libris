package org.lasalledebain;

import java.io.File;
import java.util.ArrayList;

import org.lasalledebain.libris.Field.FieldType;
import org.lasalledebain.libris.LibrisConstants;
import org.lasalledebain.libris.LibrisDatabase;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.XmlSchema;
import org.lasalledebain.libris.util.DiagnosticDatabase;
import org.lasalledebain.libris.util.Utilities;
import org.lasalledebain.libris.xmlUtils.LibrisXMLConstants;

import junit.framework.TestCase;

/**
 * Test native records + journal file + record positions
 *
 */
public class NativeFileTest extends TestCase implements LibrisConstants, LibrisXMLConstants {

	private File workingDirectory;
	private File nativeRecordsFile;
	private LibrisDatabase testDatabase;
	private ArrayList<Record> testRecords;
	private String fieldNames[] = {"booltrue", "intfield1", "textfield"};
	private FieldType fts[] = {FieldType.T_FIELD_BOOLEAN, FieldType.T_FIELD_INTEGER, FieldType.T_FIELD_STRING};
	
	
	@Override
	protected void setUp() throws Exception {
		workingDirectory = Utilities.makeTempTestDirectory();
		testDatabase = new DiagnosticDatabase(Utilities.copyTestDatabaseFile(Utilities.TEST_DB1_XML_FILE, workingDirectory));
		File schemaFile = new File(Utilities.getTestDataDirectory(), Utilities.TEST_SCHEMA2_XML_FILE);
		XmlSchema schem = XmlSchema.loadSchema(schemaFile);
		testDatabase.setSchema(schem);

		nativeRecordsFile = new File(workingDirectory, "NativeFileTest"+'.'+FILENAME_NATIVE_RECORDS_SUFFIX);
		System.out.println("NativeFileTest file: "+nativeRecordsFile.getPath());
	}
	public void testAddRecord() {
		fail("not implemented");
	}
	public void testIterator() {
		fail("not implemented");
		}
	public void testReplaceRecordInPlace() {
		fail("not implemented");
		}
	
	public void testReplaceRecordAtEnd() {
		fail("not implemented");
		}
	
	public void testRemoveRecord() {
		fail("not implemented");
		}
	public void testReopenFile() {
		fail("not implemented");
		}
	public void testRandomAccess() {
		fail("not implemented");
		}
	public int getFieldNum() {
		return 0;
	}
	public String getId() {
		return null;
	}
	public String getTitle() {
		return null;
	}
}

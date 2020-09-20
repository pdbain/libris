package org.lasalledebain.libris.ui;

import static org.lasalledebain.Utilities.TEST_DB4_XML_FILE;
import static org.lasalledebain.Utilities.TEST_DB_WITH_DEFAULTS_XML_FILE;
import static org.lasalledebain.Utilities.testLogger;
import static org.lasalledebain.Utilities.info;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.logging.Level;

import org.lasalledebain.Utilities;
import org.lasalledebain.libris.DatabaseRecord;
import org.lasalledebain.libris.Field;
import org.lasalledebain.libris.GenericDatabase;
import org.lasalledebain.libris.LibrisDatabase;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.field.FieldValue;

import junit.framework.TestCase;

@SuppressWarnings("rawtypes")
public class RecordEditTests extends TestCase {

	private static final String TEST_PUBLISHER = "test_publisher";
	private static final String TEST_TITLE = "test_title";
	private static final String TEST_AUTHOR = "test_author";
	private static final String TEST_AUTHOR2 = "test_author2";
	private static final String ID_AUTH = "ID_auth";
	private static final String ID_PUB = "ID_publisher";
	private static final String ID_TITL = "ID_title";
	private static final String ID_PAGES = "ID_pages";
	private static final String ID_HARDCOPY = "ID_hardcopy";
	PrintStream out = System.out;
	private File workingDirectory;
	public void testRecordSanity() {
		try {
			TestGUI gui = rebuildAndOpenDatabase(getName());
			BrowserWindow resultsWindow = gui.getResultsWindow();
			resultsWindow.setSelectedRecordIndex(0);
			int rid = resultsWindow.getSelectedRecordId();
			gui.displaySelectedRecord();
			Utilities.pause("opened record");
			info("selected "+rid);
			gui.setRecordWindowEditable(true);
			Utilities.pause("re-open record");
			gui.displaySelectedRecord();
			RecordDisplayPanel dispPanel = gui.getDisplayPanel();
			RecordWindow recWindow = dispPanel.getCurrentRecordWindow();
			MultipleValueUiField pagesUiField = (MultipleValueUiField) recWindow.getField(ID_PAGES);
			assertNotNull("Could not find "+ID_PAGES, pagesUiField);
			int numValues = pagesUiField.getNumValues();
			assertEquals("Wrong number of values for "+ID_PAGES, 1, numValues);
			FieldValue pagesValue = pagesUiField.getCtrl(0).getFieldValue();
			String originalMainValue = pagesValue.getMainValueAsString();
			String originalExtraValue = pagesValue.getExtraValueAsString();

			Utilities.pause("check pages values");
			assertEquals("main value wrong", "1", originalMainValue);
			assertEquals("Extra value wrong", "2", originalExtraValue);
			Utilities.pause("Close database");
			assertTrue("Could not quit", gui.quit(false));
		} catch (Throwable e) {
			e.printStackTrace();
			fail("unexpected exception");
		}
	}
	
	public void testNewRecordWindow() {
		try {
			final String testName = getName();
			TestGUI gui = rebuildAndOpenDatabase(testName);
			LibrisDatabase db = gui.getDatabase();
			File dbFile = db.getDatabaseFile();
			BrowserWindow resultsWindow = gui.getResultsWindow();
			int recId = 0;
			Record rec;

			RecordWindow rw = gui.newRecordWindow();
			rec = rw.getRecord();
			rec.setEditable(true);
			Field authFld = rec.getField(ID_AUTH);
			authFld.addValue(TEST_AUTHOR);
			Field titleFld = rec.getField(ID_TITL);
			titleFld.addValue(TEST_TITLE);
			Field pubFld = rec.getField(ID_PUB);
			pubFld.addIntegerValue(1);
			pubFld.addValuePair(-1, TEST_PUBLISHER);
			rw.refresh();
			UiField uif = rw.getField(ID_AUTH);
			checkFieldValues(uif, new String[] {TEST_AUTHOR});
			authFld.addValue(TEST_AUTHOR2);
			rw.refresh();
			uif = rw.getField(ID_AUTH);
			checkFieldValues(uif, new String[] {TEST_AUTHOR, TEST_AUTHOR2});
			checkFieldValues(rw.getField(ID_TITL), new String[] {TEST_TITLE});
		} catch (Throwable e) {
			e.printStackTrace();
			fail("unexpected exception");
		}
	}

	private void checkFieldValues(UiField uif, String[] expectedValueArray) {
		Iterator<String> expectedValues = Arrays.asList(expectedValueArray).iterator();
		for (FieldValue f: uif) {
			assertTrue("Too few values", expectedValues.hasNext());
			String expectedValue = expectedValues.next();
			String actualValue = f.getValueAsString();
			assertEquals(expectedValue, actualValue);
		}
		assertFalse("Too many values", expectedValues.hasNext());
	}
	
	public void testRecordReEdit() {
		try {
			final String testName = getName();
			TestGUI gui = rebuildAndOpenDatabase(testName);
			LibrisDatabase db = gui.getDatabase();
			File dbFile = db.getDatabaseFile();
			BrowserWindow resultsWindow = gui.getResultsWindow();
			int recId = 0;
			DatabaseRecord rec;
			{
				rec = gui.newRecord();
				rec.setEditable(true);
				Field fld = rec.getField(ID_AUTH);
				fld.addValue(TEST_AUTHOR);
				fld = rec.getField(ID_TITL);
				fld.addValue(TEST_TITLE);
				fld = rec.getField(ID_PUB);
				fld.addIntegerValue(1);
				fld.addValuePair(-1, TEST_PUBLISHER);
				db.putRecord(rec);
				recId = rec.getRecordId();
				db.save();
			}

			{
				Field fld = rec.getField(ID_PUB);
				fld.addValuePair(-1, "foobar");
				db.putRecord(rec);
				db.save();
				assertTrue("Could not close database", db.closeDatabase(false));
			}

			{
				TestGUI newGui = Utilities.openGuiAndDatabase(testName, dbFile);
				GenericDatabase<DatabaseRecord> newDb = newGui.getDatabase();
				rec = newDb.getRecord(recId);
				Field fld = rec.getField(ID_PUB);
				Iterator<String> expectedValues = Arrays.asList(new String [] {"Enumv2", TEST_PUBLISHER, "foobar"}).iterator();
				for (FieldValue fv: fld.getFieldValues()) {
					assertEquals(expectedValues.next(), fv.getValueAsString());
				}
				assertFalse("Missing value", expectedValues.hasNext());
			}

		} catch (Throwable e) {
			e.printStackTrace();
			fail("unexpected exception");
		}
	}
	
	public void testDefaultValue() {
		try {
			TestGUI gui = Utilities.rebuildAndOpenDatabase(getName(), workingDirectory, TEST_DB_WITH_DEFAULTS_XML_FILE);
			GenericDatabase<DatabaseRecord> db = gui.getDatabase();
			int pubFieldNum = db.getSchema().getFieldNum("ID_publisher");
			Record rec = gui.newRecord();
			rec.setEditable(false);
			Field fld = rec.getField("ID_auth");
			assertNull("author field not empty", fld);
			fld = rec.getField(pubFieldNum);
			assertNull("publisher not empty", fld);
			String valString = rec.getFieldValue(pubFieldNum).getValueAsString();
			assertEquals("wrong default for publisher","IBM", valString);
			FieldValue fldVal = rec.getFieldValue("ID_hardcopy");
			assertTrue("ID_hardcopy default wrong", fldVal.isTrue());
			Utilities.pause("Close database");
			assertTrue("Could not close database", gui.quit(false));
		} catch (Throwable e) {
			e.printStackTrace();
			fail("unexpected exception");
		}
	}

	public void testFieldCounts() {
		try {
			TestGUI gui = rebuildAndOpenDatabase(getName());
			BrowserWindow resultsWindow = gui.getResultsWindow();
			resultsWindow.setSelectedRecordIndex(3);
			gui.displaySelectedRecord();
			Utilities.pause("opened record");
			RecordDisplayPanel dispPanel = gui.getDisplayPanel();
			DatabaseRecordWindow recWindow = dispPanel.getCurrentRecordWindow();
			String ids[] = {ID_AUTH, ID_HARDCOPY, ID_PAGES, ID_PUB, ID_PAGES};
			int counts[] = {2, 1, 1, 1, 1};
			boolean editable = true;
			for (int j = 0; j < 3; ++j) {
				Utilities.pause("test with editable="+editable);
				for (int i = 0; i < ids.length; ++i) {
					String id = ids[i];
					UiField pagesUiField = recWindow.getField(id);
					assertNotNull("Could not find "+id, pagesUiField);
					int numValues = pagesUiField.getNumValues();
					assertEquals("Wrong number of values for "+id, counts[i], numValues);
				}
				recWindow.setEditable(editable);
				editable = !editable;
			}
			Utilities.pause("Close database");
			assertTrue("Could not close database", gui.quit(false));
		} catch (Throwable e) {
			e.printStackTrace();
			fail("unexpected exception: "+e.getMessage());
		}
	}


	public void testMissingField() {
		try {
			TestGUI gui = rebuildAndOpenDatabase(getName());
			BrowserWindow resultsWindow = gui.getResultsWindow();
			resultsWindow.setSelectedRecordIndex(1);
			int rid = resultsWindow.getSelectedRecordId();
			gui.displaySelectedRecord();
			Utilities.pause("opened record");
			info("selected "+rid);
			assertEquals("Opened wrong record", 2, rid);
			gui.setRecordWindowEditable(true);
			Utilities.pause("check publisher field");
			RecordDisplayPanel dispPanel = gui.getDisplayPanel();
			RecordWindow recWindow = dispPanel.getCurrentRecordWindow();
			UiField pubUiField = recWindow.getField(ID_PUB);
			assertNotNull("Null: "+ID_PUB, pubUiField);
			int numValues = pubUiField.getNumValues();
			assertEquals("Wrong number of values for editable "+ID_PUB, 0, numValues);
			String val = pubUiField.getRecordField().getValuesAsString();
			gui.setRecordWindowEditable(false);
			numValues = pubUiField.getNumValues();
			assertEquals("Wrong number of values for read-only"+ID_PUB, 0, numValues);
			val = pubUiField.getRecordField().getValuesAsString();
			Utilities.pause("Close database");
			assertTrue("Could not close database", gui.quit(false));
		} catch (Throwable e) {
			e.printStackTrace();
			fail("unexpected exception");
		}
	}

	
	public void testEditRecord() {
		try {
			TestGUI gui = rebuildAndOpenDatabase(getName());
			BrowserWindow resultsWindow = gui.getResultsWindow();
			resultsWindow.setSelectedRecordIndex(3);
			int rid = resultsWindow.getSelectedRecordId();
			info("selected "+rid);
			gui.duplicateRecord();
			gui.setRecordWindowEditable(true);
			RecordDisplayPanel dispPanel = gui.getDisplayPanel();
			RecordWindow recWindow = dispPanel.getCurrentRecordWindow();
			MultipleValueUiField authUiField = (MultipleValueUiField) recWindow.getField(ID_AUTH);
			assertNotNull("Could not find "+ID_AUTH, authUiField);
			authUiField.doSelect();
			Utilities.pause();
			int numAuthValues = authUiField.getNumValues();
			gui.newFieldValue();
			GuiControl newCtrl = authUiField.getCtrl(numAuthValues);
			assertNotNull("GUI control for new value is null", newCtrl);
			newCtrl.setFieldValue("new value");
			Utilities.pause();
			MultipleValueUiField pubField = (MultipleValueUiField) recWindow.getField(ID_PUB);
			assertNotNull("Could not find "+ID_PUB, pubField);
			GuiControl pubCtrl = pubField.getCtrl(0);
			String PUB_NEW_VALUE = "PubNewValue";
			pubCtrl.setFieldValue(PUB_NEW_VALUE);
			Utilities.pause();
			Record currentRec = recWindow.getRecord();
			gui.enterRecord();
			Utilities.pause();
			Field testField = currentRec.getField(ID_AUTH);
			String actualValues = testField.getValuesAsString();
			String AUTH_NEW_VALUE = "Rec4Fld1Val1, Rec4Fld1Val2, new value";
			assertEquals(ID_AUTH+" field mismatch", AUTH_NEW_VALUE, actualValues);
			testField = currentRec.getField(ID_PUB);
			actualValues = testField.getValuesAsString();
			assertEquals(ID_PUB+" field mismatch", PUB_NEW_VALUE, actualValues);
			resultsWindow.setSelectedRecordIndex(4);
			Utilities.pause();
			rid = resultsWindow.getSelectedRecordId();
			assertEquals("Wrong record opened", currentRec.getRecordId(), rid);
			Utilities.pause("Close database");
			assertTrue("Could not close database", gui.quit(true));
		} catch (Throwable e) {
			e.printStackTrace();
			fail("unexpected exception");
		}
	}
	
	public void testEditRecordWithExtraValues() {
		try {
			final String testName = getName();
			TestGUI gui = rebuildAndOpenDatabase(testName);
			LibrisDatabase db = gui.getDatabase();
			File dbFile = db.getDatabaseFile();
			BrowserWindow resultsWindow = gui.getResultsWindow();
			int lastRecord = resultsWindow.getNumRecords() - 1;
			resultsWindow.setSelectedRecordIndex(lastRecord);
			int rid = resultsWindow.getSelectedRecordId();
			info("selected "+rid);
			resultsWindow.displaySelectedRecord();
			gui.setRecordWindowEditable(true);
			RecordDisplayPanel dispPanel = gui.getDisplayPanel();
			RecordWindow recWindow = dispPanel.getCurrentRecordWindow();
			MultipleValueUiField pubField = (MultipleValueUiField) recWindow.getField(ID_PUB);
			assertNotNull("Could not find "+ID_PUB, pubField);
			GuiControl pubCtrl = pubField.getCtrl(0);
			GuiControl newControl = pubField.addControl(true);
			String PUB_NEW_VALUE = "PubNewValue";
			newControl.setFieldValue(PUB_NEW_VALUE);
			Utilities.pause();
			Record currentRec = recWindow.getRecord();
			gui.enterRecord();
			db.save();
			Utilities.pause();

			resultsWindow.setSelectedRecordIndex(lastRecord);
			resultsWindow.displaySelectedRecord();
			gui.setRecordWindowEditable(true);
			Utilities.pause();
			dispPanel = gui.getDisplayPanel();
			recWindow = dispPanel.getCurrentRecordWindow();			
			Utilities.pause("Close database");
			assertTrue("Could not close database", gui.quit(false));

			TestGUI newGui = Utilities.openGuiAndDatabase(testName, dbFile);
			GenericDatabase<DatabaseRecord> newDb = newGui.getDatabase();
			resultsWindow = newGui.getResultsWindow();
			lastRecord = resultsWindow.getNumRecords() - 1;
			resultsWindow.setSelectedRecordIndex(lastRecord);
			rid = resultsWindow.getSelectedRecordId();
			info("selected "+rid);
			resultsWindow.displaySelectedRecord();
		} catch (Throwable e) {
			e.printStackTrace();
			fail("unexpected exception");
		}
	}
	
	public void testDeleteValue() throws IOException, LibrisException {
		TestGUI gui = rebuildAndOpenDatabase(getName());
		BrowserWindow resultsWindow = gui.getResultsWindow();
		resultsWindow.setSelectedRecordIndex(3);
		gui.displaySelectedRecord();
		gui.setRecordWindowEditable(true);
		RecordDisplayPanel dispPanel = gui.getDisplayPanel();
		RecordWindow recWindow = dispPanel.getCurrentRecordWindow();
		int originalId = resultsWindow.getSelectedRecordId();
		Record originalRecord = recWindow.getRecord();
		info("selected "+originalId);
		gui.duplicateRecord();
		gui.enterRecord();
		gui.setRecordWindowEditable(true);
		recWindow = dispPanel.getCurrentRecordWindow();
		Record newRec = recWindow.getRecord();
		assertEquals("Duplicate record != original record", originalRecord, newRec);
		UiField authUiField = recWindow.getField(ID_AUTH);
		MultipleValueUiField pagesUiField = (MultipleValueUiField) recWindow.getField(ID_PAGES);
		FieldValue pagesValue = pagesUiField.getCtrl(0).getFieldValue();
		String originalMainValue = pagesValue.getMainValueAsString();
		String originalExtraValue = pagesValue.getExtraValueAsString();
		assertNotNull("Could not find "+ID_AUTH, authUiField);
		Record currentRec = recWindow.getRecord();
		authUiField.doSelect();
		Utilities.pause("Remove field");
		gui.removeFieldValue();
		gui.enterRecord();

		gui.displayRecord(currentRec.getRecordId());
		pagesValue = pagesUiField.getCtrl(0).getFieldValue();
		String newMainValue = pagesValue.getMainValueAsString();
		String newExtraValue = pagesValue.getExtraValueAsString();
		assertEquals("pages field main values differ", originalMainValue, newMainValue);
		assertEquals("pages field Extra values differ", originalExtraValue, newExtraValue);
		String newAuthFieldValues = currentRec.getField(ID_AUTH).getValuesAsString();
		assertEquals("auth field value deletion failed", "Rec4Fld1Val2", newAuthFieldValues);
		Utilities.pause("Close database");
		assertTrue("Could not close database", gui.quit(true));
	}

	public void testAddUnsetEnumValue() {
		try {
			TestGUI gui = rebuildAndOpenDatabase(getName());
			BrowserWindow resultsWindow = gui.getResultsWindow();
			resultsWindow.setSelectedRecordIndex(0);
			int rid = resultsWindow.getSelectedRecordId();
			gui.displaySelectedRecord();
			testLogger.log(Level.INFO, "selected "+rid);
			gui.setRecordWindowEditable(true);
			RecordDisplayPanel dispPanel = gui.getDisplayPanel();
			RecordWindow recWindow = dispPanel.getCurrentRecordWindow();
			Record currentRec = recWindow.getRecord();
			MultipleValueUiField pubUiField = (MultipleValueUiField) recWindow.getField(ID_PUB);
			assertNotNull("Could not find "+ID_PUB, pubUiField);
			pubUiField.doSelect();
			String oldValues = pubUiField.getRecordField().getValuesAsString();
			Utilities.pause();
			int numPubValues = pubUiField.getNumValues();
			GuiControl ctrl = pubUiField.getCtrl(numPubValues - 1);
			gui.newFieldValue();
			GuiControl newCtrl = pubUiField.getCtrl(numPubValues);
			assertNotNull("GUI control for new value is null", newCtrl);
			Utilities.pause();
			gui.enterRecord();
			Utilities.pause();
			Field testField = currentRec.getField(ID_PUB);
			String actualValues = testField.getValuesAsString();
			assertEquals("Empty enum", oldValues, actualValues);
			assertEquals("Empty enum", numPubValues, testField.getNumberOfValues());
			Utilities.pause("Close database");
			assertTrue("Could not close database", gui.quit(true));
		} catch (Throwable e) {
			e.printStackTrace();
			fail("unexpected exception "+e.getClass()+e.getMessage());
		}
	
		
	}


	public void testDuplicateTitle() {
		try {
			TestGUI gui = rebuildAndOpenDatabase(getName());
			BrowserWindow resultsWindow = gui.getResultsWindow();
			resultsWindow.setSelectedRecordIndex(0);
			gui.displaySelectedRecord();
			Utilities.pause();
			RecordDisplayPanel dispPanel = gui.getDisplayPanel();
			RecordWindow recWindow = dispPanel.getCurrentRecordWindow();
			Utilities.pause();
			MultipleValueUiField titleField = (MultipleValueUiField) recWindow.getField(ID_TITL);
			assertNotNull("Could not find "+ID_TITL, titleField);
			assertEquals("wrong initial number of field values", 1, titleField.getNumValues());
			String oldFieldValues = titleField.getRecordField().getValuesAsString();
			titleField.doSelect();
			gui.newFieldValue();
			assertEquals("wrong number of field values after new value", titleField.getNumValues(), 2);

			GuiControl titleNewValue = titleField.getCtrl(1);
			assertNotNull("control for new object is null", titleNewValue);
			String TITLE_NEW_VALUE = "TitleNewValue";
			titleNewValue.setFieldValue(TITLE_NEW_VALUE);
			Utilities.pause();
			Record currentRec = recWindow.getRecord();
			gui.enterRecord();
			Utilities.pause();
			Field testField = currentRec.getField(ID_TITL);
			String actualValues = testField.getValuesAsString();
			assertEquals(ID_PUB+" field mismatch", oldFieldValues+", "+TITLE_NEW_VALUE, actualValues);
			Utilities.pause("Close database");
			assertTrue("Could not close database", gui.quit(true));
		} catch (Throwable e) {
			e.printStackTrace();
			fail("unexpected exception");
		}
	}
	public void testDuplicateEnum() {
		try {
			TestGUI gui = rebuildAndOpenDatabase(getName());
			BrowserWindow resultsWindow = gui.getResultsWindow();
			resultsWindow.setSelectedRecordIndex(3);
			gui.displaySelectedRecord();
			Utilities.pause();
			RecordDisplayPanel dispPanel = gui.getDisplayPanel();
			RecordWindow recWindow = dispPanel.getCurrentRecordWindow();
			Utilities.pause();
			MultipleValueUiField pubField = (MultipleValueUiField) recWindow.getField(ID_PUB);
			assertNotNull("Could not find "+ID_PUB, pubField);
			assertEquals("wrong initial number of field values", pubField.getNumValues(), 1);
			String oldFieldValues = pubField.getRecordField().getValuesAsString();
			pubField.doSelect();
			gui.newFieldValue();
			assertEquals("wrong number of field values after new value", pubField.getNumValues(), 2);

			GuiControl pubCtrl = pubField.getCtrl(1);
			assertNotNull("control for new object is null", pubCtrl);
			String PUB_NEW_VALUE = "PubNewValue";
			pubCtrl.setFieldValue(PUB_NEW_VALUE);
			Utilities.pause();
			Record currentRec = recWindow.getRecord();
			gui.enterRecord();
			Utilities.pause();
			Field testField = currentRec.getField(ID_PUB);
			String actualValues = testField.getValuesAsString();
			assertEquals(ID_PUB+" field mismatch", oldFieldValues+", "+PUB_NEW_VALUE, actualValues);
			Utilities.pause("Close database");
			assertTrue("Could not close database", gui.quit(true));
		} catch (Throwable e) {
			e.printStackTrace();
			fail("unexpected exception");
		}
	}

	private TestGUI rebuildAndOpenDatabase(String testName) throws IOException,
			DatabaseException {
		String databaseFileName = TEST_DB4_XML_FILE;
		return Utilities.rebuildAndOpenDatabase(testName, workingDirectory, databaseFileName);
	}

	@Override
	protected void setUp() throws Exception {
		testLogger.log(Level.INFO, "Starting "+getName());
		workingDirectory = Utilities.makeTempTestDirectory();
	}

	@Override
	protected void tearDown() throws Exception {
		testLogger.log(Level.INFO, "Ending "+getName());
		Utilities.deleteWorkingDirectory();
	}
}

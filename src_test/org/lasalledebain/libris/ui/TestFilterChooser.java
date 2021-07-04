package org.lasalledebain.libris.ui;

import static org.lasalledebain.libris.util.Utilities.KEYWORD_DATABASE1_XML;
import static org.lasalledebain.libris.util.Utilities.KEYWORD_DATABASE4_XML;
import static org.lasalledebain.libris.util.Utilities.testLogger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.JComboBox;
import javax.swing.WindowConstants;

import org.junit.Test;
import org.lasalledebain.libris.DatabaseRecord;
import org.lasalledebain.libris.GenericDatabase;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.records.RecordStreamFilter;
import org.lasalledebain.libris.search.RecordFilter;
import org.lasalledebain.libris.search.RecordFilter.MATCH_TYPE;
import org.lasalledebain.libris.search.RecordFilter.SEARCH_TYPE;
import org.lasalledebain.libris.ui.FilterChooser.EnumerationFilterControlPanel;
import org.lasalledebain.libris.ui.FilterChooser.KeywordFilterControlPanel;
import org.lasalledebain.libris.util.Utilities;

import junit.framework.TestCase;

public class TestFilterChooser extends TestCase {
	private File workingDirectory;
	private GenericDatabase<DatabaseRecord> theDb;

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

	@Test
	public void testKeywordControls() throws DatabaseException, IOException {
		FilterChooser<DatabaseRecord> theChooser = createAndShowGUI();
		FilterChooser.KeywordFilterControlPanel firstStage = (KeywordFilterControlPanel) theChooser.filterList.get(0);
		assertFalse("Wrong default for caseSensitive", firstStage.isCaseSensitive());
		Utilities.pause();
		firstStage.caseSensitiveCheckBox.doClick();
		Utilities.pause();
		assertTrue("Wrong caseSensitive for first click ", firstStage.isCaseSensitive());
		firstStage.caseSensitiveCheckBox.doClick();
		Utilities.pause();
		assertFalse("Wrong caseSensitive for second click", firstStage.isCaseSensitive());
		firstStage.setCaseSensitive(true);
		Utilities.pause();
		assertTrue("Wrong caseSensitive for setCaseSensitive(true)", firstStage.isCaseSensitive());
		firstStage.caseSensitiveCheckBox.doClick();
		Utilities.pause();
		assertFalse("Wrong caseSensitive for third click", firstStage.isCaseSensitive());
		firstStage.caseSensitiveCheckBox.doClick();
		Utilities.pause();
		firstStage.setCaseSensitive(false);
		Utilities.pause();
		assertFalse("Wrong caseSensitive for third click", firstStage.isCaseSensitive());

		assertEquals("Wrong default match type", MATCH_TYPE.MATCH_PREFIX, firstStage.getMatchType());
		firstStage.wholeWordButton.doClick();
		Utilities.pause();
		assertEquals("Wrong match type for whole word", MATCH_TYPE.MATCH_EXACT, firstStage.getMatchType());
		firstStage.containsButton.doClick();
		Utilities.pause();
		assertEquals("Wrong match type for whole word", MATCH_TYPE.MATCH_CONTAINS, firstStage.getMatchType());
		firstStage.prefixButton.doClick();
		Utilities.pause();
		assertEquals("Wrong match type for whole word", MATCH_TYPE.MATCH_PREFIX, firstStage.getMatchType());
	}

	@Test
	public void testSelectFields() throws DatabaseException, IOException {
		FilterChooser<DatabaseRecord> theChooser = createAndShowGUI();
		FilterChooser.KeywordFilterControlPanel theStage = (KeywordFilterControlPanel) theChooser.filterList.get(0);
		int[] searchableFields = new int[] {1, 2, 4};
		int[] searchedFields = theStage.getSearchFields();
		assertTrue("wrong default", java.util.Arrays.compare(searchedFields, searchableFields) == 0);
		theStage.setSearchFields(searchableFields);
		searchedFields = theStage.getSearchFields();
		assertTrue("wrong set to full set", java.util.Arrays.compare(searchedFields, searchableFields) == 0);
		searchableFields = new int[] {2};
		theStage.setSearchFields(searchableFields);
		searchedFields = theStage.getSearchFields();
		assertTrue("wrong set to single", java.util.Arrays.compare(searchedFields, searchableFields) == 0);
	}

	public void testKeywordFilter() throws DatabaseException, IOException {
		FilterChooser<DatabaseRecord> theChooser = createAndShowGUI(KEYWORD_DATABASE4_XML);
		@SuppressWarnings("unchecked")
		FilterChooser<DatabaseRecord>.KeywordFilterControlPanel theStage = 
		(FilterChooser<DatabaseRecord>.KeywordFilterControlPanel) theChooser.filterList.get(0);
		JComboBox<SEARCH_TYPE> c = theStage.getSearchTypeChooser();
		c.setSelectedIndex(SEARCH_TYPE.T_SEARCH_KEYWORD.ordinal());
		theStage.setKeywords(new String[] {"k1"});
		Integer[] actualIds = doFiltering(theStage);
		Integer[] expectedIds = new Integer[] {1, 4};
		assertTrue("Wrong records returned", Arrays.compare(expectedIds, actualIds) == 0);
	}

	protected Integer[] doFiltering(FilterChooser<DatabaseRecord>.FilterControlPanel theStage) {
		RecordFilter<DatabaseRecord> theFilter = theStage.getFilter();
		RecordStreamFilter<DatabaseRecord> filt = new RecordStreamFilter<DatabaseRecord>(theFilter);
		Stream<DatabaseRecord> filteredStream = filt.processStream(theDb.getRecords().asStream());
		List<DatabaseRecord> result = filteredStream.collect(Collectors.toList());
		ArrayList<Integer> foo = result.stream().map(r -> r.getRecordId()).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
		Integer[] actualIds = foo.toArray(new Integer[foo.size()]);
		return actualIds;
	}

	public void testEnumFilter() throws DatabaseException, IOException {
		FilterChooser<DatabaseRecord> theChooser = createAndShowGUI(Utilities.DATABASE_WITH_GROUPS_AND_RECORDS_XML);
		FilterChooser<DatabaseRecord>.FilterControlPanel theStage = theChooser.filterList.get(0);
		JComboBox<SEARCH_TYPE> c = theStage.getSearchTypeChooser();
		c.setSelectedIndex(SEARCH_TYPE.T_SEARCH_ENUM.ordinal());
		@SuppressWarnings("unchecked")
		FilterChooser<DatabaseRecord>.EnumerationFilterControlPanel ec 
		= (FilterChooser<DatabaseRecord>.EnumerationFilterControlPanel) theChooser.filterList.get(0);
		ec.fldChooser.setSelectedIndex(1);
		ec.setValueChoice(1);
		assertTrue("Wrong default value for inherit default", ec.includeDefault.isSelected());
		checkFilter(ec, new Integer[] {3, 4}, "Wrong records returned");

		ec.includeDefault.setSelected(false);
		checkFilter(ec, new Integer[] {3}, "Wrong records returned without defaults");
	}

	public void testBooleanFilter() throws DatabaseException, IOException {
		FilterChooser<DatabaseRecord> theChooser = createAndShowGUI(Utilities.DATABASE_WITH_GROUPS_AND_RECORDS_XML);
		FilterChooser<DatabaseRecord>.FilterControlPanel theStage = theChooser.filterList.get(0);
		JComboBox<SEARCH_TYPE> c = theStage.getSearchTypeChooser();
		c.setSelectedIndex(SEARCH_TYPE.T_SEARCH_BOOLEAN.ordinal());
		@SuppressWarnings("unchecked")
		FilterChooser<DatabaseRecord>.BooleanFilterControlPanel bc 
		= (FilterChooser<DatabaseRecord>.BooleanFilterControlPanel) theChooser.filterList.get(0);
		bc.fldChooser.setSelectedIndex(1);
		assertTrue("Wrong default value for inherit default", bc.includeDefault.isSelected());
		checkFilter(bc, new Integer[] {1, 2, 3, 4, 5}, "Wrong records returned");
		bc.fieldFalse.doClick();
		checkFilter(bc, new Integer[] {6}, "Wrong records returned false value with defaults");
		bc.fieldTrue.doClick();
		bc.includeDefault.setSelected(false);
		checkFilter(bc, new Integer[] {3}, "Wrong records returned without defaults");		

		bc.fieldFalse.doClick();
		checkFilter(bc, new Integer[] {6}, "Wrong records returned false value without defaults");
	}

	protected void checkFilter(FilterChooser<DatabaseRecord>.FilterControlPanel theFilter, Integer[] expectedIds,
			String msg) {
		Integer[] actualIds = doFiltering(theFilter);
		assertTrue(msg, Arrays.compare(expectedIds, actualIds) == 0);
	}

	public void testNameFilter() throws DatabaseException, IOException {
		FilterChooser<DatabaseRecord> theChooser = createAndShowGUI(Utilities.DATABASE_WITH_GROUPS_AND_RECORDS_XML);
		FilterChooser<DatabaseRecord>.FilterControlPanel theStage = theChooser.filterList.get(0);
		JComboBox<SEARCH_TYPE> c = theStage.getSearchTypeChooser();
		c.setSelectedIndex(SEARCH_TYPE.T_SEARCH_RECORD_NAME.ordinal());
		FilterChooser<DatabaseRecord>.RecordNameFilterControlPanel cp 
		= (FilterChooser<DatabaseRecord>.RecordNameFilterControlPanel) theChooser.filterList.get(0);
		cp.nameField.setText("ieee");
		checkFilter(cp, new Integer[] {1,2,3,4}, "Default settings");
		
		cp.caseSensitiveButton.doClick();
		cp.nameField.setText("acm");
		checkFilter(cp, new Integer [0], "Wrong case");
		
		cp.nameField.setText("ACM");
		checkFilter(cp, new Integer [] {5, 6}, "Correct case");
		
		cp.nameField.setText("IEEE_micro");
		cp.exactButton.doClick();
		checkFilter(cp, new Integer[] {2}, "Exact match");
		
		cp.nameField.setText("Computer");
		cp.containsButton.doClick();
		cp.caseSensitiveButton.doClick();
		checkFilter(cp, new Integer[] {3,4}, "Exact match");
		
	}

	private FilterChooser<DatabaseRecord> createAndShowGUI() throws DatabaseException, IOException {
		return createAndShowGUI(KEYWORD_DATABASE1_XML);
	}

	private FilterChooser<DatabaseRecord> createAndShowGUI(String databaseFileName) throws DatabaseException, IOException {
		theDb = rebuildAndOpenDatabase(getName(), databaseFileName).getDatabase();
		FilterChooser<DatabaseRecord> theChooser = new FilterChooser<DatabaseRecord>(theDb);
		theChooser.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		return theChooser;
	}

	private TestGUI rebuildAndOpenDatabase(String testName, String databaseFileName) throws IOException,
	DatabaseException {
		;
		if (null == testName) testName = "workdir";
		return Utilities.rebuildAndOpenDatabase(testName, workingDirectory, databaseFileName);
	}

	public static void main(String args[]) throws Exception {
		TestFilterChooser testObject = new TestFilterChooser();

		//Schedule a job for the event-dispatching thread:
		//creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					String dbName = System.getProperty("test.database.name", "KeywordDatabase4.xml");
					testObject.setUp();
					testObject.createAndShowGUI(dbName);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}
}

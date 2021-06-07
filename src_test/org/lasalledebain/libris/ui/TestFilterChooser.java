package org.lasalledebain.libris.ui;

import static org.lasalledebain.libris.util.Utilities.KEYWORD_DATABASE1_XML;
import static org.lasalledebain.libris.util.Utilities.testLogger;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import javax.swing.WindowConstants;

import org.junit.Test;
import org.lasalledebain.libris.DatabaseRecord;
import org.lasalledebain.libris.GenericDatabase;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.util.Utilities;

import junit.framework.TestCase;

public class TestFilterChooser extends TestCase {
	private File workingDirectory;

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
	public void testSanity() throws DatabaseException, IOException {
		FilterChooser<DatabaseRecord> theChooser = createAndShowGUI();
	}

	private FilterChooser<DatabaseRecord> createAndShowGUI() throws DatabaseException, IOException {
		GenericDatabase<DatabaseRecord> theDb = rebuildAndOpenDatabase(getName()).getDatabase();
		FilterChooser<DatabaseRecord> theChooser = new FilterChooser<DatabaseRecord>(theDb);
		theChooser.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		return theChooser;
	}

	private TestGUI rebuildAndOpenDatabase(String testName) throws IOException,
	DatabaseException {
		String databaseFileName = KEYWORD_DATABASE1_XML;
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
					testObject.setUp();
					testObject.createAndShowGUI();
					testObject.tearDown();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}
}

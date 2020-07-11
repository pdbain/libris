package org.lasalledebain.libris.ui;

import static org.lasalledebain.Utilities.rebuildAndOpenDatabase;
import static org.lasalledebain.Utilities.testLogger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;

import org.junit.Test;
import org.lasalledebain.Utilities;
import org.lasalledebain.libris.DatabaseRecord;
import org.lasalledebain.libris.LibrisDatabase;
import org.lasalledebain.libris.exception.LibrisException;

import junit.framework.TestCase;

public class LayoutTests extends TestCase {

	private File workingDirectory;

	@Test
	public void testLayouts() throws FileNotFoundException, IOException, LibrisException {
		TestGUI myGui = rebuildAndOpenDatabase(getName(), workingDirectory, Utilities.EXAMPLE_DATABASE1_FILE);
		LibrisDatabase myDb = myGui.getDatabase();
		BrowserWindow resultsWindow = myGui.getResultsWindow();
		resultsWindow.setSelectedRecordIndex(3);
		RecordDisplayPanel dispPanel = myGui.getDisplayPanel();
		for (String layoutId: new String[] {"LO_shortFormDisplay", "LO_formDisplay",
				"LO_paraDisplay", "LO_tableDisplay", "LO_listDisplay", "LO_browserDisplay"}) {
			LibrisSwingLayout<DatabaseRecord> theLayout = myDb.getLayouts().getLayout(layoutId);
			dispPanel.setRecLayout(theLayout);
			myGui.displaySelectedRecord();
			Utilities.pause();
			myGui.closeWindow(false);
		}
		myGui.quit(true);
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

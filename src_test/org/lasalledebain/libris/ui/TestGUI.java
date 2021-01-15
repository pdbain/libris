package org.lasalledebain.libris.ui;

import java.io.File;

import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.util.Utilities;

public class TestGUI extends LibrisGui implements AutoCloseable {

	public TestGUI(File dbFile) throws LibrisException {
		super(dbFile, false);
	}
	public BrowserWindow getResultsWindow() {
		return resultsPanel;
	}
	public RecordDisplayPanel getDisplayPanel() {
		return displayPanel;
	}
	@Override
	public void close() throws Exception {
		if (isDatabaseOpen()) {
			Utilities.testLogger.warning("Force closing database");
			closeDatabase(true);
		}
	}


}

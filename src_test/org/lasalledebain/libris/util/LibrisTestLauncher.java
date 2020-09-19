package org.lasalledebain.libris.util;

import org.lasalledebain.libris.DatabaseRecord;
import org.lasalledebain.libris.Libris;
import org.lasalledebain.libris.ui.LibrisUi;

public class LibrisTestLauncher extends Libris {
	public static LibrisUi<DatabaseRecord> testMain(String[] args) {
		LibrisUi<DatabaseRecord> result = mainImpl(args);
		return result;
	}

}

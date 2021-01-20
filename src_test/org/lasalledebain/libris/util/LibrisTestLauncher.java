package org.lasalledebain.libris.util;

import org.lasalledebain.libris.DatabaseRecord;
import org.lasalledebain.libris.Libris;
import org.lasalledebain.libris.ui.AbstractUi;

public class LibrisTestLauncher extends Libris {
	public static AbstractUi<DatabaseRecord> testMain(String[] args) {
		AbstractUi<DatabaseRecord> result = mainImpl(args);
		return result;
	}

}

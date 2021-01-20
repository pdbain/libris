package org.lasalledebain.libris.util;

import org.lasalledebain.libris.Libris;
import org.lasalledebain.libris.ui.AbstractUi;

public class LibrisTestLauncher extends Libris {
	public static AbstractUi testMain(String[] args) {
		AbstractUi result = mainImpl(args);
		return result;
	}

}

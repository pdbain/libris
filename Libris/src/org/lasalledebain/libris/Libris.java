package org.lasalledebain.libris;

import org.lasalledebain.libris.ui.CmdlineUi;
import org.lasalledebain.libris.ui.LibrisGui;
import org.lasalledebain.libris.ui.LibrisUi;

public class Libris extends CmdlineUi {

	public Libris(LibrisDatabase db) {
		super(db);
	}

	public Libris(String[] args) {
		super(args);
		if (isParameterError()) {
			System.exit(1);
		} else {
			if (parameters.isGui()) {
				LibrisUi ui = LibrisGui.launchGui(parameters);
				if (ui.isDatabaseSelected()) {
					ui.openDatabase();
				}
			} else if (parameters.isDoIndexing()) {
				LibrisDatabase.rebuild(parameters);
			} else {
				// TODO launch cmdline UI
			}
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new Libris(args);

	}

}

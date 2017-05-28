package org.lasalledebain.libris.ui;

import java.io.File;

import javax.swing.JFrame;

import org.lasalledebain.libris.exception.LibrisException;

public abstract class LibrisWindowedUi extends LibrisUiGeneric {
	protected JFrame mainFrame;

	protected void initializeUi() {
		mainFrame = new JFrame();
	}
	public JFrame getMainFrame() {
		return mainFrame;
	}
	public LibrisWindowedUi() {
		super();
		initializeUi();
	}
	
	public LibrisWindowedUi(File dbFile) throws LibrisException {
		this(dbFile, null, false);
	}
	public LibrisWindowedUi(File databaseFile, File auxDirectory,
			boolean readOnly) throws LibrisException {
		super(databaseFile, auxDirectory, readOnly);
		initializeUi();
	}


}

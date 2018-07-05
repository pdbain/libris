package org.lasalledebain.libris;

import java.io.File;

import org.lasalledebain.libris.ui.LibrisUi;

public class LibrisDatabaseParameter {
	public File databaseFile;
	public File auxDir;
	public LibrisUi ui;
	public boolean readOnly;
	public Schema databaseSchema;
	public String schemaName;

	public LibrisDatabaseParameter(LibrisUi theUi) {
		this(theUi, null);
	}

	public LibrisDatabaseParameter(LibrisUi theUi, File dbFile) {
		this.databaseFile = dbFile;
		this.auxDir = null;
		this.ui = theUi;
		this.readOnly = false;
		databaseSchema = null;
		schemaName = "";
	}
}
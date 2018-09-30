package org.lasalledebain.libris;

import java.io.File;
import java.util.Objects;

import org.lasalledebain.libris.ui.LibrisUi;

public class LibrisDatabaseParameter {
	public File getDatabaseFile() {
		return Objects.nonNull(ui)? ui.getDatabaseFile(): null;
	}

	private File auxDir;
	/**
	 * @param ui the ui to set
	 */
	public void setUi(LibrisUi ui) {
		this.ui = ui;
	}

	/**
	 * @param schemaName the schemaName to set
	 */
	public void setSchemaName(String schemaName) {
		this.schemaName = schemaName;
	}

	/**
	 * @param databaseSchema the databaseSchema to set
	 */
	public void setDatabaseSchema(Schema databaseSchema) {
		this.databaseSchema = databaseSchema;
	}

	/**
	 * @param auxDir the auxDir to set
	 */
	public void setAuxDir(File auxDir) {
		this.auxDir = auxDir;
	}

	private LibrisUi ui;
	private boolean readOnly;
	private Schema databaseSchema;
	private String schemaName;

	public LibrisDatabaseParameter(LibrisUi theUi) {
		this.auxDir = null;
		this.ui = theUi;
		this.readOnly = false;
		databaseSchema = null;
		schemaName = "";
	}

	/**
	 * @return the auxDir
	 */
	public File getAuxDir() {
		return auxDir;
	}

	/**
	 * @return the ui
	 */
	public LibrisUi getUi() {
		return ui;
	}

	/**
	 * @return readOnly setting
	 */
	public boolean isReadOnly() {
		return readOnly;
	}

	/**
	 * @return the databaseSchema
	 */
	public Schema getDatabaseSchema() {
		return databaseSchema;
	}

	/**
	 * @return the schemaName
	 */
	public String getSchemaName() {
		return schemaName;
	}
}
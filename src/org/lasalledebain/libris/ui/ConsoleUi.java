package org.lasalledebain.libris.ui;

import static java.util.Objects.nonNull;

import org.lasalledebain.libris.LibrisDatabase;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.exception.DatabaseError;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.LibrisException;

public class ConsoleUi<RecordType extends Record> extends CmdlineUi<RecordType> {

	protected final AbstractUi parentUi;

	public ConsoleUi(AbstractUi theParentUi) {
		super(nonNull(theParentUi)? theParentUi.isDatabaseReadOnly(): true);
		parentUi = theParentUi;
	}

	@Override
	public void displayRecord(int recordId) throws LibrisException {
		alert("displayRecord: Operation not available");
	}

	@Override
	public String SelectSchemaFile(String schemaName) throws DatabaseException {
		alert("SelectSchemaFile: Operation not available");
		return null;
	}

	@Override
	public void repaint() {
		alert("Operation not available");
	}

	@Override
	public void setRecordArtifact() {
		alert("Operation not available");
	}

	@Override
	public boolean isDatabaseReadOnly() {
		return nonNull(parentUi)? parentUi.isDatabaseReadOnly(): true;
	}

	@Override
	public boolean checkAndCloseDatabase(boolean force) throws DatabaseException {
		return nonNull(parentUi)? parentUi.checkAndCloseDatabase(force): true;	
	}

	@Override
	public LibrisDatabase getDatabase() {
		return nonNull(parentUi)? parentUi.getLibrisDatabase(): null;
	}

	public LibrisDatabase getLibrisDatabase() {
		return nonNull(parentUi)? parentUi.getLibrisDatabase(): null;
		}
	
	@Override
	public String getUiTitle() {
		return nonNull(parentUi)? parentUi.getUiTitle(): "";	
	}

	@Override
	public void setUiTitle(String uiTitle) {
		if (nonNull(parentUi)) {
			parentUi.setUiTitle(uiTitle);	
		}
	}

	@Override
	public void setOpenReadOnly(boolean openReadOnly) {
		if (nonNull(parentUi)) {
			parentUi.setOpenReadOnly(openReadOnly);	
		}
	}

	@Override
	public LibrisDatabase openDatabase() throws DatabaseException {
		return nonNull(parentUi)? parentUi.openDatabase(): null;	
	}

	@Override
	public boolean closeDatabase(boolean force) throws DatabaseException {
		return nonNull(parentUi)? parentUi.closeDatabase(force): true;
	}

	@Override
	public boolean quit(boolean force) throws DatabaseException {
		return nonNull(parentUi)? parentUi.quit(force): true;	
	}

	@Override
	public boolean isDatabaseSelected() {
		return nonNull(parentUi)? parentUi.isDatabaseSelected(): true;	
	}

	@Override
	public boolean isDatabaseOpen() {
		return nonNull(parentUi)? parentUi.isDatabaseOpen(): true;	
	}

	@Override
	public boolean isDatabaseModified() {
		return nonNull(parentUi)? parentUi.isDatabaseModified(): true;	
	}

	@Override
	public UiField getSelectedField() {
		return nonNull(parentUi)? parentUi.getSelectedField(): null;
	}

	public RecordType newRecord() {
		try {
			currentDatabase.newRecord();
		} catch (InputException e) {
			throw new DatabaseError("Error creating new record", e);
		}
		return null;
	}

	@Override
	public void arrangeValues() {
		alert("Operation not available");
	}

	@Override
	public void pasteToField() {
		alert("Operation not available");
	}

	@Override
	public void recordsAccessible(boolean accessible) {
		alert("Operation not available");
	}

	@Override
	public void newFieldValue() {
		alert("Operation not available");
	}

	@Override
	public void removeFieldValue() {
	}

	@Override
	public void fieldSelected(boolean b) {
		return;
	}

	@Override
	public void setSelectedField(UiField selectedField) {
		return;
	}

	@Override
	public void saveDatabase() {
		if (nonNull(parentUi)) {
			parentUi.saveDatabase();	
		}
	}

	@Override
	public void sendChooseDatabase() {
		alert("Operation not available");
	}

	@Override
	public boolean stop() {
		return nonNull(parentUi)? parentUi.stop(): true;	
	}
}

package org.lasalledebain.libris.ui;

import java.io.File;

import org.lasalledebain.libris.LibrisDatabase;
import org.lasalledebain.libris.NamedRecordList;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.XmlSchema;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.indexes.LibrisDatabaseConfiguration;

import static java.util.Objects.nonNull;

public class ConsoleUi<RecordType extends Record> extends CmdlineUi<RecordType> {

	protected final LibrisUi<RecordType> parentUi;

	public ConsoleUi(LibrisUi<RecordType> theParentUi) {
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
	public void put(Record newRecord) throws DatabaseException {
		alert("Operation not available");
	}

	@Override
	public void repaint() {
		alert("Operation not available");
	}

	@Override
	public void rebuildDatabase(File databaseFile) {
		alert("rebuildDatabase: Operation not available");
	}

	@Override
	public void setRecordName(NamedRecordList<RecordType> namedRecs) throws InputException {
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
	protected boolean checkAndCloseDatabase(boolean force) throws DatabaseException {
		return nonNull(parentUi)? parentUi.checkAndCloseDatabase(force): true;	
	}

	@Override
	public LibrisDatabase getDatabase() {
		return nonNull(parentUi)? parentUi.getDatabase(): null;
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
	public XmlSchema getSchema() {
		alert("Operation not available");
		return null;
	}

	@Override
	public void setSchema(XmlSchema mySchema) {
		alert("Operation not available");
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
	public LibrisDatabase openDatabase(LibrisDatabaseConfiguration config) throws DatabaseException {
		alert("Operation not available");
		return currentDatabase;
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
	protected boolean isDatabaseModified() {
		return nonNull(parentUi)? parentUi.isDatabaseModified(): true;	
	}

	@Override
	public void setDatabaseFile(File dbFile) {
		alert("Operation not available");
	}

	@Override
	public UiField getSelectedField() {
		return nonNull(parentUi)? parentUi.getSelectedField(): null;
	}

	@Override
	public RecordType newRecord() {
		alert("Operation not available");
		return null;
	}

	@Override
	public void arrangeValues() {
		alert("Operation not available");
	}

	@Override
	public void addRecord(RecordType newRecord) throws DatabaseException {
		alert("Operation not available");
	}

	@Override
	public boolean rebuildDatabase(LibrisDatabaseConfiguration config) throws LibrisException {
		alert("Operation not available");
		return false;
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

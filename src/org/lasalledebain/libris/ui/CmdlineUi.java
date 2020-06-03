package org.lasalledebain.libris.ui;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;

import javax.swing.JOptionPane;

import org.lasalledebain.libris.DatabaseRecord;
import org.lasalledebain.libris.NamedRecordList;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.LibrisException;

public class CmdlineUi extends LibrisUiGeneric implements LibrisUi {

	BufferedReader cmdlineInput;
	@Override
	public void displayRecord(int recordId) throws LibrisException {
		alert("displayRecord not implemented");
	}

	@Override
	public void alert(String msg, Exception e) {
		String errorString = msg+": "+e.getMessage();
		alert(errorString);
		e.printStackTrace();
	}

	@Override
	public void alert(String msg) {
		System.err.println(msg);
	}

	@Override
	public int confirm(String msg) {
		int result = confirmImpl(msg, false);
		return result;
	}

	@Override
	public int confirmWithCancel(String msg) {
		int result = confirmImpl(msg, true);
		return result;
	}

	private int confirmImpl(String msg, boolean allowCancel) {
		System.out.println(msg + (allowCancel? " (yes/no/cancel)": " (yes/no)"));
		int result = JOptionPane.NO_OPTION;
		try {
			String response = cmdlineInput.readLine();
			do {
				if (null == response) { // TODO fix CmdlineUi.confirmImpl
					result = JOptionPane.CLOSED_OPTION;
					break;
				} else if (response.length() > 0) {
					if ("yes".startsWith(response)) {
						result = JOptionPane.YES_OPTION;
						break;
					} else if ("no".startsWith(response)) {
						result = JOptionPane.NO_OPTION;
						break;
					} else if (allowCancel && "cancel".startsWith(response)) {
						result = JOptionPane.CANCEL_OPTION;
						break;
					}
				}
			} while (true);
		} catch (IOException e) {
			result = JOptionPane.CLOSED_OPTION;
		}
		return result;
	}

	@Override
	public String SelectSchemaFile(String schemaName) throws DatabaseException {
		return promptAndReadReply("Please enter the path to the file for schema "+schemaName);
	}

	public String promptAndReadReply(String prompt) throws DatabaseException {
		System.out.print(prompt);
		System.out.print(": ");
		try {
			String response = cmdlineInput.readLine();
			return response;
		} catch (IOException e) {
			throw new DatabaseException("could not read from command line");
		}
	}

	@Override
	public void put(Record newRecord) throws DatabaseException {
		throw new InternalError(getClass().getName()+".put() not implemented");	
	}

	public void repaint() {
	}

	public void rebuildDatabase(File databaseFile) {
	}

	@Override
	public void setRecordName(NamedRecordList<DatabaseRecord> namedRecs) throws InputException {
		throw new InternalError(getClass().getName()+".setRecordName() not implemented");
	}

	@Override
	public void setRecordArtifact() {
		throw new InternalError(getClass().getName()+".setRecordArtifact() not implemented");
	}

	public boolean isDatabaseReadOnly() {
		return false;
	}

	@Override
	public boolean closeDatabase(boolean force) throws DatabaseException {
		if (!currentDatabase.closeDatabase(force)) {
			alert("Database not closed"); // TODO 4 improve cmdLineUi close database checking
			return false;
		} else {
			return true;
		}
	}

	@Override
	protected boolean checkAndCloseDatabase(boolean force) throws DatabaseException {
		// TODO write checkAndCloseDatabase
		return currentDatabase.closeDatabase(force);
	}

	public File getDatabaseFile() {
		return null;
	}

}

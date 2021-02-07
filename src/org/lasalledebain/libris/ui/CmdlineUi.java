package org.lasalledebain.libris.ui;

import static java.util.Objects.isNull;
import static org.lasalledebain.libris.LibrisDatabase.log;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;

import javax.swing.JOptionPane;

import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.LibrisException;

public class CmdlineUi<RecordType extends Record> extends AbstractUi {

	protected final BufferedReader cmdlineInput;
	
	public CmdlineUi(boolean readOnly) {
		super(readOnly);
		cmdlineInput = new BufferedReader(new InputStreamReader(System.in));
	}

	@Override
	public RecordType displayRecord(int recordId) throws LibrisException {
		alert("displayRecord not implemented");
		return null;
	}

	@Override
	public void alert(String msg, Throwable e) {
		log(Level.SEVERE, msg, e);
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

	protected void repl() {
		boolean doQuit = false;
		do {
			String prompt = getUiTitle();
			if (isNull(prompt)) {
				prompt = "";
			}
			prompt += ": ";
			try {
				String command = promptAndReadReply(prompt);
				doQuit = processCommand(command);
			} catch (DatabaseException e) {
				alert("Error: ", e);
			}
		} while (!doQuit);
		stop();
		
	}
	/**
	 * Process a user-entered command
	 * @param command user command
	 * @return true if the process should exit
	 * @throws DatabaseException
	 */
	private boolean processCommand(String command) throws DatabaseException {
		String[] parts = command.split("\\s+");
		if (parts.length == 0) return false;
		switch (parts[0]) {
		case "quit": {
			boolean force = (parts.length > 1) && parts[1].equals("-f");
			if (quit(force)) return true;
		}
			break;
			default: alert("command "+command+" not recognized");
		}
		return false;
	}

	public void repaint() {
	}

	public void rebuildDatabase(File databaseFile) {
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
	public boolean checkAndCloseDatabase(boolean force) throws DatabaseException {
		// TODO write checkAndCloseDatabase
		return currentDatabase.closeDatabase(force);
	}

	public File getDatabaseFile() {
		return null;
	}

	@Override
	public boolean start() {
		Thread consoleThread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				repl();
				
			}
		});
		consoleThread.start();
		return true;
	}

	@Override
	public boolean stop() {
		// TODO Auto-generated method stub
		return super.stop();
	}

}

package org.lasalledebain.libris.ui;

import static javax.swing.JOptionPane.showMessageDialog;

import java.awt.Component;
import java.awt.Dimension;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Objects;
import java.util.logging.Level;

import javax.swing.JFrame;

import org.lasalledebain.libris.LibrisDatabase;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.index.GroupDef;

public abstract class LibrisWindowedUi extends LibrisUiGeneric {	
	protected JFrame mainFrame;
	private GroupDef selectedGroupDef;

	public static void alert(Component parentComponent, String msg, Exception e) {
		showMessageDialog(parentComponent, formatAlertString(msg, e));
	}
	
	public static String formatAlertString(String msg, Exception e) {
		StringBuilder buff = new StringBuilder(msg);
		LibrisDatabase.log(Level.WARNING, e.getMessage(), e);
		String emessage = "";
		buff.append(e.getClass().getSimpleName());
		
		String excMsg = e.getMessage();
		if (Objects.nonNull(excMsg)) {
			buff.append(": ");
			buff.append(excMsg);
			buff.append("\n");
		}
		if (null != e) {
			emessage = LibrisUiGeneric.formatConciseStackTrace(e, buff);
		}
		String errorString = buff.toString();
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		e.printStackTrace(new PrintStream(bos));
		try {
			LibrisDatabase.librisLogger.log(Level.WARNING, bos.toString(Charset.defaultCharset().name()));
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		Throwable c = e;
		errorString += e.getClass().getSimpleName() + "\n";
		while (null != ( c = c.getCause())) {
			errorString += '\n'+c.getMessage();
		}
		LibrisDatabase.librisLogger.log(Level.FINE, emessage, e);
		return errorString;
	}

	public LibrisWindowedUi() {
		super();
		initializeUi();
		selectedGroupDef = null;
	}
	public LibrisWindowedUi(File dbFile) throws LibrisException {
		this(dbFile, null, false);
	}
	public LibrisWindowedUi(File databaseFile, File auxDirectory,
			boolean readOnly) throws LibrisException {
		super(databaseFile, auxDirectory, readOnly);
		initializeUi();
	}
	protected void initializeUi() {
		mainFrame = new JFrame();
	}
	
	public JFrame getMainFrame() {
		return mainFrame;
	}
	public void setSelectedGroup(GroupDef grpDef) {
		selectedGroupDef = grpDef;
		enableNewChild();
	}
	public GroupDef getSelectedGroup() {
		return selectedGroupDef;
	}

	void enableNewChild() {}
	public abstract Record newChildRecord(Record currentRecord, int groupNum);
	public abstract Dimension getDisplayPanelSize();
}

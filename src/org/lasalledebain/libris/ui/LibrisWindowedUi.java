package org.lasalledebain.libris.ui;

import static javax.swing.JOptionPane.showMessageDialog;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Objects;
import java.util.logging.Level;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.lasalledebain.libris.DatabaseAttributes;
import org.lasalledebain.libris.LibrisDatabase;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.exception.DatabaseError;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.index.GroupDef;

public abstract class LibrisWindowedUi extends LibrisUiGeneric {	
	protected JFrame mainFrame;
	private GroupDef selectedGroupDef;
	protected boolean databaseSelected = false;

	public abstract void closeWindow(boolean allWindows);

	@Override
	protected boolean checkAndCloseDatabase(boolean force) throws DatabaseException {
		boolean result = false;
		if (!force && currentDatabase.isModified()) {
			int choice = confirmWithCancel(Messages.getString("LibrisDatabase.save_database_before_close")); //$NON-NLS-1$
			switch (choice) {
			case JOptionPane.YES_OPTION:
				currentDatabase.save();
				result =  currentDatabase.closeDatabase(true);
			case JOptionPane.NO_OPTION:
				result =  currentDatabase.closeDatabase(true);
			case JOptionPane.CANCEL_OPTION:
			default:
				/* do nothing */
			}
		} else {
			return currentDatabase.closeDatabase(force);
		}
		return result;
	}

	public static void alert(Component parentComponent, String msg, Exception e) {
		showMessageDialog(parentComponent, formatAlertString(msg, e));
	}
	
	public static void alert(Component parentComponent, String msg) {
		showMessageDialog(parentComponent, msg);
	}
	
	public static String formatAlertString(String msg, Exception e) {
		StringBuilder buff = new StringBuilder(msg);
		LibrisDatabase.log(Level.WARNING, e.getMessage(), e);
		String emessage = "";
		buff.append(e.getClass().getSimpleName());
		
		if (null != e) {
			emessage = LibrisUiGeneric.formatConciseStackTrace(e, buff);
		}
		
		String errorString = buff.toString();
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		e.printStackTrace(new PrintStream(bos));
		try {
			LibrisDatabase.log(Level.WARNING, bos.toString(Charset.defaultCharset().name()));
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		LibrisDatabase.log(Level.FINE, emessage, e);
		return errorString;
	}

	public static void makeLabelledControl(JPanel parentPanel, Component theControl, String labelText, boolean vertical) {
		JPanel controlPanel = new JPanel();
		if (vertical) {
			BoxLayout layout = new BoxLayout(controlPanel, BoxLayout.Y_AXIS);
			controlPanel.setLayout(layout);
		}
		JLabel l = new JLabel(labelText);
		l.setLabelFor(theControl);
		controlPanel.add(l);
		controlPanel.add(theControl);
		parentPanel.add(controlPanel);
	}

	public LibrisWindowedUi() {
		super();
		initializeUi();
		selectedGroupDef = null;
	}

	public LibrisWindowedUi(File databaseFile, boolean readOnly) {
		super(databaseFile, readOnly);
		initializeUi();
	}

	protected void initializeUi() {
		mainFrame = new JFrame();
	}
	
	@Override
	public boolean quit(boolean force) throws DatabaseException {
		destroyWindow(false);
		return super.quit(force);
	}

	protected abstract void destroyWindow(boolean b);

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
	
	public void setTitle(String title) {
		super.setTitle(title);
		if (isDatabaseModified()) {
			mainFrame.setTitle(title+"*");
		} else {
			mainFrame.setTitle(title);
		}
	}

	@Override
	public void alert(String msg, Exception e) {
		String errorString = formatAlertString(msg, e);
		alert(errorString);
	}

	@Override
	public void alert(String msg) {
		alert(msg);
	}

	@Override
	public int confirm(String message) {
		return Dialogue.yesNoDialog(mainFrame, message);
	}

	public int confirmWithCancel(String msg) {
		return Dialogue.yesNoCancelDialog(mainFrame, msg);
	}

	public void updateUITitle(boolean isModified) {
		String databaseName = "no database open";
		if (null != currentDatabase) {
			DatabaseAttributes databaseAttributes = currentDatabase.getDatabaseAttributes();
			if (null != databaseAttributes) {
				databaseName = databaseAttributes.getDatabaseName();
				if (isModified) {
					databaseName = databaseName+"*";
				}
			}
		}
		setTitle(databaseName);
	}

	class WindowCloseListener extends WindowAdapter {
		@Override
		public void windowClosing(WindowEvent e) {
			if (null != currentDatabase) {
				try {
					quit(false);
				} catch (DatabaseException e1) {
					throw new DatabaseError(e1);
				}
			}
		}
	}
}


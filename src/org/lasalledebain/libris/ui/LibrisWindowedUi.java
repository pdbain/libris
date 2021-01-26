package org.lasalledebain.libris.ui;

import static java.util.Objects.nonNull;
import static javax.swing.JOptionPane.showMessageDialog;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.logging.Level;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.ProgressMonitor;

import org.lasalledebain.libris.DatabaseAttributes;
import org.lasalledebain.libris.Libris;
import org.lasalledebain.libris.LibrisDatabase;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.exception.DatabaseError;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.index.GroupDef;

public abstract class LibrisWindowedUi extends AbstractUi {	
	private static final String NO_DATABASE_OPEN = "no database open";
	private static final String DATABASE_MODIFIED = " (modified)";
	protected final JFrame mainFrame;
	private GroupDef selectedGroupDef;
	protected boolean databaseSelected = false;
	protected String title = NO_DATABASE_OPEN;
	protected LibrisUiWorker progressWorker;
	private ProgressMonitor progMon;

	public abstract boolean closeWindow(boolean allWindows);

	@Override
	public boolean checkAndCloseDatabase(boolean force) throws DatabaseException {
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
	
	public static String formatAlertString(String msg, Throwable e) {
		StringBuilder buff = new StringBuilder(msg);
		LibrisDatabase.log(Level.WARNING, e.getMessage(), e);
		String emessage = "";
		buff.append(": ");
		buff.append(e.getClass().getSimpleName());
		buff.append("\n");
		
		if (null != e) {
			emessage = Libris.formatConciseStackTrace(e, buff);
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

	public LibrisWindowedUi(boolean readOnly) {
		super(readOnly);
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
	
	public void setUiTitle(String title) {
		super.setUiTitle(title);
		if (isDatabaseModified()) {
			mainFrame.setTitle(title+DATABASE_MODIFIED);
		} else {
			mainFrame.setTitle(title);
		}
	}

	@Override
	public void alert(String msg, Throwable e) {
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

	public void updateUITitle() {
		String databaseName = NO_DATABASE_OPEN;
		if (null != currentDatabase) {
			DatabaseAttributes databaseAttributes = currentDatabase.getDatabaseAttributes();
			if (null != databaseAttributes) {
				databaseName = databaseAttributes.getDatabaseName();
			}
		}
		setUiTitle(databaseName);
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

	@Override
	public void setCurrentProgress(int currentPercentProgress) {
		if (nonNull(progressWorker)) {
			progressWorker.setWorkerProgress(currentPercentProgress);
		}
	}

	@Override
	public void addProgress(int progress) {
		final int expectedWork = getExpectedWork();
		if (expectedWork > 0) {
			setCurrentProgress((100 * addAccomplishedWork(progress))/expectedWork);
		}
	}

	@Override
	public void setProgressNote(String theNote) {
		if (nonNull(progMon)) {
			progMon.setNote(theNote);
		}
	}
	
	protected void runProgressMonitoredTask(LibrisUiWorker theWorker, String message) {
		progressWorker = theWorker;
		progMon = new ProgressMonitor(this.mainFrame, message, null, 0, 100);
		progressWorker.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if ("progress" == evt.getPropertyName() ) {
					int progress = (Integer) evt.getNewValue();
					progMon.setProgress(progress);
					if (progMon.isCanceled() || progressWorker.isDone()) {
						progressWorker = null;
						progMon = null;
					}
				}
	
			}
		});
		progressWorker.execute();
	}
}

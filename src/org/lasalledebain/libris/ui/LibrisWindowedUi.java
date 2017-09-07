package org.lasalledebain.libris.ui;

import java.io.File;

import javax.swing.JFrame;

import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.index.GroupDef;

public abstract class LibrisWindowedUi extends LibrisUiGeneric {
	protected JFrame mainFrame;
	private GroupDef selectedGroupDef;

	protected void initializeUi() {
		mainFrame = new JFrame();
	}
	public JFrame getMainFrame() {
		return mainFrame;
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

	public void setSelectedGroup(GroupDef grpDef) {
		selectedGroupDef = grpDef;
		enableNewChild();
	}
	public GroupDef getSelectedGroup() {
		return selectedGroupDef;
	}

	void enableNewChild() {}
	public abstract Record newChildRecord(Record currentRecord, int groupNum);
}

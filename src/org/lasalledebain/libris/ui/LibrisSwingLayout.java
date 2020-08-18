package org.lasalledebain.libris.ui;

import java.util.ArrayList;

import javax.swing.JComponent;

import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.RecordList;
import org.lasalledebain.libris.Schema;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.LibrisException;

public abstract class LibrisSwingLayout<RecordType extends Record> extends LibrisLayout<RecordType> {
	public LibrisSwingLayout(Schema schem){
		super(schem);
	}

	abstract ArrayList<UiField> layOutFields(RecordType rec, LibrisWindowedUi<RecordType> ui, JComponent recordPanel, ModificationTracker modTrk)
			throws DatabaseException, LibrisException;

	ArrayList<UiField> layOutFields(RecordList<RecordType> recList, LibrisWindowedUi<RecordType> ui, JComponent recordPanel, ModificationTracker modTrk)
			throws DatabaseException, LibrisException {
		return 	layOutFields(recList.getFirstRecord(), ui,  recordPanel, modTrk);
	};

	protected abstract void showRecord(int recId);
}
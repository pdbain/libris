package org.lasalledebain.libris.ui;

import java.util.ArrayList;

import javax.swing.JComponent;

import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.RecordList;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.LibrisException;

public interface LayoutSwingProcessor<RecordType extends Record> {
	 ArrayList<UiField> layOutFields(RecordType rec, LibrisWindowedUi<RecordType> ui, JComponent recordPanel, ModificationTracker modTrk)
			throws DatabaseException, LibrisException;

	ArrayList<UiField> layOutFields(RecordList<RecordType> recList, LibrisWindowedUi<RecordType> ui, JComponent recordPanel, ModificationTracker modTrk)
			throws DatabaseException, LibrisException;;


}


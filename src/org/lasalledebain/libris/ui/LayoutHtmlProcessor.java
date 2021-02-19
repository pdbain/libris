package org.lasalledebain.libris.ui;

import java.io.IOException;

import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.RecordList;
import org.lasalledebain.libris.exception.InputException;

public interface LayoutHtmlProcessor<RecordType extends Record> {

	public void layoutDisplayPanel(RecordList<RecordType> recList, HttpParameters params, int recId, StringBuffer buff) throws InputException;
	public void layOutPage(RecordList<RecordType> recList, HttpParameters params,
			LibrisLayout browserLayout, DatabaseUi ui) throws InputException, IOException;
}

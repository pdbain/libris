package org.lasalledebain.libris.ui;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.RecordList;
import org.lasalledebain.libris.exception.InputException;

public interface LayoutHtmlProcessor<RecordType extends Record> {

	public void layOutPage(RecordList<RecordType> recList, int recId, LibrisLayout<RecordType> browserLayout, DatabaseUi<RecordType> ui, HttpServletResponse resp) throws InputException, IOException;
	public void layoutDisplayPanel(RecordList<RecordType> recList, int recId, StringBuffer buff) throws InputException;

}

package org.lasalledebain.libris.ui;

import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.RecordList;
import org.lasalledebain.libris.Schema;
import org.lasalledebain.libris.exception.DatabaseError;
import org.lasalledebain.libris.exception.InputException;

public class LibrisHtmlFormLayout<RecordType extends Record> extends LibrisHtmlLayout<RecordType>{

	public LibrisHtmlFormLayout(Schema schem) {
		super(schem);
	}

	@Override
	protected void validate() throws InputException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getLayoutType() {
		return XML_LAYOUT_TYPE_HTML_FORM;
	}

	@Override
	protected void layoutDisplayPanel(RecordList<RecordType> recList, int recId, StringBuffer buff) {
		throw new DatabaseError("Not implemented");
		
	}

}

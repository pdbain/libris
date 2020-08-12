package org.lasalledebain.libris.ui;

import javax.servlet.http.HttpServletResponse;

import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.RecordList;
import org.lasalledebain.libris.Schema;
import org.lasalledebain.libris.exception.DatabaseError;
import org.lasalledebain.libris.exception.InputException;

public class LibrisHtmlListLayout<RecordType extends Record> extends LibrisHtmlLayout<RecordType>{

	public LibrisHtmlListLayout(Schema schem) {
		super(schem);
		throw new DatabaseError("LibrisHtmlListLayout not implemented");
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

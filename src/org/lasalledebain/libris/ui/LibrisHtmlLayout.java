package org.lasalledebain.libris.ui;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.Schema;
import org.lasalledebain.libris.exception.InputException;

public abstract class LibrisHtmlLayout<RecordType extends Record> extends LibrisLayout<RecordType> {

	public LibrisHtmlLayout(Schema schem) {
		super(schem);
	}

	@Override
	protected abstract void validate() throws InputException;

	@Override
	public abstract String getLayoutType();

	public abstract void layOutFields(RecordType rec, LibrisUi ui, HttpServletResponse resp, ModificationTracker modTrk) throws InputException, IOException;

}

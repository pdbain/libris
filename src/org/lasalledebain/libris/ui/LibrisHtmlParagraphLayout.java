package org.lasalledebain.libris.ui;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.Schema;
import org.lasalledebain.libris.exception.InputException;

public class LibrisHtmlParagraphLayout<RecordType extends Record> extends LibrisHtmlLayout<RecordType> {

	private ParagraphLayout<RecordType> myFormatter;

	public LibrisHtmlParagraphLayout (Schema schem) {
		super(schem);
		myFormatter = new ParagraphLayout<>(mySchema);
	}
	
	@Override
	protected void validate() throws InputException {
		// TODO Auto-generated method stub

	}

	@Override
	public String getLayoutType() {
		return XML_LAYOUT_TYPE_HTML_PARAGRAPH;
	}

	@Override
	public void layOutFields(RecordType rec, LibrisUi ui, HttpServletResponse resp, ModificationTracker modTrk) throws InputException, IOException {
		LayoutField<RecordType>[] fieldInfo = getFields();
		String htmlText = myFormatter.createHtmlParagraph(rec, fieldInfo);
		resp.getWriter().append(htmlText);
	}

}

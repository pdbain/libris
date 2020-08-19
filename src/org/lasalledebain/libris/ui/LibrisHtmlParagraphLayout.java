package org.lasalledebain.libris.ui;

import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.RecordList;
import org.lasalledebain.libris.Schema;
import org.lasalledebain.libris.exception.InputException;

@Deprecated
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
	public void layoutDisplayPanel(RecordList<RecordType> recList, int recId, StringBuffer buff) throws InputException {
		LayoutField<RecordType>[] fieldInfo = getFields();
		myFormatter.recordToParagraph(recList.getRecord(recId), fieldInfo, buff);		
	}

}

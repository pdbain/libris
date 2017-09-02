package org.lasalledebain.libris.ui;

import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.JTextArea;

import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.Schema;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.xmlUtils.LibrisXMLConstants;

public class ParagraphLayout extends Layout {

	public ParagraphLayout(Schema schem) throws DatabaseException {
		super(schem);
	}

	@Override
	ArrayList<UiField> layOutFields(Record rec, LibrisWindowedUi ui, JPanel recordPanel, ModificationTracker modTrk)
			throws DatabaseException, LibrisException {
		JTextArea content;
		FieldPosition[] fieldInfo = getFields();
		StringBuffer windowText = new StringBuffer();
		for (FieldPosition fp: fieldInfo) {
			String fieldText = rec.getField(fp.fieldNum).getValuesAsString();
			windowText.append(fieldText);
		}
		return emptyUiList;
	}

	@Override
	public String getLayoutType() {
		return LibrisXMLConstants.XML_LAYOUT_TYPE_PARAGRAPH;
	}

}

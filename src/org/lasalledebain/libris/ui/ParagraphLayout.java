package org.lasalledebain.libris.ui;

import java.util.ArrayList;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import org.lasalledebain.libris.Field;
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
		JEditorPane content = new JEditorPane();
		content.setContentType("text/html;");
		content.setEditable(false);
		String fieldSep = "<h1>";
		FieldPosition[] fieldInfo = getFields();
		StringBuffer windowText = new StringBuffer();
		String[] titles = mySchema.getFieldTitles();
		for (FieldPosition fp: fieldInfo) {
			Field fld = rec.getField(fp.fieldNum);
			if (null != fld) {
				windowText.append("<h2>");
				windowText.append(titles[fp.fieldNum]);
				windowText.append("</h2><p>");
				String fieldText = fld.getValuesAsString();
				windowText.append(fieldText);
				fieldSep = "\n\n";
			}
		}
		content.setText(windowText.toString());
		recordPanel.add(content);
		return emptyUiList;
	}

	@Override
	public String getLayoutType() {
		return LibrisXMLConstants.XML_LAYOUT_TYPE_PARAGRAPH;
	}

}

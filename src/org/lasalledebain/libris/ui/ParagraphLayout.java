package org.lasalledebain.libris.ui;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.Objects;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.RecordList;
import org.lasalledebain.libris.Schema;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.field.FieldValue;
import org.lasalledebain.libris.field.GenericField;
import org.lasalledebain.libris.xmlUtils.LibrisXMLConstants;

public class ParagraphLayout extends Layout {

	public ParagraphLayout(Schema schem) throws DatabaseException {
		super(schem);
	}

	@Override
	ArrayList<UiField> layOutFields(Record rec, LibrisWindowedUi ui, JPanel recordPanel, ModificationTracker modTrk)
			throws DatabaseException, LibrisException {
		JEditorPane content = new JEditorPane();
		JScrollPane editorScrollPane = new JScrollPane(content);
		editorScrollPane.setVerticalScrollBarPolicy(
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		editorScrollPane.setPreferredSize(ui.getDisplayPanelSize());
		editorScrollPane.setMinimumSize(new Dimension(10, 10));	
		recordPanel.add(editorScrollPane);
		recordPanel.setLayout(new GridBagLayout());
		content.setContentType("text/html;");
		content.setEditable(false);
		FieldPosition[] fieldInfo = getFields();
		StringBuffer windowText = new StringBuffer();

		windowText.append("<!DOCTYPE html>\n<html>\n<head>");
		windowText.append("<title>\n");
		windowText.append(rec.generateTitle());
		windowText.append("</title>\n");
		windowText.append("<style type=\"text/css\">\n"
				+ "body {font-family: \"Courier New\", Courier, monospace;}\n"
				+ "h1 {margin: 0.5em 0px 0px 0px; font-weight: bold; font-size: 1em;}\n"
				+ "p {margin: 0px; font-size: 1em;}\n"
				);
		windowText.append("</style>\n");
		windowText.append("</head>\n<body>\n");
		String[] titles = mySchema.getFieldTitles();
		for (FieldPosition fp: fieldInfo) {
			FieldValue val = rec.getFieldValue(fp.fieldNum);
			if (!Objects.isNull(val) && !val.isEmpty()) {
				windowText.append("<h1>");
				windowText.append(titles[fp.fieldNum]);
				windowText.append("</h1>\n<p>\n");
				String fieldText = GenericField.valuesToString(val);
				windowText.append(fieldText);
				windowText.append("\n</p>\n");
			}
		}
		windowText.append("</body>\n</html>");

		content.setText(windowText.toString());
		return emptyUiList;
	}

	@Override
	public String getLayoutType() {
		return LibrisXMLConstants.XML_LAYOUT_TYPE_PARAGRAPH;
	}

	@Override
	protected void showRecord(int recId) {
		return;
	}

}

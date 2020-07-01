package org.lasalledebain.libris.ui;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.util.ArrayList;
import java.util.Objects;

import javax.swing.JComponent;
import javax.swing.JEditorPane;

import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.Schema;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.field.FieldValue;
import org.lasalledebain.libris.field.GenericField;
import org.lasalledebain.libris.xmlUtils.LibrisXMLConstants;

public class ParagraphLayout<RecordType extends Record> extends Layout<RecordType> {

	public ParagraphLayout(Schema schem) throws DatabaseException {
		super(schem);
	}

	@Override
	ArrayList<UiField> layOutFields(RecordType rec, LibrisWindowedUi ui, JComponent recordPanel, ModificationTracker unused)
			throws DatabaseException, LibrisException {
		JEditorPane content = new JEditorPane();
		recordPanel.addComponentListener(new ComponentListener() {

			@Override
			public void componentShown(ComponentEvent e) {
				return;
			}

			@Override
			public void componentResized(ComponentEvent e) {
				Dimension rpsize = recordPanel.getVisibleRect().getSize();
				content.setPreferredSize(rpsize);
			}

			@Override
			public void componentMoved(ComponentEvent e) {
				return;
			}

			@Override
			public void componentHidden(ComponentEvent e) {
				// TODO Auto-generated method stub

			}
		});
		recordPanel.add(content);
		recordPanel.setLayout(new GridBagLayout());
		content.setContentType("text/html;");
		content.setEditable(false);
		LayoutField[] fieldInfo = getFields();
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
		windowText.append("</head>\n<body><p>\n");
		String separator = "";
		for (LayoutField fp: fieldInfo) {
			FieldValue val = rec.getFieldValue(fp.fieldNum);
			if (!Objects.isNull(val) && !val.isEmpty()) {
				windowText.append(separator);
				String fieldText = GenericField.valuesToString(val);
				windowText.append(fieldText);
				separator = ", ";
			}
		}
		windowText.append("</p></body>\n</html>");

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

package org.lasalledebain.libris.ui;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.ArrayList;
import java.util.Objects;

import javax.swing.JComponent;
import javax.swing.JEditorPane;

import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.RecordList;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.field.FieldValue;
import org.lasalledebain.libris.field.GenericField;

public class ParagraphLayoutProcessor <RecordType extends Record> extends LayoutProcessor<RecordType> {

	public ParagraphLayoutProcessor(LibrisLayout theLayout) {
		super(theLayout);
	}

	@Override
	public void layoutDisplayPanel(RecordList<RecordType> recList, HttpParameters params, int recId, StringBuffer buff) throws InputException {
		LayoutField[] fieldInfo = myLayout.getFields();
		RecordType rec = recList.getRecord(recId);
		StringBuffer windowText = new StringBuffer();
		if (null == rec) {
			windowText.append("Record "+recId+" not found");
		} else {
			recordToParagraph(rec, fieldInfo, windowText);
		}
		buff.append(windowText);
	}

	@Override
	public ArrayList<UiField> layOutFields(RecordType rec, LibrisWindowedUi<RecordType> ui, JComponent recordPanel,
			ModificationTracker modTrk) throws DatabaseException, LibrisException {
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
		LayoutField[] fieldInfo = myLayout.getFields();
		String windowText = createHtmlParagraph(rec, fieldInfo);

		content.setText(windowText);
		return LibrisLayout.emptyUiList;
	}

	public String createHtmlParagraph(RecordType rec, LayoutField[] fieldInfo)
			throws InputException {
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
		recordToParagraph(rec, fieldInfo, windowText);
		windowText.append("</p></body>\n</html>");
		return windowText.toString();
	}

	public void recordToParagraph(RecordType rec, LayoutField[] fieldInfo, StringBuffer windowText)
			throws InputException {
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
	}

	@Override
	void validate() throws InputException {
		// TODO Auto-generated method stub

	}

}

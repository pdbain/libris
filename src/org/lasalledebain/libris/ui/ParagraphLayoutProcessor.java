package org.lasalledebain.libris.ui;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.JEditorPane;

import org.lasalledebain.libris.Field;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.RecordList;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.field.FieldValue;
import org.lasalledebain.libris.field.GenericField;

import static java.util.Objects.nonNull;
import static java.util.Objects.isNull;

public class ParagraphLayoutProcessor extends LayoutProcessor {

	public ParagraphLayoutProcessor(LibrisLayout theLayout) {
		super(theLayout);
	}

	@Override
	public void layoutDisplayPanel(RecordList<Record> recList, HttpParameters params, int recId, StringBuffer buff) throws InputException {
		Record rec = recList.getRecord(recId);
		StringBuffer windowText = new StringBuffer();
		if (isNull(rec)) {
			windowText.append("Record "+recId+" not found");
		} else {
			recordToParagraph(rec, myLayout.getFields(), windowText);
		}
		buff.append(windowText);
	}

	@Override
	public ArrayList<UiField> layOutFields(Record rec, LibrisWindowedUi ui, JComponent recordPanel,
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
		String windowText = createHtmlParagraph(rec);

		content.setText(windowText);
		return LibrisLayout.emptyUiList;
	}

	public String createHtmlParagraph(Record rec)
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
		recordToParagraph(rec, myLayout.getFields(), windowText);
		windowText.append("</p></body>\n</html>");
		return windowText.toString();
	}

	public static void recordToParagraph(Record rec, LayoutField[] fieldInfo, StringBuffer windowText) throws InputException {
		String separator = "";
		for (LayoutField fp: fieldInfo) {
			Field fld = rec.getField(fp.fieldNum);
			if (nonNull(fld) && !fld.isEmpty()) {
				Iterable<? extends FieldValue> valueList = fld.getFieldValues();
				windowText.append(separator);
				String fieldText = GenericField.valuesToString(valueList);
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

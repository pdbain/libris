package org.lasalledebain.libris.ui;

import java.awt.TextArea;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.xml.stream.XMLStreamException;

import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.RecordList;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.xmlUtils.ElementWriter;

public class XmlLayoutProcessor<RecordType extends Record> extends LayoutProcessor<RecordType> {
	private static final String XML_TEXT_CLASS = "xmlText";
	private static final String XML_TEXT_STYLE = "."+XML_TEXT_CLASS +"{\n"
			+ "width: 95%;\n"
			+ "border-style: none;"
			+ "}\n";

	public XmlLayoutProcessor(LibrisLayout theLayout) {
		super(theLayout);
	}

	protected String getXmlText(Record rec) throws LibrisException, DatabaseException {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		ElementWriter streamWriter;
		try {
			streamWriter = ElementWriter.eventWriterFactory(outStream);
			rec.toXml(streamWriter);
		} catch (XMLStreamException e) {
			throw new DatabaseException(e);
		}
		String xmlText = outStream.toString();
		return xmlText;
	}

	@Override
	public void layoutDisplayPanel(RecordList<RecordType> recList, HttpParameters params, int recId, StringBuffer buff) throws InputException {

		Record rec = recList.getRecord(recId);
		StringBuffer windowText = new StringBuffer();
		if (null == rec) {
			windowText.append("Record "+recId+" not found");
		} else {
			String recText;
			try {
				recText = getXmlText(rec);
			} catch (LibrisException e) {
				throw new InputException(e);
			}
			windowText.append("<textarea readonly rows="
					+ (recText.split("[\n|\r]").length+1)
					+ " class="+XML_TEXT_CLASS
					+ ">\n");
			windowText.append(recText);
			windowText.append("</textarea>");
		}
		buff.append(windowText);
	}

	@Override
	public ArrayList<UiField> layOutFields(Record rec, LibrisWindowedUi ui, JComponent recordPanel,
			ModificationTracker modTrk) throws DatabaseException, LibrisException {
		String xmlText = getXmlText(rec);
		recordPanel.add(new TextArea(xmlText));
		return null;
	}

	@Override
	protected void validate() {
		// TODO Write xmllayout validate
		
	}
	@Override
	protected String getStyleString() {
		StringBuffer buff = new StringBuffer(super.getStyleString());
		buff.append(XML_TEXT_STYLE);
		return buff.toString();
	}

}

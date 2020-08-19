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

	public XmlLayoutProcessor(LibrisLayout<RecordType> theLayout) {
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
	public void layoutDisplayPanel(RecordList<RecordType> recList, int recId, StringBuffer buff) throws InputException {
		RecordType rec = recList.getRecord(recId);
		try {
			String recText = getXmlText(rec);
			buff.append(recText);
		} catch (LibrisException e) {
			throw new InputException(e);
		}
	}

	@Override
	public ArrayList<UiField> layOutFields(RecordType rec, LibrisWindowedUi<RecordType> ui, JComponent recordPanel,
			ModificationTracker modTrk) throws DatabaseException, LibrisException {
		String xmlText = getXmlText(rec);
		recordPanel.add(new TextArea(xmlText));
		return null;
	}

	@Override
	protected void validate() {
		// TODO Write xmllayout validate
		
	}

}

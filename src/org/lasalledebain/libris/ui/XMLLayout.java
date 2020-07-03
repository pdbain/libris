package org.lasalledebain.libris.ui;

import java.awt.TextArea;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.xml.stream.XMLStreamException;

import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.Schema;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.xmlUtils.ElementWriter;
import org.lasalledebain.libris.xmlUtils.LibrisXMLConstants;

public class XMLLayout<RecordType extends Record> extends Layout<RecordType> {

	public XMLLayout(Schema schem) {
		super(schem);
	}

	@Override
	ArrayList<UiField>  layOutFields(Record rec, LibrisWindowedUi ui, JComponent recordPanel, ModificationTracker unused)
			throws DatabaseException, LibrisException {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		ElementWriter streamWriter;
		try {
			streamWriter = ElementWriter.eventWriterFactory(outStream);
			rec.toXml(streamWriter);
		} catch (XMLStreamException e) {
			throw new DatabaseException(e);
		}
		String xmlText = outStream.toString();
		recordPanel.add(new TextArea(xmlText));
		return null;
	}

	@Override
	public String getLayoutType() {
		return LibrisXMLConstants.XML_LAYOUT_TYPE_XML;
	}

	@Override
	protected void showRecord(int recId) {
		return;
	}

}

package org.lasalledebain.libris.records;

import java.io.IOException;
import java.io.OutputStream;

import javax.xml.stream.XMLStreamException;

import org.lasalledebain.libris.GenericDatabase;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.XmlException;
import org.lasalledebain.libris.xmlUtils.ElementWriter;
import org.lasalledebain.libris.xmlUtils.LibrisXMLConstants;

public class XmlRecordsWriter<RecordType extends Record> implements RecordsWriter<RecordType> {
	GenericDatabase<RecordType> database;
	ElementWriter recWriter;
	private OutputStream opStream;
	/**
	 * @param database
	 * @param recWriter
	 * @throws XmlException 
	 */
	@Deprecated
	public XmlRecordsWriter(GenericDatabase<RecordType> database, OutputStream xmlOutputStream) throws XmlException {
		this.database = database;
		this.opStream = xmlOutputStream;
		try {
			recWriter = ElementWriter.eventWriterFactory(xmlOutputStream, LibrisXMLConstants.XML_RECORD_INDENT);
		} catch (XMLStreamException e) {
			throw new XmlException("error creating XML writer", e);
		};
	}

	
	@Override
	public void put(Record rec) throws DatabaseException {
		try {
			rec.toXml(recWriter);
		} catch (Exception e) {
			DatabaseException dbExc = new DatabaseException(e);
			dbExc.setStackTrace(e.getStackTrace());
			throw dbExc;
		}
	}

	@Override
	public void addAll(Iterable<RecordType> recList) throws DatabaseException {
		for (Record r: recList) {
			put(r);
		}
	}

	@Override
	public void closeFile() throws DatabaseException {
		try {
			opStream.close();
		} catch (IOException e) {
			throw new DatabaseException("error closing XML file", e);
		}
		opStream = null;
		recWriter = null;

	}

}

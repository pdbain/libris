package org.lasalledebain.libris.indexes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Iterator;

import javax.xml.stream.XMLStreamException;

import org.lasalledebain.libris.FileAccessManager;
import org.lasalledebain.libris.GenericDatabase;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.RecordFactory;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.exception.UserErrorException;
import org.lasalledebain.libris.exception.XmlException;
import org.lasalledebain.libris.records.RecordsWriter;
import org.lasalledebain.libris.xmlUtils.ElementWriter;
import org.lasalledebain.libris.xmlUtils.LibrisAttributes;
import org.lasalledebain.libris.xmlUtils.LibrisXMLConstants;

public class LibrisJournalFileManager<RecordType extends Record> implements Iterable<RecordType>, RecordsWriter<RecordType>, LibrisXMLConstants {
	HashMap<Integer, RecordIdAndLength> journalIndex;
	private GenericDatabase<RecordType> database;
	private FileAccessManager journalFileMgr;
	private RandomAccessFile journalFile;
	private ByteArrayOutputStream xmlBuffer;
	private ElementWriter xmlWriter;
	private LibrisAttributes databaseProperties;
	private RecordFactory<RecordType> myRecordFactory;

	public LibrisJournalFileManager(GenericDatabase<RecordType> database, FileAccessManager journalFileMr, RecordFactory<RecordType> recFact) throws LibrisException  {
		this.database = database;
		myRecordFactory = recFact;
		databaseProperties = database.getAttributes();
		journalIndex = new HashMap<Integer, RecordIdAndLength>();
		try {
			if (!database.isReadOnly()) {
				boolean initializeFile = false;
				initializeFile = journalFileMr.createIfNonexistent() || (journalFileMr.getLength() == 0);
				journalFile = journalFileMr.getReadWriteRandomAccessFile();
				if (initializeFile) {
					try {
						ElementWriter w = ElementWriter.eventWriterFactory(journalFileMr.getOpStream());
						w.writeStartElement(XML_LIBRIS_TAG, databaseProperties, false);
						w.writeStartElement(XML_RECORDS_TAG);
						w.flush();
						journalFile.seek(journalFile.length());
						journalFile.write(RECORDS_LIBRIS_CLOSING_TAG_BYTES);
						journalFileMr.releaseOpStream();
					} catch (XMLStreamException e) {
						throw new XmlException("error creating journal file", e);
					}
				}
				xmlBuffer = new ByteArrayOutputStream();
				try {
					xmlWriter = ElementWriter.eventWriterFactory(xmlBuffer);
				} catch (XMLStreamException e) {
					throw new DatabaseException("cannot create xml writer: ", e);
				}
			} else {
				journalFile = journalFileMr.getReadOnlyRandomAccessFile();
			}
		} catch (IOException e){
			throw new DatabaseException("Exception on journal file: ", e);
		}
		this.journalFileMgr = journalFileMr;
	}

	/* (non-Javadoc)
	 * @see org.lasalledebain.libris.records.RecordsWriter#put(org.lasalledebain.libris.Record)
	 */
	public void put(Record rec) throws LibrisException  {
		if (database.isReadOnly()) {
			throw new UserErrorException("database is read only");
		}
		int id = rec.getRecordId();
		xmlBuffer.reset();
		rec.toXml(xmlWriter);
		try {
			boolean writeEndTag = false;
			int length = xmlBuffer.size();
			if (journalIndex.containsKey(id)) {
				RecordIdAndLength entry = journalIndex.get(id);
				overwrite(journalFile, entry.position, entry.length);
				if (entry.length >= length) {
					journalFile.seek(entry.position);
				} else {
					seekToStartOfClosingTag();
					entry.length = length;
					entry.position = journalFile.getFilePointer();
					writeEndTag = true;
				}
			} else {
				writeEndTag = true;
				seekToStartOfClosingTag();
				long position = journalFile.getFilePointer();
				RecordIdAndLength entry = new RecordIdAndLength(length, position);
				journalIndex.put(id, entry);
			}
			journalFile.write(xmlBuffer.toByteArray());
			journalFile.write('\n');
			if (writeEndTag) {
				journalFile.write(LibrisXMLConstants.RECORDS_LIBRIS_CLOSING_TAG_BYTES);
			}
		} catch (IOException e) {
			throw new DatabaseException("Exception on journal file: ", e);
		}
	}

	@Override
	public void addAll(Iterable<RecordType> recList) throws LibrisException {
		for (Record rec: recList) {
			put(rec);
		}
	}

	@Override
	public void closeFile() throws DatabaseException {
		try {
			close();
		} catch (IOException e) {
			throw new DatabaseException(e);
		}
	}

	public boolean removeRecord(int id) throws LibrisException {
		try {
			if (journalIndex.containsKey(id)) {
				RecordIdAndLength entry = journalIndex.get(id);
				overwrite(journalFile, entry.position, entry.length);
				return true;
			} else {
				return false;
			}
		} catch (IOException e) {
			throw new DatabaseException("Exception on journal file: ", e);
		}
	}

	public void close() throws IOException {
		if (null != journalFileMgr) {
			journalFileMgr.close();
		}
		journalIndex = null;
	}

	private void seekToStartOfClosingTag() throws IOException {
		/* account for newline at end of file */
		journalFile.seek(journalFile.length()-LibrisXMLConstants.RECORDS_LIBRIS_CLOSING_TAG_BYTES.length);
	}

	private void overwrite(RandomAccessFile jFile, long l,
			int length) throws DatabaseException, IOException {
		jFile.seek(l);
		final byte[] blanks = "                                                               \n".getBytes();
		while (length > blanks.length) {
			jFile.write(blanks);
			length -= blanks.length;
		}
		if (length > 0) {
			jFile.write(blanks, blanks.length-length, length);
		}
	}

	private class RecordIdAndLength {
		long position;
		int length;
		/**
		 * @param length of record entry, including padding
		 * @param pos start position in file.
		 */
		public RecordIdAndLength(int length, long pos) {
			this.length = length;
			this.position = pos;
		}
	}

	@Override
	public Iterator<RecordType> iterator() {
		JournalFileRecordIterator<RecordType> iter;
		try {
			iter = new JournalFileRecordIterator<RecordType>(journalFileMgr, myRecordFactory);
		} catch (Exception e) {
			database.alert("Error constructing iterator", e);
			return null;
		}
		return iter;
	}

	public int getFieldNum() {
		return 0;
	}

	public String getId() {
		return null;
	}

	public String getTitle() {
		return null;
	}
}

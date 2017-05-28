package org.lasalledebain.libris.records;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Level;

import javax.xml.stream.FactoryConfigurationError;

import org.lasalledebain.libris.FileAccessManager;
import org.lasalledebain.libris.LibrisDatabase;
import org.lasalledebain.libris.LibrisFileManager;
import org.lasalledebain.libris.LibrisMetadata;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.RecordId;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.exception.XmlException;
import org.lasalledebain.libris.indexes.BulkImporter;
import org.lasalledebain.libris.indexes.RecordPositions;
import org.lasalledebain.libris.ui.Messages;
import org.lasalledebain.libris.xmlUtils.ElementManager;
import org.lasalledebain.libris.xmlUtils.LibrisXMLConstants;

/**
 * Read records from an XML file.
 *
 */
public class XmlRecordsReader implements RecordsReader, Iterator<Record>,LibrisXMLConstants {
	ElementManager recsMgr;
	private LibrisDatabase database;

	/**
	 * @param database database object
	 * @param recsMgr XML element manager for <record> element 
	 * @throws DatabaseException 
	 */
	public XmlRecordsReader(LibrisDatabase database, ElementManager recsMgr) throws DatabaseException {
		this.database = database;
		this.recsMgr = recsMgr;
		// recsMgr.parseOpenTag();
	}
	
	@Override
	public Iterator<Record> iterator() {
		try {
			XmlRecordsReader recs = new XmlRecordsReader(database, recsMgr);
			recsMgr.parseOpenTag();
			return recs;
		} catch (LibrisException e) {
			e.printStackTrace();
			database.getUi().alert("Exception: ", e); //$NON-NLS-1$
			return null;
		}
	}
	@Override
	public boolean hasNext() {
		return recsMgr.hasNext();
	}

	@Override
	public Record next() {
		Record inputRecord = null;
		ElementManager recMgr;

		try {
			recMgr = recsMgr.nextElement();
			if (null != recMgr) {
				inputRecord = database.newRecord();
				inputRecord.fromXml(recMgr);
			}
		} catch (LibrisException e) {
			database.log(Level.SEVERE, Messages.getString("XmlRecordsReader.0"), e); //$NON-NLS-1$
			LibrisException.saveException(e);
			return null;
		}
		return inputRecord;
	}

	@Override
	public void remove() {
		return;
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

	public static void importXmlRecords(LibrisDatabase database, File source) throws LibrisException {
		LibrisMetadata metadata = database.getMetadata();
		try {
			int numAdded = 0;
			ElementManager librisXmlMgr = database.makeLibrisElementManager(source);
			librisXmlMgr.parseOpenTag();
			String nextElement = librisXmlMgr.getNextId();
			ElementManager metadataMgr;
			if (XML_METADATA_TAG.equals(nextElement)) {
				metadataMgr = librisXmlMgr.nextElement();
				metadataMgr.flushElement();
			}
			ElementManager recordsMgr = librisXmlMgr.nextElement();
			XmlRecordsReader recordsRdr = new XmlRecordsReader(database, recordsMgr);
			int lastId = 0;
			metadata.setSavedRecords(0);
			LibrisFileManager fileMgr = database.getFileMgr();
			FileAccessManager recordsFileMgr = fileMgr.getRecordsFileMgr();
			RecordPositions recPosns = new RecordPositions(fileMgr.getPositionFileMgr(), false);
			BulkImporter importer = new BulkImporter(database.getSchema(), recordsFileMgr.getOpStream(), recPosns);
			importer.initialize();
			boolean nonEmpty = false;
			for (Record r: recordsRdr) {
				nonEmpty = true;
				if (null == r) {
					LibrisException e = LibrisException.getLastException();
					throw e;
				}
				importer.putRecord(r);
				++numAdded;
				database.log(Level.FINE, "importXmlRecords put record "+RecordId.toString(r.getRecordId())); //$NON-NLS-1$
				int recId = r.getRecordId();
				lastId = Math.max(lastId, recId);
			}
			metadata.setSavedRecords(numAdded);
			importer.finish(nonEmpty);
			recordsFileMgr.releaseOpStream();
			recPosns.close();
			librisXmlMgr.closeFile();
			database.closeDatabaseSource();
			metadata.setLastRecordId(lastId);
		} catch (XmlException e) {
			throw new DatabaseException(Messages.getString("XmlRecordsReader.1"), e); //$NON-NLS-1$

		} catch (FactoryConfigurationError e) {
			String msg = Messages.getString("XmlRecordsReader.4"); //$NON-NLS-1$
			database.log(Level.SEVERE, msg, e); //$NON-NLS-1$
			throw new DatabaseException(msg); //$NON-NLS-1$
		} catch (IOException e) {
			throw new DatabaseException(Messages.getString("XmlRecordsReader.6"), e); //$NON-NLS-1$

			}
	}
}

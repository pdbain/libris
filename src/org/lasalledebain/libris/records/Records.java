package org.lasalledebain.libris.records;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Level;

import org.lasalledebain.libris.DatabaseRecord;
import org.lasalledebain.libris.FileAccessManager;
import org.lasalledebain.libris.GenericDatabase;
import org.lasalledebain.libris.GenericDatabaseMetadata;
import org.lasalledebain.libris.LibrisConstants;
import org.lasalledebain.libris.LibrisDatabase;
import org.lasalledebain.libris.LibrisFileManager;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.RecordId;
import org.lasalledebain.libris.RecordList;
import org.lasalledebain.libris.XMLElement;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.exception.XmlException;
import org.lasalledebain.libris.indexes.BulkImporter;
import org.lasalledebain.libris.indexes.LibrisRecordsFileManager;
import org.lasalledebain.libris.indexes.RecordPositions;
import org.lasalledebain.libris.ui.Messages;
import org.lasalledebain.libris.util.Reporter;
import org.lasalledebain.libris.xmlUtils.ElementManager;
import org.lasalledebain.libris.xmlUtils.ElementWriter;
import org.lasalledebain.libris.xmlUtils.LibrisAttributes;

public class Records<RecordType extends Record> implements Iterable<RecordType>, XMLElement{

	private LibrisRecordsFileManager<RecordType> recMgr;
	GenericDatabase<RecordType> myDatabase;

	public Records(GenericDatabase<RecordType> db, LibrisFileManager fileMgr) throws LibrisException {
		recMgr = db.getRecordsFileMgr();
		myDatabase = db;
	}

	public Iterable<RecordType> getNativeRecordsReader() {
		return recMgr;
	}

	public void importRecords(LibrisConstants.DatabaseFormat format, File[] importFiles) throws DatabaseException {
		throw new DatabaseException("not implemented");
	}

	public void putRecords(RecordList<RecordType> modifiedRecords) throws InputException, DatabaseException {
		for (Record r: modifiedRecords) {
			recMgr.putRecord(r);
		}
	}

	public void putRecord(Record rec) throws InputException, DatabaseException {
			recMgr.putRecord(rec);
	}

	public void flush() throws IOException, DatabaseException {
		recMgr.flush();
	}

	public Iterator<RecordType> iterator() {
		return recMgr.iterator();
	}

	public Iterator<RecordType> iterator(String prefix) {
		return myDatabase.getNamedRecords().iterator();
	}

	@Override
	public String getElementTag() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void fromXml(ElementManager mgr) throws LibrisException {
		// TODO test Records.fromXml
		GenericDatabaseMetadata metadata = myDatabase.getMetadata();
		XmlRecordsReader<RecordType> recordsRdr = new XmlRecordsReader<RecordType>(myDatabase, mgr);
		int numAdded = 0;
		int lastId = 0;
		metadata.setSavedRecords(0);
		LibrisFileManager fileMgr = myDatabase.getFileMgr();
		FileAccessManager recordsFileMgr = fileMgr.getAuxiliaryFileMgr(LibrisConstants.RECORDS_FILENAME);
		RecordPositions recPosns = new RecordPositions(fileMgr.getAuxiliaryFileMgr(LibrisConstants.POSITION_FILENAME), false);
		try {
			BulkImporter importer = new BulkImporter(myDatabase.getSchema(), recordsFileMgr.getOpStream(), recPosns);
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
				LibrisDatabase.log(Level.FINE, "importXmlRecords put record "+RecordId.toString(r.getRecordId())); //$NON-NLS-1$
				int recId = r.getRecordId();
				lastId = Math.max(lastId, recId);
			}
			try (FileOutputStream reportFile = fileMgr.getUnmanagedOutputFile(LibrisConstants.IMPORT_REPORT_FILE)) {
				Reporter rpt = new Reporter();
				recPosns.generateReport(rpt);
				rpt.writeReport(reportFile);
			};

			metadata.setSavedRecords(numAdded);
			metadata.setLastRecordId(lastId);
			importer.finish(nonEmpty);
			recordsFileMgr.releaseOpStream();
			recPosns.close();
		} catch (IOException e) {
			throw new DatabaseException(Messages.getString("XmlRecordsReader.6"), e); //$NON-NLS-1$
		}
	}

	@Override
	public void toXml(ElementWriter output) throws LibrisException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public LibrisAttributes getAttributes() throws XmlException {
		// TODO Auto-generated method stub
		return null;
	}
}

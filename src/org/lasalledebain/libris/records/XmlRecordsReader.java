package org.lasalledebain.libris.records;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;
import java.util.logging.Level;

import javax.xml.stream.FactoryConfigurationError;

import org.lasalledebain.libris.DatabaseInstance;
import org.lasalledebain.libris.DatabaseRecord;
import org.lasalledebain.libris.GenericDatabase;
import org.lasalledebain.libris.GenericDatabaseMetadata;
import org.lasalledebain.libris.LibrisDatabase;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.RecordId;
import org.lasalledebain.libris.exception.Assertion;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.exception.XmlException;
import org.lasalledebain.libris.ui.Messages;
import org.lasalledebain.libris.xmlUtils.ElementManager;
import org.lasalledebain.libris.xmlUtils.LibrisXMLConstants; 

/**
 * Read records from an XML file.
 *
 */
public class XmlRecordsReader<RecordType extends Record> implements Iterable<RecordType>, Iterator<RecordType>,LibrisXMLConstants {
	ElementManager recsMgr;
	private GenericDatabase<RecordType> database;

	/**
	 * @param database database object
	 * @param recsMgr XML element manager for <record> element 
	 * @throws DatabaseException 
	 */
	public XmlRecordsReader(GenericDatabase<RecordType> database, ElementManager recsMgr) throws DatabaseException {
		this.database = database;
		this.recsMgr = recsMgr;
		// recsMgr.parseOpenTag();
	}
	
	@Override
	public Iterator<RecordType> iterator() {
		try {
			XmlRecordsReader<RecordType> recs = new XmlRecordsReader<RecordType>(database, recsMgr);
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
	public RecordType next() {
		RecordType inputRecord = null;
		ElementManager recMgr;

		try {
			recMgr = recsMgr.nextElement();
			if (null != recMgr) {
				inputRecord = database.newRecord();
				inputRecord.fromXml(recMgr);
			}
		} catch (LibrisException e) {
			LibrisDatabase.log(Level.SEVERE, Messages.getString("XmlRecordsReader.0"), e); //$NON-NLS-1$
			LibrisException.saveException(e);
			return null;
		}
		return inputRecord;
	}

	@Override
	public void remove() {
		return;
	}

	public static void importXmlRecords(LibrisDatabase database, Records<DatabaseRecord> theRecords, File source) throws LibrisException {
		try {
			ElementManager librisMgr = database.makeLibrisElementManager(source);
			librisMgr.parseOpenTag();
			String nextElement = librisMgr.getNextId();
			if (XML_INSTANCE_TAG.equals(nextElement)) {
				ElementManager instanceMgr = librisMgr.nextElement();
				instanceMgr.flushElement();
				nextElement = librisMgr.getNextId();
			}
			ElementManager metadataMgr;
			if (XML_METADATA_TAG.equals(nextElement)) {
				metadataMgr = librisMgr.nextElement();
				metadataMgr.flushElement();
				nextElement = librisMgr.getNextId();	
			}
			ElementManager recordsMgr = librisMgr.nextElement();
			theRecords.fromXml(recordsMgr);
			librisMgr.closeFile();
			database.closeDatabaseSource();
		} catch (XmlException e) {
			throw new DatabaseException(Messages.getString("XmlRecordsReader.1"), e); //$NON-NLS-1$

		} catch (FactoryConfigurationError e) {
			String msg = Messages.getString("XmlRecordsReader.4"); //$NON-NLS-1$
			LibrisDatabase.log(Level.SEVERE, msg, e); //$NON-NLS-1$
			throw new DatabaseException(msg); //$NON-NLS-1$
		} catch (IOException e) {
			throw new DatabaseException(Messages.getString("XmlRecordsReader.6"), e); //$NON-NLS-1$

			}
	}

	public static void importIncrementFile(LibrisDatabase database, Reader source, String filePath) throws LibrisException {
		GenericDatabaseMetadata metadata = database.getMetadata();
		int lastMasterId = metadata.getLastRecordId();
		try {
			ElementManager librisMgr = database.makeLibrisElementManager(source, XML_LIBRIS_TAG, filePath);
			librisMgr.parseOpenTag();
			ElementManager instanceMgr = librisMgr.nextElement();
			DatabaseInstance instanceInfo = new DatabaseInstance();
			instanceInfo.fromXml(instanceMgr);
			int idOffset = lastMasterId - instanceInfo.getRecordIdBase();
			Assertion.assertTrueError("Increment starting record ID invalid", idOffset >= 0);
			ElementManager recordsMgr = librisMgr.nextElement();
			XmlRecordsReader<DatabaseRecord> recordsRdr = new XmlRecordsReader<DatabaseRecord>(database, recordsMgr);
			int numGroups = database.getSchema().getNumGroups();
			for (DatabaseRecord r: recordsRdr) {
				if (null == r) {
					LibrisException e = LibrisException.getLastException();
					throw e;
				}
				if (idOffset > 0) {
					int oldId = r.getRecordId();
					r.setRecordId(oldId + idOffset);
					for (int groupNum = 0; groupNum < numGroups; ++groupNum) {
						int[] affiliates = r.getAffiliates(groupNum);
						if (!Objects.isNull(affiliates)) {
							final int affiliatesLength = affiliates.length;
							int newAffiliates[] = Arrays.copyOf(affiliates, affiliatesLength);
							for (int i = 0; i < affiliatesLength; ++i) {
								if (newAffiliates[i] > lastMasterId) {
									newAffiliates[i] += idOffset;
								}
							}
							r.setAffiliates(groupNum, newAffiliates);
						}
					}
				}
				database.putRecord(r);
				LibrisDatabase.log(Level.FINE, "importXmlRecords put record "+RecordId.toString(r.getRecordId())); //$NON-NLS-1$
			}
			librisMgr.closeFile();
		} catch (XmlException e) {
			throw new DatabaseException(Messages.getString("XmlRecordsReader.1"), e); //$NON-NLS-1$

		} catch (FactoryConfigurationError e) {
			String msg = Messages.getString("XmlRecordsReader.4"); //$NON-NLS-1$
			LibrisDatabase.log(Level.SEVERE, msg, e); //$NON-NLS-1$
			throw new DatabaseException(msg); //$NON-NLS-1$
		}
	}
}

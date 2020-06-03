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
import org.lasalledebain.libris.DatabaseMetadata;
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
}

package org.lasalledebain.libris.records;

import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.lasalledebain.libris.FileAccessManager;
import org.lasalledebain.libris.LibrisDatabase;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.exception.XmlException;
import org.lasalledebain.libris.field.FieldValueStringList;
import org.lasalledebain.libris.xmlUtils.ElementManager;
import org.lasalledebain.libris.xmlUtils.LibrisXMLConstants;
import org.lasalledebain.libris.xmlUtils.LibrisXmlFactory;
import org.lasalledebain.libris.xmlUtils.XmlShapes;
import org.lasalledebain.libris.xmlUtils.XmlShapes.SHAPE_LIST;

public class FilteringRecordImporter<RecordType extends Record> extends RecordImporter implements LibrisXMLConstants {

	ArrayList<FilteringFieldImporter> fieldImporters;
	public FilteringRecordImporter(LibrisDatabase db, LibrisXmlFactory fact, FileAccessManager librisImportFileMgr) throws InputException {
		super(db);
		try {
			fieldImporters = new ArrayList<FilteringFieldImporter>();
			InputStreamReader xmlFileReader = new InputStreamReader(librisImportFileMgr.getIpStream());
			String initialElementName = LibrisXMLConstants.XML_LIBRISIMPORT_TAG;
			ElementManager mgr = fact.makeElementManager(xmlFileReader, 
					librisImportFileMgr.getPath(), initialElementName, new XmlShapes(SHAPE_LIST.IMPORTER_SHAPES));
			mgr.parseOpenTag();
			for (ElementManager fmgr: mgr) {
				FilteringFieldImporter fi = new FilteringFieldImporter(fmgr, db.getSchema());
				fieldImporters.add(fi);
			}
			mgr.parseClosingTag();
		} catch (FileNotFoundException e) {
			throw new InputException(librisImportFileMgr.getPath(), e);
		} catch (DatabaseException e) {
			throw new InputException(librisImportFileMgr.getPath(), e);
		} catch (XmlException e) {
			throw new InputException(librisImportFileMgr.getPath(), e);
		}
	}
	@Override
	public Record importRecord(FieldValueStringList[] fields)
	throws LibrisException {
		Record rec = db.newRecord();
		for (FilteringFieldImporter fi: fieldImporters) {
			fi.addFieldValues(rec, fields);
		}
		return rec;
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

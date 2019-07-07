package org.lasalledebain.libris.indexes;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;

import org.lasalledebain.libris.FileAccessManager;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.RecordFactory;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.InternalError;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.xmlUtils.ElementManager;
import org.lasalledebain.libris.xmlUtils.LibrisXmlFactory;
import org.lasalledebain.libris.xmlUtils.XmlShapes;
import org.lasalledebain.libris.xmlUtils.XmlShapes.SHAPE_LIST;

class JournalFileRecordIterator<RecordType extends Record> implements Iterator<RecordType> {

	private ElementManager dbMgr, recsMgr;
	RecordFactory<RecordType> recFactory;
	private InputStream ipStream;
	private FileAccessManager fileMgr;

	public JournalFileRecordIterator(FileAccessManager theMgr, RecordFactory<RecordType> theFactory) throws InputException, DatabaseException {
		LibrisXmlFactory xmlFactory = new LibrisXmlFactory();
		try {
			fileMgr = theMgr;
			ipStream = fileMgr.getIpStream();
			dbMgr = xmlFactory.makeLibrisElementManager(new InputStreamReader(ipStream), 
					fileMgr.getPath(), LibrisJournalFileManager.XML_LIBRIS_TAG, new XmlShapes(SHAPE_LIST.DATABASE_SHAPES));
			dbMgr.parseOpenTag(); /* database */
			recsMgr = dbMgr.nextElement();
			recsMgr.parseOpenTag();
			recFactory = theFactory;
		} catch (IOException e) {
			throw new InputException("exception opening journal file", e);
		}
	}

	@Override
	public boolean hasNext() {
		boolean tempHasNext = recsMgr.hasNext();
		if (!tempHasNext) {
			try {
				fileMgr.releaseIpStream(ipStream);
			} catch (IOException e) {}
		}
		return tempHasNext;
	}

	@Override
	public RecordType next() {
		try {
			ElementManager recMgr = recsMgr.nextElement();
			RecordType rec = recFactory.makeRecord(true);
			rec.fromXml(recMgr);
			return rec;
		} catch (LibrisException e) {
			throw new InternalError("exception in journal iterator", e);
		}
	}

	@Override
	public void remove() {
		return;
	}

}
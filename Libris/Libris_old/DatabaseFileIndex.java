package Libris;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Iterator;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import Libris.LibrisException.ErrorIds;

public class DatabaseFileIndex extends DatabaseFile {
	/*
 * 
 * the file contains the byte offset of each record in the file.  
 * The final value is the number of records
 */
	private File rpFile;
	private RandomAccessFile recordPositionFile;
	private int recCount;
	public DatabaseFileIndex(File dbFile, LibrisDatabase database) {
		super(dbFile, database);
		rpFile = getRPFile();
	}
	private int lastPos;
	public int[] recordPositions;
	public String errorText;
	private ArrayList<Integer> rpTemp;
	protected boolean readDatabase(LibrisSchema schema) throws LibrisException  {
		recCount = 0;
		try {
			recordPositionFile = new RandomAccessFile(rpFile, "rw");
		} catch(IOException e) {
			throw new LibrisException(ErrorIds.ERR_NO_RECORD_POSITION_FILE, rpFile.toString()+" for writing:"+e.toString());
		}
		return(super.readDatabase());
	}
	public void startElement(String namespaceURI, String lName, // local name
			String qName, // qualified name
			Attributes attrs) throws SAXException {
		if (qName.equals("records")) {
			recCount = 0;
			rpTemp = new ArrayList<Integer>(1000);
			lastPos = lFileReader.position;
			try {
				recordPositionFile.writeInt(recCount);
			} catch (IOException e) {
				dbFileError(e, "DFI42");
			}
		} else if (qName.equals("record")) {
			try {
				recordPositionFile.writeInt(lastPos);
				rpTemp.add(recCount, lastPos);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.exit(1);
			}
		}
	}

	public void endElement(String namespaceURI, String sName, // simple name
			String qName // qualified name
	) throws SAXException {
		if (qName.equals("record")) {
			recCount++;
			lastPos = lFileReader.position;
		} else if (qName.equals("records")) {
			try {
				recordPositionFile.writeInt(lFileReader.position); // Write the end of the records
				recordPositionFile.seek(0);
				recordPositionFile.writeInt(recCount);
			} catch (IOException e) {
				dbFileError(e,"DFI64: error writing recCount");
			}
			recordPositions = new int[recCount+1];
			for (int c = 0; c < recCount; ++c) {
				recordPositions[c] = rpTemp.get(c);
			}
			recordPositions[recCount] = lastPos;
			throw(new SAXException(stopXMLParse));
		}
	}

	public boolean readRecordPositions() throws LibrisException {
		try {
			recordPositionFile = new RandomAccessFile(rpFile, "rw");
			recCount = recordPositionFile.readInt();
			recordPositions = new int[recCount+1];  // add a dummy record at the end to 
			// hold the end of the last record
			for (int i=0; i <= recCount; ++i) {
				recordPositions[i] = recordPositionFile.readInt();
			}
			return(super.readDatabase());
		} catch (IOException e) {
			throw new LibrisException(ErrorIds.ERR_READ_RECORD_POSITION, getRPFile().getPath());
		}
	}
	public int getRecCount() {
		return recCount;
	}
	public int getRecordStartPosition(int recNum) throws LibrisException {
		if (recNum >= recCount) {
			throw new LibrisException(ErrorIds.ERR_NO_START_POS,Integer.toString(recNum));
		}
		return recordPositions[recNum];
	}

}

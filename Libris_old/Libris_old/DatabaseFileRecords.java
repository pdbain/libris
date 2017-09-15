/*
 * Created on Jun 30, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package Libris;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.io.File;
import java.io.IOException;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;


/**
 * @author pdbain
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class DatabaseFileRecords extends DatabaseFile {

	private RecordList recordList;
	private XMLReader myReader;
	
	private RandomAccessFile recordFileReader;
	public DatabaseFileRecords(File dbFile, LibrisDatabase database, RecordList recordList) {
		super(dbFile, database);
		this.recordList = recordList;
		try {
			recordFileReader = new RandomAccessFile(dbFile, "r");
		} catch (FileNotFoundException e) {
			System.err.println("Could not read "+dbFile.getPath());
			e.printStackTrace();
		}
		DefaultHandler handler = this;
		XMLReader myReader;
		try {
			myReader = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
			myReader.setContentHandler(handler);
		} catch (SAXException e) {
			System.err.println("Could not create XML reader");
			e.printStackTrace();
		}
		record = null;
	}
	public DatabaseFileRecords(File currentDBFile, LibrisDatabase database) {
		this(currentDBFile, database, null);
	}
	private LibrisRecord record;
	private LibrisRecordField field;
	//	private byte [] recordBuffer;
	InputSource iSrc = null;
	private byte[] recordBuffer;
	
	protected boolean readDatabase(int startPos, int len) {
 
		try {
			recordBuffer = new byte[len];
			recordFileReader.seek(startPos);
			recordFileReader.readFully(recordBuffer);
			
			ByteArrayInputStream stream = new ByteArrayInputStream(recordBuffer);
			InputSource source = new InputSource(stream);
			XMLReader myReader = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
			myReader.setContentHandler(this);
			myReader.parse(source);
			// myReader.parse( new InputSource(new FileReader(filename)));
		} catch (Throwable t) {
        	logMsg("Exception:"+t.toString());
            if ((t.getMessage() == null ) || !t.getMessage().equals(stopXMLParse)) {
            		t.printStackTrace();
            		System.exit(1);
            }
        }
		return(true);
	}

	public void startElement(String namespaceURI, String lName, // local name
			String qName, // qualified name
			Attributes attrs) throws SAXException {
	   	if (qName.equals("record")) {
			record = new LibrisRecord(database, schema);
		} else if (qName.equals("field")) {
			try {
				field = record.addField(attrs.getValue("id"));
				String fieldValue = attrs.getValue("value");
				valuebuffer=fieldValue;
			} catch (Exception e) {
				throw new SAXException(illegalFieldType);
			}
			gather=true;
		}
	}

	public void endElement(String namespaceURI, String sName, // simple name
			String qName // qualified name
	) throws SAXException {
	   	if (qName.equals("field")) {
			try {
				field.setValue(valuebuffer);
			} catch (Exception e) {
				new SAXException(illegalFieldType);
			}
			valuebuffer = "";
			gather=false;
		} else if (qName.equals("record")) {
			if (recordList != null)
					recordList.addrecord(record);
			// System.out.println("DEBUG end element at "+Integer.toString(lFileReader.position)+"\n");
			// throw(new SAXException(stopXMLParse)); // DEBUG
		
	} else if (qName.equals("records")) {
		recordList.addrecord(record);
		logMsg("DEBUG end records at "+Integer.toString(lFileReader.position)+"\n");
		throw(new SAXException(stopXMLParse));
	}
	}

	public LibrisRecord readRecord(int recNum) throws LibrisException {
		int pos;
		pos = database.getRecordStartPosition(recNum);
		int nextPos = database.fileIndex.recordPositions[recNum+1];
		readDatabase(pos, nextPos-pos);
		return(record);
	}

}
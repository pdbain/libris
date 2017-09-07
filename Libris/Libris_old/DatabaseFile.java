/*
 * Created on Dec 23, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package Libris;

import java.io.File;
import java.io.IOException;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;
public class DatabaseFile  extends DefaultHandler{

	LibrisDatabase database;
	protected LibrisSchema schema;
	private File dbFile;
	public static final String stopXMLParse = "End of parsing", illegalFieldType = "Illegal field type";
	protected String valuebuffer;
	public LibrisIO lFileReader;
	protected boolean gather = false;
	static int verbosity = 0;

	public DatabaseFile(File dbFile, LibrisDatabase database) {
		this.database = database;
		this.schema = database.getSchema();
		this.dbFile = dbFile;
}

	protected void dbFileError(IOException e, String msg) {
		System.err.println("Error accessing database support files: "+msg);
		e.printStackTrace();
		
	}

	protected File getRPFile() {
		String temp = dbFile.getName();
		temp = "."+temp+"_rp";
		File rpFile = new File(dbFile.getParent(), temp);
		return(rpFile);
	}
	
	protected File getMaskFile(int level) {
		String temp = dbFile.getName();
		temp = "."+temp+"_msk"+Integer.toString(level);
		File mskFile = new File(dbFile.getParent(), temp);
		return(mskFile);
	}
	
	protected boolean readDatabase() {
		DefaultHandler handler = this;
        try {
            lFileReader = new LibrisIO(dbFile);
           // Parse the input
        		XMLReader myReader = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
        		myReader.setContentHandler(handler);
        		myReader.parse( new InputSource(lFileReader));
        		// myReader.parse( new InputSource(new FileReader(filename)));
        		} catch (Throwable t) {
        	logMsg("Exception:"+t.toString());
            if ((t.getMessage() == null ) || !t.getMessage().equals(stopXMLParse)) {
            		t.printStackTrace();
            }
        }
		return(true);
	}
	
//===========================================================
    // SAX DocumentHandler methods
    //===========================================================

    protected void logMsg(String msg) {
		LibrisMain.logMsg(verbosity, msg);
			
	}

	public void startDocument()
    throws SAXException
    {
    }

    public void endDocument()
    throws SAXException
    {
    	
    }

    public void startElement(String namespaceURI,
                             String lName, // local name
                             String qName, // qualified name
                             Attributes attrs)
    throws SAXException
    {
     }

    public void endElement(String namespaceURI,
                           String sName, // simple name
                           String qName  // qualified name
                          )
    throws SAXException
    {
 
    }

	public void characters(char buf[], int offset, int len) throws SAXException {
		String s = new String(buf, offset, len);
		if (gather){
			if (valuebuffer == null)
				valuebuffer = new String();
			valuebuffer += s;
		}
	}
    

}

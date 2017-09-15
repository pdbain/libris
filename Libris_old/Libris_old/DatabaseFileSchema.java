/*
 * Created on Jun 26, 2005
 *
 */
package Libris;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileWriter;

/**
 * @author pdbain
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class DatabaseFileSchema extends DatabaseFile {

	
	// private LibrisSchema schema;
	private String dbFileName;
	private FileWriter recordPositions;

	public DatabaseFileSchema(File dbFile, LibrisDatabase database) {
		super(dbFile, database);
		readDatabase();
	}
  public void startElement(String namespaceURI, String lName, // local name
			String qName, // qualified name
			Attributes attrs) throws SAXException {
 		if (qName.equals("fielddef")) {
			schema.AddField(attrs);
		}
 		if (qName.equals("enumset")) {
			try {
				schema.newEnumSet(attrs);
			} catch (LibrisException e) {
				throw new SAXException(e.getMessage());
			}
		}
 		if (qName.equals("enumchoice")) {
			try {
				schema.addEnumChoice(attrs);
			} catch (LibrisException e) {
				throw new SAXException(e.getMessage());
			}
		}
	}

public void endElement(String namespaceURI,
          String sName, // simple name
          String qName  // qualified name
         )
throws SAXException
{
	logMsg("end element at "+Integer.toString(lFileReader.position)+"\n");
	if (qName.equals("schema")) {
		// debug System.out.println("stopXMLParse: "+stopXMLParse);
		throw(new SAXException(stopXMLParse));
	}
	if (qName.equals("enumset")) {
		try {
			schema.endEnumSet();
		} catch (LibrisException e) {
			throw new SAXException(e.getMessage());
		}
	}
}


}

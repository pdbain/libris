package org.lasalledebain;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import javax.xml.stream.Location;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.lasalledebain.libris.EnumFieldChoices;
import org.lasalledebain.libris.RecordTemplate;
import org.lasalledebain.libris.Schema;
import org.lasalledebain.libris.XmlSchema;
import org.lasalledebain.libris.util.Utilities;

import junit.framework.TestCase;

public class SchemaTests extends TestCase{

	private static final String ENUM_NUMBER_STRINGS = "ENUM_numberStrings";

	public void testXmlLineNumbers () {
		File testDir = Utilities.getTestDataDirectory();
		File inputFile = new File(testDir, "schema.xml");
		XMLInputFactory fact = XMLInputFactory.newInstance();
		FileInputStream xmlInput;
		try {
			xmlInput = new FileInputStream(inputFile);
			XMLStreamReader xmlReader = fact.createXMLStreamReader(xmlInput);
			xmlReader.next();
			Location loc = xmlReader.getLocation();
			@SuppressWarnings("unused")
			int line = loc.getLineNumber();
			@SuppressWarnings("unused")
			int co = loc.getCharacterOffset();
		} catch (FileNotFoundException | XMLStreamException e) {
			e.printStackTrace();
			fail("Unexpected exception");
		}

	}
	public void testFromXml() {
		File testDir = Utilities.getTestDataDirectory();
		File inputFile = new File(testDir, "schema.xml");
		try {
			XmlSchema.loadSchema(inputFile);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Unexpected exception "+e);
		}
	}

	public void testEnumDefs() {
		File testDir = Utilities.getTestDataDirectory();
		File inputFile = new File(testDir, Utilities.TEST_SCHEMA_ENUMDEFS_XML_FILE);
		try {
			Schema s = XmlSchema.loadSchema(inputFile);
			RecordTemplate.templateFactory(s);
			EnumFieldChoices efc = s.getEnumSet(ENUM_NUMBER_STRINGS);
			assertNotNull(ENUM_NUMBER_STRINGS+" not found in schema", efc);
			String[] choices = efc.getChoices();
			Utilities.trace(ENUM_NUMBER_STRINGS+" elements: ");
			for (String c: choices) {
				Utilities.trace("\""+c+"\", ");
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
	
	public void testBadSchema() {

		File testDir = Utilities.getTestDataDirectory();
		File inputFile = new File(testDir, "badSchema.xml");
		boolean exceptionThrown = false;
		try {
			Schema s = XmlSchema.loadSchema(inputFile);
			RecordTemplate.templateFactory(s);
		} catch (Exception e) {
			assertTrue(e.getMessage().contains("line 4 element \"fielddef\" : fielddef missing attribute: id"));
			exceptionThrown = true;
		}
		assertTrue("InputDataException not thrown", exceptionThrown);
	}
}

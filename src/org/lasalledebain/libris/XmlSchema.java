package org.lasalledebain.libris;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;

import javax.xml.namespace.QName;

import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.exception.XmlException;
import org.lasalledebain.libris.index.GroupDef;
import org.lasalledebain.libris.index.GroupDefs;
import org.lasalledebain.libris.index.IndexDefs;
import org.lasalledebain.libris.xmlUtils.ElementManager;
import org.lasalledebain.libris.xmlUtils.ElementReader;
import org.lasalledebain.libris.xmlUtils.LibrisAttributes;
import org.lasalledebain.libris.xmlUtils.LibrisXMLConstants;
import org.lasalledebain.libris.xmlUtils.LibrisXmlFactory;
import org.lasalledebain.libris.xmlUtils.XmlShapes;
import org.lasalledebain.libris.xmlUtils.XmlShapes.SHAPE_LIST;

public class XmlSchema extends Schema {

	public XmlSchema(ElementReader rdr) throws LibrisException {
		super();
		fromXml(rdr);
	}
	public XmlSchema(ElementManager mgr) throws DatabaseException, InputException {
		super();
		fromXml(mgr);
	}

	public void fromXml(ElementReader xmlReader) throws LibrisException {
		ElementManager schemaManager = new ElementManager(xmlReader, new QName(LibrisXMLConstants.XML_SCHEMA_TAG),
				new XmlShapes(SHAPE_LIST.DATABASE_SHAPES));
		fromXml(schemaManager);
	}

	public void fromXml(ElementManager schemaManager) throws DatabaseException, InputException
	{
		xmlAttributes = schemaManager.parseOpenTag(getXmlTag());
		if (!schemaManager.hasNext()) {
			throw new XmlException(schemaManager, "<schema> cannot be empty");
		}
		ElementManager groupDefsManager = schemaManager.nextElement(XML_GROUPDEFS_TAG);
		myGroupDefs = new GroupDefs(groupDefsManager);
		for (GroupDef gd: myGroupDefs) {
			addField(gd);
		}

		ElementManager fieldDefsManager = schemaManager.nextElement(XML_FIELDDEFS_TAG);
		parseFieldDefs(fieldDefsManager);

		ElementManager indexDefsManager = schemaManager.nextElement(XML_INDEXDEFS_TAG);
		myIndexDefs = new IndexDefs(this,indexDefsManager);

		schemaManager.parseClosingTag();
	}

	protected void parseFieldDefs(ElementManager fieldDefsManager)
	throws DatabaseException, InputException {
		fieldDefsManager.parseOpenTag();
		while (fieldDefsManager.hasNext()) {
			String nextId;
			nextId = fieldDefsManager.getNextId();
			if (nextId.equals(EnumFieldChoices.getXmlTag())) {
				ElementManager enumSetManager = fieldDefsManager.nextElement();
				EnumFieldChoices c = EnumFieldChoices.fieldChoicesFactory(enumSetManager);
				String cId = c.getId();
				if (enumSets.containsKey(cId)) {
					throw new XmlException(enumSetManager, "duplicate enumset "+cId);
				}
				addEnumSet(cId, c);					
			} else {
				break;
			}
		}
		while (fieldDefsManager.hasNext()) {
			String nextId;
			nextId = fieldDefsManager.getNextId();
			if (nextId.equals(LibrisXMLConstants.XML_FIELDDEF_TAG)) {
				ElementManager fieldDefManager = fieldDefsManager.nextElement();
				FieldTemplate f = new FieldTemplate(this);
				f.fromXml(fieldDefManager);
				String fId = f.getFieldId();
				if (fieldNumById.containsKey(fId)) {
					throw new XmlException(fieldDefManager, "duplicate field "+fId);
				}
				addField(f);
			} else {
				throw new XmlException("Unexpected tag "+nextId+" in "+XML_FIELDDEFS_TAG+" section");
			}
		}
		fieldDefsManager.parseClosingTag();
	}
	public static Schema loadSchema(File schemaFile) throws LibrisException  {
		FileReader rdr;
		try {
			rdr = new FileReader(schemaFile);
			LibrisXmlFactory xmlFactory = new LibrisXmlFactory();
			ElementReader xmlReader = xmlFactory.makeReader(rdr, schemaFile.getPath());
			Schema s = new XmlSchema(xmlReader);
			return s;
		} catch (FileNotFoundException e) {
			throw new InputException("Cannot open "+schemaFile.getPath(), e);
		}
	}

}

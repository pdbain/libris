package org.lasalledebain.libris;

import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.exception.XmlException;
import org.lasalledebain.libris.xmlUtils.ElementManager;
import org.lasalledebain.libris.xmlUtils.ElementShape;
import org.lasalledebain.libris.xmlUtils.ElementWriter;
import org.lasalledebain.libris.xmlUtils.LibrisAttributes;
import org.lasalledebain.libris.xmlUtils.XmlShapes;

public class DatabaseInstance implements XmlImportable, XmlExportable {
	private String selfDatabaseId;
	private String parentDatabaseId;
	private String startingRecordId;
	
	public static String getXmlTag() {
		return XML_GROUPDEFS_TAG;
	}
	
	@Override
	public void fromXml(ElementManager mgr) throws LibrisException {
		mgr.parseOpenTag();
		LibrisAttributes attrs = mgr.getElementAttributes();
		selfDatabaseId = attrs.get(XML_INSTANCE_SELFID_ATTR);
		parentDatabaseId = attrs.get(XML_INSTANCE_PARENTID_ATTR);
		startingRecordId = attrs.get(XML_INSTANCE_STARTRECID_ATTR);
		mgr.parseClosingTag();
	}

	@Override
	public void toXml(ElementWriter output) throws LibrisException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public LibrisAttributes getAttributes() throws XmlException {
		// TODO Auto-generated method stub
		return null;
	}

	static public ElementShape getXmlShape() {
		return XmlShapes.makeShape(getXmlTag(),
				new String [] {}, new String [] 
						{XML_INSTANCE_SELFID_ATTR, XML_INSTANCE_STARTRECID_ATTR}, 
						new String [][] {{XML_INSTANCE_PARENTID_ATTR}, {""}}, false);
	}

	public String getSelfDatabaseId() {
		return selfDatabaseId;
	}

	public String getParentDatabaseId() {
		return parentDatabaseId;
	}

	public String getStartingRecordId() {
		return startingRecordId;
	}

}

package org.lasalledebain.libris;

import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.exception.XmlException;
import org.lasalledebain.libris.xmlUtils.ElementManager;
import org.lasalledebain.libris.xmlUtils.ElementShape;
import org.lasalledebain.libris.xmlUtils.LibrisAttributes;
import org.lasalledebain.libris.xmlUtils.XmlShapes;

public class DatabaseInstance extends LibrisElement {
	private String selfDatabaseId;
	private String parentDatabaseId;
	private int startingRecordId;
	
	public static String getXmlTag() {
		return XML_GROUPDEFS_TAG;
	}
	
	@Override
	public void fromXml(ElementManager mgr) throws LibrisException {
		mgr.parseOpenTag();
		LibrisAttributes attrs = mgr.getElementAttributes();
		selfDatabaseId = attrs.get(XML_INSTANCE_SELFID_ATTR);
		parentDatabaseId = attrs.get(XML_INSTANCE_PARENTID_ATTR);
		if (null == parentDatabaseId) {
			parentDatabaseId = "";
		}
		String startingRecordIdString = attrs.get(XML_INSTANCE_STARTRECID_ATTR);
		if (null == startingRecordIdString) {
			startingRecordId = LibrisConstants.NULL_RECORD_ID;
		} else {
			startingRecordId = Integer.parseInt(startingRecordIdString);
		}

		mgr.parseClosingTag();
	}

	// TODO recordInstance: make records < startingRecordId read-only

	@Override
	public LibrisAttributes getAttributes() throws XmlException {
		LibrisAttributes attrs = new LibrisAttributes(
				new String[][] {{XML_INSTANCE_SELFID_ATTR, selfDatabaseId},
					{XML_INSTANCE_PARENTID_ATTR, parentDatabaseId},
					{XML_INSTANCE_STARTRECID_ATTR, Integer.toString(startingRecordId)}}
				);
		return attrs;
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

	public int getStartingRecordId() {
		return startingRecordId;
	}

	@Override
	public String getElementTag() {
		return getXmlTag();
	}

}

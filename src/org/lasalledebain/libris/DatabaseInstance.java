package org.lasalledebain.libris;

import java.util.Date;
import java.util.Objects;

import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.XmlException;
import org.lasalledebain.libris.xmlUtils.ElementManager;
import org.lasalledebain.libris.xmlUtils.ElementShape;
import org.lasalledebain.libris.xmlUtils.LibrisAttributes;
import org.lasalledebain.libris.xmlUtils.XmlShapes;

public class DatabaseInstance extends LibrisElement {
	private int recordIdBase;
	private Date forkDate;
	private Date joinDate;
	public DatabaseInstance() {
		super();
	}

	public DatabaseInstance(LibrisMetadata metaData) {
		recordIdBase = metaData.getLastRecordId();
		forkDate = LibrisMetadata.getCurrentDate();
		joinDate = null;
	}

	/**
	 * Get the ID of the last record in the root database at the time of the fork.
	 * @return record ID
	 */
	public int getRecordIdBase() {
		return recordIdBase;
	}

	public Date getForkDate() {
		return forkDate;
	}

	public boolean isJoined() {
		return !Objects.isNull(joinDate);
	}

	
	@Override
	public void fromXml(ElementManager mgr) throws InputException {
		mgr.parseOpenTag();
		LibrisAttributes attrs = mgr.getElementAttributes();
		forkDate = LibrisAttributes.parseDate(attrs.get(XML_INSTANCE_FORKDATE_ATTR));
		joinDate = LibrisAttributes.parseDate(attrs.get(XML_INSTANCE_JOINDATE_ATTR));
		String startingRecordIdString = attrs.get(XML_INSTANCE_BASERECID_ATTR);
		if (null == startingRecordIdString) {
			recordIdBase = LibrisConstants.NULL_RECORD_ID;
		} else {
			recordIdBase = Integer.parseInt(startingRecordIdString);
		}

		mgr.parseClosingTag();
	}

	// TODO recordInstance: make records < startingRecordId read-only

	@Override
	public LibrisAttributes getAttributes() throws XmlException {
		LibrisAttributes attrs = new LibrisAttributes(
				new String[][] {
					{XML_INSTANCE_BASERECID_ATTR, Integer.toString(recordIdBase)},
					{XML_INSTANCE_FORKDATE_ATTR, LibrisMetadata.formatDate(forkDate)}
					});
		if (null != joinDate) {
			attrs.setAttribute(XML_INSTANCE_JOINDATE_ATTR, LibrisMetadata.formatDate(joinDate));
		}
		return attrs;
	}

	public Date getJoinDate() {
		return joinDate;
	}

	public void doJoin() {
		joinDate = LibrisMetadata.getCurrentDate();
	}

	public static String getXmlTag() {
		return XML_INSTANCE_TAG;
	}

	static public ElementShape getXmlShape() {
		return XmlShapes.makeShape(getXmlTag(),
				new String [] {}, new String [] 
						{XML_INSTANCE_BASERECID_ATTR, XML_INSTANCE_FORKDATE_ATTR}, 
						new String [][] {{XML_INSTANCE_JOINDATE_ATTR, ""}}, false);
	}

	@Override
	public String getElementTag() {
		return getXmlTag();
	}
}

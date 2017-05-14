package org.lasalledebain.libris.index;

import org.lasalledebain.libris.XmlExportable;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.exception.XmlException;
import org.lasalledebain.libris.xmlUtils.ElementWriter;
import org.lasalledebain.libris.xmlUtils.LibrisAttributes;
import org.lasalledebain.libris.xmlUtils.LibrisXMLConstants;

public class Group implements XmlExportable, LibrisXMLConstants {

	public static final int NULL_GROUP = -1;
	@Override
	public LibrisAttributes getAttributes() throws XmlException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void toXml(ElementWriter output) throws LibrisException {
		// TODO Auto-generated method stub

	}

}

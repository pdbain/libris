package org.lasalledebain.libris;

import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.exception.XmlException;
import org.lasalledebain.libris.xmlUtils.ElementWriter;
import org.lasalledebain.libris.xmlUtils.LibrisAttributes;
import org.lasalledebain.libris.xmlUtils.LibrisXMLConstants;

public interface XmlExportable extends LibrisXMLConstants {
	public abstract void toXml(ElementWriter output) throws LibrisException;
	public abstract LibrisAttributes getAttributes() throws XmlException;
}

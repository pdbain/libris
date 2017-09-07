package org.lasalledebain.libris;

import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.xmlUtils.ElementManager;
import org.lasalledebain.libris.xmlUtils.LibrisXMLConstants;

public interface XmlImportable extends LibrisXMLConstants {
	public abstract void fromXml(ElementManager mgr) throws LibrisException;
}

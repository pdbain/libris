package org.lasalledebain.libris.ui;

import org.lasalledebain.libris.XMLElement;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.exception.XmlException;
import org.lasalledebain.libris.xmlUtils.ElementManager;
import org.lasalledebain.libris.xmlUtils.ElementWriter;
import org.lasalledebain.libris.xmlUtils.LibrisAttributes;

public abstract class LibrisLayout implements XMLElement {

	public static String getXmlTag() {
		return XML_LAYOUT_TAG;
	}

	protected String id;
	protected String title = null;

	public abstract String getLayoutType();

	@Override
	public abstract void fromXml(ElementManager mgr) throws LibrisException;

	@Override
	public abstract void toXml(ElementWriter output) throws LibrisException;

	@Override
	public LibrisAttributes getAttributes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getElementTag() {
		return getXmlTag();
	}

	public String getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

}

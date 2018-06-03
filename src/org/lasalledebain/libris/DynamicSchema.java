package org.lasalledebain.libris;

import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.exception.UserErrorException;
import org.lasalledebain.libris.xmlUtils.ElementManager;

public class DynamicSchema extends Schema {
	@Override
	public void fromXml(ElementManager mgr) throws LibrisException {
		throw new UserErrorException("fromXml not supported");
	}

}

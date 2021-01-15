package org.lasalledebain.libris.util;

import org.lasalledebain.libris.Schema;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.exception.UserErrorException;
import org.lasalledebain.libris.xmlUtils.ElementManager;

public class MockSchema extends Schema {

	@Override
	public void fromXml(ElementManager mgr) throws LibrisException {
		throw new UserErrorException("fromXml undefined");
	}

}

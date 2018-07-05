package org.lasalledebain.libris;

import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.exception.UserErrorException;
import org.lasalledebain.libris.index.GroupDefs;
import org.lasalledebain.libris.index.IndexDefs;
import org.lasalledebain.libris.xmlUtils.ElementManager;

public class DynamicSchema extends Schema {
	public DynamicSchema() {
		super();
		myGroupDefs = new GroupDefs();
		myIndexDefs = new IndexDefs(this);

	}

	@Override
	public void fromXml(ElementManager mgr) throws LibrisException {
		throw new UserErrorException("fromXml not supported");
	}

}

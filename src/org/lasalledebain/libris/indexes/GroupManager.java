package org.lasalledebain.libris.indexes;

import org.lasalledebain.libris.GenericDatabase;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.index.GroupDefs;

public class GroupManager {
	private GenericDatabase database;
	GroupDefs defs;

	public GroupManager(GenericDatabase db) {
		database = db;
	}

	public GroupDefs getDefs() {
		return defs;
	}

	public void setDefs(GroupDefs defs) {
		this.defs = defs;
	}

	public Record getRecord(int parentId) throws LibrisException {
		return database.getRecord(parentId);
	}
}

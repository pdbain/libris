package org.lasalledebain.libris.indexes;

import org.lasalledebain.libris.LibrisDatabase;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.RecordId;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.index.GroupDefs;

public class GroupManager {
	private LibrisDatabase database;
	GroupDefs defs;

	public GroupManager(LibrisDatabase db) {
		database = db;
	}

	public GroupDefs getDefs() {
		return defs;
	}

	public void setDefs(GroupDefs defs) {
		this.defs = defs;
	}

	public Record getRecord(RecordId parentId) throws LibrisException {
		return database.getRecord(parentId);
	}

}

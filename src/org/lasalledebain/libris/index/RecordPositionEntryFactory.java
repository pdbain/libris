package org.lasalledebain.libris.index;

import org.lasalledebain.libris.hashfile.EntryFactory;
import org.lasalledebain.libris.hashfile.FixedSizeEntryFactory;

public class RecordPositionEntryFactory implements FixedSizeEntryFactory<RecordPositionEntry>{

	@Override
	public RecordPositionEntry makeEntry() {
		return new RecordPositionEntry();
	}

	public RecordPositionEntry makeEntry(int recordId, long position) {
		return new RecordPositionEntry(recordId, position);
	}

	@Override
	public int getEntrySize() {
		return RecordPositionEntry.getRecordPositionEntryLength();
	}

}

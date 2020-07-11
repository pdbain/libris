package org.lasalledebain.libris.ui;

import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.Schema;

public abstract class MultiRecordLayout<RecordType extends Record>  extends LibrisSwingLayout<RecordType> {

	public MultiRecordLayout(Schema schem) {
		super(schem);
	}
	
	@Override
	public boolean isSingleRecord() {
		return false;
	}

}

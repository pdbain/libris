package org.lasalledebain.libris.ui;

import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.Schema;
import org.lasalledebain.libris.exception.DatabaseException;

public abstract class MultiRecordLayout<RecordType extends Record>  extends Layout<RecordType> {

	public MultiRecordLayout(Schema schem) throws DatabaseException {
		super(schem);
	}
	
	@Override
	public boolean isSingleRecord() {
		return false;
	}

}

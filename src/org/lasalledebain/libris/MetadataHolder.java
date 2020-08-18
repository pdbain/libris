package org.lasalledebain.libris;

import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.ui.Layouts;
import org.lasalledebain.libris.xmlUtils.ElementManager;
import org.lasalledebain.libris.xmlUtils.ElementWriter;

public class MetadataHolder<RecordType extends Record> extends LibrisMetadata<RecordType> {
	Schema mySchema;
	Layouts<RecordType> myLayouts;
	public MetadataHolder(LibrisDatabase database) {
		super(database);
	}

	public MetadataHolder(Schema theSchema, Layouts<RecordType> theLayouts) {
		mySchema = theSchema;
		myLayouts = theLayouts;
	}

	@Override
	public void fromXml(ElementManager mgr) throws LibrisException {
		throw new DatabaseException("Not implemented");
	}

	@Override
	protected void writeContents(ElementWriter output) throws LibrisException {
		mySchema.toXml(output);
		myLayouts.toXml(output);
	}

}

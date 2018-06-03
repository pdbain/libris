package org.lasalledebain.libris;

import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.ui.Layouts;
import org.lasalledebain.libris.xmlUtils.ElementManager;

public class XmlMetadata extends LibrisMetadata {
	private boolean schemaInline;

	public XmlMetadata(LibrisDatabase database) {
		super(database);
	}
	public void fromXml(ElementManager metadataMgr) throws InputException, DatabaseException {
		ElementManager schemaMgr;

		metadataMgr.parseOpenTag();
		schemaMgr = metadataMgr.nextElement();
		Schema schem = new XmlSchema(schemaMgr);
		database.setSchema(schem);
		uiLayouts = new Layouts(schem);
		ElementManager layoutsMgr = metadataMgr.nextElement();
		uiLayouts.fromXml(layoutsMgr);
		metadataMgr.parseClosingTag();
	}
	public void setSchemaInline(boolean schemaInline) {
		this.schemaInline = schemaInline;
	}

	public boolean isSchemaInline() {
		return schemaInline;
	}

}

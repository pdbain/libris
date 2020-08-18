package org.lasalledebain.libris;

import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.ui.Layouts;
import org.lasalledebain.libris.xmlUtils.ElementManager;

public class LibrisDatabaseMetadata extends LibrisMetadata<DatabaseRecord> {
	private boolean hasDocRepo;

	public LibrisDatabaseMetadata(LibrisDatabase database) {
		super(database);
	}
	public void fromXml(ElementManager metadataMgr) throws LibrisException {
		ElementManager schemaMgr;

		metadataMgr.parseOpenTag();
		schemaMgr = metadataMgr.nextElement();
		Schema schem = new XmlSchema(schemaMgr);
		database.setSchema(schem);
		uiLayouts = new Layouts<DatabaseRecord>(schem);
		ElementManager layoutsMgr = metadataMgr.nextElement();
		uiLayouts.fromXml(layoutsMgr);
		metadataMgr.parseClosingTag();
	}

	@Override
	public boolean equals(Object comparand) {
		if (!super.equals(comparand)) {
			return false;
		} else if (!LibrisDatabaseMetadata.class.isAssignableFrom(comparand.getClass())) {
			return false;
		} else {
			LibrisDatabaseMetadata otherMetadat = (LibrisDatabaseMetadata) comparand;
			return uiLayouts.equals(otherMetadat.uiLayouts)
					&& (hasDocRepo == otherMetadat.hasDocRepo);
		}
	}

}

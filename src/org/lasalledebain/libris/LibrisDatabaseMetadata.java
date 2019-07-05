package org.lasalledebain.libris;

import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.ui.Layouts;
import org.lasalledebain.libris.xmlUtils.ElementManager;

public class LibrisDatabaseMetadata extends LibrisMetadata {
	private boolean hasDocRepo;

	public LibrisDatabaseMetadata(LibrisDatabase database) {
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
	public boolean hasDocumentRepository() {
		return hasDocRepo;
	}

	public void hasDocumentRepository(boolean hasRepo) {
		this.hasDocRepo = hasRepo;
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
package org.lasalledebain.libris.ui;

import java.util.ArrayList;

import javax.swing.JPanel;

import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.Schema;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.xmlUtils.LibrisXMLConstants;

public class ListLayout extends Layout {

	public ListLayout(Schema schem) throws DatabaseException {
		super(schem);
	}

	@Override
	public String getLayoutType() {
		return LibrisXMLConstants.XML_LAYOUT_TYPE_LIST;
	}

	@Override
	ArrayList<UiField> layOutFields(Record rec, LibrisWindowedUi ui, JPanel recordPanel, ModificationTracker modTrk)
			throws DatabaseException {
		// TODO write ListLayout.layOutFields
		return null;
	}

}

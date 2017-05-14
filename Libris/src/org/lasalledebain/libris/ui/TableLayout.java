package org.lasalledebain.libris.ui;

import java.util.ArrayList;

import javax.swing.JPanel;

import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.Schema;
import org.lasalledebain.libris.exception.DatabaseException;

public class TableLayout extends Layout {

	public TableLayout(Schema schem) throws DatabaseException {
		super(schem);
	}

	@Override
	public void addBodyField(FieldPositionParameter params)
			throws DatabaseException {
		throw new InternalError(getClass().getName()+" not implemented");
	}

	@Override
	public String getLayoutType() {
		return XML_LAYOUT_TYPE_TABLE;
	}

	@Override
	ArrayList<UiField> layOutFields(Record rec, LibrisWindowedUi ui, JPanel recordPanel,
			ModificationTracker modTrk)
			throws DatabaseException {
		// TODO write TableLayout.layOutFields
		return null;
	}

}

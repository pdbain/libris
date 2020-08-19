package org.lasalledebain.libris.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.http.HttpServletResponse;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.border.TitledBorder;

import org.lasalledebain.libris.DatabaseRecord;
import org.lasalledebain.libris.Field;
import org.lasalledebain.libris.Field.FieldType;
import org.lasalledebain.libris.GenericDatabase;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.RecordList;
import org.lasalledebain.libris.Schema;
import org.lasalledebain.libris.exception.DatabaseError;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.index.GroupDef;
import org.lasalledebain.libris.index.GroupDefs;
import org.lasalledebain.libris.index.GroupMember;
@Deprecated
public class FormLayout<RecordType extends Record> extends LibrisSwingLayout<RecordType> implements LayoutSwingProcessor<RecordType>, LayoutHtmlProcessor<RecordType> {
	@Override
	public boolean isEditable() {
		return true;
	}

	public FormLayout(Schema schem) {
		super(schem);
	}

	@Override
	protected void validate() throws InputException {
		for (LayoutField<RecordType> lf: getFields()) {
			String fid = lf.getId();
			FieldType fType = mySchema.getFieldType(fid);
			if (lf.getControlTypeName().equals(GuiConstants.GUI_ENUMFIELD)
					&& !fType.equals(FieldType.T_FIELD_ENUM)) {
				throw new InputException("Cannot use enumfield layout control for non-enum field "+fid);
			}
		}
	}

	@Override
	public
	ArrayList<UiField> layOutFields(RecordList<RecordType> recList, LibrisWindowedUi<RecordType> ui, JComponent recordPanel,
			ModificationTracker modTrk) throws DatabaseException, LibrisException {
		RecordType rec = recList.getFirstRecord();
		return layOutFields(rec, ui, recordPanel, modTrk);
	}

	@Override
	protected void showRecord(int recId) {
		return;
	}

	@Override
	public void layOutPage(RecordList<RecordType> recList, int recId, LibrisLayout<RecordType> browserLayout,
			DatabaseUi<RecordType> ui, HttpServletResponse resp) throws InputException, IOException {
		throw new DatabaseError("not implemented");
		// TODO Auto-generated method stub
		
	}

	@Override
	public void layoutDisplayPanel(RecordList<RecordType> recList, int recId, StringBuffer buff) throws InputException {
		throw new DatabaseError("not implemented");
		// TODO Auto-generated method stub
		
	}

}

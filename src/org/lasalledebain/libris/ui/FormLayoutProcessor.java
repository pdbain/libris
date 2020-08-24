package org.lasalledebain.libris.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.border.TitledBorder;

import org.lasalledebain.libris.DatabaseRecord;
import org.lasalledebain.libris.Field;
import org.lasalledebain.libris.GenericDatabase;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.RecordList;
import org.lasalledebain.libris.Schema;
import org.lasalledebain.libris.Field.FieldType;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.index.GroupDef;
import org.lasalledebain.libris.index.GroupDefs;
import org.lasalledebain.libris.index.GroupMember;

public class FormLayoutProcessor<RecordType extends Record> extends LayoutProcessor<RecordType> {

	public FormLayoutProcessor(LibrisLayout<RecordType> theLayout) {
		super(theLayout);
	}

	@Override
	public ArrayList<UiField> layOutFields(RecordType rec, LibrisWindowedUi<RecordType> ui, JComponent recordPanel,
			ModificationTracker modTrk) throws DatabaseException, LibrisException {
		final boolean modifiable = modTrk.isModifiable();
		JComponent fieldPanel = null;
		Schema mySchema = myLayout.getSchema();
		ArrayList<UiField> guiFields = new ArrayList<UiField>();
		int numGroups = mySchema.getNumGroups();
		if ((numGroups > 0) && rec.hasAffiliations()) {
			GroupDefs defs = mySchema.getGroupDefs();
			JPanel groupPanel =  new JPanel(new GridLayout(1, numGroups));
			fieldPanel = new JPanel();
			JSplitPane groupsAndFields = new JSplitPane(JSplitPane.VERTICAL_SPLIT, groupPanel, fieldPanel);
			recordPanel.add(groupsAndFields);
			GenericDatabase<DatabaseRecord> db = ui.getDatabase();
			int groupNum = 0;
			for (GroupDef def: defs) {
				String groupName = def.getFieldTitle();
				TitledBorder affiliateBorder = BorderFactory.createTitledBorder(UiField.LINE_BORDER, groupName);
				Box groupBox = Box.createHorizontalBox();
				groupBox.setBorder(affiliateBorder);
				GuiControl<RecordType> uiField = new NameList<RecordType>(ui, db, rec, def, modifiable);
				JComponent comp = uiField.getGuiComponent();
				GroupMember gm = rec.getMember(groupNum);
				if (null == gm) {
					gm = new GroupMember(defs, def);
					rec.setMember(groupNum, gm);
				}
				SingleControlUiField guiFld = new SingleControlUiField(gm, modTrk);
				guiFld.setControl(uiField);
				groupBox.add(comp);
				groupPanel.add(groupBox);
				guiFields.add(guiFld);
				++groupNum;
			}
		} else {
			fieldPanel = recordPanel;
		}
		GridBagLayout panelLayout = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		fieldPanel.setLayout(panelLayout);
		for (LayoutField<RecordType> fp: myLayout.getFields()) {
			int fieldNum = fp.getFieldNum();
			Field fld = myLayout.getField(rec, fieldNum);
			if (null == fld) {
				continue;
			}
			c.gridx = fp.getHpos(); c.gridy = fp.getVpos();
			c.gridwidth = fp.isCarriageReturn()? GridBagConstraints.REMAINDER: fp.getHspan();
			c.gridheight = fp.getVspan();
			MultipleValueUiField guiFld = GuiControlFactory.makeMultiControlField(fp, fld, modTrk);
			JComponent comp = guiFld.getGuiComponent();
			panelLayout.setConstraints(comp, c);
			fieldPanel.add(comp);
			guiFields.add(guiFld);
		}
		return guiFields;
	}

	@Override
	public void layoutDisplayPanel(RecordList<RecordType> recList, int recId, StringBuffer buff) throws InputException {
		buff.append("<p>Record "+recId+" not found</p>");
	}
	@Override
	protected void validate() throws InputException {
		Schema mySchema = myLayout.getSchema();
		for (LayoutField<RecordType> lf: myLayout.getFields()) {
			String fid = lf.getId();
			FieldType fType = mySchema.getFieldType(fid);
			if (lf.getControlTypeName().equals(GuiConstants.GUI_ENUMFIELD)
					&& !fType.equals(FieldType.T_FIELD_ENUM)) {
				throw new InputException("Cannot use enumfield layout control for non-enum field "+fid);
			}
		}
	}

}

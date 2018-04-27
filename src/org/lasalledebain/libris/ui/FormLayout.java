package org.lasalledebain.libris.ui;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.border.TitledBorder;

import org.lasalledebain.libris.Field;
import org.lasalledebain.libris.Field.FieldType;
import org.lasalledebain.libris.LibrisDatabase;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.Schema;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.index.GroupDef;
import org.lasalledebain.libris.index.GroupDefs;
import org.lasalledebain.libris.index.GroupMember;
import org.lasalledebain.libris.xmlUtils.LibrisXMLConstants;

public class FormLayout extends Layout {
	public FormLayout(Schema schem) throws DatabaseException {
		super(schem);
	}

	@Override
	protected void validate() throws InputException {
		for (FieldInfo fp: getFields()) {
			String fid = fp.getId();
			FieldType fType = mySchema.getFieldType(fid);
			if (fp.getControlTypeName().equals(GuiConstants.GUI_ENUMFIELD)
					&& !fType.equals(FieldType.T_FIELD_ENUM)) {
				throw new InputException("Cannot use enumfield layout control for non-enum field "+fid);
			}
		}
	}

	@Override
	public String getLayoutType() {
		return LibrisXMLConstants.XML_LAYOUT_TYPE_FORM;
	}

	@Override
	ArrayList<UiField> layOutFields(Record rec, LibrisWindowedUi ui, JPanel recordPanel, ModificationTracker modTrk)
	throws LibrisException {
		final boolean modifiable = modTrk.isModifiable();
		JPanel fieldPanel = null;
		ArrayList<UiField> guiFields = new ArrayList<UiField>();
		int numGroups = mySchema.getNumGroups();
		if (numGroups > 0) {
			GroupDefs defs = mySchema.getGroupDefs();
			JPanel groupPanel =  new JPanel(new GridLayout(1, numGroups));
			fieldPanel = new JPanel();
			JSplitPane groupsAndFields = new JSplitPane(JSplitPane.VERTICAL_SPLIT, groupPanel, fieldPanel);
			recordPanel.add(groupsAndFields);
			LibrisDatabase db = ui.getDatabase();
			int groupNum = 0;
			for (GroupDef def: defs) {
				String groupName = def.getFieldTitle();
				TitledBorder affiliateBorder = BorderFactory.createTitledBorder(UiField.LINE_BORDER, groupName);

				Box groupBox = Box.createHorizontalBox();
				groupBox.setBorder(affiliateBorder);
				GuiControl uiField = new NameList(ui, db, rec, def);
				uiField.setEditable(modifiable);
				Component comp = uiField.getGuiComponent();
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
		for (FieldPosition fp: getFields()) {
			int fieldNum = fp.getFieldNum();
			Field fld = getField(rec, fieldNum);
			if (null == fld) {
				continue;
			}
			c.gridx = fp.getHpos(); c.gridy = fp.getVpos();
			c.gridwidth = fp.isCarriageReturn()? GridBagConstraints.REMAINDER: fp.getHspan();
			c.gridheight = fp.getVspan();
			MultipleValueUiField guiFld = GuiControlFactory.makeMultiControlField(fp, fld, modTrk);
			Component comp = guiFld.getGuiComponent();
			panelLayout.setConstraints(comp, c);
			fieldPanel.add(comp);
			guiFields.add(guiFld);
		}
		return guiFields;
	}

}

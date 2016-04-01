package org.lasalledebain.libris.ui;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;

import javax.swing.JPanel;

import org.lasalledebain.libris.Field;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.Schema;
import org.lasalledebain.libris.Field.FieldType;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.xmlUtils.LibrisXMLConstants;


public class FormLayout extends Layout {

	public FormLayout(Schema schem) {
		super(schem);
	}

	@Override
	protected void validate() throws InputException {
		for (FieldPosition fp: getFields()) {
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
	ArrayList<UiField> layOutFields(Record rec, JPanel recordPanel, ModificationTracker modTrk)
			throws DatabaseException {
		GridBagLayout panelLayout = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		recordPanel.setLayout(panelLayout);
		ArrayList<UiField> guiFields = new ArrayList<UiField>();
		for (FieldPosition fp: getFields()) {
			int fieldNum = fp.getFieldNum();
			Field fld = null;
			try {
				fld = rec.getField(fieldNum);
			} catch (InputException e) {
				throw new DatabaseException("Error in layout \""+getId()+"\"", e);
				
			}
			if (null == fld) {
				continue;
			}
			c.gridx = fp.getHpos(); c.gridy = fp.getVpos();
			c.gridwidth = fp.isCarriageReturn()? GridBagConstraints.REMAINDER: fp.getHspan();
			c.gridheight = fp.getVspan();
			UiField guiFld = createControl(fp, fld, modTrk);
			Component comp = guiFld.getGuiComponent();
			panelLayout.setConstraints(comp, c);
			recordPanel.add(comp);
			guiFields.add(guiFld);
		}
		return guiFields;
	}

private UiField createControl(FieldPosition fp, Field fld, ModificationTracker modTrk) throws DatabaseException {
	UiField guiFld = GuiControlFactory.makeControl(fp, fld, modTrk);
	return guiFld;
}

}

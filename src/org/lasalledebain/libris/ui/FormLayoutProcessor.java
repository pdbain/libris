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
import org.lasalledebain.libris.Field.FieldType;
import org.lasalledebain.libris.GenericDatabase;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.RecordList;
import org.lasalledebain.libris.Schema;
import org.lasalledebain.libris.exception.Assertion;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.field.FieldValue;
import org.lasalledebain.libris.index.GroupDef;
import org.lasalledebain.libris.index.GroupDefs;
import org.lasalledebain.libris.index.GroupMember;

public class FormLayoutProcessor<RecordType extends Record> extends LayoutProcessor<RecordType> {

	private static final String FORM_FIELD_GRID_CLASS = "formFieldGrid";
	private static final String RECORD_FIELD_CLASS = "recordField";
	private static final String RECORD_PANEL_CLASS = "recordPanel",
			GROUP_PANEL_CLASS = "groupPanel",
			FIELDS_PANEL_CLASS = "fieldsPanel",
			MULTICONTROL_CELL_CLASS = "multiControlCell",
			FIELD_TITLE_CLASS="fieldTitle",
			FIELD_TEXT_CLASS="fieldText";
	private final String myStyleString;
	public FormLayoutProcessor(LibrisLayout<RecordType> theLayout) {
		super(theLayout);
		myStyleString = makeStyleString();
	}

	private String makeStyleString() {
		StringBuffer buff = new StringBuffer(super.getStyleString());
		buff.append(
				"."+ FORM_FIELD_GRID_CLASS + " {\n" + 
						"  display: grid;\n" + 
						"}\n"
						+ "."+ FIELD_TITLE_CLASS + " {\n" + 
						"float:left;\n" + 
						"font-size: 100%;\n"
						+ "padding-right: 15px;\n"
						+ "font-weight: bold;\n"
						+ "}\n"
						+ "."+ FIELD_TEXT_CLASS + " {\n"
						+ "display:inline;\n"
						+ "}\n"
						+ "."+ MULTICONTROL_CELL_CLASS + " {\n"
						+ GREY_BORDER
						+ "border-collapse: collapse;\n"
						+ "}\n"
						+ "."+ FIELDS_PANEL_CLASS + " {\n" + 
						BACKGROUND_COLOR_WHITE
						+"}\n"
				);
		for (LayoutField<RecordType> fp: myLayout.getFields()) {
			buff.append("."
					+ RECORD_FIELD_CLASS+fp.getFieldNum()+" {\n" + 
					"  grid-column: "+(1+fp.getHpos())+" / span "+fp.getHspan()+";\n" + 
					"  grid-row: "+(1+fp.getVpos())+" / span "+fp.getVspan()+" ;\n"
					+ "    float:left;\n"
					+ "} /*"
					+fp.getId()
					+ "*/\n");
		}
		return buff.toString();
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
		RecordType rec = recList.getRecord(recId);
		if (null == rec) {
			buff.append("<p>Record "+recId+" not found</p>");
			return;
		}
		startDiv(buff, RECORD_PANEL_CLASS); {
			Schema mySchema = myLayout.getSchema();
			int numGroups = mySchema.getNumGroups();
			if ((numGroups > 0) && rec.hasAffiliations()) {
				startDiv(buff, GROUP_PANEL_CLASS);
				GroupDefs defs = mySchema.getGroupDefs();
				GenericDatabase<DatabaseRecord> db = myUi.getDatabase();
				for (GroupDef def: defs) {
					int groupNum = def.getGroupNum();
					final String controlId = "affiliateGroup"+groupNum;
					buff.append("<select disabled>"); {
						int[] affiliates = rec.getAffiliates(groupNum);
						for (int affId: affiliates) {
							DatabaseRecord aff = db.getRecord(affId);
							Assertion.assertNotNull(myUi, "Affiliate record not found: "+affId, aff);
							buff.append("<option value=\""+affId+"\" "
									+ ">"+aff.generateTitle()+"</option>");
						}
					} buff.append("</select>\n");
					buff.append("<label for=\""+controlId+"\">"+def.getFieldTitle()+"</form>\n");
				}
				endDiv(buff);
			}
			startDiv(buff, new String[] {FIELDS_PANEL_CLASS, FORM_FIELD_GRID_CLASS}); {
				for (LayoutField<RecordType> fp: myLayout.getFields()) {
					int fieldNum = fp.fieldNum;
					Field fld = rec.getField(fieldNum);
					if (null == fld) {
						continue;
					}
					startDiv(buff, new String[] {MULTICONTROL_CELL_CLASS, RECORD_FIELD_CLASS+fieldNum}); {
						int numValues = fld.getNumberOfValues();
						startDiv(buff, FIELD_TITLE_CLASS);
						buff.append(fp.getTitle());
						endDiv(buff);
						buff.append("<!--"
								+fp.getId()
								+ "-->\n");
						startDiv(buff, FIELD_TEXT_CLASS);
						String separator = "";
						for (FieldValue fv: fld.getFieldValues()) {
							buff.append(separator);
							buff.append(fv.getValueAsString());
							separator = "<br/>\n";
						}
						endDiv(buff);
					} endDiv(buff);

				}
			} endDiv(buff);
		} endDiv(buff);
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

	@Override
	protected String getStyleString() {
		return makeStyleString(); // TODO DEBUG
		// TODO real code return myStyleString;
	}
}

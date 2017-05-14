package org.lasalledebain.libris.ui;
import java.util.HashMap;

import org.lasalledebain.libris.Field.FieldType;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.xmlUtils.LibrisXMLConstants;


public class FieldInfo implements LibrisXMLConstants, GuiConstants{

	protected final String id;
	protected final String title;
	protected final Layout containingLayout;
	protected final String controlTypeName;
	public FieldInfo(Layout containingLayout, String ctrlType, String id, String title) throws DatabaseException {
		super();
		String controlType = ctrlType;
		if (controlType.equals(LibrisXMLConstants.DEFAULT_GUI_CONTROL)) {
			controlTypeName = GUI_TEXTBOX;
			controlType = defaultControlType.get(containingLayout.getFieldType(id));
		} else {
			controlTypeName = controlType;
		}
		this.containingLayout = containingLayout;
		this.id = id;
		this.title = title;
	}

	public String getTitle() {
		return title;
	}

	public String getId() {
		return id;
	}

	public String getControlTypeName() {
		return controlTypeName;
	}

	protected static HashMap<FieldType, String> defaultControlType = initializeDefaultControlTypes();
	private static HashMap<FieldType, String> initializeDefaultControlTypes() {
		HashMap<FieldType, String> temp = new HashMap<FieldType, String>();
		temp.put(FieldType.T_FIELD_BOOLEAN, GuiConstants.GUI_CHECKBOX);
		temp.put(FieldType.T_FIELD_ENUM, GuiConstants.GUI_ENUMFIELD);
		temp.put(FieldType.T_FIELD_INDEXENTRY, GuiConstants.GUI_TEXTFIELD);
		temp.put(FieldType.T_FIELD_INTEGER, GuiConstants.GUI_TEXTFIELD);
		temp.put(FieldType.T_FIELD_PAIR, GuiConstants.GUI_PAIRFIELD);
		temp.put(FieldType.T_FIELD_STRING, GuiConstants.GUI_TEXTFIELD);
		temp.put(FieldType.T_FIELD_TEXT, GuiConstants.GUI_TEXTBOX);
		temp.put(FieldType.T_FIELD_AFFILIATES, GuiConstants.GUI_NAMES_BROWSER);
		return temp;
	}

}

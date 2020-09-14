package org.lasalledebain.libris.ui;

public interface LibrisHTMLConstants {

	String BORDER_COLOUR = "LightGrey";
	String HTML_BACKGROUND_COLOUR = "LightCyan";
	String CORNER_RADIUS = "5px;";
	String GREY_BORDER = "border: 1px solid "
			+ BORDER_COLOUR
			+ ";\n";
	String BACKGROUND_COLOR_LIGHTCYAN = "background-color: " + HTML_BACKGROUND_COLOUR + ";\n";
	String BACKGROUND_COLOR_WHITE = "background-color: white;\n";

	String HTTP_PARAM_RECORD_ID = "recId";
	String HTTP_PARAM_LAYOUT_ID = "layout";
	String HTTP_BROWSER_STARTING_RECORD = "browserStartingRecord";
	String BROWSER_STARTING_RECORD_CONTROL = "browserStartingRecordControl";
	String RECORD_BROWSER_ID = "recordBrowser";
	String RECORD_SELECT_CLASS = "recordSelect";
	String LAYOUT_SELECT_CLASS = "layoutSelect";
	String NAVIGATION_PANEL_CLASS = "navigationPanel";
	String ONCHANGE_THIS_FORM_SUBMIT = "\" onchange=\"this.form.submit()\"";
	String MAIN_FRAME = "mainFrame";
	String CONTENT_PANEL_CLASS = "contentPanel";
	String BROWSER_PANEL_CLASS = "browserPanel";
	String BROWSER_ITEM_CLASS = "browserItem";
	String DISPLAY_PANEL_CLASS = "displayPanel";
	String RECORT_TITLE_CLASS = "recordTitle";
	String FIELD_TITLE_TEXT_CLASS = "fieldTitleText";
	String FIELD_TITLE_BLOCK_CLASS = "fieldTitleBlock";
	String NAVIGATION_BUTTONS_CLASS = "navigationButtons";
	String FIELD_TEXT_CLASS = "fieldText";
	String FIELD_BLOCK_CLASS = "fieldBlock";
	String FIELDS_PANEL_CLASS = "fieldsPanel";
}

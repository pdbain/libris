package org.lasalledebain.libris.xmlUtils;


public interface LibrisXMLConstants {

	public static final String tabsAsString = "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t";
	public static final String XML_EXTRA_VALUE_ATTR = "extravalue";
	public static final String XML_VALUESEPARATOR_ATTR = "valueseparator";
	public static final String XML_INHERIT_ATTR = "inherit";
	public static final String XML_FIELD_TAG = "field";
	public static final String XML_FIELD_ID_ATTR = "id";
	public static final String XML_SET_ID_ATTR = "id";
	public static final String XML_ENUMSET_TAG = "enumset";
	public static final String XML_ENUMCHOICE_ID_ATTR = "id";
	public static final String XML_ENUMCHOICE_VALUE_ATTR = "value";
	public static final String XML_ENUMCHOICE_TAG = "enumchoice";
	public static final String XML_GROUPDEF_ID_ATTR = "id";
	public static final String XML_GROUPDEF_TITLE_ATTR = "title";
	public static final String XML_GROUPDEF_STRUCTURE_ATTR = "structure";
	public static final String XML_GROUPDEF_STRUCTURE_HIERARCHICAL = "hierarchical";
	public static final String XML_GROUPDEF_STRUCTURE_FLAT = "flat";
	public static final String XML_RECORD_ID_ATTR = "id";
	public static final String XML_RECORD_NAME_ATTR = "name";
	public static final String XML_RECORD_ARTIFACT_ATTR = "artifact";
	public static final String XML_MEMBER_GROUP_ATTR = "group";
	public static final String XML_MEMBER_PARENT_ATTR = "parent";
	public static final String XML_INDEXDEF_ID_ATTR = "id";
	public static final String XML_INDEXFIELD_ID_ATTR = "id";
	public static final String XML_INDEXFIELD_STOPLIST_ATTR = "false";
	public static final String XML_LIBRIS_TAG = "libris";
	public static final String XML_ARTIFACTS_TAG = "artifacts";
	public static final String XML_METADATA_TAG = "metadata";
	public static final String XML_SCHEMA_TAG = "schema";
	public static final String XML_RECORDS_TAG = "records";
	public static final String XML_FIELDDEF_TAG = "fielddef";
	public static final String XML_RECORD_TAG = "record";
	public static final String XML_MEMBER_TAG = "member";
	public static final String XML_AFFILIATION_TAG = "affiliation";
	public static final String XML_AFFILIATE_ATTR = "affiliate";
	
	public static final String XML_DATABASE_DATE_ATTR = "date";
	public static final String XML_DATABASE_LOCKED_ATTR = "locked";
	public static final String XML_DATABASE_NAME_ATTR = "databasename";
	public static final String XML_DATABASE_SCHEMA_NAME_ATTR = "schemaname";
	public static final String XML_DATABASE_SCHEMA_LOCATION_ATTR = "schemalocation";
	public static final String XML_DATABASE_REPOSITORY_LOCATION_ATTR = "repositorylocation";
	
	public static final String XML_FIELDDEF_ID_ATTR = "id";
	public static final String XML_FIELDDEF_TITLE_ATTR = "title";
	public static final String XML_FIELDDEF_TYPE_TAG = "datatype";
	public static final String XML_FIELDDEF_DEFAULT_VALUE_ATTR = "default";
	public static final String XML_FIELDDEF_ENUMSET_TAG = "enumset";
	public static final String XML_FIELDDEF_EDITABLE_ATTR = "editable";
	public static final String XML_FIELDDEF_RESTRICTED_ATTR = "restricted";
	public static final String XML_FIELDDEF_SINGLEVALUE_ATTR = "singlevalue";
	public static final String XML_FIELDDEF_INHERIT_ATTR = "inherit";
	
	public static final String XML_FIELDDEFS_TAG = "fielddefs";
	public static final String XML_GROUPDEFS_TAG = "groupdefs";
	public static final String XML_GROUPDEF_TAG = "groupdef";
	public static final String XML_INDEXES_TAG = "indexes";
	public static final String XML_INDEXDEFS_TAG = "indexdefs";
	public static final String XML_INDEXDEF_TAG = "indexdef";
	public static final String XML_INDEXFIELD_TAG = "indexfield";
	public static final String XML_LAYOUTFIELD_TAG = "layoutfield";
	public static final String XML_INSTANCE_TAG = "instance";
	
	public static final String XML_INDEX_NAME_KEYWORDS = "INDEX_RECORD_KEYWORDS";
	
	public static final String XML_LIBRISIMPORT_TAG = "librisimport";
	public static final String XML_LIBRISIMPORT_FIELD_TAG = "field";
	public static final String XML_LIBRISIMPORT_DEFAULT_TAG = "default";
	public static final String XML_LIBRISIMPORT_COLUMN_TAG = "column";
	public static final String XML_LIBRISIMPORT_TRANSLATE_TAG = "translate";
	public static final String XML_LIBRISIMPORT_FIELD_ID_ATTR = "id";
	public static final String XML_LIBRISIMPORT_DEFAULT_DATA_ATTR = "data";
	public static final String XML_LIBRISIMPORT_COLUMN_NUM_ATTR = "columnNum";
	public static final String XML_LIBRISIMPORT_COLUMN_MATCH_ATTR = "match";
	public static final String XML_LIBRISIMPORT_COLUMN_INCLUDE_ATTR = "includeOnMatch";
	public static final String XML_LIBRISIMPORT_TRANSLATE_FROM_ATTR = "from";
	public static final String XML_LIBRISIMPORT_TRANSLATE_TO_ATTR = "to";
	
	public static final String XML_LAYOUTFIELD_ID_ATTR = "id";
	public static final String XML_LAYOUTFIELD_TITLE_ATTR = "title";
	public static final String XML_LAYOUTFIELD_CONTROL_ATTR = "control";
	public static final String XML_LAYOUTFIELD_HEIGHT_ATTR = "height";
	public static final String XML_LAYOUTFIELD_WIDTH_ATTR = "width";
	public static final String XML_LAYOUTFIELD_HSPAN_ATTR = "hspan";
	public static final String XML_LAYOUTFIELD_VSPAN_ATTR = "vspan";
	public static final String XML_LAYOUTFIELD_RETURN_ATTR = "return";

	public static final String XML_LAYOUT_TAG = "layout";
	public static final String DEFAULT_GUI_CONTROL = "default";
	public static final String XML_LAYOUTS_TAG = "layouts";
	public static final String XML_LAYOUTUSAGE_TAG = "layoutusage";
	public static final String XML_LAYOUT_ID_ATTR = "id";
	public static final String XML_LAYOUT_TITLE_ATTR = "title";

	public static final String XML_LAYOUT_TYPE_ATTR = "type";
	
	public static final String XML_LAYOUT_TYPE_TABLE = "table";
	public static final String XML_LAYOUT_TYPE_XML = "xml";
	public static final String XML_LAYOUT_TYPE_FORM = "form";
	public static final String XML_LAYOUT_TYPE_LIST = "list";
	public static final String XML_LAYOUT_TYPE_PARAGRAPH = "paragraph";
	
	public static final String XML_LAYOUT_USEDBY_ATTR = "usedby";
	public static final String XML_LAYOUT_HEIGHT_ATTR = "height";
	public static final String XML_LAYOUT_WIDTH_ATTR = "width";
	public static final String XML_LAYOUT_USAGE_SUMMARYDISPLAY = "summarydisplay";
	public static final String XML_LAYOUT_USAGE_NEWRECORD = "newrecord";
	
	public static final String XML_SCHEMA_VERSION_ATTR = "schemaversion";

	public static final String XML_INSTANCE_BASERECID_ATTR = "baserecid";
	public static final String XML_INSTANCE_FORKDATE_ATTR = "forkdate";
	public static final String XML_INSTANCE_JOINDATE_ATTR = "joindate";

	public static final String XML_FIELDDEF_TYPE_DEFAULT_VALUE = "default";
	public static final String XML_FIELDDEF_TYPE_STRING_VALUE = "string";

	public static final int XML_LIBRIS_INDENT = 0;
	public static final int XML_RECORDS_INDENT = XML_LIBRIS_INDENT+1;
	public static final int XML_RECORD_INDENT = XML_RECORDS_INDENT+1;
	public static final int XML_FIELD_INDENT = XML_RECORD_INDENT+1;

	public static final String emptyElementMemberList[] = {};
	public static final String emptyRequiredAttributesList[] = {};
	public static final String emptyOptionalAttributesList[][] = {};
	public static final char[] tabs = tabsAsString.toCharArray();
	public static final byte[] tabsAsBytes = tabsAsString.getBytes();
	public static final String RECORDS_LIBRIS_CLOSING_TAG = ('\t'+ElementWriter.makeClosingTag(XML_RECORDS_TAG)
	+'\n'+ElementWriter.makeClosingTag(XML_LIBRIS_TAG)+'\n');
	public static final byte[] RECORDS_LIBRIS_CLOSING_TAG_BYTES = RECORDS_LIBRIS_CLOSING_TAG.getBytes();
}

package org.lasalledebain.libris.xmlUtils;

import java.util.HashMap;

import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.index.GroupDef;
import org.lasalledebain.libris.index.GroupDefs;
import org.lasalledebain.libris.index.GroupMember;


public class XmlShapes implements LibrisXMLConstants {
	private static final String XML_FIELDDEF_EDITABLE_ATTR = "editable";
	private static final String XML_FIELDDEF_RESTRICTED_ATTR = "restricted";
	public static final HashMap<String, ElementShape> xmlShapes = initializeXmlShapes();
	public static final HashMap<String, ElementShape> importShapes = initializeImportShapes();
	public enum  SHAPE_LIST {
		DATABASE_SHAPES, IMPORTER_SHAPES
	};
	private HashMap<String, ElementShape> myShapes;
	public XmlShapes(SHAPE_LIST shapeFamily) {
		switch (shapeFamily) {
		case DATABASE_SHAPES: myShapes = xmlShapes; break;
		case IMPORTER_SHAPES: myShapes = importShapes; break;
		}
	}
	private static String[] emptyList = new String[0];
	private static String[][] emptyListList = new String[0][];

	private static HashMap<String, ElementShape> initializeXmlShapes() {
		HashMap<String, ElementShape> shapes = new HashMap<String, ElementShape>();

		makeShape(shapes, XML_LIBRIS_TAG, new String[] {XML_METADATA_TAG, XML_RECORDS_TAG }, 
				new String[] { 
				XML_SCHEMA_NAME_ATTR, XML_SCHEMA_VERSION_ATTR},
				new String[][] {{XML_DATABASE_NAME_ATTR, "unknown"}, {XML_DATABASE_DATE_ATTR, ""},
						{XML_SCHEMA_LOCATION_ATTR, ""},
						{XML_DATABASE_BRANCH_ATTR, XML_DATABASE_BRANCH_MAIN}});
		makeShape(shapes, XML_ENUMCHOICE_TAG, emptyList, new String[] {XML_ENUMCHOICE_ID_ATTR},
				new String[][]{{XML_ENUMCHOICE_VALUE_ATTR, ""}});
		shapes.put(XML_ENUMSET_TAG, makeEnumsetXmlShape());
		
		shapes.put(GroupDefs.getXmlTag(), GroupDefs.getXmlShape());
		shapes.put(GroupDef.getTag(), GroupDef.getShape());
		makeShape(shapes, 
				XML_FIELDDEF_TAG, 
				emptyList, 
				new String[] {XML_FIELDDEF_ID_ATTR},
				new String[][] {
				{XML_FIELDDEF_TITLE_ATTR, ""}, 
				{XML_FIELDDEF_TYPE_TAG, XML_FIELDDEF_DEFAULT_VALUE_ATTR}, {XML_FIELDDEF_ENUMSET_TAG, ""}, 
				{XML_FIELDDEF_DEFAULT_VALUE_ATTR, ""},
				{XML_FIELDDEF_RESTRICTED_ATTR, "false"}, 
				{XML_FIELDDEF_EDITABLE_ATTR, "true"}, {XML_VALUESEPARATOR_ATTR, ""}, {XML_INHERIT_ATTR, ""}
		});
		
		makeShape(shapes, 
				XML_FIELD_TAG, 
				emptyList, 
				new String[] {XML_RECORD_ID_ATTR},
		new String[][] {{XML_ENUMCHOICE_VALUE_ATTR, ""}, {XML_EXTRA_VALUE_ATTR, ""}}, 
		true);

		makeShape(shapes, 
				XML_LAYOUTFIELD_TAG,
				emptyList, 
				new String[] {"id"},
				new String[][] {{"title", ""}, {"row", ""}, {"column",""}, {"height", ""}, {"width", ""}, 
						{"return", "false"}, {"hspan", "1"}, {"vspan", "1"}, {"control", DEFAULT_GUI_CONTROL}});
		makeShape(shapes,
				XML_FIELDDEFS_TAG, 
				new String[] {XML_ENUMSET_TAG, XML_FIELDDEF_TAG},
				emptyList,
				emptyListList);
		shapes.put(XML_INDEXDEFS_TAG, makeIndexDefsXmlShape());
		shapes.put(XML_INDEXES_TAG, makeIndexesXmlShape());
		shapes.put(XML_LAYOUT_TAG, makeLayoutXmlShape());
		shapes.put(XML_LAYOUTS_TAG, makeLayoutsXmlShape());
		shapes.put(XML_LAYOUTUSAGE_TAG, makeLayoutUsageXmlShape());
		shapes.put(XML_METADATA_TAG, makeMetadataXmlShape());
		shapes.put(Record.getElementTag(), Record.getShape());
		shapes.put(GroupMember.getMemberTag(), GroupMember.getMemberShape());
		shapes.put(GroupMember.getAffiliationTag(), GroupMember.getAffiliationShape());
		shapes.put(XML_RECORDS_TAG, makeRecordsXmlShape());
		shapes.put(XML_SCHEMA_TAG, makeSchemaXmlShape());

		return shapes;
	}

	private static HashMap<String, ElementShape> initializeImportShapes() {
		HashMap<String, ElementShape> shapes = new HashMap<String, ElementShape>();

		makeShape(shapes, XML_LIBRISIMPORT_TAG, new String[] {XML_LIBRISIMPORT_FIELD_TAG}, 
				null, null);
		makeShape(shapes, XML_LIBRISIMPORT_FIELD_TAG, 
				new String[] {XML_LIBRISIMPORT_DEFAULT_TAG, XML_LIBRISIMPORT_COLUMN_TAG}, 
				new String[] {XML_LIBRISIMPORT_FIELD_ID_ATTR}, null);
		makeShape(shapes, XML_LIBRISIMPORT_DEFAULT_TAG, null, 
				new String[] {XML_LIBRISIMPORT_DEFAULT_DATA_ATTR}, null);
		makeShape(shapes, XML_LIBRISIMPORT_COLUMN_TAG, new String[] {XML_LIBRISIMPORT_TRANSLATE_TAG}, 
				new String[] {XML_LIBRISIMPORT_COLUMN_NUM_ATTR}, 
				new String[][] {{XML_LIBRISIMPORT_COLUMN_MATCH_ATTR, ""}, {XML_LIBRISIMPORT_COLUMN_INCLUDE_ATTR, "false"}});
		makeShape(shapes, XML_LIBRISIMPORT_TRANSLATE_TAG, null, 
				new String[] {XML_LIBRISIMPORT_TRANSLATE_FROM_ATTR, XML_LIBRISIMPORT_TRANSLATE_TO_ATTR}, null);
		return shapes;
	}
	
	private static ElementShape makeShape(HashMap<String,ElementShape> shapes, final String xmlTag,
			 String[] subElementNames,
			 String[] requiredAttributeNames,
			 String[][] optionalAttributeNamesAndValues) {
		return makeShape(shapes, xmlTag,
				 subElementNames,
				 requiredAttributeNames,
				 optionalAttributeNamesAndValues, false);
	}

	private static ElementShape makeShape(HashMap<String,ElementShape> shapes, final String xmlTag,
			 String[] subElementNames,
			 String[] requiredAttributeNames,
			 String[][] optionalAttributeNamesAndValues,
			 boolean hasContent) {
		ElementShape s = makeShape(xmlTag, subElementNames,
				requiredAttributeNames, optionalAttributeNamesAndValues,
				hasContent);
		shapes.put(xmlTag, s);
		return s;
	
		
	}

	/**
	 * @param xmlTag
	 * @param subElementNames
	 * @param requiredAttributeNames
	 * @param optionalAttributeNamesAndValues
	 * @param hasContent
	 * @return
	 */
	public static ElementShape makeShape(final String xmlTag,
			String[] subElementNames, String[] requiredAttributeNames,
			String[][] optionalAttributeNamesAndValues, boolean hasContent) {
		ElementShape s = new ElementShape(xmlTag);
		if (null != subElementNames) {
			s.setSubElementNames(subElementNames);
		}
		if (null != requiredAttributeNames) {
			s.setRequiredAttributeNames(requiredAttributeNames);
		}
		if (null != optionalAttributeNamesAndValues) {
			s.setOptionalAttributeNames(optionalAttributeNamesAndValues);
		}
		s.setHasContent(hasContent);
		return s;
	}

	private static ElementShape makeMetadataXmlShape() {
	 ElementShape s = new ElementShape(XML_METADATA_TAG);
		s.setSubElementNames(new String[] {XML_SCHEMA_TAG, XML_LAYOUTS_TAG});
		return s;
	}

	private static ElementShape makeRecordsXmlShape() {
		ElementShape s = new ElementShape(XML_RECORDS_TAG);
		s.setSubElementNames(new String[] {XML_RECORD_TAG});
		return s;
	}

	private static ElementShape makeSchemaXmlShape() {
		ElementShape s = new ElementShape(XML_SCHEMA_TAG);
		s.setSubElementNames(new String[] {XML_FIELDDEFS_TAG, XML_GROUPDEFS_TAG, 
				XML_INDEXDEFS_TAG});
		return s;
	}

	private static ElementShape makeEnumsetXmlShape() {
		ElementShape s = new ElementShape(XML_ENUMSET_TAG);
		s.setRequiredAttributeNames(new String[] {XML_SET_ID_ATTR});
		s.setSubElementNames(new String[] {XML_ENUMCHOICE_TAG});
		return s;
	}

	@Deprecated
	private static ElementShape makeIndexDefsXmlShape() {
		ElementShape s = new ElementShape(XML_INDEXDEFS_TAG);
		s.setSubElementNames(new String[] {XML_INDEXDEFS_TAG});
		return s;
	}

	@Deprecated
	private static ElementShape makeIndexesXmlShape() {
		ElementShape s = new ElementShape(XML_INDEXES_TAG);
		s.setSubElementNames(emptyList);
		return s;
	}

	@Deprecated
	private static ElementShape makeLayoutXmlShape() {
		 ElementShape s = new ElementShape(XML_LAYOUT_TAG);
		s.setSubElementNames(new String[] {XML_LAYOUTFIELD_TAG, XML_LAYOUTUSAGE_TAG});
		s.setRequiredAttributeNames(new String[] {LibrisXMLConstants.XML_LAYOUT_ID_ATTR});
		s.setOptionalAttributeNames(new String[][] {{"title", ""}, {"type","table"}, {"height", "300"}, {"width", "400"}});
		return s;
	}

	@Deprecated
	private static ElementShape makeLayoutsXmlShape() {
		 ElementShape s = new ElementShape(XML_LAYOUTS_TAG);
			s.setSubElementNames(new String[] {XML_LAYOUT_TAG});
			return s;
		}

	@Deprecated
private static ElementShape makeLayoutUsageXmlShape() {
		 ElementShape s = new ElementShape(XML_LAYOUTUSAGE_TAG);
			s.setRequiredAttributeNames(new String[] {XML_LAYOUT_USEDBY_ATTR});
			return s;
		}
	public ElementShape getShape(String elementTag) {
		return myShapes.get(elementTag);
	}

	public String getTitle() {
		return null;
	}

	public String getId() {
		return null;
	}

	public int getFieldNum() {
		return 0;
	}
}

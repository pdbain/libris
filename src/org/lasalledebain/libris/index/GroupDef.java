package org.lasalledebain.libris.index;

import org.lasalledebain.libris.Field.FieldType;
import org.lasalledebain.libris.FieldTemplate;
import org.lasalledebain.libris.Schema;
import org.lasalledebain.libris.XMLElement;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.XmlException;
import org.lasalledebain.libris.xmlUtils.ElementManager;
import org.lasalledebain.libris.xmlUtils.ElementShape;
import org.lasalledebain.libris.xmlUtils.ElementWriter;
import org.lasalledebain.libris.xmlUtils.LibrisAttributes;
import org.lasalledebain.libris.xmlUtils.XmlShapes;

public class GroupDef extends FieldTemplate implements XMLElement {

	/*	<!ELEMENT groupdef EMPTY >

	<!ATTLIST groupdef
		id ID #REQUIRED
		title CDATA #IMPLIED
		structure %groupstructure; "hierarchical"
	>
*/
	public enum GroupStructure {
		STRUCTURE_FLAT(XML_GROUPDEF_STRUCTURE_FLAT), STRUCTURE_HIERARCHICAL(XML_GROUPDEF_STRUCTURE_HIERARCHICAL);
		String structName;
		private GroupStructure(String name) {
			structName = name;
		}
		@Override
		public String toString() {
			return structName;
		}
	};

	private GroupStructure structureType = GroupStructure.STRUCTURE_HIERARCHICAL;
	private int groupNum;
	
	public GroupDef(Schema s, String id, String title, int num) {
		super(s, id, title, FieldType.T_FIELD_AFFILIATES);
		groupNum = num;
	}
	public GroupDef(Schema s, int num) {
		super(s);
		ftype = FieldType.T_FIELD_AFFILIATES;
		factory = fieldClasses.get(ftype);
		groupNum = num;
	}
	@Override
	public boolean isContentField() {
		return false;
	}

	@Override
	public LibrisAttributes getAttributes() {
		LibrisAttributes attrs = new LibrisAttributes(
				new String[][] {{XML_GROUPDEF_ID_ATTR, fieldId}, {XML_GROUPDEF_STRUCTURE_ATTR, getStructureType().toString()}}
				);
		if ((null != fieldTitle) && !fieldTitle.isEmpty()) {
			attrs.setAttribute(XML_GROUPDEF_TITLE_ATTR, fieldTitle);
		}
		return attrs;
	}

	@Override
	public void toXml(ElementWriter xmlWriter) throws XmlException  {
		xmlWriter.writeStartElement(XML_GROUPDEF_TAG, getAttributes(), true);	
	}

	@Override
	public void fromXml(ElementManager mgr) throws InputException  {
		LibrisAttributes attrs = mgr.parseOpenTag();
		fieldId = attrs.get(XML_GROUPDEF_ID_ATTR).intern();
		fieldTitle = attrs.get(XML_GROUPDEF_TITLE_ATTR);
		String structureTypeName = attrs.get(XML_GROUPDEF_STRUCTURE_ATTR);
		if (null == structureTypeName) {
			structureTypeName = GroupStructure.STRUCTURE_HIERARCHICAL.toString();
		} else {
			structureTypeName.intern();
		}
		if (structureTypeName.equals(XML_GROUPDEF_STRUCTURE_HIERARCHICAL)) {
			structureType  =  GroupStructure.STRUCTURE_HIERARCHICAL;
		} else if (structureTypeName.equals(XML_GROUPDEF_STRUCTURE_FLAT)) {
			structureType  =  GroupStructure.STRUCTURE_FLAT;
		} else {
			throw new InputException("unrecognised group structure type "+structureTypeName);
		}
	}

	static public ElementShape getShape() {
		return XmlShapes.makeShape(getTag(), emptyElementMemberList, 
				new String [] {XML_GROUPDEF_ID_ATTR},
				new String [][] {{XML_GROUPDEF_TITLE_ATTR, ""}, {XML_GROUPDEF_STRUCTURE_ATTR, XML_GROUPDEF_STRUCTURE_HIERARCHICAL}}, false);
	}
	public static String getTag() {
		return XML_GROUPDEF_TAG;
	}
	
	public GroupStructure getStructureType() {
		return structureType;
	}

	public void setGroupNum(int groupNum) {
		this.groupNum=groupNum;
	}

	public int getGroupNum() {
		return groupNum;
	}
}

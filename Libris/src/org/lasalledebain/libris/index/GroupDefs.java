package org.lasalledebain.libris.index;

import java.util.HashMap;

import org.lasalledebain.libris.XmlExportable;
import org.lasalledebain.libris.XmlImportable;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.XmlException;
import org.lasalledebain.libris.xmlUtils.ElementManager;
import org.lasalledebain.libris.xmlUtils.ElementShape;
import org.lasalledebain.libris.xmlUtils.ElementWriter;
import org.lasalledebain.libris.xmlUtils.LibrisAttributes;
import org.lasalledebain.libris.xmlUtils.LibrisXMLConstants;
import org.lasalledebain.libris.xmlUtils.XmlShapes;

public class GroupDefs implements XmlExportable, XmlImportable, LibrisXMLConstants {

	HashMap<String, GroupDef> groupList;
	int numGroups;
	public static String getXmlTag() {
		return XML_GROUPDEFS_TAG;
	}
	
	static public ElementShape getXmlShape() {
		return XmlShapes.makeShape(getXmlTag(),
				new String [] {GroupDef.getTag()}, emptyRequiredAttributesList, emptyOptionalAttributesList, false);
	}
	
	@Override
	public boolean equals(Object comparand) {
		if (!comparand.getClass().isAssignableFrom(GroupDefs.class)) {
			return false;
		} else {
			GroupDefs otherGroupDefs = (GroupDefs) comparand;
			return groupList.equals(otherGroupDefs.groupList);
		}
	}

	public GroupDefs() {
		groupList = new HashMap<String, GroupDef>();
		numGroups = 0;
	}

	@Override
	public void fromXml(ElementManager mgr) throws InputException {
		mgr.parseOpenTag();
		int groupNum = 0;
		while (mgr.hasNext()) {
			@SuppressWarnings("unused")
			GroupDef newGroup = new GroupDef();
			ElementManager groupMgr = mgr.nextElement();
			newGroup.fromXml(groupMgr);
			String groupName = newGroup.getId();
			addGroup(newGroup, groupNum, groupName);
			++groupNum;
		}
		numGroups = groupNum;
		mgr.parseClosingTag();
	}

	public int getNumGroups() {
		return numGroups;
	}

	public void addGroup(GroupDef newGroup, int groupNum, String groupName) {
		newGroup.setGroupNum(groupNum);
		groupList.put(groupName, newGroup);
	}

	@Override
	public LibrisAttributes getAttributes() {
		return LibrisAttributes.getLibrisEmptyAttributes();
	}

	@Override
	public void toXml(ElementWriter output) throws XmlException {
		output.writeStartElement(XML_GROUPDEFS_TAG);
		for (GroupDef g: groupList.values()) {
			g.toXml(output);
		}
		output.writeEndElement();	
	}

	public GroupDef getGroupDef(String groupName) {
		return groupList.get(groupName);
	}
	
	public Iterable<String> getGroupIds() {
		return groupList.keySet();
	}

	public int groupNameToNum(String groupName) {
		GroupDef def = getGroupDef(groupName);
		if (null == def) {
			return -1;
		}
		return def.getGroupNum();
	}
}

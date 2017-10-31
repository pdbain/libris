package org.lasalledebain.libris.index;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;

import org.lasalledebain.libris.XMLElement;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.XmlException;
import org.lasalledebain.libris.xmlUtils.ElementManager;
import org.lasalledebain.libris.xmlUtils.ElementShape;
import org.lasalledebain.libris.xmlUtils.ElementWriter;
import org.lasalledebain.libris.xmlUtils.LibrisAttributes;
import org.lasalledebain.libris.xmlUtils.XmlShapes;

public class GroupDefs implements XMLElement, Iterable<GroupDef> {

	LinkedHashMap<String, GroupDef> groupMap;
	ArrayList<String> groupIds;
	ArrayList<GroupDef> defList;
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
			return groupMap.equals(otherGroupDefs.groupMap);
		}
	}

	public GroupDefs() {
		groupMap = new LinkedHashMap<String, GroupDef>();
		groupIds = new ArrayList<String>();
		defList = new ArrayList<GroupDef>();
		numGroups = 0;
	}
// TODO 1. Test max 255 members per group
	@Override
	public void fromXml(ElementManager mgr) throws InputException {
		mgr.parseOpenTag();
		int groupNum = 0;
		while (mgr.hasNext()) {
			GroupDef newGroup = new GroupDef(null, groupNum);
			ElementManager groupMgr = mgr.nextElement();
			newGroup.fromXml(groupMgr);
			String groupName = newGroup.getFieldId();
			addGroup(newGroup);
			++groupNum;
		}
		numGroups = groupNum;
		mgr.parseClosingTag();
	}

	public int getNumGroups() {
		return numGroups;
	}

	public void addGroup(GroupDef newGroup) {
		String id = newGroup.getFieldId();
		groupMap.put(id, newGroup);
		groupIds.add(id);
		defList.add(newGroup);
	}

	@Override
	public LibrisAttributes getAttributes() {
		return LibrisAttributes.getLibrisEmptyAttributes();
	}

	@Override
	public void toXml(ElementWriter output) throws XmlException {
		output.writeStartElement(getXmlTag());
		for (GroupDef g: groupMap.values()) {
			g.toXml(output);
		}
		output.writeEndElement();	
	}

	public GroupDef getGroupDef(String groupId) {
		return groupMap.get(groupId);
	}
	
	public GroupDef getGroupDef(int groupNum) {
		return defList.get(groupNum);
	}
	
	public Iterable<String> getGroupIds() {
		return groupIds;
	}

	public int groupIdToNum(String groupId) {
		GroupDef def = getGroupDef(groupId);
		if (null == def) {
			return -1;
		}
		return def.getGroupNum();
	}
	
	@Override
	public Iterator<GroupDef> iterator() {
		Iterator<GroupDef> result = null;
		if (null != groupMap) {
			result = groupMap.values().iterator();
		}
		return result;
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

	@Override
	public String getElementTag() {
		return getXmlTag();
	}
}

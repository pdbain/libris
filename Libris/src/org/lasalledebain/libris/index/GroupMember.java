package org.lasalledebain.libris.index;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.lasalledebain.libris.RecordId;
import org.lasalledebain.libris.Schema;
import org.lasalledebain.libris.XmlExportable;
import org.lasalledebain.libris.XmlImportable;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.InternalError;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.exception.StructureException;
import org.lasalledebain.libris.exception.XmlException;
import org.lasalledebain.libris.indexes.GroupManager;
import org.lasalledebain.libris.xmlUtils.ElementManager;
import org.lasalledebain.libris.xmlUtils.ElementShape;
import org.lasalledebain.libris.xmlUtils.ElementWriter;
import org.lasalledebain.libris.xmlUtils.LibrisAttributes;
import org.lasalledebain.libris.xmlUtils.LibrisXMLConstants;
import org.lasalledebain.libris.xmlUtils.XmlShapes;

/**
 * Represents a node in a group graph.
 * A Group has a parent (except the root), 0 or more children to whom it is a parent,
 * and 0 or more affiliates, with whom it has no parent-child relationship.
 *
 */
public class GroupMember implements LibrisXMLConstants, XmlImportable, XmlExportable {

	private static final String memberTag = XML_MEMBER_TAG;
	private static final String affiliationTag = XML_AFFILIATION_TAG;
	private String group;
	private int groupNum;
	private RecordId parent;
	public RecordId getParent() {
		return parent;
	}

	public String getGroup() {
		return group;
	}
	
	public int getGroupNum() {
		return groupNum;
	}

	public String getTitle() {
		return title;
	}

	private String title;
	ArrayList <RecordId> affiliations;
	boolean hasAffiliations;
	private GroupDefs defs;
	static private final ArrayList<RecordId> dummyAffiliations = new ArrayList<RecordId>(0);
	public GroupMember(GroupDefs defs) {
		affiliations = null;
		hasAffiliations = false;
		this.defs = defs;
	}
	
	static public ElementShape getMemberShape() {
		return XmlShapes.makeShape(memberTag,
		new String [] {XML_AFFILIATION_TAG}, new String [] {XML_MEMBER_GROUP_ATTR},
		new String [][] {{XML_MEMBER_TITLE_ATTR, ""}, {XML_MEMBER_PARENT_ATTR, ""}}, false);
	}

	static public ElementShape getAffiliationShape() {
		return XmlShapes.makeShape(affiliationTag,
		new String [] {}, new String [] {XML_AFFILIATE_ATTR},
		new String [][] {}, false);
	}

	@Override
	public void fromXml(ElementManager mgr) throws LibrisException {
		HashMap<String, String> attrs = mgr.parseOpenTag();
		group = attrs.get(XML_MEMBER_GROUP_ATTR);
		groupNum = defs.groupNameToNum(group);
		if (groupNum < 0) {
			throw new InputException("Undefined group: "+group);
		}
		String parentString = attrs.get(XML_MEMBER_PARENT_ATTR);
		if ((null == parentString) || parentString.isEmpty()) {
			parent = RecordId.getNullId();
		} else {
			parent = new RecordId(parentString);
		}
		title = attrs.get(XML_MEMBER_TITLE_ATTR);
		if (mgr.hasNext()) {
			affiliations = new ArrayList<RecordId>(1);
			hasAffiliations = true;
			do {
				ElementManager subMgr = mgr.nextElement();
				HashMap<String, String> affiliationAttrs = subMgr.parseOpenTag();
				String affId = affiliationAttrs.get(XML_AFFILIATE_ATTR);
				affiliations.add(new RecordId(affId));
			} while (mgr.hasNext());
		}
	}

	@Override
	public void toXml(ElementWriter output) throws LibrisException {
		LibrisAttributes attrs = getAttributes();
		output.writeStartElement(XML_MEMBER_TAG, attrs, hasAffiliations);
		if (hasAffiliations) {
			for (RecordId affId: affiliations) {
				LibrisAttributes affAttrs = new LibrisAttributes();
				affAttrs.setAttribute(XML_MEMBER_PARENT_ATTR, affId.toString());
				output.writeStartElement(XML_AFFILIATION_TAG, affAttrs, false);
			}
			output.writeEndElement();
		}
	}

	@Override
	public LibrisAttributes getAttributes() throws XmlException {
		LibrisAttributes attrs = new LibrisAttributes();
		attrs.setAttribute(XML_MEMBER_PARENT_ATTR, parent.toString());
		attrs.setAttribute(XML_MEMBER_TITLE_ATTR, title);		
		attrs.setAttribute(XML_MEMBER_GROUP_ATTR, group);		
		return attrs;
	}

	public static String getAffiliationTag() {
		return affiliationTag;
	}

	public static String getMemberTag() {
		return memberTag;
	}

	public Iterable<RecordId> getAffiliations() {
		if (hasAffiliations) {
			return affiliations;
		} else {
			return dummyAffiliations;
		}
	}
}

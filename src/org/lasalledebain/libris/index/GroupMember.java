package org.lasalledebain.libris.index;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.lasalledebain.libris.Field;
import org.lasalledebain.libris.RecordId;
import org.lasalledebain.libris.XMLElement;
import org.lasalledebain.libris.XmlExportable;
import org.lasalledebain.libris.XmlImportable;
import org.lasalledebain.libris.exception.FieldDataException;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.exception.XmlException;
import org.lasalledebain.libris.field.FieldValue;
import org.lasalledebain.libris.field.GenericField;
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
public class GroupMember extends GenericField implements XMLElement {

	private static final int NULL_ID = RecordId.getNullId();
	private static final String memberTag = XML_MEMBER_TAG;
	private static final String affiliationTag = XML_AFFILIATION_TAG;
	GroupDef def;
	
	int affiliations[];
	private GroupDefs defs;
	static private final int[] dummyAffiliations = new int[0];

	public int getParent() {
		return (affiliations.length == 0) ? NULL_ID: affiliations[0];
	}

	public String getGroupId() {
		return def.getFieldId();
	}
	
	public int getGroupNum() {
		return def.getGroupNum();
	}

	public GroupMember(GroupDefs defs, GroupDef template) {
		super(template);
		affiliations = dummyAffiliations;
		this.defs = defs;
		this.def = template;
	}
	
	static public ElementShape getMemberShape() {
		return XmlShapes.makeShape(memberTag,
		new String [] {XML_AFFILIATION_TAG}, new String [] {XML_MEMBER_GROUP_ATTR},
		new String [][] {{XML_MEMBER_PARENT_ATTR, ""}}, false);
	}

	static public ElementShape getAffiliationShape() {
		return XmlShapes.makeShape(affiliationTag,
		new String [] {}, new String [] {XML_AFFILIATE_ATTR},
		new String [][] {}, false);
	}

	@Override
	public void fromXml(ElementManager mgr) throws LibrisException {
		HashMap<String, String> attrs = mgr.parseOpenTag();
		String groupId = attrs.get(XML_MEMBER_GROUP_ATTR);
		def = defs.getGroupDef(groupId);
		if (null == def) {
			throw new InputException("Undefined group: "+groupId);
		}
		ArrayList<Integer> tempAffiliations = null;
		String parentString = attrs.get(XML_MEMBER_PARENT_ATTR);
		if ((null != parentString) && !parentString.isEmpty()) {
			int parent = Integer.parseInt(parentString);
			tempAffiliations = new ArrayList<Integer>(1);
			tempAffiliations.add(parent);
		}
		if (mgr.hasNext()) {
			if (null == tempAffiliations) {
				throw new InputException("Cannot have affiliations without a parent");
			}
			do {
				ElementManager subMgr = mgr.nextElement();
				HashMap<String, String> affiliationAttrs = subMgr.parseOpenTag();
				String affId = affiliationAttrs.get(XML_AFFILIATE_ATTR);
				tempAffiliations.add(Integer.parseInt(affId));
			} while (mgr.hasNext());
		}
		if (null != tempAffiliations) {
			int[] result = listToArray(tempAffiliations);
			affiliations = result;
		}
	}

	private int[] listToArray(ArrayList<Integer> theList) {
		int result[] = new int[theList.size()];
		int index = 0;
		for (Integer i: theList) {
			result[index++] = i.intValue();
		}
		return result;
	}

	@Override
	public void toXml(ElementWriter output) throws LibrisException {
		LibrisAttributes attrs = getAttributes();
		boolean empty = affiliations.length == 0;
		output.writeStartElement(XML_MEMBER_TAG, attrs, empty);
		if (!empty) {
			boolean first = true;
			for (int affId: affiliations) {
				if (first) {
					first = false;
					continue;
				}
				LibrisAttributes affAttrs = new LibrisAttributes();
				affAttrs.setAttribute(XML_MEMBER_PARENT_ATTR, Integer.toString(affId));
				output.writeStartElement(XML_AFFILIATION_TAG, affAttrs, false);
			}
			output.writeEndElement();
		}
	}

	@Override
	public LibrisAttributes getAttributes() throws XmlException {
		LibrisAttributes attrs = new LibrisAttributes();
		attrs.setAttribute(XML_MEMBER_PARENT_ATTR, Integer.toString(getParent()));
		attrs.setAttribute(XML_MEMBER_GROUP_ATTR, def.getFieldId());		
		return attrs;
	}

	@Override
	public int getNumberOfValues() {
		return (null == affiliations) ? 0 : affiliations.length;
	}

	public static String getAffiliationTag() {
		return affiliationTag;
	}

	public static String getMemberTag() {
		return memberTag;
	}

	public int[] getAffiliations() {
		return affiliations;
	}

	@Override
	public void addValue(String data) throws FieldDataException {
		try {
			int intValue = Integer.parseInt(data);
			addIntegerValue(intValue);
		} catch (NumberFormatException e) {
			throw new FieldDataException("Illegal record ID: "+data);
		}
	}

	@Override
	public void addIntegerValue(int value) throws FieldDataException {
		int tempArray[] = new int[affiliations.length + 1];
		System.arraycopy(affiliations, 0, tempArray, 0, affiliations.length);
		tempArray[tempArray.length - 1] = value;
		Arrays.sort(tempArray, 1, tempArray.length);
		affiliations = tempArray;
	}

	@Override
	public Field duplicate() throws FieldDataException {
		// TODO Auto-generated method stub
		return null;
	}

	public String getTitle() {
		return def.getFieldTitle();
	}

	public void setParent(int parent) {
		if ((affiliations.length > 0) && (NULL_ID == affiliations[0])) {
			affiliations[0] = parent;
		} else {
			int[] tempAffiliations = new int[affiliations.length + 1];
			tempAffiliations[0] = parent;
			System.arraycopy(affiliations, 0, tempAffiliations, 1, affiliations.length);
			Arrays.sort(tempAffiliations, 1, tempAffiliations.length);
			affiliations = tempAffiliations;
		}
	}

	public static int[] getDummyAffiliations() {
		return dummyAffiliations;
	}

	public GroupDef getDef() {
		return def;
	}

	public int getFieldNum() {
		return 0;
	}

	public String getId() {
		return null;
	}

	@Override
	public void setValues(Iterable<FieldValue> valueList)
			throws FieldDataException {
		ArrayList<Integer> tempAffiliations = new ArrayList<Integer>(1);
		for (FieldValue fv: valueList) {
			tempAffiliations.add(fv.getValueAsInt());
		}
		affiliations = listToArray(tempAffiliations);
	}
}

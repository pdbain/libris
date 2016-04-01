package org.lasalledebain.libris.index;

import java.util.Vector;

import org.lasalledebain.libris.XmlExportable;
import org.lasalledebain.libris.XmlImportable;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.exception.StructureException;
import org.lasalledebain.libris.exception.XmlException;
import org.lasalledebain.libris.xmlUtils.ElementManager;
import org.lasalledebain.libris.xmlUtils.ElementWriter;
import org.lasalledebain.libris.xmlUtils.LibrisAttributes;

import quicktime.app.display.GroupMember;

@Deprecated
public class ObsoleteGroupMember implements XmlExportable, XmlImportable {
	
	private ObsoleteGroupMember masterNode;
	NodeList parents, children;
	int recordId;
	
	public int getRecordId() {
		return recordId;
	}

	ObsoleteGroupMember (ObsoleteGroupMember factory, ObsoleteGroupMember parent, int id) {
		this.masterNode = factory;
		parents = new NodeList(1);
		parents.add(parent);
		recordId = id;
	}
	
	public ObsoleteGroupMember[] getParents() {
		return parents.getNodes();
	}
	
	public ObsoleteGroupMember getPrincipalPrent () {
		return parents.firstElement();
		
	}
	
	public void addParent(ObsoleteGroupMember parent) {
		synchronized (masterNode) {
			addParentLink(parent);
			parent.addChildLink(this);
		}
	}
	
	public void addParentLink(ObsoleteGroupMember parent) {
		synchronized (masterNode) {
			parents.add(parent);
		}
	}
	
	public void addChildLink(ObsoleteGroupMember child) {
		synchronized (masterNode) {
			if (null == children) {
				children = new NodeList(1);
			}
			children.add(child);
		}
	}
	
	public void addChild(ObsoleteGroupMember child) {
		synchronized (masterNode) {
			addChild(child);
			child.addParentLink(this);
		}
	}
	
	public  ObsoleteGroupMember[] getChildren() {
		synchronized (masterNode) {
			if (null == children) {
				return  null;
			} else {
				return children.getNodes();
			}
		}
	}
	
	public int[] getParentIds () {
		return parents.getIds();
	}
	
	public int[] getChildIds () {
		return children.getIds();
	}
	
	private static class NodeList {
		Vector<ObsoleteGroupMember> list;
		public NodeList(int i) {
			list = new Vector<ObsoleteGroupMember>(1);
		}

		public int[] getIds() {
			if ((null == list) || list.isEmpty()) {
				return new int[0];
			}
			int ids[] = new int[list.size()];
			int cursor = 0;
			for (ObsoleteGroupMember n:list) {
				ids[cursor++] = n.getRecordId();
			}
			return ids;
		}

		public synchronized void add(ObsoleteGroupMember node) {
			if (null == list) {
				list = new Vector<ObsoleteGroupMember>(1);
			}
			list.add(node);
		}

		public synchronized ObsoleteGroupMember firstElement() {
			if ((null == list) || list.isEmpty()) {
				return null;
			} else {
				return list.firstElement();
			}
		}

		public  synchronized ObsoleteGroupMember[] getNodes() {
			if (null == list) {
				return 	new ObsoleteGroupMember[0];
			} else {
				return list.toArray(new ObsoleteGroupMember[list.size()]);
			}


		}

		public int size() {
			if (null == list) {
				return 0;
			} else {
				return list.size();
			}
		}
	}

	public void setRoot() throws StructureException {
		if (parents.size() != 0) {
			throw new StructureException("node "+recordId+" cannot be made root because it has parents");
		}
	}

	@Override
	public LibrisAttributes getAttributes() throws XmlException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void toXml(ElementWriter output) throws LibrisException {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void fromXml(ElementManager mgr) throws LibrisException {
		// TODO Auto-generated method stub
		// TODO parse affiliation
		
	}
}

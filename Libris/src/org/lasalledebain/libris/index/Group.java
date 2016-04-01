package org.lasalledebain.libris.index;

import java.util.Iterator;
import java.util.TreeSet;

import org.lasalledebain.libris.RecordId;
import org.lasalledebain.libris.XmlExportable;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.exception.XmlException;
import org.lasalledebain.libris.xmlUtils.ElementWriter;
import org.lasalledebain.libris.xmlUtils.LibrisAttributes;
import org.lasalledebain.libris.xmlUtils.LibrisXMLConstants;

public class Group implements XmlExportable, LibrisXMLConstants {

	public static final int NULL_GROUP = -1;
	/**
	 * Group has a list of children and affiliates.
	 * This data is ephemeral.  The hash table is managed by GroupInvertedListManager.
	 */
	TreeSet<RecordId> children;
	TreeSet<RecordId> affiliates;
	private RecordId id;

	public RecordId getId() {
		return id;
	}

	public Group(RecordId myself) {
		children = new TreeSet<RecordId>();
		affiliates = new TreeSet<RecordId>();
		this.id = myself;
	}
	
	public void addChild(RecordId id) {
		children.add(id);
	}
	
	public void addAffiliate(RecordId id) {
		affiliates.add(id);
	}
	
	Iterator<RecordId> getChildren() {
		return children.iterator();
	}

	Iterator<RecordId> getAffiliates() {
		return affiliates.iterator();
	}
	
	int getNumChildren() {
		return children.size();
	}
	
	int getNumAffiliates() {
		return affiliates.size();
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

}

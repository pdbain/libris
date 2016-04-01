package org.lasalledebain.libris.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;
import java.util.logging.Level;

import javax.swing.JPanel;

import org.lasalledebain.libris.LibrisDatabase;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.Schema;
import org.lasalledebain.libris.XmlExportable;
import org.lasalledebain.libris.Field.FieldType;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.exception.XmlException;
import org.lasalledebain.libris.xmlUtils.ElementManager;
import org.lasalledebain.libris.xmlUtils.ElementWriter;
import org.lasalledebain.libris.xmlUtils.LibrisAttributes;
import org.lasalledebain.libris.xmlUtils.LibrisXMLConstants;

public abstract class Layout implements XmlExportable, LibrisXMLConstants {
LibrisDatabase db = null;
	public Layout(LibrisDatabase db) {
		super();
		this.db = db;
	}


	@Override
	public LibrisAttributes getAttributes() {
		LibrisAttributes attrs = new LibrisAttributes();
		attrs.setAttribute(XML_LAYOUT_ID_ATTR, getId());
		if (null != title) {
			attrs.setAttribute(LibrisXMLConstants.XML_LAYOUT_TITLE_ATTR, title);
		}
		attrs.setAttribute(XML_LAYOUT_TYPE_ATTR, getLayoutType());
		attrs.setAttribute(XML_LAYOUT_HEIGHT_ATTR, Integer.toString(getHeight()));
		attrs.setAttribute(XML_LAYOUT_WIDTH_ATTR, Integer.toString(getWidth()));
		return attrs;
	}


	@Override
	public void toXml(ElementWriter output) throws LibrisException {
		output.writeStartElement(XML_LAYOUT_TAG, getAttributes(), false);
		for (String user: layoutUsers) {
			LibrisAttributes  attr = new LibrisAttributes();
			attr.setAttribute(XML_LAYOUT_USEDBY_ATTR, user);
			output.writeStartElement(XML_LAYOUTUSAGE_TAG, attr, true);
		}
		
		for (FieldPosition f: fieldList) {
			LibrisAttributes attr = f.getAttributes();
			output.writeStartElement(XML_LAYOUTFIELD_TAG, attr, true);
		}
		output.writeEndElement();
	}

	private String title = null;
	private String id;
	protected int height;
	protected int width;
	Schema mySchema = null;
	protected ArrayList<FieldPosition> fieldList;
	Vector <String> layoutUsers;
	protected FieldPosition positionList = null;
	
	public Layout(Schema schem) {
		mySchema = schem;
		fieldList = new ArrayList<FieldPosition>();
		layoutUsers = new Vector<String>(1);
	}
	
	private void parseUsage(ElementManager layoutMgr) throws InputException  {
		HashMap<String, String> attrs = layoutMgr.parseOpenTag();
		String usedBy = attrs.get(XML_LAYOUT_USEDBY_ATTR);
		layoutUsers.add(usedBy);
	}

	public void setId(String id) {
		this.id = id;
	}


	public String getId() {
		return id;
	}


	/**
	 * @return height in pixels of the window
	 */
	public int getHeight() {
		return height;
	}


	/**
	 * @return width in pixels of the window
	 */
	public int getWidth() {
		return width;
	}


	void readLayoutField(ElementManager fieldMgr, FieldPositionParameter fParams) throws InputException, DatabaseException {

		HashMap<String, String> values = fieldMgr.parseOpenTag();
		
		fParams.setParams(values);
		fParams.setFieldNum(mySchema.getFieldNum(fParams.getId()));
		addField(fParams);
		if (fieldMgr.hasNext()) {
			throw new XmlException(fieldMgr, "layoutfield cannot contain other elements");
		}	
	}

	public void addField(FieldPositionParameter fParams) throws DatabaseException {
		FieldPosition l = new FieldPosition(this, positionList, fParams);
		positionList = l;
		fieldList.add(l);
	}

	void setHeight(String h) {
		this.height = Integer.parseInt(h);
	}
	void setWidth(String w) {
		this.width = Integer.parseInt(w);		
	}
	public String getTitle() {
		return title;
	}

	abstract ArrayList<UiField> layOutFields(Record rec, JPanel recordPanel, ModificationTracker modTrk)
	throws DatabaseException;
	
	public void setIdAndTitle(String myTitle, String myId) throws DatabaseException {
		if (null == myId) {
			throw new DatabaseException("missing id attribute in layout element");
		}
		this.id = myId;
		this.title = (null == myTitle)? myId: myTitle;
	}

	public String[] getFieldIds() {
		Vector<String> fieldIds = new Vector<String>();
		for (FieldPosition p: fieldList) {
			fieldIds.add(p.getId());
		}
		return fieldIds.toArray(new String[fieldIds.size()]);
	}

	public String getFieldTitle(String id) {
		if (null == mySchema) {
			return id;
		} else {
			return mySchema.getFieldTemplate(id).getFieldTitle();
		}
	}
	
	public void getControlType(String id) {
		
	}

	FieldPosition[] getFields() {
		FieldPosition[] positions = new FieldPosition[fieldList.size()];
		fieldList.toArray(positions);
		return positions;
	}
	
	public abstract String getLayoutType();

	public FieldType getFieldType(String id) {
		return mySchema.getFieldType(id);
	}

	public int getNumberOfFields() {
		// TODO getNumberOfFields
		return 0;
	}

	public Iterable<String> getLayoutUsers() {
		return layoutUsers;
	}


	static Layout fromXml(Schema schem, ElementManager layoutMgr)
	throws InputException, DatabaseException {
		HashMap<String, String> values = layoutMgr.parseOpenTag();
		String layoutType = values.get(XML_LAYOUT_TYPE_ATTR);
		Layout l;
		if (XML_LAYOUT_TYPE_TABLE.equals(layoutType)) {
			l = new TableLayout(schem);
		} else if (layoutType.equalsIgnoreCase(LibrisXMLConstants.XML_LAYOUT_TYPE_FORM)) {
			l = new FormLayout(schem);
		} else if (layoutType.equalsIgnoreCase(LibrisXMLConstants.XML_LAYOUT_TYPE_LIST)) {
				l = new ListLayout(schem);
		} else {
			throw new InputException("layout type "+layoutType+" not supported");
		}
		l.setHeight(values.get("height"));
		l.setWidth(values.get("width"));
		FieldPositionParameter fParams = new FieldPositionParameter();
		l.setIdAndTitle(values.get("title"), values.get("id"));
		while (layoutMgr.hasNext()) {
			ElementManager subElementMgr = layoutMgr.nextElement();
			if (subElementMgr.getElementTag().equals(XML_LAYOUTFIELD_TAG)) {
				l.readLayoutField(subElementMgr, fParams);
			} else {
				l.parseUsage(subElementMgr);
			}
			subElementMgr.parseClosingTag();
		}
		l.validate();
		return l;
	}


	protected void validate() throws InputException {
		return;
	}


	@Override
	public boolean equals(Object obj) {
		try {
			Layout otherLayout = (Layout) obj;
			return otherLayout.getAttributes().equals(getAttributes());
		} catch (ClassCastException e) {
			db.log(Level.WARNING, "Type mismatch in Layout.equals()", e);
			return false;
		}
	}
}

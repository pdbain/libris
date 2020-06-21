package org.lasalledebain.libris.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;
import java.util.logging.Level;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.lasalledebain.libris.Field;
import org.lasalledebain.libris.Field.FieldType;
import org.lasalledebain.libris.LibrisDatabase;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.RecordList;
import org.lasalledebain.libris.Schema;
import org.lasalledebain.libris.XMLElement;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.exception.XmlException;
import org.lasalledebain.libris.xmlUtils.ElementManager;
import org.lasalledebain.libris.xmlUtils.ElementWriter;
import org.lasalledebain.libris.xmlUtils.LibrisAttributes;
import org.lasalledebain.libris.xmlUtils.LibrisXMLConstants;

public abstract class Layout<RecordType extends Record> implements XMLElement {
	protected int height;
	protected int width;
	Schema mySchema = null;
	protected ArrayList<FieldPosition> bodyFieldList;
	Vector <String> layoutUsers;
	protected FieldPosition positionList = null;
	private String id;
	private String title = null;
	final static ArrayList<UiField> emptyUiList = new ArrayList<>();

	public Layout(Schema schem) throws DatabaseException {
		mySchema = schem;
		bodyFieldList = new ArrayList<FieldPosition>();
		layoutUsers = new Vector<String>(1);
	}

	public Schema getSchema() {
		return mySchema;
	}

	public void setId(String id) {
		this.id = id;
	}


	public String getId() {
		return id;
	}

	public boolean isSingleRecord() {
		return true;
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
		addBodyField(fParams);
		if (fieldMgr.hasNext()) {
			throw new XmlException(fieldMgr, "layoutfield cannot contain other elements");
		}	
	}

	public void addBodyField(FieldPositionParameter fParams) throws DatabaseException {
		FieldPosition l = new FieldPosition(this, positionList, fParams);
		positionList = l;
		bodyFieldList.add(l);
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
	
	public int getNumFields() {
		return bodyFieldList.size();
	}

	abstract ArrayList<UiField> layOutFields(RecordType rec, LibrisWindowedUi ui, JComponent recordPanel, ModificationTracker modTrk)
			throws DatabaseException, LibrisException;

	ArrayList<UiField> layOutFields(RecordList<RecordType> recList, LibrisWindowedUi ui, JComponent recordPanel, ModificationTracker modTrk)
			throws DatabaseException, LibrisException {
		return 	layOutFields(recList.getFirstRecord(), ui,  recordPanel, modTrk);
	};

	public void setIdAndTitle(String myTitle, String myId) throws DatabaseException {
		if (null == myId) {
			throw new DatabaseException("missing id attribute in layout element");
		}
		this.id = myId;
		this.title = (null == myTitle)? myId: myTitle;
	}

	public String[] getFieldIds() {
		Vector<String> fieldIds = new Vector<String>();
		for (FieldPosition p: bodyFieldList) {
			fieldIds.add(p.getId());
		}
		return fieldIds.toArray(new String[fieldIds.size()]);
	}

	public String getFieldTitle(int id) {
		return bodyFieldList.get(id).title;
	}

	FieldPosition[] getFields() {
		FieldPosition[] positions = new FieldPosition[bodyFieldList.size()];
		bodyFieldList.toArray(positions);
		return positions;
	}

	public abstract String getLayoutType();

	public FieldType getFieldType(String id) {
		return mySchema.getFieldType(id);
	}

	public Iterable<String> getLayoutUsers() {
		return layoutUsers;
	}

	static Layout layoutFactory(Schema schem, ElementManager mgr)
			throws InputException, DatabaseException {
		HashMap<String, String> values = mgr.parseOpenTag();
		String layoutType = values.get(XML_LAYOUT_TYPE_ATTR);
		Layout l;
		switch (layoutType) {
		case XML_LAYOUT_TYPE_TABLE: 
			l = new TableLayout(schem);
			break;
		case LibrisXMLConstants.XML_LAYOUT_TYPE_FORM:
			l = new FormLayout(schem);
			break;
		case XML_LAYOUT_TYPE_LIST:
			l = new ListLayout(schem);
			break;
		case XML_LAYOUT_TYPE_PARAGRAPH:
			l = new ParagraphLayout(schem);
			break;
		default:
			throw new InputException("layout type "+layoutType+" not supported");
		}
		l.initialize(mgr, values);
		return l;
	}

	@Override
	public void fromXml(ElementManager mgr) throws LibrisException {
		HashMap<String, String> values = mgr.parseOpenTag();
		initialize(mgr, values);
	}

	protected void initialize(ElementManager layoutMgr, HashMap<String, String> values)
			throws DatabaseException, XmlException, InputException {
		setHeight(values.get("height"));
		setWidth(values.get("width"));
		FieldPositionParameter fParams = new FieldPositionParameter();
		setIdAndTitle(values.get("title"), values.get("id"));
		while (layoutMgr.hasNext()) {
			ElementManager subElementMgr = layoutMgr.nextElement();
			if (subElementMgr.getElementTag().equals(XML_LAYOUTFIELD_TAG)) {
				readLayoutField(subElementMgr, fParams);
			} else {
				parseUsage(subElementMgr);
			}
			subElementMgr.parseClosingTag();
		}
		validate();
	}

	public static String getXmlTag() {
		return XML_LAYOUT_TAG;
	}

	@Override
	public String getElementTag() {
		return getXmlTag();
	}

	@Override
	public void toXml(ElementWriter output) throws LibrisException {
		output.writeStartElement(getElementTag(), getAttributes(), false);
		for (String user: layoutUsers) {
			LibrisAttributes  attr = new LibrisAttributes();
			attr.setAttribute(XML_LAYOUT_USEDBY_ATTR, user);
			output.writeStartElement(XML_LAYOUTUSAGE_TAG, attr, true);
		}

		for (FieldPosition f: bodyFieldList) {
			LibrisAttributes attr = f.getAttributes();
			output.writeStartElement(XML_LAYOUTFIELD_TAG, attr, true);
		}
		output.writeEndElement();
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

	private void parseUsage(ElementManager layoutMgr) throws InputException  {
		HashMap<String, String> attrs = layoutMgr.parseOpenTag();
		String usedBy = attrs.get(XML_LAYOUT_USEDBY_ATTR);
		layoutUsers.add(usedBy);
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
			LibrisDatabase.log(Level.WARNING, "Type mismatch in Layout.equals()", e);
			return false;
		}
	}


	protected Field getField(Record rec, int fieldNum)
			throws LibrisException {
		Field fld = null;
		try {
			fld = rec.getField(fieldNum);
			if (null == fld) {
				fld = rec.getDefaultField(fieldNum);
			}
		} catch (InputException e) {
			throw new DatabaseException("Error in layout \""+getId()+"\"", e);			
		}
		return fld;
	}

	protected Field getField(Record rec, String fieldId)
			throws LibrisException {
		int fieldNum = mySchema.getFieldNum(fieldId);
		return getField(rec, fieldNum);
	}
	protected abstract void showRecord(int recId);
}

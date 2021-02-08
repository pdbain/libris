package org.lasalledebain.libris.ui;

import static org.lasalledebain.libris.util.StringUtils.isStringEmpty;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;
import java.util.stream.Stream;

import javax.swing.JComponent;

import org.lasalledebain.libris.Field;
import org.lasalledebain.libris.Field.FieldType;
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

public class LibrisLayout implements XMLElement {

	final static ArrayList<UiField> emptyUiList = new ArrayList<>();
	protected String id;
	protected String title = null;
	protected int height;
	protected int width;
	protected String layoutType;
	protected Schema mySchema = null;
	protected ArrayList<LayoutField> bodyFieldList;
	protected LayoutField positionList = null;
	protected ArrayList<String> layoutUsers;
	protected final Layouts myLayouts;
	protected LayoutProcessor layoutProc;
	private int tableRightEdge;
	private final GuiControlFactory ctrlFactory;

	public LibrisLayout(Schema schem, Layouts theLayouts) {
		mySchema = schem;
		bodyFieldList = new ArrayList<LayoutField>();
		layoutUsers = new ArrayList<String>(1);
		myLayouts = theLayouts;
		layoutProc = null;
		ctrlFactory = new GuiControlFactory();
	}

	public LibrisLayout(Schema schem) {
		this(schem, null);
	}

	@Override
	public String getElementTag() {
		return getXmlTag();
	}

	@Override
	public void fromXml(ElementManager mgr) throws LibrisException {
		LibrisAttributes values = mgr.parseOpenTag();
		setHeight(values.get("height"));
		setWidth(values.get("width"));
		setIdAndTitle(values.get("title"), values.get("id"));
		String theType = values.get(XML_LAYOUT_TYPE_ATTR);
		setType(theType);
		while (mgr.hasNext()) {
			ElementManager subElementMgr = mgr.nextElement();
			if (subElementMgr.getElementTag().equals(XML_LAYOUTFIELD_TAG)) {
				LayoutField l = new LayoutField(this, positionList, ctrlFactory);
				l.fromXml(subElementMgr);
				tableRightEdge = Math.max(l.getRightEdge(), tableRightEdge);
				l.setFieldNum(mySchema.getFieldNum(l.getId()));
				positionList = l;
				bodyFieldList.add(l);
				if (subElementMgr.hasNext()) {
					throw new XmlException(subElementMgr, "layoutfield cannot contain other elements");
				}
			} else {
				parseUsage(subElementMgr);
			}
			subElementMgr.parseClosingTag();
		}
		layoutProc = getLayoutProcessor(theType);
		if (null == layoutProc) {
			throw new InputException("Invalid layout type: "+theType, mgr);
		}
		layoutProc.validate();
	}

	private LayoutProcessor getLayoutProcessor(String theType) {
		LayoutProcessor result = null;
		switch (theType) {
		case XML_LAYOUT_TYPE_XML: 
			result = new XmlLayoutProcessor(this);
			break;
		case XML_LAYOUT_TYPE_TABLE: 
			result = new TableLayoutProcessor(this);
			break;
		case XML_LAYOUT_TYPE_FORM: {
			result = new FormLayoutProcessor(this, ctrlFactory);
			break;
		}

		case XML_LAYOUT_TYPE_LIST:
			result = new ListLayoutProcessor<>(this);
			break;
		case XML_LAYOUT_TYPE_PARAGRAPH: 
			result = new ParagraphLayoutProcessor(this);
			break;
		default: result = null;
		}
		return result;
	}

	@Override
	public void toXml(ElementWriter output) throws LibrisException {
		output.writeStartElement(getElementTag(), getAttributes(), false);
		for (String user: layoutUsers) {
			LibrisAttributes  attr = new LibrisAttributes();
			attr.setAttribute(XML_LAYOUT_USEDBY_ATTR, user);
			output.writeStartElement(XML_LAYOUTUSAGE_TAG, attr, true);
		}

		for (LayoutField f: bodyFieldList) {
			LibrisAttributes attr = f.getAttributes();
			output.writeStartElement(XML_LAYOUTFIELD_TAG, attr, true);
		}
		output.writeEndElement();
	}

	private void parseUsage(ElementManager layoutMgr) throws InputException {
		LibrisAttributes attrs = layoutMgr.parseOpenTag();
		String usedBy = attrs.get(XML_LAYOUT_USEDBY_ATTR);
		layoutUsers.add(usedBy);
	}

	@Override
	public LibrisAttributes getAttributes() {
		LibrisAttributes attrs = new LibrisAttributes();
		attrs.setAttribute(XML_LAYOUT_ID_ATTR, getId());
		if (null != title) {
			attrs.setAttribute(LibrisXMLConstants.XML_LAYOUT_TITLE_ATTR, title);
		}
		attrs.setAttribute(XML_LAYOUT_TYPE_ATTR, layoutType);
		attrs.setAttribute(XML_LAYOUT_HEIGHT_ATTR, Integer.toString(getHeight()));
		attrs.setAttribute(XML_LAYOUT_WIDTH_ATTR, Integer.toString(getWidth()));
		return attrs;
	}

	LayoutField[] getFields() {
		LayoutField[] positions = new LayoutField[bodyFieldList.size()];
		bodyFieldList.toArray(positions);
		return positions;
	}

	public ArrayList<UiField> layOutFields(Record rec, LibrisWindowedUi ui, JComponent recordPanel, ModificationTracker modTrk)
			throws DatabaseException, LibrisException {
		return layoutProc.layOutFields(rec, ui, recordPanel, modTrk);
	}

	public ArrayList<UiField> layOutFields(RecordList<Record> recList, LibrisWindowedUi ui, JComponent recordPanel, ModificationTracker modTrk)
			throws DatabaseException, LibrisException {
		return 	layoutProc.layOutFields(recList, ui, recordPanel, modTrk);
	};
	
	public void layOutPage(RecordList<Record> recList, HttpParameters parameterObject, 
			LibrisLayout browserLayout, DatabaseUi ui) throws InputException, IOException {
		layoutProc.layOutPage(recList, parameterObject, browserLayout, ui);
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

	public int getNumFields() {
		return bodyFieldList.size();
	}

	public Schema getSchema() {
		return mySchema;
	}
	
	Stream <LibrisLayout> getLayouts() {
		return myLayouts.getLayouts();
	}


	public String getId() {
		return id;
	}

	public String getTitle() {
		return isStringEmpty(title)? "<unknown>": title;
	}

	public String[] getFieldIds() {
		Vector<String> fieldIds = new Vector<String>();
		for (LayoutField p: bodyFieldList) {
			fieldIds.add(p.getId());
		}
		return fieldIds.toArray(new String[fieldIds.size()]);
	}

	public String getFieldTitle(int id) {
		return bodyFieldList.get(id).title;
	}

	public void setId(String id) {
		this.id = id;
	}


	public void setIdAndTitle(String myTitle, String myId) throws DatabaseException {
		if (null == myId) {
			throw new DatabaseException("missing id attribute in layout element");
		}
		this.id = myId;
		this.title = (null == myTitle)? myId: myTitle;
	}

	public boolean isSingleRecord() {
		return true;
	}

	public boolean isEditable() {
		return false;
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

	protected void setHeight(String h) {
		height = Integer.parseInt(h);
	}

	protected void setWidth(String w) {
		width = Integer.parseInt(w);
	}

	public FieldType getFieldType(String id) {
		return mySchema.getFieldType(id);
	}

	public Iterable<String> getLayoutUsers() {
		return layoutUsers;
	}


	public static String getXmlTag() {
		return XML_LAYOUT_TAG;
	}

	@Override
	public boolean equals(Object obj) {
		if (getClass().isAssignableFrom(obj.getClass())) {
			LibrisLayout comparand = (LibrisLayout) obj;
			return comparand.getAttributes().equals(getAttributes());
		} else {
			return false;
		}
	}

	public void setType(String theType) {
		this.layoutType = theType;
	}

	ArrayList<LayoutField> getBodyFieldList() {
		return bodyFieldList;
	}
	protected void showRecord(int recId) {
		return;
	}

	int getTableRightEdge() {
		return tableRightEdge;
	}

}

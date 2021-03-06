package org.lasalledebain.libris.ui;

import java.awt.Dimension;
import java.util.HashMap;
import java.util.Iterator;

import org.lasalledebain.libris.Field.FieldType;
import org.lasalledebain.libris.XMLElement;
import org.lasalledebain.libris.exception.Assertion;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.xmlUtils.ElementManager;
import org.lasalledebain.libris.xmlUtils.ElementWriter;
import org.lasalledebain.libris.xmlUtils.LibrisAttributes;
import org.lasalledebain.libris.xmlUtils.LibrisXMLConstants;

public class LayoutField implements XMLElement, Iterable<LayoutField>, LibrisXMLConstants {
	protected String controlTypeName;
	protected String id;
	protected String title;
	protected int fieldNum;
	int height = -1, width = -1, vpos = -1, hpos = -1, hspan = -1, vspan = -1;
	private LayoutField prevLink;
	private LibrisLayout containingLayout;
	protected GuiControlConstructor ctrlConstructor;
	private boolean carriageReturn = false;
	private int myRightEdge;
	private final GuiControlFactory ctrlFactory;

	public LayoutField(LibrisLayout containingLayout, LayoutField previous, GuiControlFactory theFactory) throws DatabaseException {
		this.containingLayout = containingLayout;
		prevLink = previous;
		ctrlFactory = theFactory;
	}

	public void setFieldNum(int fieldNum) {
		this.fieldNum = fieldNum;
	}

	protected static final HashMap<FieldType, String> defaultControlType = initializeDefaultControlTypes();
	private static HashMap<FieldType, String> initializeDefaultControlTypes() {
		HashMap<FieldType, String> temp = new HashMap<FieldType, String>();
		temp.put(FieldType.T_FIELD_BOOLEAN, GuiConstants.GUI_CHECKBOX);
		temp.put(FieldType.T_FIELD_ENUM, GuiConstants.GUI_ENUMFIELD);
		temp.put(FieldType.T_FIELD_INDEXENTRY, GuiConstants.GUI_TEXTFIELD);
		temp.put(FieldType.T_FIELD_INTEGER, GuiConstants.GUI_TEXTFIELD);
		temp.put(FieldType.T_FIELD_PAIR, GuiConstants.GUI_PAIRFIELD);
		temp.put(FieldType.T_FIELD_STRING, GuiConstants.GUI_TEXTFIELD);
		temp.put(FieldType.T_FIELD_TEXT, GuiConstants.GUI_TEXTBOX);
		temp.put(FieldType.T_FIELD_LOCATION, GuiConstants.GUI_LOCATIONFIELD);
		temp.put(FieldType.T_FIELD_AFFILIATES, GuiConstants.GUI_NAMES_BROWSER);
		return temp;
	}

	private int checkForOverlap() throws DatabaseException {
		int myRightEdge = hpos+hspan-1;
		LayoutField cursor = prevLink;
		while (null != cursor) {
			int otherBottomEdge = cursor.vpos+cursor.vspan-1;
			int otherRightEdge = cursor.hpos+cursor.hspan-1;
			if (vpos <= otherBottomEdge) { /* previous fields can be on same or higher rows */
				if (!((myRightEdge < cursor.hpos) || (hpos > otherRightEdge))) {
					throw new DatabaseException(containingLayout.getId()+": field "+id+" overlaps "+cursor.id);
				}
			}
			cursor = cursor.prevLink;
		}
		return myRightEdge;
	}

	public GuiControlConstructor getControlContructor() {
		return ctrlConstructor;
	}

	public int getFieldNum() {
		return fieldNum;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getVpos() {
		return vpos;
	}

	public void setVpos(int row) {
		this.vpos = row;
	}

	public int getHpos() {
		return hpos;
	}

	public void setHpos(int column) {
		this.hpos = column;
	}

	public int getHspan() {
		return hspan;
	}	

	public int getVspan() {
		return vspan;
	}

	public boolean isCarriageReturn() {
		return carriageReturn;
	}

	@Override
	public Iterator<LayoutField> iterator() {
		return new FieldPositionIterator(this);
	}

	public class FieldPositionIterator implements Iterator<LayoutField> {
		LayoutField cursor;
		/**
		 * @param cursor
		 */
		public FieldPositionIterator(LayoutField cursor) {
			this.cursor = cursor;
		}

		@Override
		public boolean hasNext() {
			return cursor != null;
		}

		@Override
		public LayoutField next() {
			LayoutField result = cursor;
			if (null != cursor) {
				cursor = cursor.prevLink;
			}
			return result;
		}

		@Override
		public void remove() {
			return;
		}

	}

	public LibrisAttributes getAttributes() {
		LibrisAttributes attrs = new LibrisAttributes();
		attrs.setAttribute(XML_LAYOUTFIELD_ID_ATTR, id);
		if (null != title) {
			attrs.setAttribute(XML_LAYOUTFIELD_TITLE_ATTR, title);			
		}
		if (carriageReturn) {
			attrs.addAttribute(XML_LAYOUTFIELD_RETURN_ATTR, carriageReturn);						
		}
		if (height >= 0) {
			attrs.setAttribute(XML_LAYOUTFIELD_HEIGHT_ATTR, height);						
		}
		if (width >= 0) {
			attrs.setAttribute(XML_LAYOUTFIELD_WIDTH_ATTR, width);						
		}
		if (vspan >= 0) {
			attrs.setAttribute(XML_LAYOUTFIELD_VSPAN_ATTR, vspan);						
		}
		if (hspan >= 0) {
			attrs.setAttribute(XML_LAYOUTFIELD_HSPAN_ATTR, hspan);						
		}
		attrs.setAttribute(XML_LAYOUTFIELD_CONTROL_ATTR, controlTypeName);
		return attrs;
	}

	@Override
	public String getElementTag() {
		return LibrisXMLConstants.XML_LAYOUTFIELD_TAG;
	}

	@Override
	public void fromXml(ElementManager mgr) throws LibrisException {
		LibrisAttributes values = mgr.parseOpenTag();

		id = values.get("id");
		title = values.get("title");

		String controlType = values.get("control").intern();
		if (controlType.equals(LibrisXMLConstants.DEFAULT_GUI_CONTROL)) {
			controlTypeName = GuiConstants.GUI_TEXTBOX;
			controlType = defaultControlType.get(containingLayout.getFieldType(id));
		} else {
			controlTypeName = controlType;
		}
		
		Assertion.assertNotNullError("unrecognized control type "+controlTypeName, ctrlConstructor = ctrlFactory.getControlConstructor(controlTypeName));

		Dimension dims = Layouts.getDefaultDimensions(controlType);

		String heightString = values.get("height");
		if (heightString.isEmpty()) {
			height = (null == dims)? -1: dims.height;
		} else {
			height = Integer.parseInt(heightString);			
		}
		String widthString = values.get("width");
		if (widthString.isEmpty()) {
			width = (null == dims)? -1: dims.width;
		} else {
			width = Integer.parseInt(widthString);			
		}

		if ((null != dims) && ((height < 1) || (width < 1))) {
			throw new DatabaseException("field dimensions must be positive");
		}

		hspan = Integer.parseInt(values.get("hspan"));
		vspan = Integer.parseInt(values.get("vspan"));
		if ((hspan < 1) || (vspan < 1)) {
			throw new DatabaseException("field dimensions, spans, and motions must be positive");
		}

		carriageReturn = Boolean.parseBoolean(values.get("return"));
		if (prevLink != null) {
			if (prevLink.isCarriageReturn()) {
				vpos = prevLink.getVpos() + 1;
				hpos = 0;
			} else {
				vpos = prevLink.getVpos();
				hpos = prevLink.getHpos() + prevLink.getHspan();
			}
		} else {
			hpos = vpos = 0;
		}
		myRightEdge = checkForOverlap();
	}

	public int getRightEdge() {
		return myRightEdge;
	}
	@Override
	public void toXml(ElementWriter output) throws LibrisException {
		LibrisAttributes attr = getAttributes();
		output.writeStartElement(XML_LAYOUTFIELD_TAG, attr, true);		
	}

	public String getTitle() {
		String result = ((null != title) && !title.isEmpty())? title: containingLayout.getSchema().getFieldTitle(fieldNum);
		return result;
	}

	public String getId() {
		return id;
	}

	public String getControlTypeName() {
		return controlTypeName;
	}

}

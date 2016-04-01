package org.lasalledebain.libris.ui;

import java.util.HashMap;
import java.util.Iterator;

import org.lasalledebain.libris.Field.FieldType;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.ui.GuiControlFactory.ControlConstructor;
import org.lasalledebain.libris.xmlUtils.LibrisAttributes;
import org.lasalledebain.libris.xmlUtils.LibrisXMLConstants;

public class FieldPosition implements Iterable<FieldPosition>, LibrisXMLConstants, GuiConstants {
	public boolean isCarriageReturn() {
		return carriageReturn;
	}

	int height = -1, width = -1, vpos = -1, hpos = -1, hspan = -1, vspan = -1;
	private String id, title;
	public String getTitle() {
		return title;
	}

	private FieldPosition prevLink;
	private Layout containingLayout;
	private ControlConstructor control;
	private final String controlTypeName;
	public String getControlTypeName() {
		return controlTypeName;
	}

	private boolean carriageReturn = false;
	private int fieldNum;
	static private HashMap<FieldType, String> defaultControlType = initializeDefaultControlTypes();

	public ControlConstructor getControlContructor() {
		return control;
	}

	private static HashMap<FieldType, String> initializeDefaultControlTypes() {
		HashMap<FieldType, String> temp = new HashMap<FieldType, String>();
		temp.put(FieldType.T_FIELD_BOOLEAN, GuiConstants.GUI_CHECKBOX);
		temp.put(FieldType.T_FIELD_ENUM, GuiConstants.GUI_ENUMFIELD);
		temp.put(FieldType.T_FIELD_INDEXENTRY, GuiConstants.GUI_TEXTFIELD);
		temp.put(FieldType.T_FIELD_INTEGER, GuiConstants.GUI_TEXTFIELD);
		temp.put(FieldType.T_FIELD_PAIR, GuiConstants.GUI_PAIRFIELD);
		temp.put(FieldType.T_FIELD_STRING, GuiConstants.GUI_TEXTFIELD);
		temp.put(FieldType.T_FIELD_TEXT, GuiConstants.GUI_TEXTBOX);
		return temp;
	}

	public String getId() {
		return id;
	}

	public FieldPosition(Layout containingLayout, FieldPosition previous, FieldPositionParameter params) throws DatabaseException {
		id = params.getId();
		fieldNum = params.getFieldNum();
		width = params.getWidth();
		height = params.getHeight();
		hspan = params.getHspan();
		vspan = params.getVspan();
		carriageReturn = params.getReturn();
		if (previous != null) {
			if (previous.isCarriageReturn()) {
				vpos = previous.getVpos() + 1;
				hpos = 0;
			} else {
				vpos = previous.getVpos();
				hpos = previous.getHpos() + previous.getHspan();
			}
		} else {
			hpos = vpos = 0;
		}
		String controlType = params.getControlType();
		if (controlType.equals(LibrisXMLConstants.DEFAULT_GUI_CONTROL)) {
			controlTypeName = GUI_TEXTBOX;
			controlType = defaultControlType.get(containingLayout.getFieldType(id));
		} else {
			controlTypeName = controlType;
		}
		this.control=GuiControlFactory.getControlConstructor(controlType);
		if (null == control) {
			throw new DatabaseException("unrecognized control type "+controlType);
		}
		prevLink = previous;
		this.containingLayout = containingLayout;
		title = params.getTitle();
		if ((null == title) || title.isEmpty()) {
			title = containingLayout.getFieldTitle(id);
		}
		checkForOverlap();
	}

	private void checkForOverlap() throws DatabaseException {
		int myRightEdge = hpos+hspan-1;
		FieldPosition cursor = prevLink;
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

	@Override
	public Iterator<FieldPosition> iterator() {
		return new FieldPositionIterator(this);
	}

	public class FieldPositionIterator implements Iterator<FieldPosition> {
		FieldPosition cursor;
		/**
		 * @param cursor
		 */
		public FieldPositionIterator(FieldPosition cursor) {
			this.cursor = cursor;
		}
	
		@Override
		public boolean hasNext() {
			return cursor != null;
		}
	
		@Override
		public FieldPosition next() {
			FieldPosition result = cursor;
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

	public LibrisAttributes getAttributes() throws DatabaseException {
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

	public int getFieldNum() {
		return fieldNum;
	}

}

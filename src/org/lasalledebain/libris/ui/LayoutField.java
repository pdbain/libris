package org.lasalledebain.libris.ui;

import java.util.Iterator;

import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.ui.GuiControlFactory.ControlConstructor;
import org.lasalledebain.libris.xmlUtils.ElementManager;
import org.lasalledebain.libris.xmlUtils.ElementWriter;
import org.lasalledebain.libris.xmlUtils.LibrisAttributes;

public class LayoutField<RecordType extends Record> extends FieldInfo<RecordType> implements Iterable<LayoutField<RecordType>> {
	protected final int fieldNum;
	int height = -1, width = -1, vpos = -1, hpos = -1, hspan = -1, vspan = -1;
	private LayoutField<RecordType> prevLink;
	private Layout<RecordType> containingLayout;
	protected final ControlConstructor control;

	public boolean isCarriageReturn() {
		return carriageReturn;
	}

	private boolean carriageReturn = false;
	public LayoutField(Layout<RecordType> containingLayout, LayoutField<RecordType> previous, FieldPositionParameter params) throws DatabaseException {
		super(containingLayout, params.getControlType(), params.getId(), params.getTitle());
		control=GuiControlFactory.getControlConstructor(controlTypeName);
		if (null == control) {
			throw new DatabaseException("unrecognized control type "+controlTypeName);
		}
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
		prevLink = previous;
		checkForOverlap();
	}

	private void checkForOverlap() throws DatabaseException {
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
	}

	public ControlConstructor getControlContructor() {
		return control;
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

	@Override
	public Iterator<LayoutField<RecordType>> iterator() {
		return new FieldPositionIterator(this);
	}

	public class FieldPositionIterator implements Iterator<LayoutField<RecordType>> {
		LayoutField<RecordType> cursor;
		/**
		 * @param cursor
		 */
		public FieldPositionIterator(LayoutField<RecordType> cursor) {
			this.cursor = cursor;
		}
	
		@Override
		public boolean hasNext() {
			return cursor != null;
		}
	
		@Override
		public LayoutField<RecordType> next() {
			LayoutField<RecordType> result = cursor;
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

	@Override
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void fromXml(ElementManager mgr) throws LibrisException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void toXml(ElementWriter output) throws LibrisException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getControlTypeName() {
		return controlTypeName;
	}

}

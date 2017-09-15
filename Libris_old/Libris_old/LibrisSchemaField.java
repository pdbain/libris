/*
 * Created on Dec 25, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package Libris;

import org.xml.sax.Attributes;

import Libris.LibrisFieldDatatype.FieldType;
import Libris.LibrisSchema.librisEnumSet;

/**
 * @author pdbain
 *
 * Contains the schema information for a particular field
 */
public class LibrisSchemaField {

	String id; 
	String parent;
	String name;
	int xposition, yposition, height, width;
	FieldType datatype; // (string|boolean|INTEGER|INTEGERS|INDEXENTRY)
	boolean summary, indexable, printable, visible, editable;
	private int fieldNum;
	private int verbosity = 0;
	private boolean restricted;
	private librisEnumSet enumset;
	public librisEnumSet getEnumset() {
		return enumset;
	}

	/**
	 * @param schema 
	 * @param attrs Attributes of the field
	 */
	private LibrisSchema schema;
	public LibrisSchemaField(LibrisSchema schema, Attributes attrs) {
		this.schema = schema;
		id = attrs.getValue("id");
		parent = attrs.getValue("parent");
		name = attrs.getValue("name");
		logMsg("Creating field "+name);
		try {
			String value;
			value = new String();
			xposition = ((value = attrs.getValue("xposition")) == null)? 
					0: Integer.parseInt(value);
			yposition =  ((value = attrs.getValue("yposition")) == null)? 
					0: Integer.parseInt(value);
			datatype = LibrisFieldDatatype.getDatatype(
					((value = attrs.getValue("datatype")) == null)? "string":value);
 			height =  ((value = attrs.getValue("height")) == null)? 
					0: Integer.parseInt(value);
			width =  ((value = attrs.getValue("width")) == null)? 
					0: Integer.parseInt(value);
			summary = ((value = attrs.getValue("summary")) == null)? 
					true: Boolean.valueOf(value);
			indexable = ((value = attrs.getValue("indexable")) == null)? 
					true: Boolean.valueOf(value);
			printable = ((value = attrs.getValue("printable")) == null)? 
					true: Boolean.valueOf(value);
			visible = ((value = attrs.getValue("visible")) == null)? 
					true: Boolean.valueOf(value);
			editable = ((value = attrs.getValue("editable")) == null)? 
					true: Boolean.valueOf(value);
			restricted = ((value = attrs.getValue("restricted")) == null)? 
					true: Boolean.valueOf(value);
			if ((value = attrs.getValue("enumset")) != null) {
				enumset = schema.getEnumSet(value);
				if (null == enumset) {
					throw new LibrisException(LibrisException.ErrorIds.ERR_UNDEFINED_ENUMSET_ID, value+" from "+name);
				}

			}
			if ((datatype == FieldType.T_FIELD_ENUM) && (null == enumset)) {
				throw new LibrisException(LibrisException.ErrorIds.ERR_NO_ENUMSET_ID, name);
			} else if ((datatype != FieldType.T_FIELD_ENUM) && (null != enumset)) {
				throw new LibrisException(LibrisException.ErrorIds.ERR_ENUMSET_IN_NON_ENUM_FIELD, name);
			}
		} catch (Throwable t) {
            System.out.println("Syntax parsing"+id+attrs.toString());
		}
	}
	
	private void logMsg(String msg) {
		LibrisMain.logMsg(verbosity , msg);
	}

	/**
	 * @return Returns the datatype.
	 */
	public FieldType getDatatype() {
		return datatype;
	}
	/**
	 * @param datatype The datatype to set.
	 */
	public void setDatatype(FieldType datatype) {
		this.datatype = datatype;
	}
	/**
	 * @return Returns the editable.
	 */
	public boolean isEditable() {
		return editable;
	}
	/**
	 * @param editable The editable to set.
	 */
	public void setEditable(boolean editable) {
		this.editable = editable;
	}
	/**
	 * @return Returns the height.
	 */
	public int getHeight() {
		return height;
	}
	/**
	 * @param height The height to set.
	 */
	public void setHeight(int height) {
		this.height = height;
	}
	/**
	 * @return Returns the id.
	 */
	public String getId() {
		return id;
	}
	/**
	 * @param id The id to set.
	 */
	public void setId(String id) {
		this.id = id;
	}
	/**
	 * @return Returns the indexable.
	 */
	public boolean isIndexable() {
		return indexable;
	}
	/**
	 * @param indexable The indexable to set.
	 */
	public void setIndexable(boolean indexable) {
		this.indexable = indexable;
	}
	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name The name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return Returns the parent.
	 */
	public String getParent() {
		return parent;
	}
	/**
	 * @param parent The parent to set.
	 */
	public void setParent(String parent) {
		this.parent = parent;
	}
	/**
	 * @return Returns the printable.
	 */
	public boolean isPrintable() {
		return printable;
	}
	/**
	 * @param printable The printable to set.
	 */
	public void setPrintable(boolean printable) {
		this.printable = printable;
	}
	/**
	 * @return Returns the summary.
	 */
	public boolean isSummary() {
		return summary;
	}
	/**
	 * @param summary The summary to set.
	 */
	public void setSummary(boolean summary) {
		this.summary = summary;
	}
	/**
	 * @return Returns the visible.
	 */
	public boolean isVisible() {
		return visible;
	}
	/**
	 * @param visible The visible to set.
	 */
	public void setVisible(boolean visible) {
		this.visible = visible;
	}
	/**
	 * @return Returns the width.
	 */
	public int getWidth() {
		return width;
	}
	/**
	 * @param width The width to set.
	 */
	public void setWidth(int width) {
		this.width = width;
	}
	/**
	 * @return Returns the xposition.
	 */
	public int getXposition() {
		return xposition;
	}
	/**
	 * @param xposition The xposition to set.
	 */
	public void setXposition(int xposition) {
		this.xposition = xposition;
	}
	/**
	 * @return Returns the yposition.
	 */
	public int getYposition() {
		return yposition;
	}
	/**
	 * @param yposition The yposition to set.
	 */
	public void setYposition(int yposition) {
		this.yposition = yposition;
	}

	/**
	 * @param i
	 */
	public void setFieldNum(int i) {
		this.fieldNum = i;
	}

	/**
	 * @return
	 */
	public int getFieldNum() {
		return(this.fieldNum);
	}

	public boolean isRestricted() {
		return restricted;
	}

	public String getEnumChoice(String valueOrId) throws LibrisException {
		if (null == enumset) {
			throw new LibrisException(LibrisException.ErrorIds.ERR_NO_ENUMSET_ID, name);
		}
		return null;
	}

}

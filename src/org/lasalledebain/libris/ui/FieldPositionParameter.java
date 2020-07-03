package org.lasalledebain.libris.ui;

import java.awt.Dimension;
import java.util.HashMap;

import org.lasalledebain.libris.exception.DatabaseException;


@Deprecated
public class FieldPositionParameter {
	public String id;
	int fieldNum = -1;
	public int width;
	public int height;
	public int hspan;
	public int vspan;
	private String title;
	private String controlType;
	public boolean carriageReturn;
	public int across;

	public FieldPositionParameter() {
		id = "";
		fieldNum = -1;
		width = 0;
		height = 0;
		hspan = 0;
		vspan = 0;
		title = null;
		controlType = "";
		carriageReturn = false;
		across = 0;
	}

	public void setControlType(String controlType) {
		this.controlType = controlType;
	}

	public void setParams(HashMap<String, String> values) throws DatabaseException {
		id = values.get("id");
		title = values.get("title");
		if ((null == title) || title.isEmpty()) {
			title = id;
		}
		carriageReturn = Boolean.parseBoolean(values.get("return"));
		controlType = values.get("control").intern();
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
	}

	public int getFieldNum() {
		return fieldNum;
	}

	public void setFieldNum(int fieldNum) {
		this.fieldNum = fieldNum;
	}

	public String getControlType() {
		return controlType;
	}

	public String getId() {
		return id;
	}

	public boolean getReturn() {
		return carriageReturn;
	}

	public int getAcross() {
		return across;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public int getHspan() {
		return hspan;
	}

	public int getVspan() {
		return vspan;
	}

	public String getTitle() {
		return title;
	}
}
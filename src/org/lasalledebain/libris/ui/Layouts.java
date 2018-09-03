package org.lasalledebain.libris.ui;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;
import java.util.logging.Level;

import org.lasalledebain.libris.LibrisDatabase;
import org.lasalledebain.libris.Schema;
import org.lasalledebain.libris.XMLElement;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.xmlUtils.ElementManager;
import org.lasalledebain.libris.xmlUtils.ElementShape;
import org.lasalledebain.libris.xmlUtils.ElementWriter;
import org.lasalledebain.libris.xmlUtils.LibrisAttributes;
import org.lasalledebain.libris.xmlUtils.LibrisEmptyAttributes;
import org.lasalledebain.libris.xmlUtils.XmlShapes;

public class Layouts implements XMLElement {
	public static final String DEFAULT_LAYOUT_VALUE = "default";
	HashMap<String, Layout> layouts;
	ArrayList<String> layoutIds;
	HashMap <String,Layout> usage = new HashMap<String, Layout>();
	private Schema schem;
	
	protected static HashMap<String, Dimension> defaultDimensionStrings = initializeDefaultDimensions();
	public Layouts(Schema mySchema) {
		schem = mySchema;
		layouts = new HashMap<String, Layout>();
		layoutIds = new ArrayList<String>();
	}

	private static HashMap<String, Dimension> initializeDefaultDimensions() {
		HashMap<String, Dimension> dims = new HashMap<String, Dimension>();
		dims.put(GuiConstants.GUI_TEXTBOX, new Dimension(25,1));
		dims.put(GuiConstants.GUI_PAIRFIELD, new Dimension(3,1));
		dims.put(GuiConstants.GUI_LOCATIONFIELD, new Dimension(25,1));
		return dims;
	}

	public void fromXml(ElementManager mgr) throws InputException, DatabaseException {
		mgr.parseOpenTag();
		while (mgr.hasNext()) {
			ElementManager layoutMgr = mgr.nextElement();
			Layout l = Layout.layoutFactory(schem, layoutMgr);
			String id = l.getId();
			layouts.put(id, l);
			layoutIds.add(id);
			Iterable<String> layoutUsers = l.getLayoutUsers();
			for (String u: layoutUsers) {
				if (usage.containsKey(u)) {
					throw new DatabaseException(layoutMgr, "duplicate layout user "+u);
				} else {
					usage.put(u, l);
				}
			}
		}
		mgr.parseClosingTag();
	}
	
	public Layout getLayout(String id) {
		Layout l = layouts.get(id);
		return l;
	}

	public String[]	getLayoutIds() {
		return layoutIds.toArray(new String[layoutIds.size()]);
	}

	public static Dimension getDefaultDimensions(String controlType) {
		Dimension dims = defaultDimensionStrings.get(controlType);
		return dims;
	}

	/**
	 * Get a sorted list of layouts of a given type 
	 * @param layoutType
	 */
	public String[] getLayoutIds(String layoutType) {
		String[] ids = getLayoutIds();
		Vector<String> typeIds = new Vector<String>(ids.length);
		for (String i: ids) {
			if (getLayout(i).getLayoutType().equals(layoutType)) {
				typeIds.add(i);
			}
		}
		return typeIds.toArray(new String[typeIds.size()]);
	}
	
	public Layout getLayoutByUsage(String user) throws DatabaseException {
		Layout l = usage.get(user);
		if (null == l) {
			throw new DatabaseException("no layout defined for "+user);
		} else {
			return l;
		}
	}

	@Override
	public void toXml(ElementWriter output) throws LibrisException {
		output.writeStartElement(XML_LAYOUTS_TAG);
		for (String id: getLayoutIds()) {
			getLayout(id).toXml(output);
		}
		output.writeEndElement();	
	}

	@Override
	public LibrisAttributes getAttributes() {
		return LibrisEmptyAttributes.getLibrisEmptyAttributes();
	}

	@Override
	public boolean equals(Object comparand) {
		try {
			Layouts otherLayouts = (Layouts) comparand;
			return otherLayouts.layouts.equals(layouts);
		} catch (ClassCastException e) {
			final String msg = "type mismatch in Layouts.equals()";
			LibrisDatabase.log(Level.WARNING, msg, e);
			return false;
		}
	}

	public static String getXmlTag() {
		return XML_LAYOUTS_TAG;
	}
	static public ElementShape getShape() {
		return XmlShapes.makeShape(getTag(), 
				new String [] {XML_LAYOUT_TAG},
				XmlShapes.emptyRequiredAttributesList,
				XmlShapes.emptyOptionalAttributesList,
				false);
	}
	private static String getTag() {
		return getXmlTag();
	}

	public int getFieldNum() {
		return 0;
	}

	public String getId() {
		return null;
	}

	public String getTitle() {
		return null;
	}

	@Override
	public String getElementTag() {
		return getXmlTag();
	}
}

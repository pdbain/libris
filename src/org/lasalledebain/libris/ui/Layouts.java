package org.lasalledebain.libris.ui;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;
import java.util.function.Function;
import java.util.logging.Level;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.XMLEvent;

import org.lasalledebain.libris.LibrisDatabase;
import org.lasalledebain.libris.Record;
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

import static java.util.Objects.isNull;

public class Layouts<RecordType extends Record> implements XMLElement {
	public static final String DEFAULT_LAYOUT_VALUE = "default";
	private final static HashMap<String, Function<Schema, Layout>> layoutGenerators = populateLayoutGenerators();

	private static HashMap<String, Function<Schema, Layout>> populateLayoutGenerators() {
		HashMap<String, Function<Schema, Layout>> gens = new HashMap<>(5);
		gens.put(XML_LAYOUT_TYPE_XML, schem -> new XMLLayout(schem));
		gens.put(XML_LAYOUT_TYPE_TABLE, schem -> new TableLayout(schem));
		gens.put(XML_LAYOUT_TYPE_FORM, schem ->  new FormLayout(schem));
		gens.put(XML_LAYOUT_TYPE_LIST, schem ->  new ListLayout(schem));
		gens.put(XML_LAYOUT_TYPE_PARAGRAPH, schem ->  new ParagraphLayout(schem));

		return gens;
	}

	HashMap<String, Layout<RecordType>> layouts;
	ArrayList<String> layoutIds;
	HashMap <String,Layout<RecordType>> usage = new HashMap<>();
	private Schema schem;

	protected static HashMap<String, Dimension> defaultDimensionStrings = initializeDefaultDimensions();
	public Layouts(Schema mySchema) {
		schem = mySchema;
		layouts = new HashMap<>();
		layoutIds = new ArrayList<String>();
	}

	private static HashMap<String, Dimension> initializeDefaultDimensions() {
		HashMap<String, Dimension> dims = new HashMap<String, Dimension>();
		dims.put(GuiConstants.GUI_TEXTBOX, new Dimension(25,1));
		dims.put(GuiConstants.GUI_PAIRFIELD, new Dimension(3,1));
		dims.put(GuiConstants.GUI_LOCATIONFIELD, new Dimension(25,1));
		return dims;
	}

	public void fromXml(ElementManager mgr) throws LibrisException {
		mgr.parseOpenTag();
		while (mgr.hasNext()) {
			try {
				XMLEvent nextElement = mgr.peek();
				Attribute layoutTypeAttr = nextElement.asStartElement().getAttributeByName(new QName(XML_LAYOUT_TYPE_ATTR));
				if (isNull(layoutTypeAttr)) {
					throw new InputException("Element "+nextElement+" missing ID attribute");
				}
				String layoutType = layoutTypeAttr.getValue();
				Function<Schema, Layout> gen = layoutGenerators.get(layoutType);
				if (isNull(gen)) {
					throw new InputException("Layout type "+layoutType+" undefined");
				}
				Layout<RecordType> theLayout = gen.apply(schem);
				ElementManager layoutMgr = mgr.nextElement();
				theLayout.fromXml(layoutMgr);
				String layoutId = theLayout.getId();
				layouts.put(layoutId, theLayout);
				layoutIds.add(layoutId);
				Iterable<String> layoutUsers = theLayout.getLayoutUsers();
				for (String u: layoutUsers) {
					if (usage.containsKey(u)) {
						throw new DatabaseException(layoutMgr, "duplicate layout user "+u);
					} else {
						usage.put(u, theLayout);
					}
				}
			} catch (XMLStreamException e) {
				throw new InputException(e);
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

	public Layout<RecordType> getLayoutByUsage(String user) throws DatabaseException {
		Layout<RecordType> l = usage.get(user);
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
			Layouts<RecordType> otherLayouts = (Layouts<RecordType>) comparand;
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

package org.lasalledebain.libris.ui;

import static java.util.Objects.isNull;
import static org.lasalledebain.libris.exception.Assertion.assertNotNullDatabaseException;
import static org.lasalledebain.libris.exception.Assertion.assertNotNullError;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashMap;
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
import org.lasalledebain.libris.exception.Assertion;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.xmlUtils.ElementManager;
import org.lasalledebain.libris.xmlUtils.ElementWriter;
import org.lasalledebain.libris.xmlUtils.LibrisAttributes;
import org.lasalledebain.libris.xmlUtils.LibrisEmptyAttributes;

public class Layouts<RecordType extends Record> implements XMLElement {
	enum LAYOUT_MEDIUM {
		LM_SWING, LM_TEXT, LM_HTML;
	};
	public static final String DEFAULT_LAYOUT_VALUE = "default";
	private final HashMap<String, Function<Schema, LibrisSwingLayout<RecordType>>> swingLayoutGenerators = populateLayoutGenerators();
	private final HashMap<String, Function<Schema, LibrisHtmlLayout<RecordType>>> htmlLayoutGenerators = populateHtmlLayoutGenerators();
	private final static HashMap<String, LAYOUT_MEDIUM> mediumMap = populateMediumMap();

	private HashMap<String, Function<Schema, LibrisSwingLayout<RecordType>>> populateLayoutGenerators() {
		HashMap<String, Function<Schema, LibrisSwingLayout<RecordType>>> gens = new HashMap<>(5);
		gens.put(XML_LAYOUT_TYPE_XML, schem -> new XMLLayout<RecordType>(schem));
		gens.put(XML_LAYOUT_TYPE_TABLE, schem -> new TableLayout<RecordType>(schem));
		gens.put(XML_LAYOUT_TYPE_FORM, schem ->  new FormLayout<RecordType>(schem));
		gens.put(XML_LAYOUT_TYPE_LIST, schem ->  new ListLayout<RecordType>(schem));
		gens.put(XML_LAYOUT_TYPE_PARAGRAPH, schem ->  new ParagraphLayout<RecordType>(schem));

		return gens;
	}

	private HashMap<String, Function<Schema, LibrisHtmlLayout<RecordType>>> populateHtmlLayoutGenerators() {
		HashMap<String, Function<Schema, LibrisHtmlLayout<RecordType>>> gens = new HashMap<>(5);
		gens.put(XML_LAYOUT_TYPE_HTML_FORM, schem -> new LibrisHtmlFormLayout<RecordType>(schem));
		gens.put(XML_LAYOUT_TYPE_HTML_PARAGRAPH, schem -> new LibrisHtmlParagraphLayout<RecordType>(schem));
		gens.put(XML_LAYOUT_TYPE_HTML_LIST, schem -> new LibrisHtmlListLayout<RecordType>(schem));
		return gens;
	}

	private static HashMap<String, LAYOUT_MEDIUM> populateMediumMap() {
		HashMap<String, LAYOUT_MEDIUM> map = new HashMap<>(5);
		map.put(XML_LAYOUT_TYPE_XML, LAYOUT_MEDIUM.LM_SWING);
		map.put(XML_LAYOUT_TYPE_TABLE, LAYOUT_MEDIUM.LM_SWING);
		map.put(XML_LAYOUT_TYPE_FORM, LAYOUT_MEDIUM.LM_SWING);
		map.put(XML_LAYOUT_TYPE_LIST, LAYOUT_MEDIUM.LM_SWING);
		map.put(XML_LAYOUT_TYPE_PARAGRAPH, LAYOUT_MEDIUM.LM_SWING);
		map.put(XML_LAYOUT_TYPE_HTML_FORM, LAYOUT_MEDIUM.LM_HTML);
		map.put(XML_LAYOUT_TYPE_HTML_LIST, LAYOUT_MEDIUM.LM_HTML);
		map.put(XML_LAYOUT_TYPE_HTML_PARAGRAPH, LAYOUT_MEDIUM.LM_HTML);
		return map;
	}

	HashMap<String, LibrisSwingLayout<RecordType>> swingLayouts;
	ArrayList<String> swingLayoutIds;
	HashMap <String,LibrisSwingLayout<RecordType>> swingLayoutUsage = new HashMap<>();
	HashMap <String,LibrisHtmlLayout<RecordType>> htmlLayoutUsage = new HashMap<>();
	private Schema schem;

	HashMap<String, LibrisHtmlLayout<RecordType>> htmlLayouts;
	ArrayList<String> htmlLayoutIds;

	protected static HashMap<String, Dimension> defaultDimensionStrings = initializeDefaultDimensions();
	public Layouts(Schema mySchema) {
		schem = mySchema;
		swingLayouts = new HashMap<>();
		swingLayoutIds = new ArrayList<String>();
		htmlLayouts = new HashMap<>();
		htmlLayoutIds = new ArrayList<>();
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
				LAYOUT_MEDIUM medium = mediumMap.get(layoutType);
				Assertion.assertNotNullInputException("Undefined layout type: ",  layoutType, medium);
				ElementManager layoutMgr = mgr.nextElement();
				switch (medium) {
				case LM_SWING: {
					Function<Schema, LibrisSwingLayout<RecordType>> gen = swingLayoutGenerators.get(layoutType);
					LibrisSwingLayout<RecordType> theLayout = gen.apply(schem);
					theLayout.fromXml(layoutMgr);
					String layoutId = theLayout.getId();
					swingLayouts.put(layoutId, theLayout);
					swingLayoutIds.add(layoutId);
					for (String u: theLayout.getLayoutUsers()) {
						Assertion.assertTrueError("duplicate layout user ", u, !swingLayoutUsage.containsKey(u)) ;
						swingLayoutUsage.put(u, theLayout);
					}
				}
				break;
				case LM_HTML:
				{
					Function<Schema, LibrisHtmlLayout<RecordType>> gen = htmlLayoutGenerators.get(layoutType);
					LibrisHtmlLayout<RecordType> theLayout = gen.apply(schem);
					theLayout.fromXml(layoutMgr);
					String layoutId = theLayout.getId();
					htmlLayouts.put(layoutId, theLayout);
					htmlLayoutIds.add(layoutId);
					for (String u: theLayout.getLayoutUsers()) {
						Assertion.assertTrueError("duplicate layout user ", u, !htmlLayoutUsage.containsKey(u)) ;
						htmlLayoutUsage.put(u, theLayout);
					}
				}
				break;
				default:
					break;
				}
			} catch (XMLStreamException e) {
				throw new InputException(e);
			}
		}
		mgr.parseClosingTag();
	}
	@Deprecated
	public LibrisSwingLayout<RecordType> getSwingLayout(String id) {
		return swingLayouts.get(id);
	}

	@Deprecated
	public String[]	getSwingLayoutIds() {
		return swingLayoutIds.toArray(new String[swingLayoutIds.size()]);
	}

	@Deprecated
	public LibrisHtmlLayout<RecordType> getHtmlLayout(String id) {
		return htmlLayouts.get(id);
	}

	@Deprecated
	public String[]	getHtmlLayoutIds() {
		return htmlLayoutIds.toArray(new String[htmlLayoutIds.size()]);
	}

	public static Dimension getDefaultDimensions(String controlType) {
		Dimension dims = defaultDimensionStrings.get(controlType);
		return dims;
	}

	public LibrisSwingLayout<RecordType> getSwingLayoutByUsage(String user) throws DatabaseException {
		LibrisSwingLayout<RecordType> l = swingLayoutUsage.get(user);
		assertNotNullDatabaseException("No layout defined:",  user, l);
		return l;
	}

	public LibrisHtmlLayout<RecordType> getHtmlLayoutByUsage(String user) {
		LibrisHtmlLayout<RecordType> l = htmlLayoutUsage.get(user);
		assertNotNullError("No layout defined:",  user, l);
		return l;
	}

	@Override
	public void toXml(ElementWriter output) throws LibrisException {
		output.writeStartElement(XML_LAYOUTS_TAG);
		for (String id: getSwingLayoutIds()) {
			getSwingLayout(id).toXml(output);
		}
		output.writeEndElement();	
	}

	@Override
	public LibrisAttributes getAttributes() {
		return LibrisEmptyAttributes.getLibrisEmptyAttributes();
	}

	public boolean equals(Layouts<RecordType> comparand) {
		try {
			Layouts<RecordType> otherLayouts = comparand;
			return otherLayouts.swingLayouts.equals(swingLayouts);
		} catch (ClassCastException e) {
			final String msg = "type mismatch in Layouts.equals()";
			LibrisDatabase.log(Level.WARNING, msg, e);
			return false;
		}
	}

	@Override
	public boolean equals(Object comparand) {
		return false;
	}

	public static String getXmlTag() {
		return XML_LAYOUTS_TAG;
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

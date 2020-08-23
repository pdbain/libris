package org.lasalledebain.libris.ui;

import static org.lasalledebain.libris.exception.Assertion.assertNotNullDatabaseException;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;

import org.lasalledebain.libris.LibrisDatabase;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.Schema;
import org.lasalledebain.libris.XMLElement;
import org.lasalledebain.libris.exception.Assertion;
import org.lasalledebain.libris.exception.DatabaseException;
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

	private Schema schem;
	HashMap <String,LibrisLayout<RecordType>> layoutUsageMap = new HashMap<>();
	HashMap<String, LibrisLayout<RecordType>> myLayouts;
	ArrayList<String> layoutIds;

	protected static HashMap<String, Dimension> defaultDimensionStrings = initializeDefaultDimensions();
	public Layouts(Schema mySchema) {
		schem = mySchema;
		myLayouts = new HashMap<>();
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
			ElementManager layoutMgr = mgr.nextElement();
			LibrisLayout<RecordType> theLayout = new LibrisLayout<>(schem, this);
			theLayout.fromXml(layoutMgr);
			String layoutId = theLayout.getId();
			for (String u: theLayout.getLayoutUsers()) {
				Assertion.assertTrueError("duplicate layout user ", u, !layoutUsageMap.containsKey(u)) ;
				layoutUsageMap.put(u, theLayout);
			}
			myLayouts.put(layoutId, theLayout);
			layoutIds.add(layoutId);
		}
		mgr.parseClosingTag();
	}

	public LibrisLayout<RecordType> getLayout(String id) {
		return myLayouts.get(id);
	}

	public String[]	getLayoutIds() {
		return layoutIds.toArray(new String[layoutIds.size()]);
	}

	public static Dimension getDefaultDimensions(String controlType) {
		Dimension dims = defaultDimensionStrings.get(controlType);
		return dims;
	}

	public LibrisLayout<RecordType> getLayoutByUsage(String user) throws DatabaseException {
		LibrisLayout<RecordType> l = layoutUsageMap.get(user);
		assertNotNullDatabaseException("No layout defined:",  user, l);
		return l;
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

	public boolean equals(Layouts<RecordType> comparand) {
		try {
			Layouts<RecordType> otherLayouts = comparand;
			return otherLayouts.myLayouts.equals(myLayouts);
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

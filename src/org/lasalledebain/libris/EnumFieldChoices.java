package org.lasalledebain.libris;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.FieldDataException;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.XmlException;
import org.lasalledebain.libris.field.FieldEnumValue;
import org.lasalledebain.libris.field.FieldValue;
import org.lasalledebain.libris.xmlUtils.ElementManager;
import org.lasalledebain.libris.xmlUtils.ElementWriter;
import org.lasalledebain.libris.xmlUtils.LibrisAttributes;
import org.lasalledebain.libris.xmlUtils.LibrisXMLConstants;

public class EnumFieldChoices implements XMLElement {
	ArrayList<String> enumChoices = new ArrayList<String>();
	ArrayList<String> choicevalues = new ArrayList<String>();
	HashMap<String, Integer> choiceIds = new HashMap<String, Integer>();
	final ArrayList<FieldEnumValue> legalValues;
	public List<FieldValue> getLegalValues() {
		return Collections.unmodifiableList(legalValues);
	}

	public static final int  INVALID_CHOICE = -1;
	int numChoices = 0;
	public EnumFieldChoices(String id) {
		this();
		this.setId = id;
	}

	public EnumFieldChoices() {
		legalValues = new ArrayList<>();
	}

	public EnumFieldChoices(String name, String[] choices) throws DatabaseException {
		this(name);
		addChoices(choices);
	}

	public int maxChoice() {
		return numChoices-1;
	}

	private String setId;
	public void addChoice(String valueId, String valueString) throws DatabaseException {
		
		final String internId = valueId.intern();
		enumChoices.add(numChoices, internId);
		choicevalues.add(numChoices, valueString.intern());
		choiceIds.put(internId, numChoices);
		++numChoices;
		try {
			legalValues.add(new FieldEnumValue(this, numChoices-1));
		} catch (FieldDataException e) {
			throw new DatabaseException("Cannot add "+valueId+':'+valueString, e);
		}
		if (numChoices > Short.MAX_VALUE) {
			throw new DatabaseException("Cannot add "+valueId+". Only "+Short.MAX_VALUE+" choices allowed");
		}
	}
	
	public void addChoices(String[] enumValues) throws DatabaseException {
		for (String s: enumValues) {
			addChoice(s);
		}
	}

	public void addChoice(String choiceId) throws DatabaseException {
		addChoice(choiceId, choiceId);
	}

	
	public String [] getChoices() {
		String[] c = new String[enumChoices.size()];
		enumChoices.toArray(c);
		return c;
	}

	public String [] getChoiceValues() {
		String[] c = new String[choicevalues.size()];
		choicevalues.toArray(c);
		return c;
	}

	public String getChoiceId(int j) throws FieldDataException {
		if ((j < 0) || (j>= numChoices)) {
			throw new FieldDataException("enum choice "+Integer.toString(j)+" not defined");
		}
		String id = enumChoices.get(j);
		return id;
	}

	public String getChoiceValue(int j) throws FieldDataException {
		if ((j < 0) || (j>= numChoices)) {
			throw new FieldDataException("enum choice "+Integer.toString(j)+" not defined");
		}
		String v = choicevalues.get(j);
		return v;
	}

	public String getChoiceValue(String key) {
		String val = choicevalues.get(choiceIds.get(key));
		return val;
	}

	public int indexFromId(String id) throws FieldDataException {
		Integer i = choiceIds.get(id);
		if (null == i) {
			throw new FieldDataException("enum choice "+id+" not defined");
		}
		return i.intValue();
	}

	public String getId() {
		return setId;
	}
	
	public FieldEnumValue of(int choiceId) {
		return legalValues.get(choiceId);
	}
	
	public FieldEnumValue of(String id) throws FieldDataException {
		return legalValues.get(indexFromId(id));
	}
	
	public static EnumFieldChoices fieldChoicesFactory(ElementManager enumSetManager) throws InputException, DatabaseException {
		EnumFieldChoices c = new EnumFieldChoices();
		c.fromXml(enumSetManager);
		return c;
	}

	@Override
	public boolean equals(Object comparand) {
		try {
			EnumFieldChoices otherChoices = (EnumFieldChoices) comparand;
			if (!choiceIds.equals(otherChoices.choiceIds)) {
				return false;
			} else {
				return choicevalues.equals(otherChoices.choicevalues);
			}
		} catch (ClassCastException e) {
			LibrisDatabase.log(Level.WARNING, "Incompatible comparand for "+getClass().getName()+".equals()", e);
			return false;
		}
	}

	public void fromXml(ElementManager enumSetManager) throws InputException, DatabaseException  {
		LibrisAttributes attributes = enumSetManager.parseOpenTag();
		
		setId = attributes.get(LibrisXMLConstants.XML_SET_ID_ATTR);
		while (enumSetManager.hasNext()) {
			ElementManager enumChoiceManager = enumSetManager.nextElement();
			LibrisAttributes choiceAttributes = enumChoiceManager.parseOpenTag();
			String id = choiceAttributes.get(LibrisXMLConstants.XML_ENUMCHOICE_ID_ATTR);
			String value = choiceAttributes.get(LibrisXMLConstants.XML_ENUMCHOICE_VALUE_ATTR);
			enumChoiceManager.parseClosingTag();
			if (value.isEmpty()) {
				value = id;
			}
			addChoice(id, value);
		}
	}

	@Override
	public LibrisAttributes getAttributes() {
		return new LibrisAttributes(new String[][] {{XML_ENUMCHOICE_ID_ATTR, setId}});
	}

	@Override
	public void toXml(ElementWriter xmlWriter) throws XmlException {
		xmlWriter.writeStartElement(getXmlTag(), getAttributes(), false);
		for (int i = 0; i < numChoices; ++i) {
			final String cId = enumChoices.get(i);
			final String cValue = choicevalues.get(i);
			LibrisAttributes choiceAttrs = new LibrisAttributes(new String [][] {{XML_SET_ID_ATTR, cId}});
			if (!cValue.equals(cId)) {
				choiceAttrs.setAttribute(XML_ENUMCHOICE_VALUE_ATTR, cValue);
			}
			xmlWriter.writeStartElement(XML_ENUMCHOICE_TAG, choiceAttrs, true);
		}
		xmlWriter.writeEndElement();
	}

	@Override
	public String getElementTag() {
		return getXmlTag();
	}

	public static String getXmlTag() {
		return "enumset";
	}

	public FieldEnumValue getChoice(int choiceId) throws FieldDataException {
		return new FieldEnumValue(this, choiceId);
	}
}

package org.lasalledebain.libris.xmlUtils;

import java.util.HashMap;

import javax.xml.namespace.QName;

import org.lasalledebain.libris.Field.FieldType;

public class ElementShape {

	QName elementTag;
	QName[] subElements;
	QName requiredAttributes[];
	QName optionalAttributes[];
	HashMap<QName, String> defaultAttributeValues;
	private boolean hasContentFlag;
	static HashMap<FieldType, Boolean> xmlPreferences = initializeXmlPreferences();
		
	public ElementShape(String elementTag) {
		setTag(elementTag);
	}

	private static HashMap<FieldType, Boolean> initializeXmlPreferences() {
		
		HashMap<FieldType, Boolean> temp = new HashMap<FieldType, Boolean>(); 
		for (FieldType f: FieldType.values()) {
			if (f.equals(FieldType.T_FIELD_TEXT)) {
				temp.put(f, false);
			} else {
				temp.put(f, true);
			}
		}
		return temp;
	}

	public void setTag(String tag) {
		elementTag = new QName(tag);
	}
	
	public void setRequiredAttributeNames(String[] requiredAttributeNames) {
		this.requiredAttributes = new QName[requiredAttributeNames.length];
		for (int i=0;i<requiredAttributeNames.length; ++i) {
			requiredAttributes[i] = new QName(requiredAttributeNames[i]);
		}
	}

	/**
	 * @param optionalAttributeNamesAndValues vector of {attribute name, default value pairs}
	 */
	public void setOptionalAttributeNames(String[][] optionalAttributeNamesAndValues) {
		this.optionalAttributes = new QName[optionalAttributeNamesAndValues.length];
		defaultAttributeValues = new HashMap<QName, String>(optionalAttributes.length);
		for (int i=0;i<optionalAttributeNamesAndValues.length; ++i) {
			optionalAttributes[i] = new QName(optionalAttributeNamesAndValues[i][0]);
			defaultAttributeValues.put(optionalAttributes[i], optionalAttributeNamesAndValues[i][1]);
		}
	}

	public void setSubElementNames(String[] subElementNames) {
		if (null != subElementNames) {
			this.subElements = new QName[subElementNames.length];
			for (int i=0;i<subElementNames.length; ++i) {
				subElements[i] = new QName(subElementNames[i]);
			}
		}
	}

	public QName getElementTag() {
		return elementTag;
	}

	public QName[] getSubElements() {
		if (null == subElements) {
			subElements = new QName[0];
		}
		return subElements;
	}

	public QName[] getRequiredAttributes() {
		if (null == requiredAttributes) {
			requiredAttributes = new QName[0];
		}
		return requiredAttributes;
	}

	public QName[] getOptionalAttributes() {
		if (null == optionalAttributes) {
			optionalAttributes = new QName[0];
		}
		return optionalAttributes;
	}

	public String getDefaultValue(QName attr) {
		return defaultAttributeValues.get(attr);
	}

	public void setHasContent(boolean hc) {
		this.hasContentFlag = hc;
	}

	public boolean hasContent() {
		return hasContentFlag;
	}

	public boolean hasSubElements() {
		return ((null != subElements) && (subElements.length > 0));
	}

	public static boolean storeValueInAttributes(FieldType fieldType) {
		return xmlPreferences.get(fieldType);
	}
}

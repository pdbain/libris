package org.lasalledebain.libris.xmlUtils;

import java.io.File;
import java.io.Reader;

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;

import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.XmlException;

public class LibrisXmlFactory {
	private XMLInputFactory inputFactory;

	/**
	 * @return
	 * @throws FactoryConfigurationError
	 */
	public synchronized XMLInputFactory getXmlInputFactory()
			throws FactoryConfigurationError {
		if (null == inputFactory) {
			inputFactory = XMLInputFactory.newInstance();
		}
		return inputFactory;
	}
	
	public ElementReader makeReader(Reader fileReader, String sourceFilePath) throws InputException, FactoryConfigurationError {
		return new ElementReader(getXmlInputFactory(), fileReader, sourceFilePath);
	}

	/**
	 * @param initialElementName
	 * @param reader
	 * @return
	 * @throws InputException 
	 * @throws FactoryConfigurationError
	 * @throws SchemaException
	 */
	public ElementManager makeLibrisElementManager(Reader reader, 
			String sourceFile, String initialElementName, XmlShapes shapes) 
	throws InputException {
		ElementManager mgr;
		try {
			mgr = new ElementManager(makeReader(reader, sourceFile), new QName(initialElementName), shapes);
		} catch (XmlException e) {
			throw new InputException("Error parsing XML element "+initialElementName, e);
		}
		return mgr;
	}

}

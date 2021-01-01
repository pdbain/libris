package org.lasalledebain.libris.records;

import java.util.ArrayList;

import org.lasalledebain.libris.Field;
import org.lasalledebain.libris.LibrisConstants;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.Schema;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.exception.XmlException;
import org.lasalledebain.libris.field.FieldValueStringList;
import org.lasalledebain.libris.xmlUtils.ElementManager;
import org.lasalledebain.libris.xmlUtils.LibrisAttributes;
import org.lasalledebain.libris.xmlUtils.LibrisXMLConstants;

class FilteringFieldImporter implements LibrisXMLConstants, LibrisConstants {

	ArrayList<FieldDataSource> sources;
	private int fieldNum;

	public FilteringFieldImporter(ElementManager fmgr, Schema schem) throws DatabaseException, InputException, XmlException {
		
		sources = new ArrayList<FieldDataSource>();
		LibrisAttributes attrs = fmgr.parseOpenTag();
		String idString = attrs.get(XML_LIBRISIMPORT_FIELD_ID_ATTR);
		fieldNum = schem.getFieldNum(idString);
		if (NULL_FIELD_NUM == fieldNum) {
			throw new InputException("Unrecognized field ID:"+idString);
		}
		for (ElementManager srcMgr: fmgr) {
			sources.add(sourceFactory(srcMgr) );
		}
		fmgr.parseClosingTag();
	}
	
	private FieldDataSource sourceFactory(ElementManager srcMgr) throws DatabaseException, InputException, XmlException {
		String srcType = srcMgr.getElementTag();
		if (srcType.equals(XML_LIBRISIMPORT_DEFAULT_TAG)) {
			return new DefaultFieldDataSource(srcMgr);
		} else {
			return new ColumnFieldDataSource(srcMgr);
		}
	}
	
	Field addFieldValues(Record rec, FieldValueStringList[] row) throws LibrisException, InputException {
		Field result = null;
		for (FieldDataSource s: sources) {
			Field temp = s.addFieldValue(rec, row);
			if (null != temp) {
				result = temp;
			}
		}
		return result;
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

	private abstract class FieldDataSource {
		abstract Field addFieldValue(Record rec, FieldValueStringList[] row) throws DatabaseException, InputException;
	}
	
	private class DefaultFieldDataSource extends FieldDataSource {		
		private String fieldData;

		DefaultFieldDataSource(ElementManager srcMgr) throws InputException {
			LibrisAttributes attrs = srcMgr.parseOpenTag();
			fieldData = attrs.get(XML_LIBRISIMPORT_DEFAULT_DATA_ATTR);
			srcMgr.parseClosingTag();
		}

		@Override
		Field addFieldValue(Record rec, FieldValueStringList[] row) throws InputException {
			Field result = rec.addFieldValue(fieldNum, fieldData);
			return result;
		}

	}
	public class ColumnFieldDataSource extends FieldDataSource {

		private String match;
		private short columnNum;
		ArrayList<String[]> translations;
		private boolean includeOnMatch;

		ColumnFieldDataSource(ElementManager srcMgr) throws DatabaseException, InputException, XmlException {
			LibrisAttributes attrs = srcMgr.parseOpenTag();
			String columnNumString = attrs.get(XML_LIBRISIMPORT_COLUMN_NUM_ATTR);
			try {
				columnNum = Short.parseShort(columnNumString);
			} catch (NumberFormatException e) {
				throw new InputException("invalid contents of columnNum attribute:"+columnNumString, 
						srcMgr.getSourceFilePath(), e);
			}
			match = attrs.get(XML_LIBRISIMPORT_COLUMN_MATCH_ATTR);
			includeOnMatch = Boolean.parseBoolean(attrs.get(XML_LIBRISIMPORT_COLUMN_INCLUDE_ATTR));
			translations = new ArrayList<String[]>();
			for (ElementManager translateManager: srcMgr) {
				LibrisAttributes translateAttrs = translateManager.parseOpenTag();
				String[] fromTo = new String[2];
				fromTo[0] = translateAttrs.get(XML_LIBRISIMPORT_TRANSLATE_FROM_ATTR);
				fromTo[1] = translateAttrs.get(XML_LIBRISIMPORT_TRANSLATE_TO_ATTR);
				translations.add(fromTo);
				translateManager.parseClosingTag();
			}
			srcMgr.parseClosingTag();
		}

		@Override
		Field addFieldValue(Record rec, FieldValueStringList[] row) throws InputException, DatabaseException {
			FieldValueStringList values;
			Field result = null;
			try {
				values = row[columnNum-1];
			} catch (ArrayIndexOutOfBoundsException e) {
				throw new InputException("invalid column number:"+(columnNum-1), e);
			}
			if (match.length() > 0) {
				boolean discard = includeOnMatch;
				for (String v: values) {
					if (match.matches(v)) {
						discard = !discard;
						break;
					}
				}
				if (discard) {
					return result;
				}
			}
			for (String v: values) {
				String fieldData = v;
				for (String[] fromTo: translations) {
					if (v.matches(fromTo[0])) {
						fieldData = v.replaceFirst(fromTo[0], fromTo[1]);
						break;
					}
				}
				result = rec.addFieldValue(fieldNum, fieldData);
			}
			return result;
		}
	}

}

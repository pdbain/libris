package org.lasalledebain.libris.field;

import java.util.Iterator;

import org.lasalledebain.libris.exception.FieldDataException;
import org.lasalledebain.libris.xmlUtils.LibrisAttributes;
import org.lasalledebain.libris.xmlUtils.LibrisXMLConstants;

public abstract class FieldValue {
	FieldValue nextValue;
	public FieldValueIterator iterator() {
		return new RecordFieldValueIterator(this);
	}
	private static final EmptyFieldValue emptyFieldValueSingleton = new EmptyFieldValue();

	public static EmptyFieldValue getEmptyfieldvaluesingleton() {
		return emptyFieldValueSingleton;
	}

	public class RecordFieldValueIterator implements FieldValueIterator, Iterable<FieldValue> {

		private FieldValue next;

		public RecordFieldValueIterator(FieldValue head) {
			this.next = head;
		}

		@Override
		public boolean hasNext() {
			return (null != next);
		}

		@Override
		public FieldValue next() {
			FieldValue oldNext = next;
			next = next.nextValue;
			return oldNext;
		}

		@Override
		public void remove() {
		}

		@Override
		public Iterator<FieldValue> iterator() {
			return this;
		}

	}

	public abstract String getValueAsString();

	public LibrisAttributes getValueAsAttributes() throws FieldDataException {
		LibrisAttributes values = new LibrisAttributes();
		String mainValueAsKey = getMainValueAsKey();
		if (null != mainValueAsKey) {
			values.setAttribute(LibrisXMLConstants.XML_ENUMCHOICE_VALUE_ATTR, mainValueAsKey);
		}
		String extraValue = getExtraValueAsKey();
		if (null != extraValue) {
			values.setAttribute(LibrisXMLConstants.XML_EXTRA_VALUE_ATTR, extraValue);
		}
		return values;
	}

	public String getMainValueAsKey() throws FieldDataException {
		return getValueAsString();
	}

	public String getExtraValueAsKey() {
		return null;
	}

	public String getMainValueAsString() throws FieldDataException {
		return getValueAsString();
	}

	/**
	 * @return the second value of the pair, or null if it is undefined.
	 */
	public String getExtraValueAsString() {
		return "";
	}

	public boolean isTrue() {
		return false;
	}

	public boolean isEmpty() {
		return false;
	}

	public int getValueAsInt() {
		return 0;
	}
	
	@Override
	public boolean equals(Object comparand) {
		if (comparand == this) return true;
		else if (comparand instanceof FieldValue) {
			final FieldValue comparandValue = FieldValue.class.cast(comparand);
			return equals(comparandValue);
		}
		else return false;
	}

	protected abstract boolean equals(FieldValue comparand);

	public void append(FieldValue temp) {
		FieldValue cursor = this;
		while (null != cursor.nextValue) {
			cursor = cursor.nextValue;
		}
		cursor.nextValue = temp;
	}

	public abstract FieldValue duplicate();
}
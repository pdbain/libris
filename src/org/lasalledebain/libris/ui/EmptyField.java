package org.lasalledebain.libris.ui;

import java.util.Iterator;

import org.lasalledebain.libris.field.FieldValue;
import org.lasalledebain.libris.field.FieldValueIterator;

public class EmptyField implements Iterable<FieldValue> {

	private static EmptyFieldIterator EmptyIterator;
	@Override
	public Iterator<FieldValue> iterator() {
		if (null == EmptyIterator) {
			EmptyIterator = new EmptyFieldIterator();
		}
		return EmptyIterator;
	}
	private class EmptyFieldIterator implements FieldValueIterator {

		@Override
		public boolean hasNext() {
			return false;
		}

		@Override
		public FieldValue next() {
			return null;
		}

		@Override
		public void remove() {		
		}

	}
}

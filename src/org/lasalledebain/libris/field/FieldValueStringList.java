package org.lasalledebain.libris.field;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.lasalledebain.libris.exception.UserErrorException;

public class FieldValueStringList implements Iterable<String> {
	String valueList[];
	public String getValue(int i) {
		return valueList[i];
	}

	public FieldValueStringList(String[] valueList) throws UserErrorException {
		if (null == valueList) {
			throw new UserErrorException("Value list is empty");
		}
		this.valueList = valueList;
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		String separator = "";
		for (String s: valueList) {
			result.append(separator);
			result.append(s);
			separator = ", ";
		}
		return result.toString();
}

	@Override
	public Iterator<String> iterator() {
		List<String> valueIterator = Arrays.asList(valueList);
		return valueIterator.iterator();
	}
}

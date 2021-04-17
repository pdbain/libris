package org.lasalledebain.libris.records;

import java.util.function.Predicate;
import java.util.stream.Stream;

import org.lasalledebain.libris.Record;

public class RecordStreamFilter<T extends Record> extends RecordStreamFunction<T> {
	private final Predicate<T> myPredicate;
	
	public RecordStreamFilter(Predicate<T> thePredicate) {
		myPredicate = thePredicate;
	}
	@Override
	public Stream<T> processStream(Stream<T> inputStream) {
		return inputStream.filter(myPredicate);
	}

}

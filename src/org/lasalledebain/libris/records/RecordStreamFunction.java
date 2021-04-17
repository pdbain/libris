package org.lasalledebain.libris.records;

import java.util.stream.Stream;

import org.lasalledebain.libris.Record;

public abstract class RecordStreamFunction<T extends Record> {

	public abstract Stream<T> processStream(Stream<T> inputStream);
}

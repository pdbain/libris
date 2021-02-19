/**
 * 
 */
package org.lasalledebain.libris.indexes;

import java.util.HashMap;
import java.util.Set;

import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.util.Reporter;

public class MockLibrisRecordMap extends LibrisRecordMap {
	HashMap<Integer, Long> recMap;
	public MockLibrisRecordMap() {
		recMap = new HashMap<Integer, Long>();
	}
	public Set<Integer> getIds() {
		return recMap.keySet();
	}
	public long getRecordPosition(int recordId) {
		final Long recPos = recMap.get(recordId);
		return (recPos == null) ? 0: recPos.longValue();
	}


	@Override
	public int size() {
		return recMap.size();
	}
	public void putRecordPosition(int recordId, long recordPos) {
		recMap.put(recordId, recordPos);
	}
	@Override
	public void close() throws DatabaseException {
		return;
	}
	@Override
	public void flush() throws DatabaseException {
		return;
	}
	@Override
	public void generateReport(Reporter rpt) {
		/* EMPTY */
	}
	@Override
	public boolean setSize(int numRecords) {
		/* not required */
		return false;
	}

}
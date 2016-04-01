package org.lasalledebain.libris.indexes;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UTFDataFormatException;
import java.util.Iterator;
import java.util.logging.Level;

import org.lasalledebain.libris.Field;
import org.lasalledebain.libris.FileAccessManager;
import org.lasalledebain.libris.LibrisConstants;
import org.lasalledebain.libris.LibrisDatabase;
import org.lasalledebain.libris.LibrisFileManager;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.RecordId;
import org.lasalledebain.libris.RecordInstance;
import org.lasalledebain.libris.RecordTemplate;
import org.lasalledebain.libris.Schema;
import org.lasalledebain.libris.Field.FieldType;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.FieldDataException;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.InternalError;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.exception.OutputException;
import org.lasalledebain.libris.exception.UserErrorException;
import org.lasalledebain.libris.field.FieldValue;
import org.lasalledebain.libris.records.RecordsReader;

public class LibrisRecordsFileManager implements RecordsReader, Iterable<Record>, LibrisConstants {

	private static final long MINIMUM_RECORDS_FILE_LENGTH = 2*RecordHeader.getHeaderLength(); /* head & tail, record and free */

	/**
	 * 
	 * Record file: 2 doubly-linked lists, each represented by a RecordHeader:
	 * 	- records
	 * 	- free space
	 * 
	 * Record format:
	 * 	header
	 *  2 bytes size of field index
	 * 	4 bytes record ID.
	 * 	field index
	 * 		2 bytes fieldID
	 * 		data:
	 * 			4 bytes field offset to string or string pair data; or
	 * 			4 bytes field contents for integer;
	 * 			2 bytes field for enum in range
	 * 				4 extra bytes offset to string extravalue if out of range 
	 * 			1 byte boolean
	 * 		fields are in the same order in the file as in the index.
	 * records:
	 * 	fields are native size of the field. 
	 * 		text fields are written in modified UTF-8 as described by java.io.DataOutput.writeUTF()
	 * 		range fields are pairs of text fields
	 */

	private FileAccessManager recordsFile;
	private Schema dbSchema;
	protected RecordPositions recPosns;

	private RecordHeader recordList = null;
	private RecordHeader freeList = null;
	private RandomAccessFile recordsFileStore;
	private boolean readOnly;

	private LibrisDatabase database;

	/**
	 * @param database
	 * @param dbSchema
	 * @param recordPositions
	 * @param recordsFile
	 * @param positionFile 
	 * @throws LibrisException 
	 */
	public LibrisRecordsFileManager(LibrisDatabase db, boolean readOnly, Schema dbSchema, LibrisFileManager fileMgr) throws LibrisException {
		this(db, readOnly, dbSchema, fileMgr.getRecordsFileMgr(), new RecordPositions(fileMgr.getPositionFileMgr(), readOnly));
	}

	public LibrisRecordsFileManager(LibrisDatabase db, boolean readOnly, Schema dbSchema, FileAccessManager recordsFile, RecordPositions recPosns) throws LibrisException {
		this.database = db;
		this.readOnly = readOnly;
		this.dbSchema = dbSchema;
		this.recPosns = recPosns;			
		this.recordsFile = recordsFile;
		open();
	}

	public void open() throws LibrisException {
		try {
			if (!recordsFile.exists()) {
				recordsFile.createNewFile();
			}
			recordsFileStore = readOnly? recordsFile.getReadOnlyRandomAccessFile(): recordsFile.getReadWriteRandomAccessFile();
			if (recordsFile.getLength() < MINIMUM_RECORDS_FILE_LENGTH) {
				reset();
			}
			recordList = new RecordHeader(recordsFileStore, 0);
			freeList = new RecordHeader(recordsFileStore, RecordHeader.getHeaderLength());
		} catch (FileNotFoundException e) {
			throw new DatabaseException("error opening records file", e);
		} catch (IOException e) {
			throw new DatabaseException("error reading records file", e);

		}
	}

	public void flush() throws DatabaseException {
		if (null != recordList) {
			recordList.save();
		}
		if (null != freeList) {
			freeList.save();
		}
		if (null != recPosns) {
			recPosns.flush();
		}
	}
	
	public void close() throws IOException, DatabaseException {
		recPosns.close();
		recordsFile.close();
	}

	public void reset() throws LibrisException {
		if (readOnly) {
			throw new UserErrorException("attempting to recreate records file of read-only database");
		}
		RecordHeader hdr = new RecordHeader(recordsFileStore);
		hdr.setFilePosition(0);
		hdr.save();
		hdr.setFilePosition(RecordHeader.getHeaderLength());
		hdr.save();
	}

	@Override
	public Iterator<Record> iterator() {
		final RecordIterator iter = new RecordIterator();
		return iter;
	}
	
	/**
	 * @param recordFilePosition 
	 * @param entrySize bytes required, not including header
	 * @return position in file.
	 * @throws DatabaseException
	 */
	private RecordHeader allocateSpace(long recordFilePosition, int entrySize) throws DatabaseException {
	
		try {
			RecordHeader result = null;
			if (0 != recordFilePosition) {
				RecordHeader currentHeader = 
					RecordHeader.createRecordHeaderFromDataPosition(recordsFileStore, recordFilePosition);
				if (currentHeader.getSize() >= entrySize) {
					currentHeader.setReusable(true);
					result = currentHeader;
				} else {
					//	link the current block to the free list
					recordList.remove(currentHeader);
					freeList.add(currentHeader);
				}
			}
			if (null == result) {
				Iterator<RecordHeader> rhi = freeList.iterator();
				while (rhi.hasNext()) {
					RecordHeader rh = rhi.next();
					if (rh.getSize() >= entrySize) {
						rhi.remove();
						result = rh;
						break;
					}
				} 
			}
			if (null == result) {
				/* no free space.  Extend the file */
				long allocatedPosition = recordsFileStore.length();
				result = new RecordHeader(recordsFileStore);
				result.setFilePosition(allocatedPosition);
				result.setSize(entrySize);
			}
			return result;
		} catch (IOException e) {
			throw new DatabaseException("error writing record file", e);
		}
	}

	/**
	 * Save a record in the database.  If it is an existing record and the current space is
	 * sufficient, replace in place.  If not, add the current record's space (if any) to the 
	 * free list, then allocate space, from the free list if possible, for the record, and save the data.
	 * @param recData record data
	 * @throws DatabaseException
	 */
	public void putRecord(Record recData) throws DatabaseException {
		database.log(Level.FINE, "LibrisRecordsFileManager.putRecord "+recData.getRecordId());
		byte[] recordBuffer = null;
		try {
			 recordBuffer = formatRecord(dbSchema, recData);
			int recordSize = recordBuffer.length; /* include the record ID */
			long recordFilePosition = recPosns.getRecordFilePosition(recData.getRecordId());
			RecordHeader hdr = allocateSpace(recordFilePosition, recordSize);
			if (!hdr.isReusable()) {
				recordList.add(hdr);
			}
			long allocatedPosition = hdr.getDataPosition();
			recordsFileStore.seek(allocatedPosition);
			recordsFileStore.write(recordBuffer);
			recPosns.setRecordFilePosition(recData.getRecordId(), allocatedPosition);
		} catch (IOException e) {
			throw new DatabaseException("error writing record "+recData.getRecordId(), e);
		} catch (OutputException e) {
			throw new DatabaseException("error writing record "+recData.getRecordId(), e);
		}
	}

	/**
	 * @param recData
	 * @param fieldIndex
	 * @param fieldValues
	 * @return byte array with record data in native format
	 * @throws DatabaseException
	 * @throws FieldDataException
	 * @throws OutputException 
	 * @throws InputException
	 */
	static byte[] formatRecord(Schema dbSchema, Record recData)
			throws DatabaseException, OutputException {
		ByteArrayOutputStream recordBuffer;
		recordBuffer = new ByteArrayOutputStream(256);
		ByteArrayOutputStream fieldIndex = new ByteArrayOutputStream();
		ByteArrayOutputStream fieldValues = new ByteArrayOutputStream();
		DataOutputStream indexStream = new DataOutputStream(fieldIndex);
		DataOutputStream valuesStream = new DataOutputStream(fieldValues);
		DataOutputStream recordBufferStream = new DataOutputStream(recordBuffer);

		for (Field f: recData.getFields()) {
			try {
				short fieldNum = dbSchema.getFieldNum(f.getFieldId());
				FieldType ft = f.getType();
				for (FieldValue fv: f.getFieldValues()) { // FIXME NPE here
					int oldValueEnd = fieldValues.size();
					indexStream.writeShort(fieldNum); 
					switch (ft) {
					case T_FIELD_ENUM:
					{
						int fieldValue = fv.getValueAsInt();
						if ((fieldValue < ENUM_VALUE_OUT_OF_RANGE) || (fieldValue > Short.MAX_VALUE)) {
							throw new DatabaseException("Record "+recData.getRecordId()+" field "+fieldNum+" out of range");
						}
						indexStream.writeShort(fieldValue);
						if (ENUM_VALUE_OUT_OF_RANGE == fieldValue) {
							indexStream.writeInt(oldValueEnd);
							valuesStream.writeUTF(fv.getExtraValueAsString());
						}
					}
					break;
					case T_FIELD_INTEGER:
					case T_FIELD_INDEXENTRY: 
					{
						int fieldValue = fv.getValueAsInt();
						indexStream.writeInt(fieldValue); 
					}
					break;
					case T_FIELD_BOOLEAN: 
						indexStream.writeByte(fv.getValueAsInt()); 
						break;
					case T_FIELD_STRING:
					case T_FIELD_TEXT:
						try {
							indexStream.writeInt(oldValueEnd);
							valuesStream.writeUTF(fv.getValueAsString());
						} catch (UTFDataFormatException e) {
							throw new DatabaseException("UTF format exception in "+recData.getRecordId()+" field "+fieldNum);
						}
						break;
					case T_FIELD_PAIR:
						try {
							String mainValue = fv.getMainValueAsString();
							valuesStream.writeUTF(mainValue);
							String secondValue = fv.getExtraValueAsString();
							valuesStream.writeUTF(secondValue);
							indexStream.writeInt(oldValueEnd);
						} catch (UTFDataFormatException e) {
							throw new DatabaseException("UTF format exception in "+recData.getRecordId()+" field "+fieldNum);
						} catch (FieldDataException e) {
							throw new DatabaseException("Invalid data in "+recData.getRecordId()+" field "+fieldNum);
							}
						break;
					}
				}
			} catch (IOException e) {
				throw new OutputException("error writing field "+f.getFieldId(), e);
			}
		}
		try {
			indexStream.writeInt(fieldValues.size()); /* pointer to padding */
			recordBufferStream.writeShort(fieldIndex.size());
			recordBufferStream.writeInt(recData.getRecordId().getRecordNumber());
			recordBufferStream.write(fieldIndex.toByteArray());
			recordBufferStream.write(fieldValues.toByteArray());
			recordBufferStream.flush();
		} catch (IOException e) {
			throw new OutputException("error writing record "+recData.getRecordId(), e);
		}
		return recordBuffer.toByteArray();
	}
	public Record getRecord(RecordId id) throws InputException, DatabaseException {
		long pos = recPosns.getRecordFilePosition(id);
		if (pos > 0) try {
			recordsFileStore.seek(pos);
			Record rec = readRecord(pos);
			return rec;
		} catch (IOException e) {
			throw new InputException("Error accessing records file", e);
		}
		return null;
	}

	public void removeRecord(RecordId rid) throws DatabaseException {
	
		try {
			long recordFilePosition = recPosns.getRecordFilePosition(rid);
			if (0 != recordFilePosition) {
				RecordHeader currentHeader = 
					RecordHeader.createRecordHeaderFromDataPosition(recordsFileStore, recordFilePosition);
				recordList.remove(currentHeader);
				freeList.add(currentHeader);
			} else {
				throw new DatabaseException("error deleting record "+rid);
			}
		} catch (IOException e) {
			throw new DatabaseException("error writing record file", e);
		}
	}

	private Record readRecord(long recordPosition) throws InputException, DatabaseException {
		try {
			int indexSize = recordsFileStore.readShort() & 0x0000ffff;
			int recId = recordsFileStore.readInt();
			byte indexData[] = new byte[indexSize];
			int bytesRead = recordsFileStore.read(indexData);
			if (bytesRead < indexSize) {
				throw new InputException("EOF encountered when reading record field index");
			}
			long valueBase = recordsFileStore.getFilePointer();

			RecordInstance r = new RecordInstance(RecordTemplate.templateFactory(dbSchema));
			ByteArrayInputStream fieldIndex = new ByteArrayInputStream(indexData);
			DataInputStream indexStream = new DataInputStream(fieldIndex);
			r.setRecordId(recId);

			short fieldNum = LibrisConstants.NULL_FIELD_NUM;
			int indexCursor = 0;
			int indexEnd = indexSize - 4; /* subtract padding */
			try {
				while (indexCursor < indexEnd) {
					fieldNum = indexStream.readShort();
					FieldType ft = dbSchema.getFieldType(fieldNum);
					indexCursor += 2;
					switch (ft) {
					case T_FIELD_ENUM:
					{
						int fieldValue = indexStream.readShort();
						indexCursor += 2;
						if (ENUM_VALUE_OUT_OF_RANGE != fieldValue) {
							r.addFieldValue(fieldNum, fieldValue);
						} else {
							long offset = indexStream.readInt() + valueBase;
							indexCursor += 4;
							recordsFileStore.seek(offset);
							String extraValue = recordsFileStore.readUTF();
							r.addFieldValue(fieldNum, fieldValue, extraValue);
						}
						break;
					}
					case T_FIELD_INTEGER:
					case T_FIELD_INDEXENTRY: 
					{
						int fieldValue = indexStream.readInt();
						r.addFieldValue(fieldNum, fieldValue);
						indexCursor += 4;
					}
					break;
					case T_FIELD_BOOLEAN: 
					{
						int fieldValue = indexStream.readByte();
						r.addFieldValue(fieldNum, fieldValue);
						indexCursor += 1;
					}
					break;
					case T_FIELD_STRING:
					case T_FIELD_TEXT:
					{
						long offset = indexStream.readInt() + valueBase;
						indexCursor += 4;
						recordsFileStore.seek(offset);
						String fieldValue = recordsFileStore.readUTF();
						r.addFieldValue(fieldNum, fieldValue);
					}
					break;
					case T_FIELD_PAIR: {
						long offset = indexStream.readInt() + valueBase;
						indexCursor += 4;
						recordsFileStore.seek(offset);
							String fieldValue = recordsFileStore.readUTF();
							String extraValue = recordsFileStore.readUTF();
							r.addFieldValue(fieldNum, fieldValue, extraValue);
					}
					}
				}
			} catch (FieldDataException e) {
				String newMsg = "\nreading record "+recId+" field "+fieldNum;
				throw new FieldDataException(newMsg, e);
			}
			return r;
		} catch (IOException e) {
			throw new InputException(e);
		}
	}


	private class RecordIterator implements Iterator<Record> {
		Iterator<RecordHeader> rhi;
		private RecordHeader currentRecord = null;
		private Iterator<Record> modifiedRecordsIterator;

		private RecordIterator() {
			rhi = recordList.iterator();
			modifiedRecordsIterator = database.getModifiedRecords().iterator();
		}
		
		@Override
		public boolean hasNext() {
			return rhi.hasNext() || modifiedRecordsIterator.hasNext();
		}

		@Override
		public Record next() {
			try {
				Record result = null;
				if (rhi.hasNext()) {
					currentRecord = rhi.next();
					result = readRecord(currentRecord.getDataPosition());
				} else {
					result = modifiedRecordsIterator.next();
				}
				return result;
			} catch (Exception e) {
				throw new InternalError("error in LibrisRecordsFileManager.next", e);//LibrisDatabase.setLastException(e);
			}
		}

		@Override
		public void remove() {
			rhi.remove();
		}
	}

	public int countRecords() {
		int count = 0;
		for (@SuppressWarnings("unused") RecordHeader hdr: recordList) {
			++count;
		}
		return count;
	}
}

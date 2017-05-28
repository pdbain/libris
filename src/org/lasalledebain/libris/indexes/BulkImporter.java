/**
 * 
 */
package org.lasalledebain.libris.indexes;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.Schema;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.OutputException;

public class BulkImporter {
	long firstRecordPosition;
	long lastPosition;
	private RecordPositions recPosns;
	private FileChannel recFileChannel;
	private Schema dbSchema;
	private byte[] lastHeader;
	private FileOutputStream recFileWriter;

	public BulkImporter(Schema dbSchema, FileOutputStream recFileWriter, RecordPositions recPosns) throws IOException {
		this.recPosns = recPosns;
		this.recFileWriter = recFileWriter;
		this.dbSchema = dbSchema;
		lastPosition = 0;
		recFileChannel = recFileWriter.getChannel();
		initialize();
	}

	/**
	 * @throws IOException
	 */
	public void initialize() throws IOException {
		recFileChannel.position(0l);
		byte emptyHeader[] = RecordHeader.formatHeader(0, 0, 0);
		recFileWriter.write(emptyHeader);  /* record list */
		recFileWriter.write(emptyHeader);  /* free list */
		firstRecordPosition = recFileChannel.position();
	}

	public void putRecord(Record rec) throws DatabaseException, OutputException {
		try {
			long currentPosition = recFileChannel.position();
			byte recordData[] = LibrisRecordsFileManager.formatRecord(dbSchema, rec);
			long nextRecordPosition = currentPosition + RecordHeader.getHeaderLength() + recordData.length;
			lastHeader = RecordHeader.formatHeader((int) lastPosition, (int) nextRecordPosition, (int) recordData.length);
			lastPosition = currentPosition;
			recPosns.setRecordFilePosition(rec.getRecordId(), currentPosition+RecordHeader.getHeaderLength());
			recFileWriter.write(lastHeader);
			recFileWriter.write(recordData);
		} catch (IOException e) {
			throw new OutputException("Error writing record header", e);
		}
	}

	public void finish(boolean nonEmpty) throws OutputException, DatabaseException  {
		try {
			recFileWriter.flush();
			if (nonEmpty) {
				byte recordListRoot[] = RecordHeader.formatHeader((int) lastPosition, (int) firstRecordPosition, 0);
				recFileChannel.position(0);
				recFileWriter.write(recordListRoot);
				recFileWriter.flush();
				recFileChannel.position(lastPosition);
				RecordHeader hdr = new RecordHeader(new DataInputStream(new ByteArrayInputStream(lastHeader)));
				hdr.setNext(0);
				lastHeader = hdr.toByteArray();
				recFileWriter.write(lastHeader);
			}
			recPosns.flush();
			recPosns.close();
		} catch (IOException e) {
			throw new OutputException("Error writing record header", e);
		}
	}
}
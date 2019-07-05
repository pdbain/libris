package org.lasalledebain.libris.util;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.RandomAccessFile;

import org.lasalledebain.libris.FileAccessManager;
import org.lasalledebain.libris.LibrisConstants;
import org.lasalledebain.libris.LibrisFileManager;
import org.lasalledebain.libris.exception.UserErrorException;
import org.lasalledebain.libris.indexes.RecordHeader;

public class DbDump {

	private static PrintStream out;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length < 1) {
			printUsage();
			System.exit(1);
		}
		String dbPath = args[0];
		File dbFileObj = new File(dbPath);
		if (!dbFileObj.exists()) {
			printUsage();
			System.exit(1);
		}
		out = System.out;
		LibrisFileManager fm;
		try {
			fm = new LibrisFileManager(dbFileObj, LibrisConstants.AUX_DIRECTORY_NAME);
			dumpRecords(fm);
		} catch (UserErrorException e) {
			System.err.println("Cannot open database: "+e.getMessage());
		}
	}

	private static void dumpRecords(LibrisFileManager fm) {
		FileAccessManager rf = fm.getAuxiliaryFileMgr(LibrisFileManager.RECORDS_FILENAME);
		try {
			RandomAccessFile recordsFileStore = rf.getReadOnlyRandomAccessFile();
			RecordHeader recordList = new RecordHeader(recordsFileStore, 0);
			RecordHeader freeList = new RecordHeader(recordsFileStore, RecordHeader.getHeaderLength());
			dumpRecord("recordList", recordsFileStore, recordList, false);
			RecordHeader hdr = recordList;
			while (hdr.hasNext()) {
				hdr = new RecordHeader(recordsFileStore, hdr.getNext());
				dumpRecord("record", recordsFileStore, hdr, true);
			}
			rf.releaseRaRoFile(recordsFileStore);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void dumpRecord(String hdrType, RandomAccessFile recordsFileStore, RecordHeader hdr, boolean dumpContents) throws IOException {
		out.printf("%s: posn = %x next = %x prev = %x size = %x", hdrType, hdr.getFilePosition(), 
				hdr.getNext(), hdr.getPrev(), hdr.getSize());
		if (dumpContents) {
			long pos = hdr.getDataPosition();
			recordsFileStore.seek(pos);
			short fiSize = recordsFileStore.readShort();
			int recId = recordsFileStore.readInt();
			out.printf(" fieldIndexLength %d id %d", fiSize, recId);
		}
		out.println();
	}

	private static void printUsage() {
		System.out.println("DbDump <path to database>");
	}

}

package org.lasalledebain.hashtable;

import static org.lasalledebain.libris.util.Utilities.testLogger;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;

import org.junit.Test;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.hashfile.NumericKeyHashEntry;
import org.lasalledebain.libris.hashfile.VariableSizeHashEntry;
import org.lasalledebain.libris.index.AbstractVariableSizeHashEntry;
import org.lasalledebain.libris.indexes.FileSpaceManager;
import org.lasalledebain.libris.indexes.MockVariableSizeEntryNumericKeyHashFile;
import org.lasalledebain.libris.util.Utilities;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

public class VariableSizeHashFileTests extends TestCase {
	private static final int oversizeThreshold = 42;
	private File testFileObject;
	private File workingDirectory;

	@Test
	public void testAddAndGet() {
		try {
			MockVariableSizeEntryNumericKeyHashFile htable = makeHashFile();
			ArrayList<MockVariableSizeHashEntry> entries = new ArrayList<MockVariableSizeHashEntry>();

			addEntries(htable, entries, 32, 0, true);

			for (AbstractVariableSizeHashEntry e: entries) {
				NumericKeyHashEntry f = htable.getEntry(e.getKey());
				assertNotNull("Could not find entry", f);
				assertEquals("Entry mismatch", e, f);
			}
		} catch (IOException e) {
			e.printStackTrace();
			fail("Unexpected exception on hashfile");
		} catch (DatabaseException e) {
			e.printStackTrace();
			fail("Unexpected exception on hashfile");
		}
	}

	@Test
	public void testOverflow() {
		try {
			MockVariableSizeEntryNumericKeyHashFile htable = makeHashFile();
			ArrayList<MockVariableSizeHashEntry> entries = new ArrayList<MockVariableSizeHashEntry>();

			int currentKey = 1;
			while (currentKey < 1000) {
				currentKey = addEntries(htable, entries, 127, currentKey, true);
				print(currentKey+" entries added.  Checking...\n");
				for (AbstractVariableSizeHashEntry e: entries) {
					int key = e.getKey();
					NumericKeyHashEntry f = htable.getEntry(key);
					if (null == f) {
						print("key="+key+" not found; ");
						print("\n");
					}
					try {
						assertNotNull("Could not find entry "+key, f);
						assertEquals("Entry mismatch", e, f);
					} catch (AssertionFailedError a) {
						throw a;
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			fail("Unexpected exception on hashfile");
		} catch (DatabaseException e) {
			e.printStackTrace();
			fail("Unexpected exception on hashfile");
		}
	}

	@Test
	public void testVariableSizedEntries() {
		try {
			MockVariableSizeEntryNumericKeyHashFile htable = makeHashFile();
			ArrayList<VariableSizeHashEntry> entries = new ArrayList<VariableSizeHashEntry>();

			int currentKey = 1;
			while (currentKey < 1000) {
				currentKey = addVariableSizeEntries(htable, entries, 127, currentKey, 1, 1024);
				print(currentKey+" entries added.  Checking...\n");
				for (VariableSizeHashEntry e: entries) {
					int key = e.getKey();
					NumericKeyHashEntry f = htable.getEntry(key);
					if (null == f) {
						print("key="+key+" not found; ");
						print("\n");
					}
					try {
						assertNotNull("Could not find entry "+key, f);
						assertEquals("Entry mismatch", e, f);
					} catch (AssertionFailedError a) {
						throw a;
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			fail("Unexpected exception on hashfile");
		} catch (DatabaseException e) {
			e.printStackTrace();
			fail("Unexpected exception on hashfile");
		}
	}

	@Test
	public void testFlush() {
		try {
			MockVariableSizeEntryNumericKeyHashFile htable = makeHashFile();
			ArrayList<VariableSizeHashEntry> entries = new ArrayList<VariableSizeHashEntry>();

			int currentKey = 1;

			currentKey = addVariableSizeEntries(htable, entries, 20, currentKey, 1, 1024);
			htable.flush();
			MockVariableSizeEntryNumericKeyHashFile htable2 = makeHashFile();
			print(currentKey+" entries added.  Checking...\n");
			for (VariableSizeHashEntry e: entries) {
				int key = e.getKey();
				NumericKeyHashEntry f = htable2.getEntry(key);
				if (null == f) {
					print("key="+key+" not found; ");
					print("\n");
				}
				try {
					assertNotNull("Could not find entry "+key, f);
					assertEquals("Entry mismatch", e, f);
				} catch (AssertionFailedError a) {
					throw a;
				}
			}
				} catch (IOException e) {
			e.printStackTrace();
			fail("Unexpected exception on hashfile");
		} catch (DatabaseException e) {
			e.printStackTrace();
			fail("Unexpected exception on hashfile");
		}
	}

	private MockVariableSizeEntryNumericKeyHashFile makeHashFile() throws IOException {
		return new MockVariableSizeEntryNumericKeyHashFile(backingStore, new MockOverflowManager(mgr));
	}

	@Test
	public void testReplace() {
		try {
			MockVariableSizeEntryNumericKeyHashFile htable = makeHashFile();
			ArrayList<MockVariableSizeHashEntry> entries = new ArrayList<MockVariableSizeHashEntry>();

			for (MockVariableSizeHashEntry e: entries) {

				byte oldData[] = e.getData();
				for (int i=0;i<oldData.length;++i) {
					oldData[i] += i;
				}
				htable.addEntry(e);
				htable.flush();
				int key = e.getKey();
				NumericKeyHashEntry f = htable.getEntry(key);
				assertNotNull("Could not find entry "+key, f);
				assertEquals("Entry mismatch", e, f);
			}
		} catch (IOException e) {
			e.printStackTrace();
			fail("Unexpected exception on hashfile");
		} catch (DatabaseException e) {
			e.printStackTrace();
			fail("Unexpected exception on hashfile");
		}
	}

	private int addVariableSizeEntries(MockVariableSizeEntryNumericKeyHashFile htable,
			ArrayList<VariableSizeHashEntry> entries, int numEntries, int keyBase, int minimum,
			int maximum) throws DatabaseException, IOException {
		int modulus = Math.max(1, numEntries/64);
		Random r = new Random(keyBase);
		MockVariableSizeEntryFactory efactory = new MockVariableSizeEntryFactory(28);
		for (int i=0; i<numEntries; i++) {
			int length = r.nextInt(maximum - minimum) + minimum;
			MockVariableSizeHashEntry e = makeVariableSizeEntry(keyBase+i, length);
			htable.addEntry(e);
			entries.add(e);
			if ((i > 0) && (i%modulus == 0)) {
				print(">");
			}
		}
		htable.flush();
		print("\n"+numEntries+" added\n");

		return keyBase+numEntries;
	}

	private RandomAccessFile backingStore;
	private FileSpaceManager mgr;
	private StringBuffer logBuffer;

	public MockVariableSizeHashEntry makeVariableSizeEntry(int key, int len) {
		byte[] dat = new byte[len];
		for (int i = 0; i < len; ++i) {
			dat[i] = (byte) (key + i);
		}
		MockVariableSizeHashEntry newEntry = new MockVariableSizeHashEntry(key, dat);
		newEntry.setOversize((oversizeThreshold > 0) && (len >= oversizeThreshold));
		return newEntry;
	}

	private int addEntries(MockVariableSizeEntryNumericKeyHashFile htable,
			ArrayList<MockVariableSizeHashEntry> entries, int numEntries, int keyBase, boolean countUp)
	throws IOException, DatabaseException {
		int modulus = Math.max(1, numEntries/64);

		MockVariableSizeEntryFactory efactory = new MockVariableSizeEntryFactory(28);
		for (int i=0; i<numEntries; i++) {
			MockVariableSizeHashEntry e = efactory.makeEntry(countUp? (keyBase+i):(keyBase+numEntries-i));
			htable.addEntry(e);
			entries.add(e);
			if ((i > 0) && (i%modulus == 0)) {
				print(">");
			}
		}
		htable.flush();
		print("\n"+numEntries+" added\n");

		return keyBase+numEntries;
	}

	protected void setUp() throws Exception {
		workingDirectory = Utilities.makeTempTestDirectory();
		logBuffer = new StringBuffer();
		testLogger.log(Level.INFO, getClass().getName()+" start "+getName());
		if (null == testFileObject) {
			testFileObject = Utilities.makeTestFileObject(workingDirectory, "hashFile");
		}
		if (!testFileObject.exists()) {
			backingStore = new RandomAccessFile(testFileObject, "rw");
		}
		mgr = Utilities.makeFileSpaceManager(workingDirectory, getName()+"_mgr");
	}

	@Override
	protected void tearDown() throws Exception {
		println("end "+getName());
		if (null != testFileObject) {
			testFileObject.delete();
		}
		Utilities.destroyFileSpaceManager(mgr);
		Utilities.deleteWorkingDirectory();
	}

	private void print(String msg) {
		logBuffer.append(msg);
	}
	private void println(String msg) {
		logBuffer.append(msg);
		testLogger.log(Level.INFO, logBuffer.toString());
		logBuffer.delete(0, logBuffer.length());
	}
}

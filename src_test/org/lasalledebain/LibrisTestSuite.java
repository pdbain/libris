package org.lasalledebain;

import org.lasalledebain.group.GroupDefsTests;
import org.lasalledebain.group.GroupManagerTests;
import org.lasalledebain.group.InheritanceTest;
import org.lasalledebain.group.MemberTest;
import org.lasalledebain.hashtable.HashBucketTests;
import org.lasalledebain.hashtable.HashFileTest;
import org.lasalledebain.hashtable.TermCountEntryBucketTest;
import org.lasalledebain.hashtable.VariableSizeEntryHashBucketTest;
import org.lasalledebain.libris.indexes.FileSpaceManagerTests;
import org.lasalledebain.libris.indexes.LibrisRecordMapTest;
import org.lasalledebain.libris.indexes.SortedKeyValueFileManagerTest;
import org.lasalledebain.libris.indexes.TermcountHashfileTests;
import org.lasalledebain.libris.indexes.TestFileRecordMap;
import org.lasalledebain.libris.indexes.TestKeyIntegerTuple;
import org.lasalledebain.libris.search.BloomFilterTest;
import org.lasalledebain.libris.search.TestRecordFilter;
import org.lasalledebain.libris.ui.HtmlTests;
import org.lasalledebain.libris.ui.ImportTests;
import org.lasalledebain.libris.ui.LauncherTests;
import org.lasalledebain.libris.ui.LayoutTests;
import org.lasalledebain.libris.ui.RecordEditTests;
import org.lasalledebain.recordimport.CsvImportTest;
import org.lasalledebain.recordimport.TestPDF;
import org.lasalledebain.repository.ArtifactTest;
import org.lasalledebain.repository.RepositoryTest;

import junit.framework.Test;
import junit.framework.TestSuite;

public class LibrisTestSuite {

	public static boolean ignoreUnimplemented = Boolean.getBoolean("org.lasalledebain.libris.test.IgnoreUnimplementedTests");
	public static Test suite() {
		TestSuite suite = new TestSuite("Test for org.lasalledebain");
		//$JUnit-BEGIN$
		suite.addTestSuite(LauncherTests.class);
		suite.addTestSuite(HtmlTests.class);
		suite.addTestSuite(ConfigTest.class);
		suite.addTestSuite(LayoutTests.class);
		suite.addTestSuite(ArtifactTest.class);
		suite.addTestSuite(TestRecordFilter.class);
		suite.addTestSuite(TermcountHashfileTests.class);
		suite.addTestSuite(TermCountEntryBucketTest.class);
		suite.addTestSuite(UtilitiesTest.class);
		suite.addTestSuite(TestPDF.class);
		suite.addTestSuite(RepositoryTest.class);
		suite.addTestSuite(BloomFilterTest.class);
		suite.addTestSuite(FieldTest.class);
		suite.addTestSuite(HashFileTest.class);
		suite.addTestSuite(RecordTests.class);
		suite.addTestSuite(EnumTests.class);
		suite.addTestSuite(RecordListTests.class);
		suite.addTestSuite(GuiTests.class);
		suite.addTestSuite(RecordEditTests.class);
		suite.addTestSuite(SchemaTests.class);
		suite.addTestSuite(FileSpaceManagerTests.class);
		suite.addTestSuite(LibrisRecordMapTest.class);
		suite.addTestSuite(HashBucketTests.class);
		suite.addTestSuite(VariableSizeEntryHashBucketTest.class);
		suite.addTestSuite(SortedKeyValueFileManagerTest.class);
		suite.addTestSuite(DatabaseTests.class);
		suite.addTestSuite(RecordListTests.class);
		suite.addTestSuite(JournalTest.class);
		suite.addTestSuite(ImportTests.class);
		suite.addTestSuite(CsvImportTest.class);
		suite.addTestSuite(GroupManagerTests.class);
		suite.addTestSuite(GroupDefsTests.class);
		suite.addTestSuite(MemberTest.class);
		suite.addTestSuite(TestKeyIntegerTuple.class);
		suite.addTestSuite(TestFileRecordMap.class);
		suite.addTestSuite(InheritanceTest.class);
		suite.addTestSuite(DatabaseStressTests.class);
		suite.addTestSuite(VariableSizeEntryHashBucketTest.class);
		//$JUnit-END$
		return suite;
	}

}

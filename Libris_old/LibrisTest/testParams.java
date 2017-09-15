package LibrisTest;

import junit.framework.TestCase;
public class testParams {
	private static final String TEST_DATABASE_DIRECTORY_PROPERTY = "TestDatabaseDirectory";
	private static final String TESTDATABASE = "Testdatabase1.xml";

	public static String getTestDatabase() {
		String dir = System.getProperty(TEST_DATABASE_DIRECTORY_PROPERTY);
		if (null == dir) {
			TestCase.fail("property "+TEST_DATABASE_DIRECTORY_PROPERTY+" undefined");
		}
		if (!dir.endsWith("/")) {
			dir = dir +"/";
		}
		String testDatabase = dir+TESTDATABASE;
		return testDatabase;
	}

}

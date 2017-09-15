package LibrisTest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import Libris.LibrisDatabase;
import Libris.LibrisException;
import Libris.LibrisFieldDatatype;
import Libris.LibrisRecord;
import Libris.LibrisRecordField;
import Libris.LibrisSchema;
import Libris.LibrisSchemaField;
import Libris.RecordList;
import Libris.LibrisException.ErrorIds;
import Libris.LibrisFieldDatatype.FieldType;
import junit.framework.TestCase;

public class TestLibris extends TestCase {
	LibrisDatabase database;
	LibrisRecord record;

	public void testOpenDatabase () {
		System.out.println("testOpendatabase\n");
		database = new LibrisDatabase();
		database.openDatabase(testParams.getTestDatabase(), true);
		LibrisSchema schema = database.getSchema();
		String fieldIDs[][]= {{"StringField1","sf1"}, {"StringField2", "sf2"}, {"IntField1","if1"}, { "IntField2","if2"}, 
				{ "IntsField1","isf1"}, { "BoolField1","bf1"}, { "IndexField1","ixf1"}, {"EnumField1", "ef1"}};
		for (int i = 0; i < fieldIDs.length; ++i) {
			String fid = fieldIDs[i][0];
			String fname = fieldIDs[i][1];
			LibrisSchemaField f = schema.getField(fid);
			String actualName = f.getName();
			assertEquals("field names equal", actualName, fname);
			if (fid.startsWith("String")) {
				assertEquals("field type correct", f.getDatatype(), FieldType.T_FIELD_STRING);
			} else if (fid.startsWith("Bool")){
				assertEquals("field type correct", f.getDatatype(), FieldType.T_FIELD_BOOLEAN);				
			} else if (fid.startsWith("Ints")){
				assertEquals("field type correct", f.getDatatype(), FieldType.T_FIELD_INTEGERS);				
			} else if (fid.startsWith("Int")){
				assertEquals("field type correct", f.getDatatype(), FieldType.T_FIELD_INTEGER);				
			} else if (fid.startsWith("Index")){
				assertEquals("field type correct", f.getDatatype(), FieldType.T_FIELD_INDEXENTRY);				
			} else if (fid.startsWith("Enum")){
				assertEquals("field type correct", f.getDatatype(), FieldType.T_FIELD_ENUM);				
			}
		}
	}

	public void testCreateRecord () {
		System.out.println("testCreateRecord\n");
		database = new LibrisDatabase();
		database.openDatabase(testParams.getTestDatabase(), true);
		try {
			record = database.newRecord();
		} catch (Exception e) {
			fail(e.getMessage());
		}
		record.display(null, true);
	}

	public void testIndexdatabase() {
		System.out.println("testIndexdatabase\n");
		LibrisDatabase database = new LibrisDatabase();
		database.openDatabase(testParams.getTestDatabase(), true);
		try {
			database.RebuildIndex();
		} catch (LibrisException e) {
			fail("unexpected exception in testIndexdatabase");
		}
	}

	public void checkValues(LibrisRecord r, String[][] values) {

		for (int i = 0; i < values.length; ++i) {
			String k, v;
			k = values[i][0];
			v = r.getField(k).getValue();
			assertEquals("checking field "+k, values[i][1], v);
		}
	}

	public void testReadSomeRecords() {

		String [][][] testValueSet = 
		{{{"StringField1", "abcdef"},
					{"StringField2", " abc defg hijkl mnopqr"},
					{"IntField1", "12"},
					{"IntField2", "98"}},

					{{"StringField1", "qwerty"},
						{"StringField2", " a sd fgh jklz xcvbn"},
						{"IntField1", "34"},
						{"IntField2", "56"}},

						{{"StringField1", "qwerty"},
							{"IntField1", "34"}},

							{{"StringField2", "Qazwsx Edcrfv Tgb Yhnujm"},
								{"IntField1", "3"},
								{"IntField2", "1234567890"}},

								{{"StringField1", "qazwsxedcrfv tgbyhnujmikolp"},
									{"StringField2", "Qazwsx Edcrfv Tgb Yhnujm"},
									{"IntField1", "3"},
									{"IntField2", "1234567890"}},

									{{"IntField1", "3"},
										{"StringField2", " The quick brown fox jumps over the lazy dog."},
										{"IntField2", "1234567890"}}};

		System.out.println("testReadSomeRecords\n");
		LibrisDatabase database = new LibrisDatabase();
		database.openDatabase(testParams.getTestDatabase(), false);
		RecordList rl = new RecordList(database);

		LibrisRecord r;
		for (int i = 0; i < testValueSet.length; ++i ){
			try {
				r= rl.readRecord(i);
				String[][] testValues = testValueSet[i];
				checkValues(r, testValues);
			} catch (LibrisException e) {
				fail("testReadSomeRecords unexpected exception");
			}
		}

		rl.display();
	}

	public void testReadUndefinedRecords() {
		boolean exceptionThrown = false;
		System.out.println("testReadUndefinedRecords\n");
		LibrisDatabase database = new LibrisDatabase();
		database.openDatabase(testParams.getTestDatabase(), false);
		RecordList rl = new RecordList(database);
		LibrisException.setSuppress(ErrorIds.ERR_NO_RECORD_POSITION_FILE, true);
		try {
			rl.readRecord(10000);
		} catch (LibrisException e) {
			exceptionThrown = (e.getErrId() == ErrorIds.ERR_NO_START_POS);
		}
		LibrisException.setSuppress(ErrorIds.ERR_NO_START_POS, false);
		assertTrue("correct exception thrown in testReadUndefinedRecords", exceptionThrown);
	}

	public void testQuery() throws Throwable {
		try {
			System.out.println("testQuery\n");
			LibrisDatabase database = new LibrisDatabase();
			database.openDatabase(testParams.getTestDatabase(), false);
			String fieldName = "StringField1";
			int expectedNumResults =2;
			{
				String[] l = {"qwerty"};
				runQueryAndCheckKeywords(database, l, fieldName,
						expectedNumResults);
			}
			expectedNumResults = 1;
			{
				String [] l = {"qazwsxedcrfv", "tgbyhnujmikolp"};
				runQueryAndCheckKeywords(database, l, fieldName, expectedNumResults);
			}
		} catch (Throwable e) {
			e.printStackTrace();
			throw(e);
		}
	}
/**
 * @testcase test_enum1
 * @purpose test enums
 * @method create and index fields using enum data type
 * @expect field parsed, correct data recovered
 * @variant with, without @id attribute
 */
	/**
	 * @testcase test_boolean1
	 * @purpose test boolean fields
	 * @method create and index fields using boolean data type
	 * @expect field parsed, correct data recovered
	 * @variant true, false values
	 */
	/** @testcase test_enum_err1
	 * @purpose only enum fields can use enum attribute
	 * @method create enumset, define field of non-enum type with enumset attribute
	 * @expect exception
	 * @variant
	 */
	
	/**
	 * @testcase test_enum_err2
	 * @purpose verify that enum field must have ensumet attributes
	 * @method declare an enum field without an enumset attribute, set its value
	 * @expect ERR_NO_ENUMSET_ID exception with field name
	 * @variant
	/**
	 * 
	 * @param database
	 * @param l
	 * @param fieldName
	 * @param expectedNumResults
	 * @throws LibrisException
	 */
	
	private void runQueryAndCheckKeywords(LibrisDatabase database, String[] l, String fieldName,
			int expectedNumResults) throws LibrisException {
		ArrayList <String[]> q = new ArrayList<String[]>();
		q.add(l);
		RecordList rl = new RecordList(database);
		rl.runQuery(q);
		ArrayList <LibrisRecord> result = rl.getList();
		assertEquals("check number of records found for keywords "+l[0], expectedNumResults, result.size());
		Iterator<LibrisRecord> i = result.iterator();
		while (i.hasNext()) {
			LibrisRecord r = i.next();
			for (int k = 0; k < l.length; ++k) {
				assertTrue("check that record contains the keyword", r.getField(fieldName).getValue().contains(l[k]));
			}
		}
	}
}

package org.lasalledebain;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;

import org.lasalledebain.libris.EnumFieldChoices;
import org.lasalledebain.libris.Field;
import org.lasalledebain.libris.FieldTemplate;
import org.lasalledebain.libris.LibrisConstants;
import org.lasalledebain.libris.exception.DatabaseError;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.FieldDataException;
import org.lasalledebain.libris.field.FieldBooleanValue;
import org.lasalledebain.libris.field.FieldSingleStringValue;
import org.lasalledebain.libris.field.FieldValue;
import org.lasalledebain.libris.util.Lorem;
import org.lasalledebain.libris.util.MockSchema;
import org.lasalledebain.libris.util.Utilities;

import junit.framework.TestCase;


public class FieldTest extends TestCase {
	public void testBoolean() throws FieldDataException {
		FieldTemplate ft = new FieldTemplate(new MockSchema(), "f1", "Field 1", Field.FieldType.T_FIELD_BOOLEAN);

		Field f = ft.newField();

		f.addValue("true");
		String fd = f.getFirstFieldValue().getValueAsString();
		assertTrue("wrong result for true boolean", fd.equalsIgnoreCase("true"));
		f.changeValue("false");
		fd = f.getFirstFieldValue().getValueAsString();
		assertTrue("wrong result for false boolean", fd.equalsIgnoreCase("false"));
		FieldValue v = f.removeValue();
		assertFalse("wrong value for remove", v.isTrue());
		f.addValue(new FieldBooleanValue(true));
		v = f.getFirstFieldValue();
		assertTrue("wrong value for set with FieldValue", v.isTrue());
		f.removeValue();
		boolean excThrown = false;
		f.addValue(new FieldSingleStringValue("false"));
		v = f.getFirstFieldValue();
		assertFalse("wrong value for set with string value", v.isTrue());
		try {
			f.addValue("true");
		} catch (FieldDataException e) {
			excThrown = true;
		}
		assertTrue("no exception on extra data", excThrown);
	}

	public void testBooleanIllegalValue() throws FieldDataException {
		FieldTemplate ft = new FieldTemplate(new MockSchema(), "f1", "Field 1", Field.FieldType.T_FIELD_BOOLEAN);

		Field f = ft.newField();
		boolean excThrown = false;
		try {
			f.addValue("bogus");
		} catch (FieldDataException e) {
			excThrown = true;
		}
		assertTrue("no exception on extra data", excThrown);
	}

	public void testEnumMultipleValues() throws FieldDataException, DatabaseException {
		FieldTemplate ft = Utilities.createTemplate("f1", Field.FieldType.T_FIELD_ENUM);
		String[] myChoices = {"first", "second", "third", "fourth"};
		ft.setEnumChoices(new EnumFieldChoices("MockEnumFieldChoices", myChoices));
		Field f = ft.newField();
		for (int i = 0; i < myChoices.length; ++i) {
			f.addValue(myChoices[i]);
		}
		for (int i = 0; i < myChoices.length; ++i) {
			FieldValue v = f.removeValue();
			assertTrue("wrong choice for remove", v.getValueAsInt() == i);
		}
		for (int i = 0; i < myChoices.length - 1; ++i) {
			f.addValue(myChoices[i]);
		}
		f.changeValue(myChoices[myChoices.length-1]);

		int i = myChoices.length-1;
		for (FieldValue v: f.getFieldValues()) {
			assertEquals("wrong choice for value", i, v.getValueAsInt());
			i = (i % (myChoices.length - 1)) + 1;
		}
		assertEquals("wrong number of values",  myChoices.length - 1, f.getNumberOfValues());
	}

	public void testReadOnlyView() throws FieldDataException {
		FieldTemplate ft = Utilities.createTemplate("f1", Field.FieldType.T_FIELD_INTEGER);
		Field f = ft.newField();
		f.addIntegerValue(42);
		assertFalse("regular view  read-only", f.isReadOnly());
		FieldValue v = f.getFirstFieldValue();
		assertEquals("wrong value for integer field", 42, v.getValueAsInt());
		Field rof = f.getReadOnlyView();
		v = rof.getFirstFieldValue();
		assertEquals("wrong value for read-only field", 42, v.getValueAsInt());
		assertTrue("read-only view not read-only", rof.isReadOnly());
		boolean thrown = false;
		try {
			rof.addIntegerValue(99);
		} catch (FieldDataException e) {
			thrown = true;
		}
		assertTrue("No error addIntegerValue read-only view", thrown);

		thrown = false;
		try {
			rof.setValues(new FieldValue[0]);
		} catch (DatabaseError e) {
			thrown = true;
		}
		assertTrue("No error setValues(array) read-only view", thrown);


		thrown = false;
		try {
			rof.setValues(Arrays.asList((new FieldValue[0])));
		} catch (DatabaseError e) {
			thrown = true;
		}
		assertTrue("No error setValues(iterator) read-only view", thrown);

		thrown = false;
		try {
			rof.addValue("foo");
		} catch (FieldDataException e) {
			thrown = true;
		}
		assertTrue("No error addvalue(string) read-only view", thrown);

		thrown = false;
		try {
			rof.addValuePair(1, "foo");
		} catch (FieldDataException e) {
			thrown = true;
		}
		assertTrue("No error addvaluepair read-only view", thrown);

		thrown = false;
		try {
			rof.addValuePair("foo", "bar");
		} catch (FieldDataException e) {
			thrown = true;
		}
		assertTrue("No error addvalue(string, string) read-only view", thrown);

		thrown = false;
		try {
			rof.removeValue();
		} catch (DatabaseError e) {
			thrown = true;
		}
		assertTrue("No error removeValue read-only view", thrown);

	}
	public void testInteger() {
		FieldTemplate ft = Utilities.createTemplate("f1", Field.FieldType.T_FIELD_INTEGER);
		try {
			int [] testData = {0, -1, 1, 1234, -5678};
			for (int i: testData) {
				Field f = ft.newField();
				String testString = Integer.toString(i);
				f.addValue(testString);
				FieldValue firstFieldValue = f.getFirstFieldValue();
				int d = firstFieldValue.getValueAsInt();
				assertEquals("mismatch in int data "+testString, i, d);
				String s = firstFieldValue.getValueAsString();
				assertEquals("mismatch in string data", testString, s);
			}
			boolean excThrown = false;
			try {
				Field f = ft.newField();
				f.addValue("foobar");
			} catch (FieldDataException e) {
				excThrown = true;
			}
			assertTrue("no exception on bad data", excThrown);
		} catch (FieldDataException e) {
			fail("fieldDataException");
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}

	}
	public void testRange() {
		FieldTemplate ft = Utilities.createTemplate("f1", Field.FieldType.T_FIELD_PAIR);
		try {
			Field f = ft.newField();
			try {
				String[][] expectedData = {{"", ""}, {"testString1", ""}, {"2","3"}};
				String[] d = getValuesAsStringPair(f);
				checkValues(expectedData[0], d);

				f.addValue(expectedData[1][0]);
				d = getValuesAsStringPair(f);
				checkValues(expectedData[1], d);

				f.removeValue();
				f.addValuePair(expectedData[2][0], expectedData[2][1]);
				d = getValuesAsStringPair(f);
				checkValues(expectedData[2], d);

			} catch (Exception e) {
				e.printStackTrace();
				fail("unexpected exception "+e.getClass().getName()+": "+e.getMessage());
			}

		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
	private String[] getValuesAsStringPair(Field f) {
		FieldValue v = f.getFirstFieldValue();
		String[] pair = new String[2];
		try {
			pair[0] = v.getMainValueAsString();
		} catch (FieldDataException e) {
			e.printStackTrace();
			fail();
		}
		pair[1] = v.getExtraValueAsString();
		return pair;
	}
	private void checkValues(String[] expectedData, String[] d) {
		assertEquals("wrong number of elements ", expectedData.length, d.length);
		for (int j = 0; j < d.length; ++j) {
			assertEquals("mismatch in range value: ", expectedData[j], d[j]);
		}
	}

	public void testStringField() throws FieldDataException {
		FieldTemplate ft = Utilities.createTemplate("f1", Field.FieldType.T_FIELD_STRING);
		Field f = ft.newField();
		String [] testData = {"The", " quick brown ", "fox jumped\nover the lazy dog"};

		for (String testString: testData) {

			f.addValue(testString);
			String d = f.getFirstFieldValue().getValueAsString();
			assertEquals("wrong result for "+testString, d, testString);
			FieldValue r = f.removeValue();
			if (null != r) {
				assertEquals("wrong result for removed value "+testString, r.getValueAsString(), testString);
			}

		}
	}

	public void testFieldToString() throws FieldDataException {
		FieldTemplate ft = Utilities.createTemplate("f1", Field.FieldType.T_FIELD_PAIR);
		Field f = ft.newField();

		Iterator<String> myWords = Arrays.asList(Lorem.words).iterator();
		Random r = new Random(getName().hashCode());
		StringBuffer expected = new StringBuffer();
		String separator = "";
		for (int i = 0; i < 6; ++i) {
			expected.append(separator);
			String w1 = myWords.next();
			if (r.nextBoolean()) {
				f.addValue(w1);
				expected.append(w1);
			}
			else {
				String w2 = myWords.next();
				f.addValuePair(w1, w2);
				expected.append(w1);
				expected.append('-');
				expected.append(w2);
			}
			separator = ", ";
		}
		String actual = f.getValuesAsString();
		assertEquals("Wrong string value for field", expected.toString(), actual);
	}

	public void testEnumField() throws FieldDataException, DatabaseException {
		FieldTemplate ft = Utilities.createTemplate("f1", Field.FieldType.T_FIELD_ENUM);
		String[][] valueNames = {{"toe"}, {"foo", "bar"}, {"bob", "carol", "ted", "alice"}};
		for (int valueSet = 0; valueSet < valueNames.length; ++valueSet) {
			ft.setEnumChoices(new EnumFieldChoices("MockEnumFieldChoices", valueNames[valueSet]));
			setAndCheckEnumData(ft, valueNames[valueSet]);
		}
	}

	public void testEnumFieldExtraValues() throws DatabaseException, FieldDataException {
		FieldTemplate ft = Utilities.createTemplate("f1", Field.FieldType.T_FIELD_ENUM);
		String[] myChoices = {"first", "second", "third", "fourth"};
		String[] myExtraChoices = {"extra_first", "extra_second", "extra_third", "extra_fourth"};
		ft.setEnumChoices(new EnumFieldChoices("MockEnumFieldChoices", myChoices));
		Field f = ft.newField();
		for (int i = 0; i < myChoices.length; ++i) {
			if (0 == (i % 2)) f.addIntegerValue(i);
			else  f.addValuePair((int) LibrisConstants.ENUM_VALUE_OUT_OF_RANGE, myExtraChoices[i]);
		}

		int i = 0;
		for (FieldValue v: f.getFieldValues()) {
			assertEquals("wrong value", ((0 == (i % 2))? myChoices[i]: myExtraChoices[i]), v.getValueAsString());
			i++;
		}
		f.removeValue();
		f.changeValue(myChoices[1]);
		Iterator<FieldValue> valueIterator = f.getFieldValues().iterator();
		assertEquals("wrong first value after change", 1, valueIterator.next().getValueAsInt());
		assertEquals("wrong second value after change", myChoices[2], valueIterator.next().getValueAsString());

		f.removeValue();
		f.changeValue((int) LibrisConstants.ENUM_VALUE_OUT_OF_RANGE, myExtraChoices[0]);
		valueIterator = f.getFieldValues().iterator();
		assertEquals("wrong first value after second change", myExtraChoices[0], valueIterator.next().getValueAsString());
		assertEquals("wrong second value after second change", myExtraChoices[3], valueIterator.next().getValueAsString());
	}

	public void testMultipleStringValues() {
		FieldTemplate ft = Utilities.createTemplate("tmsv", Field.FieldType.T_FIELD_STRING);
		try {
			Field f = ft.newField();
			try {
				String [] testData = {"one", "two", "three"};
				for (String testString: testData) {

					f.addValue(testString);
				}
				int valueCount = f.getNumberOfValues();
				assertEquals("wrong number of values", testData.length, valueCount);
				Iterator<FieldValue> vi = f.getFieldValues().iterator();
				int i = 0;
				while (vi.hasNext()) {
					String d = vi.next().getValueAsString();
					assertEquals("wrong result for "+testData[i], d, testData[i]);
					++i;
				}
			} catch (Exception e) {
				e.printStackTrace();
				fail("unexpected exception "+e.getClass().getName()+" "+e.getMessage());
			}

		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	private void setAndCheckEnumData(FieldTemplate ft, String[] valueNames) throws FieldDataException {
		Field f = ft.newField();
		for (int i = 0; i < valueNames.length; ++i) {
			f.addValue(valueNames[i]);
			FieldValue v = f.getFirstFieldValue();
			assertEquals("enum index", i, v.getValueAsInt());
			assertEquals("enum name", valueNames[i], v.getValueAsString());
			f.removeValue();
		}
		boolean excThrown = false;
		try {
			f.addValue("foobar");
		} catch (FieldDataException e) {
			excThrown = true;
		}
		assertTrue("no exception on bad data", excThrown);
	}
}
